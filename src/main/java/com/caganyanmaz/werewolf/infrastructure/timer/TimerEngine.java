package com.caganyanmaz.werewolf.infrastructure.timer;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.application.room.TimerHandlers;

@Component
public class TimerEngine {
    private final PriorityBlockingQueue<ScheduledItem> pq = new PriorityBlockingQueue<>();
    private final ConcurrentMap<String, RoomTimer> timers = new ConcurrentHashMap<>();
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final TimerHandlers handlers;

    public TimerEngine(TimerHandlers handlers) {
        this.handlers = handlers;
        worker.submit(this::loop);
    }

    public void upsert(RoomTimer t) {
        String key = key(t.roomId(), t.id());
        timers.put(key, t);
        if (t.state() == TimerState.RUNNING) {
            pq.offer(new ScheduledItem(t.roomId(), t.id(), t.endAt()));
        }
    }

    public Optional<RoomTimer> get(String roomId, String id) {
        return Optional.ofNullable(timers.get(key(roomId, id)));
    }

    public void pause(String roomId, String id) {
        timers.computeIfPresent(key(roomId,id), (k, old) -> old.pauseNow());
    }

    public void resume(String roomId, String id) {
        timers.computeIfPresent(key(roomId,id), (k, old) -> {
            RoomTimer nu = old.resumeNow();
            pq.offer(new ScheduledItem(nu.roomId(), nu.id(), nu.endAt()));
            return nu;
        });
    }

    public void extend(String roomId, String id, long millis) {
        timers.computeIfPresent(key(roomId,id), (k, old) -> {
            RoomTimer nu = old.extendMillis(millis);
            pq.offer(new ScheduledItem(nu.roomId(), nu.id(), nu.endAt()));
            return nu;
        });
    }

    private void loop() {
        for (;;) {
            try {
                ScheduledItem next = pq.take();
                String k = key(next.roomId, next.id);
                RoomTimer t = timers.get(k);
                if (t == null || t.state() != TimerState.RUNNING) continue;
                if (Instant.now().isBefore(t.endAt())) { // extended
                    pq.offer(new ScheduledItem(t.roomId(), t.id(), t.endAt()));
                    continue;
                }
                timers.put(k, t.fired());
                handlers.onFire(t); // app-layer action + broadcasts
            } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
        }
    }

    private static String key(String roomId, String id) { return roomId + ":" + id; }

    private static final class ScheduledItem implements Comparable<ScheduledItem> {
        final String roomId, id; final Instant when;
        ScheduledItem(String roomId, String id, Instant when){ this.roomId=roomId; this.id=id; this.when=when; }
        public int compareTo(ScheduledItem o){ return this.when.compareTo(o.when); }
    }
}