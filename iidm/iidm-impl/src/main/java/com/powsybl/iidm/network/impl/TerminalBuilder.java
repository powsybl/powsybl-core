/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.impl.util.Ref;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalBuilder {

    private final Ref<? extends MultiStateObject> network;

    private final Validable validable;

    private Integer node;

    private String bus;

    private String connectableBus;

    TerminalBuilder(Ref<? extends MultiStateObject> network, Validable validable) {
        this.network = network;
        this.validable = validable;
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

        if (node == null) {
            if (connectionBus == null) {
                throw new ValidationException(validable, "connectable bus is not set");
            }

            return new BusTerminal(network, connectionBus, bus != null);
        } else {
            return new NodeTerminal(network, node);
        }
    }

    private String getConnectionBus() {
        if (bus != null) {
            if ((connectableBus != null) && (!bus.equals(connectableBus))) {
                throw new ValidationException(validable, "connection bus is different to connectable bus");
            }

            return bus;
        } else {
            return connectableBus;
        }
    }
}
