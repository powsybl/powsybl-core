/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class NodeNumberBasedBusRef extends AbstractBusRef {

    private final String voltageLevelId;
    private final int node;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NodeNumberBasedBusRef(@JsonProperty("voltageLevelId") String voltageLevelId, @JsonProperty("node") int node) {
        this.voltageLevelId = Objects.requireNonNull(voltageLevelId);
        this.node = node;
    }

    @Override
    Optional<Bus> resolveByLevel(Network network, TopologyLevel level) {
        final VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        if (voltageLevel == null) {
            return Optional.empty();
        }
        if (Objects.equals(TopologyKind.NODE_BREAKER, voltageLevel.getTopologyKind())) {
            final Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
            if (terminal == null) {
                return Optional.empty();
            }
            return chooseBusByLevel(terminal, level);
        } else {
            throw new PowsyblException("Underlying topology not supported.");
        }
    }

    public int getNode() {
        return node;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeNumberBasedBusRef)) {
            return false;
        }

        NodeNumberBasedBusRef that = (NodeNumberBasedBusRef) o;

        if (!getVoltageLevelId().equals(that.getVoltageLevelId())) {
            return false;
        }
        return getNode() == that.getNode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(voltageLevelId, node);
    }

}
