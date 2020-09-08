/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractInjectionAdder<T extends AbstractInjectionAdder<T>> extends AbstractIdentifiableAdder<T> {

    private Integer node;

    private String bus;

    private Terminal.ConnectionStatus connectionStatus;

    public T setNode(int node) {
        this.node = node;
        return (T) this;
    }

    public T setBus(String bus) {
        this.bus = bus;
        return (T) this;
    }

    public T setConnectionStatus(Terminal.ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
        return (T) this;
    }

    protected TerminalExt checkAndGetTerminal() {
        return new TerminalBuilder(getNetwork().getRef(), this)
                .setNode(node)
                .setBus(bus)
                .setConnectionStatus(connectionStatus)
                .build();
    }
}
