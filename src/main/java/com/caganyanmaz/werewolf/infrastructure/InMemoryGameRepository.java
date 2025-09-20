package com.caganyanmaz.werewolf.infrastructure;

import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.application.ports.GameRepository;
import com.caganyanmaz.werewolf.domain.Game;

@Component
public class InMemoryGameRepository implements GameRepository {
    private final InMemoryRepository<Game> repo = new InMemoryRepository<Game>(Game::game_id);
    @Override public Game get(String id) { return repo.get(id); }
    @Override public void save(Game game) { repo.save(game); }
}

