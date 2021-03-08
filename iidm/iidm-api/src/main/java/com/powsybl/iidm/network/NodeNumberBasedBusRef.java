/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class NodeNumberBasedBusRef extends AbstractVoltageLevelBasedBusRef {

    private final int node;

    public NodeNumberBasedBusRef(VoltageLevel voltageLevel, int node) {
        super(voltageLevel);
        this.node = node;
    }

    @Override
    public Optional<Bus> resolve() {
        try {
            final VoltageLevel.NodeBreakerView nodeBreakerView = voltageLevel.getNodeBreakerView();
            return Optional.ofNullable(nodeBreakerView.getTerminal(node).getBusView().getBus());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
