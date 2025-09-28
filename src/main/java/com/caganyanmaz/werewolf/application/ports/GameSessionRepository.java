package com.caganyanmaz.werewolf.application.ports;

import com.caganyanmaz.werewolf.application.game.GameSession;

public interface GameSessionRepository {
    GameSession get(String game_id);
    void save(GameSession game);
    boolean exists(String game_id);
}
