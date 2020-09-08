/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalBuilder {

    private final Ref<? extends VariantManagerHolder> network;

    private final Validable validable;

    private Integer node;

    private String bus;

    private Terminal.ConnectionStatus connectionStatus;

    TerminalBuilder(Ref<? extends VariantManagerHolder> network, Validable validable) {
        this.network = network;
        this.validable = validable;
    }

    TerminalBuilder setBus(String bus) {
        this.bus = bus;
        return this;
    }

    TerminalBuilder setConnectionStatus(Terminal.ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        return this;
    }

    TerminalBuilder setNode(Integer node) {
        this.node = node;
        return this;
    }

    TerminalExt build() {
        if (node != null && bus != null) {
            throw new ValidationException(validable,
                    "connection node and connection bus are exclusives");
        }

        if (node == null) {
            return new BusTerminal(network, bus, Terminal.ConnectionStatus.CONNECTED.equals(connectionStatus));
        } else {
            return new NodeTerminal(network, node);
        }
    }
}
