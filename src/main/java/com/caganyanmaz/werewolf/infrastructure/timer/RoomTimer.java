package com.caganyanmaz.werewolf.infrastructure.timer;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public record RoomTimer(
    String roomId, String id, String label,
    Instant startAt, Instant endAt, TimerState state,
    Map<String,Object> payload
) {
    public static RoomTimer run(String roomId, String id, String label, Instant start, Instant end, Map<String,Object> payload) {
        return new RoomTimer(roomId, id, label, start, end, TimerState.RUNNING, payload==null?Collections.emptyMap():payload);
    }
    public RoomTimer fired()    { return new RoomTimer(roomId, id, label, startAt, endAt, TimerState.FIRED, payload); }
    public RoomTimer pauseNow() {
        long remaining = Math.max(0, endAt.toEpochMilli() - Instant.now().toEpochMilli());
        var p = new java.util.HashMap<>(payload);
        p.put("remainingMs", remaining);
        return new RoomTimer(roomId, id, label, startAt, endAt, TimerState.PAUSED, p);
    }
    public RoomTimer resumeNow() {
        long remaining = ((Number)payload.getOrDefault("remainingMs", 0)).longValue();
        Instant now = Instant.now();
        return new RoomTimer(roomId, id, label, now, now.plusMillis(remaining), TimerState.RUNNING, payload);
    }
    public RoomTimer extendMillis(long by)   { return new RoomTimer(roomId, id, label, startAt, endAt.plusMillis(by), state, payload); }
}
