package com.caganyanmaz.werewolf.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Game {
    private Phase phase = Phase.DAY;
    private int day_count = 0;
    Map<String, Player> players = new LinkedHashMap<>();
    public Game(Collection<String> player_ids) {
        for (String player_id : player_ids) {
            Player prev = players.putIfAbsent(player_id, new Player());
            if (prev != null) {
                throw new IllegalArgumentException("Multiple players with same id passed to the game constructor");
            }
        }
    }
}
