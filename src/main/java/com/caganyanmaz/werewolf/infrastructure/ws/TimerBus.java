package com.caganyanmaz.werewolf.infrastructure.ws;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.infrastructure.timer.RoomTimer;

@Component
public class TimerBus {
    private final SimpMessagingTemplate ws;
    public TimerBus(SimpMessagingTemplate ws){ this.ws = ws; }

    public void broadcastUpsert(RoomTimer t) { send(t.roomId(), "timer.upsert", t); }
    public void broadcastFired(RoomTimer t)  { send(t.roomId(), "timer.fired",  t); }
    public void broadcastPatch(RoomTimer t)  { send(t.roomId(), "timer.patch",  t); }
    public void notice(String roomId, String text) {
        ws.convertAndSend("/topic/room/" + roomId, Map.of("type","notice","text",text,"serverNow",Instant.now().toString()));
    }
    private void send(String roomId, String type, RoomTimer t) {
        ws.convertAndSend("/topic/timers/" + roomId,
            Map.of("type", type, "serverNow", Instant.now().toString(), "roomId", roomId, "timer", t));
    }
}
