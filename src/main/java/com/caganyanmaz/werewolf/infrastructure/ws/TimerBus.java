package com.caganyanmaz.werewolf.infrastructure.ws;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class TimerBus {
    private final SimpMessagingTemplate ws;
    public TimerBus(SimpMessagingTemplate ws){ this.ws = ws; }

    public void broadcastUpsertSimple(String roomId, String id, String kind,
                                      Instant startAt, Instant endAt, Map<String,Object> payload) {
        ws.convertAndSend("/topic/timers/" + roomId, Map.of(
            "type", "timer.upsert",
            "serverNow", Instant.now().toString(),
            "roomId", roomId,
            "timer", Map.of(
                "id", id,
                "label", id,
                "state", "RUNNING",
                "startAt", startAt.toString(),
                "endAt", endAt.toString(),
                "payload", merge(payload, Map.of("kind", kind))
            )
        ));
    }

    public void broadcastFiredSimple(String roomId, String id, String kind,
                                     Instant startAt, Instant endAt, Map<String,Object> payload) {
        ws.convertAndSend("/topic/timers/" + roomId, Map.of(
            "type", "timer.fired",
            "serverNow", Instant.now().toString(),
            "roomId", roomId,
            "timer", Map.of(
                "id", id,
                "label", id,
                "state", "FIRED",
                "startAt", startAt.toString(),
                "endAt", endAt.toString(),
                "payload", merge(payload, Map.of("kind", kind))
            )
        ));
    }

    private static Map<String,Object> merge(Map<String,Object> a, Map<String,Object> b) {
        var m = new java.util.HashMap<>(a);
        m.putAll(b);
        return m;
    }
}