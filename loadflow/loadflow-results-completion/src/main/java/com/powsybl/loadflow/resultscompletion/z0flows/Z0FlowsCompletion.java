package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.List;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

public class Z0FlowsCompletion {

    public Z0FlowsCompletion(Network network) {
        this.network = network;
    }

    public void complete() {
        z0busGroups().forEach(Z0BusGroup::complete);
    }

    private List<Z0BusGroup> z0busGroups() {
        List<Z0BusGroup> z0busGroups = new ArrayList<>();
        network.getBusView().getBusStream().forEach(bus -> {
            if (!contains(z0busGroups, bus)) {
                Z0BusGroup z0bg = new Z0BusGroup(bus);
                z0bg.exploreZ0();
                if (z0bg.valid()) {
                    z0busGroups.add(z0bg);
                }
            }
        });
        return z0busGroups;
    }

    private boolean contains(List<Z0BusGroup> z0busGroups, Bus bus) {
        // FIXME(Luma): To avoid iteration, maybe keep all found buses in a Set?
        for (Z0BusGroup z : z0busGroups) {
            if (z.contains(bus)) {
                return true;
            }
        }
        return false;
    }

    private final Network network;
}
