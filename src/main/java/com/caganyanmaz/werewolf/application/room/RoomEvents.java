package com.caganyanmaz.werewolf.application.room;

import java.util.Map;

public interface RoomEvents {
    void lobby_changed(String room_id, RoomService.LobbyView view);
    void game_started(String room_id, String game_id);
    void game_changed(String room_id,  RoomService.GameView view);
    void notice(String room_id, String message);
    void timer_fired(String room_id, String timer_id, Map<String, Object> payload);
}
