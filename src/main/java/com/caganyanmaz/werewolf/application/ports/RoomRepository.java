package com.caganyanmaz.werewolf.application.ports;

import com.caganyanmaz.werewolf.application.room.Room;

public interface RoomRepository {
    Room get(String room_id);
    void save(Room room);
    boolean exists(String room_id);
}