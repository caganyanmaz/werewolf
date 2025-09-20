package com.caganyanmaz.werewolf.application.ports;

import com.caganyanmaz.werewolf.domain.Game;

public interface GameRepository {
    Game get(String game_id);
    void save(Game game);
}
