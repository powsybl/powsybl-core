/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class Equipments {

    private Equipments() {
    }

    public record ConnectionInfo(Bus connectionBus, boolean connected) {
    }

    public static ConnectionInfo getConnectionInfoInBusBreakerView(Terminal t) {
        Bus bus = t.getBusBreakerView().getBus();
        boolean connected;
        if (bus != null) {
            connected = true;
        } else {
            connected = false;
            bus = t.getBusBreakerView().getConnectableBus();
        }
        return new ConnectionInfo(bus, connected);
    }

}
