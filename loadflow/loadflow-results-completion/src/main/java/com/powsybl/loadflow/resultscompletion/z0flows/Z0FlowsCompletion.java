package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

public class Z0FlowsCompletion {

    public Z0FlowsCompletion(Network network, Z0LineChecker z0Line) {
        this.network = network;
        this.z0Line = z0Line;
        processed = new HashSet<>();
    }

    public void complete() {
        z0busGroups().forEach(Z0BusGroup::complete);
    }

    private List<Z0BusGroup> z0busGroups() {
        List<Z0BusGroup> z0busGroups = new ArrayList<>();
        network.getBusView().getBusStream().forEach(bus -> {
            if (!processed.contains(bus)) {
                Z0BusGroup z0bg = new Z0BusGroup(bus, z0Line);
                z0bg.exploreZ0(processed);
                if (z0bg.valid()) {
                    z0busGroups.add(z0bg);
                }
            }
        });
        return z0busGroups;
    }

    private final Network network;
    private final Z0LineChecker z0Line;
    private Set<Bus> processed;
}
