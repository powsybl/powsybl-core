/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Optional;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackBusAdderImpl extends AbstractExtensionAdder<VoltageLevel, SlackBus> implements SlackBusAdder {

    private static final int NODE_UNINITIALIZED = -1;

    private String busId;
    private int node = NODE_UNINITIALIZED;

    public SlackBusAdderImpl(VoltageLevel voltageLevel) {
        super(voltageLevel);
    }

    @Override
    public SlackBusAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public SlackBusAdder setBusId(String busId) {
        this.busId = busId;
        return this;
    }

    @Override
    public SlackBus createExtension(VoltageLevel voltageLevel) {
        switch (voltageLevel.getTopologyKind()) {
            case NODE_BREAKER:
                if (node == NODE_UNINITIALIZED) {
                    throw new PowsyblException("Node needs to be set for a SlackBus in VoltageLevel.NodeBreakerView");
                }
                Optional<Terminal> optTerminal = voltageLevel.getNodeBreakerView().getOptionalTerminal(node);
                if (!optTerminal.isPresent()) {
                    throw new PowsyblException("The given slackBus node lacks a terminal");
                }
                return new SlackBusNodeBreakerImpl(optTerminal.get(), voltageLevel);
            case BUS_BREAKER:
                if (busId == null) {
                    throw new PowsyblException("BusId needs to be set for a SlackBus in VoltageLevel.BusBreakerView");
                }
                return new SlackBusBusBreakerImpl(busId, voltageLevel);
            default:
                throw new AssertionError("Unexpected TopologyKind of given voltageLevel: "
                    + voltageLevel.getTopologyKind());
        }
    }
}
