package com.caganyanmaz.werewolf.application.room;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.caganyanmaz.werewolf.domain.Phase;
import com.caganyanmaz.werewolf.infrastructure.timer.RoomTimer;
import com.caganyanmaz.werewolf.infrastructure.timer.TimerEngine;
import com.caganyanmaz.werewolf.infrastructure.ws.TimerBus;

@Component
public class TimerHandlers {
    private final RoomService room_service;
    private final TimerEngine engine;
    private final TimerBus bus; // wraps SimpMessagingTemplate for WS broadcasts

    public TimerHandlers(RoomService room_service, TimerEngine engine, TimerBus bus) {
        this.room_service = room_service;
        this.engine = engine;
        this.bus = bus;
    }

    // Convenience APIs your RoomService can call
    public void startLobbyCountdown(String roomId, Duration d, int minPlayers) {
        var t = RoomTimer.run(
            roomId, "lobby-countdown", "Lobby starts in",
            Instant.now(), Instant.now().plus(d),
            Map.of("kind", "LOBBY", "minPlayers", minPlayers)
        );
        engine.upsert(t);
        bus.broadcastUpsert(t);
    }

    public void startPhase(String roomId, Phase phase, Duration d) {
        var id = "phase-" + phase.name().toLowerCase();
        var t = RoomTimer.run(
            roomId, id, "Phase: " + phase.name(),
            Instant.now(), Instant.now().plus(d),
            Map.of("kind", "PHASE", "phase", phase.name())
        );
        engine.upsert(t);
        bus.broadcastUpsert(t);
    }

    // Called by engine when deadline hits
    public void onFire(RoomTimer t) {
        var kind = String.valueOf(t.payload().getOrDefault("kind", "GENERIC"));
        switch (kind) {
            case "LOBBY" -> {
                int min = ((Number)t.payload().getOrDefault("minPlayers", 6)).intValue();
                if (room_service.playerCount(t.roomId()) >= min) {
                    room_service.startGame(t.roomId()); // set status RUNNING etc.
                    startPhase(t.roomId(), Phase.NIGHT, Duration.ofSeconds(90));
                } else {
                    bus.notice(t.roomId(), "Not enough players; restarting lobby timer");
                    startLobbyCountdown(t.roomId(), Duration.ofSeconds(60), min);
                }
            }
            case "PHASE" -> {
                var p = Phase.valueOf((String)t.payload().get("phase"));
                var next = p.next(); // or compute via RoomService
                room_service.advancePhase(t.roomId(), next);
                startPhase(t.roomId(), next, room_service.phaseDuration(next));
            }
            default -> {}
        }
        bus.broadcastFired(t);
    }
}
