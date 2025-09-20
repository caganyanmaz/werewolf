package com.caganyanmaz.werewolf.application.room;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.caganyanmaz.werewolf.application.ports.GameRepository;
import com.caganyanmaz.werewolf.application.ports.RoomRepository;
import com.caganyanmaz.werewolf.domain.Role;
import com.caganyanmaz.werewolf.utils.TextIdentifierGenerator;

@Service 
public class RoomService {
    private final RoomRepository rooms;
    private final GameRepository games;
    private final RoomEvents events;

    public RoomService(RoomRepository rooms, GameRepository games, RoomEvents events) {
        this.rooms = rooms;
        this.games = games;
        this.events = events;
    }

    public String create_room(String host_id) {
        String room_id = TextIdentifierGenerator.getInstance().create_identifier();
        Room room = new Room(room_id, host_id);
        rooms.save(room);
        events.lobby_changed(room_id, LobbyView.from(room));
        return room_id;
    }

    public void add_participant_to_room(String room_id, String player_id, String nickname) {
        rooms.get(room_id).add_participant(player_id, nickname);
    }

    public boolean is_player_in_room(String room_id, String player_id) {
        rooms.get(room_id).is_player_in(player_id);
    }

    public LobbyView get_lobby_view(String room_id) {
        Room r = rooms.get(room_id);
        return LobbyView.from(r);
    }


    public record LobbyView(String room_id,
                        String host_id,
                        RoomStatus status,
                        List<LobbyPlayer> players,
                        Instant deadline) {
        public static LobbyView from(Room r) {
            var ps = r.participants().stream()
                .map(p -> new LobbyPlayer(p.player_id(), p.nickname(), p.ready()))
                .toList();
            return new LobbyView(r.room_id(), r.host_id(), r.status(), ps, r.deadline().orElse(null));
        }
    }

    public record LobbyPlayer(String id, String nickname, boolean ready) {}

    public record GameView(String id, String name, boolean ready) {}

    public record PlayerView(String id, String name, Role role, boolean alive) {}
}
