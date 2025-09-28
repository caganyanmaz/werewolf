package com.caganyanmaz.werewolf.infrastructure;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.application.room.RoomEvents;
import com.caganyanmaz.werewolf.application.room.RoomService;

@Component
public class WebSocketRoomEvents implements RoomEvents {
    private final SimpMessagingTemplate smt;

    public WebSocketRoomEvents(SimpMessagingTemplate smt) { this.smt = smt; }

    @Override
    public void lobby_changed(String room_id, RoomService.LobbyView view) {
        smt.convertAndSend("/topic/room." + room_id + ".lobby", view);
    }

    @Override
    public void game_started(String room_id, String game_id) {
        smt.convertAndSend("/topic/room." + room_id + ".events",
                   (Object) Map.of("type", "GAME_STARTED", "game_id", game_id));
    }

    @Override
    public void game_changed(String room_id, RoomService.GameView view) {
        smt.convertAndSend("/topic/room." + room_id + ".game", view);
    }
    
    @Override
    public void notice(String room_id, String message) {
        smt.convertAndSend("/topic/room." + room_id + ".message", message);
    }

    @Override
    public void timer_fired(String room_id, String timer_id, Map<String, Object> payload) {
        smt.convertAndSend("/topic/room." + room_id + ".timer." + timer_id + "fired");
    }

}
