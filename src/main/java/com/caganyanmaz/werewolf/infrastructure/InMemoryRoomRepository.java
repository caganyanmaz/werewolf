package com.caganyanmaz.werewolf.infrastructure;

import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.application.ports.RoomRepository;
import com.caganyanmaz.werewolf.application.room.Room;

@Component
public class InMemoryRoomRepository implements RoomRepository {
    private final InMemoryRepository<Room> repo = new InMemoryRepository<Room>(Room::room_id);
    @Override public Room get(String id) { return repo.get(id); }
    @Override public void save(Room room) { repo.save(room); }
}
