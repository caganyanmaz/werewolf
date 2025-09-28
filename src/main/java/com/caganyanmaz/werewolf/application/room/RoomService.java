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

import com.caganyanmaz.werewolf.application.ports.GameRepository;
import com.caganyanmaz.werewolf.application.ports.RoomRepository;
import com.caganyanmaz.werewolf.domain.Phase;
import com.caganyanmaz.werewolf.domain.Role;
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
    private final GameRepository games;
    private final RoomEvents events;
    private final ScheduledExecutorService scheduler;

    // Per-room scheduled futures
    private final ConcurrentMap<String, RoomTimers> timers = new ConcurrentHashMap<>();

    public RoomService(RoomRepository rooms,
                       GameRepository games,
                       RoomEvents events,
                       ScheduledExecutorService scheduler) {
        this.rooms = rooms;
        this.games = games;
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

    /** TODO: implement your real start criteria */
    public boolean is_game_able_to_start(String room_id) {
        return true;
    }

    /**
     * TODO: set criteria to start the game
     */
    public void start_game(String room_id) {
        Room room = rooms.get(room_id);
        System.out.println("Starting the game");
        room.start_game();
        events.lobby_changed(room_id, LobbyView.from(room));
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
            } else {
                // Re-arm with same/min tweaked duration; keep it simple
                events.notice(room_id, "Not enough players; restarting lobby timer.");
                start_lobby_countdown(room_id, d, minPlayers);
            }
            // Optional: emit a “timer fired” message if you have such an event method
            events.timer_fired(room_id, "lobby-countdown", Map.of("kind", "LOBBY", "minPlayers", minPlayers));
        }, delayMs, TimeUnit.MILLISECONDS);

        timers(room_id).lobby().set(f);
    }

    /** Cancel the lobby countdown if any. */
    public void cancel_lobby_timer(String room_id) {
        var ref = timers(room_id).lobby().getAndSet(null);
        if (ref != null) ref.cancel(false);
    }

    /** Start/restart the current phase with a deadline and broadcast. */
    public void start_phase(String room_id, Phase phase, Duration d) {
        cancel_phase_timer(room_id);

        Instant now = Instant.now();
        Instant end = now.plus(d);

        // Update room state (status RUNNING, phase info in Room if you store it, and deadline)
        Room room = rooms.get(room_id);
        room.start_game();
        room.set_deadline(end);

        // Broadcast to clients (reuse lobby_changed to carry deadline; or call a phase event if you have one)
        events.lobby_changed(room_id, LobbyView.from(room));
        // If you have a dedicated event, prefer it:
        // events.phase_changed(room_id, PhaseView.from(room, now, end));

        long delayMs = Math.max(0, end.toEpochMilli() - Instant.now().toEpochMilli());
        ScheduledFuture<?> f = scheduler.schedule(() -> {
            // Advance to next phase
            Phase current = phase;
            Phase next = next_phase(room_id, current);
            // Update room and re-arm
            set_phase(room_id, next);
            start_phase(room_id, next, default_phase_duration(next));

            // Optional: announce phase-timer fired
            events.timer_fired(room_id, "phase-" + current.name().toLowerCase(),
                    Map.of("kind", "PHASE", "phase", current.name()));
        }, delayMs, TimeUnit.MILLISECONDS);

        timers(room_id).phase().set(f);
    }

    public void cancel_phase_timer(String room_id) {
        var ref = timers(room_id).phase().getAndSet(null);
        if (ref != null) ref.cancel(false);
    }

    /** Extend the current phase by `by` (simple: cancel & re-start with new remaining). */
    public void extend_phase(String room_id, Duration by) {
        // TODO: Implement Later
    }

    /* =====================  Helpers & Model-Oriented Ops  ===================== */

    private RoomTimers timers(String room_id) {
        return timers.computeIfAbsent(room_id, __ -> new RoomTimers());
    }

    /** Choose the next phase by your rules (tie handling, etc.). */
    private Phase next_phase(String room_id, Phase current) {
        // TODO: Implement this
        return Phase.DAY;
    }

    /** Persist only the phase (no timer changes). */
    private void set_phase(String room_id, Phase p) {
        // TODO: Implement this later
    }

    private Duration default_phase_duration(Phase p) {
        return Duration.ofSeconds(5);
        /* 
        return switch (p) {
            case NIGHT   -> Duration.ofSeconds(90);
            case DAY     -> Duration.ofMinutes(3);
            case VOTE    -> Duration.ofSeconds(45);
            case RESOLVE -> Duration.ofSeconds(10);
            default      -> Duration.ofSeconds(0);
        };
        */
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

    public record LobbyPlayer(String id, String nickname, boolean ready) {}
    public record GameView(String id, String name, boolean ready) {}
    public record PlayerView(String id, String name, Role role, boolean alive) {}

    /* =====================  Per-room futures holder  ===================== */

    private static final class RoomTimers {
        private final java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> lobby = new java.util.concurrent.atomic.AtomicReference<>();
        private final java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> phase = new java.util.concurrent.atomic.AtomicReference<>();
        public java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> lobby() { return lobby; }
        public java.util.concurrent.atomic.AtomicReference<ScheduledFuture<?>> phase() { return phase; }
    }
}