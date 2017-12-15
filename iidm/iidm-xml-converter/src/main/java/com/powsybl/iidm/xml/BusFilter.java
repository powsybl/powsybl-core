/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BusFilter {

    private final Set<String> buses;

    private final XMLExportOptions options;

    static BusFilter create(Network n, XMLExportOptions options) {
        Set<String> buses = null;
        if (options.isOnlyMainCc()) {
            buses = new HashSet<>();
            // keep bus of main cc
            if (options.getTopologyLevel() == TopologyLevel.BUS_BRANCH) {
                for (Bus b : n.getBusView().getBuses()) {
                    if (b.isInMainConnectedComponent()) {
                        buses.add(b.getId());
                    }
                }
            } else {
                for (Bus b : n.getBusBreakerView().getBuses()) {
                    if (b.isInMainConnectedComponent()) {
                        buses.add(b.getId());
                    }
                }
            }
            // and also bus at the other side of open branches
            for (Branch branch : n.getBranches()) {
                Terminal t1 = branch.getTerminal1();
                Terminal t2 = branch.getTerminal2();
                if (options.getTopologyLevel() == TopologyLevel.BUS_BRANCH) {
                    Bus b1 = t1.getBusView().getConnectableBus();
                    Bus b2 = t2.getBusView().getConnectableBus();
                    if ((b1 != null && b1.isInMainConnectedComponent()) && b2 != null && !b2.isInMainConnectedComponent()) {
                        buses.add(b2.getId());
                    } else if (b1 != null && !b1.isInMainConnectedComponent() && b2 != null && b2.isInMainConnectedComponent()) {
                        buses.add(b1.getId());
                    }
                } else {
                    Bus b1 = t1.getBusBreakerView().getConnectableBus();
                    Bus b2 = t2.getBusBreakerView().getConnectableBus();
                    if (b1.isInMainConnectedComponent() && !b2.isInMainConnectedComponent()) {
                        buses.add(b2.getId());
                    } else if (!b1.isInMainConnectedComponent() && b2.isInMainConnectedComponent()) {
                        buses.add(b1.getId());
                    }
                }
            }
            for (ThreeWindingsTransformer twt : n.getThreeWindingsTransformers()) {
                throw new AssertionError("TODO");
            }
        }
        return new BusFilter(buses, options);
    }

    BusFilter(Set<String> buses, XMLExportOptions options) {
        this.buses = buses;
        this.options = Objects.requireNonNull(options);
    }

    BusFilter(XMLExportOptions options) {
        this(null, options);
    }

    public boolean test(Bus b) {
        return buses == null || buses.contains(b.getId());
    }

    public boolean test(Connectable<?> connectable) {
        if (buses == null) {
            return true;
        }
        for (Terminal t : connectable.getTerminals()) {
            Bus b = options.getTopologyLevel() == TopologyLevel.BUS_BRANCH ? t.getBusView().getConnectableBus() : t.getBusBreakerView().getConnectableBus();
            if (b != null && !buses.contains(b.getId())) {
                return false;
            }
        }
        return true;
    }
}
