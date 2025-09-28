package com.caganyanmaz.werewolf.application.game;

import com.caganyanmaz.werewolf.application.room.Participant;

public record PlayerInfo(String player_id, String nickname, boolean alive) {}