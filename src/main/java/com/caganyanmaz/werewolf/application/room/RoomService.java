package com.caganyanmaz.werewolf.application.room;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.caganyanmaz.werewolf.application.game.GameSession;
import com.caganyanmaz.werewolf.application.ports.GameSessionRepository;
import com.caganyanmaz.werewolf.application.ports.RoomRepository;
import com.caganyanmaz.werewolf.domain.Phase;
import com.caganyanmaz.werewolf.utils.TextIdentifierGenerator;

/**
 * Simple, non-circular RoomService with built-in timers.
 * - One ScheduledExecutorService (injected as a bean).
 * - At most one "lobby" and one "phase" timer per room.
 * - Updates Room.deadline and broadcasts lobby_changed so clients can render a countdown.
 */
@Service
public class RoomService {
    private final RoomRepository rooms;
    private final GameSessionRepository game_sessions;
    private final RoomEvents events;
    private final ScheduledExecutorService scheduler;

    // Per-room scheduled futures
    private final ConcurrentMap<String, RoomTimers> timers = new ConcurrentHashMap<>();

    public RoomService(RoomRepository rooms,
                       GameSessionRepository game_sessions,
                       RoomEvents events,
                       ScheduledExecutorService scheduler) {
        this.rooms = rooms;
        this.game_sessions = game_sessions;
        this.events = events;
        this.scheduler = scheduler;
    }

    /* =====================  Public API you already had  ===================== */

    public String create_room(String host_id) {
        String room_id = TextIdentifierGenerator.getInstance().create_identifier();
        Room room = new Room(room_id, host_id);
        rooms.save(room);
        events.lobby_changed(room_id, LobbyView.from(room));
        return room_id;
    }

    public void add_participant_to_room(String room_id, String player_id, String nickname) {
        Room room = rooms.get(room_id);
        room.add_participant(player_id, nickname);
        events.lobby_changed(room_id, LobbyView.from(room));
    }

    public LobbyView get_lobby_view(String room_id) {
        Room r = rooms.get(room_id);
        return LobbyView.from(r);
    }

    public GameView get_game_view(String game_id) {
        GameSession game_session = game_sessions.get(game_id);
        return GameView.from(game_session);
    }

    public void update_player_ready(String room_id, String player_id, boolean ready) {
        Room room = rooms.get(room_id);
        room.toggle_ready(player_id, ready);
        if (room.is_everyone_ready()) {
            start_game(room_id);
        } else if (room.is_host_ready()) {
            start_lobby_countdown(room_id, Duration.ofSeconds(5), 0);
        }
        events.lobby_changed(room_id, LobbyView.from(room));
    }

    public boolean is_player_in_lobby(String room_id, String player_id) {
        return rooms.exists(room_id) && rooms.get(room_id).is_player_in_room(player_id);
    }

    public boolean is_player_in_game(String game_id, String player_id) {
        return game_sessions.exists(game_id) && game_sessions.get(game_id).is_player_in_room(player_id);
    }

    /** TODO: implement your real start criteria */
    public boolean is_game_able_to_start(String room_id) {
        return rooms.exists(room_id) && !rooms.get(room_id).is_game_started();
    }

    public void start_game(String room_id) {
        System.out.println("Starting the game");
        cancel_lobby_timer(room_id);
        Room room = rooms.get(room_id);
        String game_id = "game_" + room_id;
        GameSession game_session = new GameSession(game_id, room.participants());
        game_sessions.save(game_session);
        room.set_game_started();
        events.game_started(room_id, game_id);
    }

    /* =====================  Minimal Timer Controls  ===================== */

    /** Start/restart a lobby countdown and broadcast updated deadline. */
    public void start_lobby_countdown(String room_id, Duration d, int minPlayers) {
        System.out.println("Starting the countdown...");
        cancel_lobby_timer(room_id);

        Instant now = Instant.now();
        Instant end = now.plus(d);

        // Persist deadline on the room so clients can render countdown after refresh/reconnect.
        Room room = rooms.get(room_id);
        room.start_countdown();
        room.set_deadline(end);

        // Broadcast lobby change (includes deadline in LobbyView)
        events.lobby_changed(room_id, LobbyView.from(room));

        // Arm the timer
        long delayMs = Math.max(0, end.toEpochMilli() - Instant.now().toEpochMilli());
        ScheduledFuture<?> f = scheduler.schedule(() -> {
            if (is_game_able_to_start(room_id)) {
                start_game(room_id);
            }
        }, delayMs, TimeUnit.MILLISECONDS);

        timers(room_id).lobby().set(f);
    }

    /** Cancel the lobby countdown if any. */
    public void cancel_lobby_timer(String room_id) {
        var ref = timers(room_id).lobby().getAndSet(null);
        if (ref != null) ref.cancel(false);
    }

    public void cancel_phase_timer(String room_id) {
        var ref = timers(room_id).phase().getAndSet(null);
        if (ref != null) ref.cancel(false);
    }

    /* =====================  Helpers & Model-Oriented Ops  ===================== */

    private RoomTimers timers(String room_id) {
        return timers.computeIfAbsent(room_id, __ -> new RoomTimers());
    }


    /* =====================  DTOs you already expose  ===================== */

    public record LobbyView(String room_id,
                            String host_id,
                            RoomStatus status,
                            List<LobbyPlayer> players,
                            Instant deadline,
                            Instant server_now) {
        public static LobbyView from(Room r) {
            var ps = r.participants().stream()
                .map(p -> new LobbyPlayer(p.player_id(), p.nickname(), p.ready()))
                .toList();
            return new LobbyView(
                r.room_id(),
                r.host_id(),
                r.status(),
                ps,
                r.deadline().orElse(null),
                Instant.now()
            );
        }
    }

    public record GameView(
        String game_id, 
        Phase phase,
        List<PlayerView> players, 
        Instant deadline, 
        Instant server_now) {
        public static GameView from(GameSession g) {
            var ps = g.players().stream()
                .map(p -> new PlayerView(p.player_id(), p.nickname(), p.alive()))
                .toList();
            return new GameView(
                g.game_id(),
                g.phase(),
                ps,
                g.deadline(),
                Instant.now()
            ); 
        }
    }

    public record LobbyPlayer(String id, String nickname, boolean ready) {}
    public record PlayerView(String id, String name, boolean alive) {}

    /* =====================  Per-room futures holder  ===================== */

    private static final class RoomTimers {
        private final java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> lobby = new java.util.concurrent.atomic.AtomicReference<>();
        private final java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> phase = new java.util.concurrent.atomic.AtomicReference<>();
        public java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> lobby() { return lobby; }
        public java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> phase() { return phase; }
    }
}