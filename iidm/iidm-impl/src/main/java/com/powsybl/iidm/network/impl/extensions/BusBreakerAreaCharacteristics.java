/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class BusBreakerAreaCharacteristics extends AbstractAreaCharacteristics {

    private final Set<String> buses = new HashSet<>();
    private final NodeBreakerDataImpl nodeBreakerView = new NodeBreakerDataImpl();
    private final BusBreakerDataImpl busBreakerView = new BusBreakerDataImpl();

    BusBreakerAreaCharacteristics(Set<String> buses, Characteristics c, VoltageLevel voltageLevel) {
        super(c, voltageLevel);
        this.buses.addAll(buses);
    }

    static class NodeBreakerDataImpl implements NodeBreakerData {
        @Override
        public Set<Integer> getNodes() {
            throw new UnsupportedOperationException("Not supported in a bus breaker topology");
        }
    }

    class BusBreakerDataImpl implements BusBreakerData {
        @Override
        public Set<String> getBusIds() {
            return Collections.unmodifiableSet(buses);
        }
    }

    @Override
    public Set<Terminal> getTerminals() {
        return voltageLevel.getConnectableStream()
                .flatMap(c -> (Stream<Terminal>) c.getTerminals().stream())
                .filter(t -> t.getBusBreakerView().getBus() != null)
                .filter(t -> buses.contains(t.getBusBreakerView().getBus().getId()))
                .collect(Collectors.toSet());
    }

    @Override
    public NodeBreakerData getNodeBreakerData() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerData getBusBreakerData() {
        return busBreakerView;
    }

    void addBus(String busId) {
        this.buses.add(busId);
    }
}
