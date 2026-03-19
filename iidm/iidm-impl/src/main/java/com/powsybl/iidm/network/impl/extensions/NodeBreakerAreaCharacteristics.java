/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
class NodeBreakerAreaCharacteristics extends AbstractAreaCharacteristics {

    private final Set<Integer> nodes = new HashSet<>();
    private final NodeBreakerDataImpl nodeBreakerView = new NodeBreakerDataImpl();
    private final BusBreakerDataImpl busBreakerView = new BusBreakerDataImpl();

    class NodeBreakerDataImpl implements NodeBreakerData {
        @Override
        public Set<Integer> getNodes() {
            return Collections.unmodifiableSet(nodes);
        }
    }

    static class BusBreakerDataImpl implements BusBreakerData {
        @Override
        public Set<String> getBusIds() {
            throw new UnsupportedOperationException("Not supported in a node breaker topology");
        }
    }

    NodeBreakerAreaCharacteristics(Set<Integer> nodes, Characteristics characteristics, VoltageLevel voltageLevel) {
        super(characteristics, voltageLevel);
        this.nodes.addAll(nodes);
    }

    @Override
    public Set<Terminal> getTerminals() {
        return voltageLevel.getConnectableStream().flatMap(c -> (Stream<Terminal>) c.getTerminals().stream()).filter(t -> nodes.contains(t.getNodeBreakerView().getNode())).collect(Collectors.toSet());
    }

    @Override
    public NodeBreakerData getNodeBreakerData() {
        return nodeBreakerView;
    }

    @Override
    public BusBreakerData getBusBreakerData() {
        return busBreakerView;
    }
}
