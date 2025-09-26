package com.caganyanmaz.werewolf.application.room;

public interface RoomEvents {
    void lobby_changed(String room_id, RoomService.LobbyView view);
    void game_started(String room_id, String game_id);
    void game_changed(String room_id,  RoomService.GameView view);
}
