package com.caganyanmaz.werewolf.application.ports;

import java.util.List;
import java.util.Optional;

import com.caganyanmaz.werewolf.domain.setup.Setup;
import com.caganyanmaz.werewolf.domain.setup.SetupId;

public interface SetupCatalog {
    List<Setup> listAll();
    Optional<Setup> findById(SetupId id);

}