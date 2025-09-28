package com.caganyanmaz.werewolf.application.game;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.caganyanmaz.werewolf.application.room.Participant;
import com.caganyanmaz.werewolf.domain.Game;
import com.caganyanmaz.werewolf.domain.Phase;

public class GameSession {
    private final Map<String, String> player_id_to_nickname = new LinkedHashMap<>();
    Game game;
    private String id;
    private Instant timer_deadline;

    public GameSession(String id, Collection<Participant> participants) {
        this.id = id;
        for (Participant participant : participants) {
            String player_id = participant.player_id();
            String nickname  = participant.nickname();
            String prev = player_id_to_nickname.putIfAbsent(player_id, nickname);
            if (prev != null) {
                throw new IllegalArgumentException("Two players with same id passed to the construction of GameSession");
            }
        }
        this.game = new Game(player_id_to_nickname.keySet());
    }

    public String game_id() {
        return id;
    }

    public Collection<PlayerInfo> players() {
        // TODO: Get the player info from the game, adding the nicknames (the pure game logic shouldn't have access to game ids)
        return new ArrayList();
    }

    public boolean is_player_in_room(String player_id) {
        // TODO: Implement this later
        return true;
    }

    public Phase phase() {
        return Phase.DAY;
    }
    public Instant deadline() {
        return timer_deadline;
    }
}

