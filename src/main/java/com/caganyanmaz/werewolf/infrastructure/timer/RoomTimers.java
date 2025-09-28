package com.caganyanmaz.werewolf.infrastructure.timer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RoomTimers {
    public final AtomicReference<ScheduledFuture<?>> lobby = new AtomicReference<>();
    public final AtomicReference<ScheduledFuture<?>> phase = new AtomicReference<>();
}