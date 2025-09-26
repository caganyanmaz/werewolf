package com.caganyanmaz.werewolf.application.room;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Room {
    private final String room_id;
    private String host_id;
    private RoomStatus status = RoomStatus.LOBBY;

    private final Map<String, Participant> participants = new LinkedHashMap<>();
    // reference to a domain game id (once started, null while in lobby)
    private String game_id;

    // optional timing metadata (application-level)
    private Instant day_deadline;
    private Duration day_duration;

    public Room(String room_id, String host_id) {
        this.room_id = room_id;
        this.host_id = host_id;
    }

    public void add_participant(String player_id, String nickname) {
        var existing = participants.get(player_id);
        participants.put(player_id, new Participant(player_id, nickname, existing != null && existing.ready()));
    }

    public void toggle_ready(String player_id, boolean ready) {
        var p = participants.get(player_id);
        participants.put(player_id, new Participant(p.player_id(), p.nickname(), ready));
    }

    public boolean is_player_in_room(String player_id) {
        return participants.containsKey(player_id);
    }

    public boolean is_host(String player_id) {
        return Objects.equals(host_id, player_id);
    }

    public Collection<Participant> participants() {
        return participants.values();
    }

    public RoomStatus status() {
        return status;
    }

    public String room_id() {
        return room_id;
    }

    public String host_id() {
        return host_id;
    }

    public String game_id() {
        return game_id;
    }

    /* state transitions owned by application */
    public void mark_running(String new_game_id) {
        this.status = RoomStatus.RUNNING;
        this.game_id = new_game_id;
    }

    public void setDeadline(Instant at) {
        this.day_deadline = at;
    }

    public Optional<Instant> deadline() {
        return Optional.ofNullable(day_deadline);
    }
}
