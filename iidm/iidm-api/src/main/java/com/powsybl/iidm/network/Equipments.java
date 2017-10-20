/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

import java.util.Iterator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Equipments {

    private Equipments() {
    }

    public static class ConnectionInfo {

        private final Bus connectionBus;

        private final boolean connected;

        public ConnectionInfo(Bus connectionBus, boolean connected) {
            this.connectionBus = connectionBus;
            this.connected = connected;
        }

        public Bus getConnectionBus() {
            return connectionBus;
        }

        public boolean isConnected() {
            return connected;
        }

    }

    public static ConnectionInfo getConnectionInfoInBusBreakerView(Terminal t) {
        Bus bus = t.getBusBreakerView().getBus();
        boolean connected;
        if (bus != null) {
            connected = true;
        } else {
            connected = false;
            bus = t.getBusBreakerView().getConnectableBus();
            if (bus == null) {
                // otherwise take first bus of the substation at the same voltage
                // level...
                Iterator<Bus> itVLB = t.getVoltageLevel().getBusBreakerView().getBuses().iterator();
                if (itVLB.hasNext()) {
                    bus = itVLB.next();
                } else {
                    throw new PowsyblException("Cannot find a connection bus");
                }
            }
        }
        return new ConnectionInfo(bus, connected);
    }

}
