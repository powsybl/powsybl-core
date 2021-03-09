/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class NodeNumberBasedBusRef extends AbstractVoltageLevelBasedBusRef {

    private final int node;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public NodeNumberBasedBusRef(@JsonProperty("voltageLevelId") String voltageLevelId, @JsonProperty("node") int node) {
        super(voltageLevelId);
        this.node = node;
    }

    @Override
    public Optional<Bus> resolve(Network network) {
        return safeGetVoltageLevel(network).map(VoltageLevel::getNodeBreakerView)
                .map(nbb -> {
                    final Terminal terminal = nbb.getTerminal(node);
                    if (terminal == null) {
                        return null;
                    }
                    return terminal.getBusView().getBus();
                });
    }

    public int getNode() {
        return node;
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
        int result = getVoltageLevelId().hashCode();
        result = 31 * result + getNode();
        return result;
    }

}
