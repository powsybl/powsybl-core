/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SlackTerminalAdderImpl extends AbstractExtensionAdder<VoltageLevel, SlackTerminal> implements SlackTerminalAdder {

    private static final int NODE_UNINITIALIZED = -1;

    private String busId;
    private int node = NODE_UNINITIALIZED;

    public SlackTerminalAdderImpl(VoltageLevel voltageLevel) {
        super(voltageLevel);
    }

    @Override
    public SlackTerminalAdder setNode(int node) {
        this.node = node;
        return this;
    }

    @Override
    public SlackTerminalAdder setBusId(String busId) {
        this.busId = busId;
        return this;
    }

    @Override
    public SlackTerminal createExtension(VoltageLevel voltageLevel) {
        switch (voltageLevel.getTopologyKind()) {
            case NODE_BREAKER:
                if (node == NODE_UNINITIALIZED) {
                    throw new PowsyblException("Node needs to be set for a SlackBus in VoltageLevel.NodeBreakerView");
                }
                return new SlackTerminalNodeBreakerImpl(node, voltageLevel);
            case BUS_BREAKER:
                if (busId == null) {
                    throw new PowsyblException("BusId needs to be set for a SlackBus in VoltageLevel.BusBreakerView");
                }
                return new SlackTerminalBusBreakerImpl(busId, voltageLevel);
            default:
                throw new AssertionError("Unexpected TopologyKind of given voltageLevel: "
                    + voltageLevel.getTopologyKind());
        }
    }
}
