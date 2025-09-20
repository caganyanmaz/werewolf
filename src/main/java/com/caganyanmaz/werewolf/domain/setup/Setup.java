package com.caganyanmaz.werewolf.domain.setup;

import java.util.Map;
import com.caganyanmaz.werewolf.domain.Role;

public record Setup(
    SetupId id,
    String name,
    int minPlayers,
    int maxPlayers,
    Map<Role, Integer> roleCounts
) 
{
    public int totalRoles() { return roleCounts.values().stream().mapToInt(Integer::intValue).sum(); }
}
