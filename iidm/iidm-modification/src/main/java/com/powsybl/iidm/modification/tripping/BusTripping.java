package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Set;

public class BusTripping extends AbstractTripping {

    public BusTripping(String id) {
        super(id);
    }

    @Override
    public void traverse(Network network, Set<Switch> switchesToOpen, Set<Terminal> terminalsToDisconnect, Set<Terminal> traversedTerminals) {
        Objects.requireNonNull(network);

        Bus bus = network.getBusBreakerView().getBus(id);
        if (bus == null) {
            throw new PowsyblException("Bus section '" + id + "' not found");
        }

        for (Terminal t : bus.getConnectedTerminals()) {
            TrippingTopologyTraverser.traverse(t, switchesToOpen, terminalsToDisconnect, traversedTerminals);
        }
    }
}
