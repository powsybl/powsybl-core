/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TerminalNumber;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.TopologyKind;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TerminalBuilder {

    private final Ref<? extends VariantManagerHolder> network;

    private final Validable validable;

    private final ThreeSides side;

    private final TerminalNumber terminalNumber;

    private final TopologyKind topologyKind;

    private Integer node;

    private String bus;

    private String connectableBus;

    TerminalBuilder(Ref<? extends VariantManagerHolder> network, TopologyKind topologyKind, Validable validable, ThreeSides side, TerminalNumber terminalNumber) {
        this.network = Objects.requireNonNull(network);
        this.validable = Objects.requireNonNull(validable);
        this.topologyKind = Objects.requireNonNull(topologyKind);
        this.side = side;
        this.terminalNumber = terminalNumber;
    }

    TerminalBuilder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    TerminalBuilder setConnectableBus(String connectableBus) {
        this.connectableBus = connectableBus;
        return this;
    }

    TerminalBuilder setNode(Integer node) {
        this.node = node;
        return this;
    }

    TerminalExt build() {
        String connectionBus = getConnectionBus();
        if (node != null && connectionBus != null) {
            throw new ValidationException(validable,
                    "connection node and connection bus are exclusives");
        }

        if (topologyKind == TopologyKind.NODE_BREAKER) {
            if (node == null) {
                throw new ValidationException(validable, "node is not set");
            }
            return new NodeTerminal(network, side, terminalNumber, node);
        } else {
            if (connectionBus == null) {
                throw new ValidationException(validable, "connectable bus is not set");
            }
            return new BusTerminal(network, side, terminalNumber, connectionBus, bus != null);
        }
    }

    private String getConnectionBus() {
        if (bus != null) {
            if (connectableBus != null && !bus.equals(connectableBus)) {
                throw new ValidationException(validable, "connection bus is different to connectable bus");
            }

            return bus;
        } else {
            return connectableBus;
        }
    }
}
