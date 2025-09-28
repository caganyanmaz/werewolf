package com.caganyanmaz.werewolf.infrastructure;

import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.application.ports.GameSessionRepository;
import com.caganyanmaz.werewolf.application.game.GameSession;

@Component
public class InMemoryGameSessionRepository implements GameSessionRepository {
    private final InMemoryRepository<GameSession> repo = new InMemoryRepository<GameSession>(GameSession::game_id);
    @Override public GameSession get(String id) { return repo.get(id); }
    @Override public void save(GameSession game_session) { repo.save(game_session); }
    @Override public boolean exists(String id) { return repo.exists(id); }
}

