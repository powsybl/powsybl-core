/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * Class to describe the characteristics of a fault that occurs on a bus and that is to be simulated.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public class BusFault extends AbstractFault {

    public BusFault(String id, String elementId, double r, double x, ConnectionType connection, FaultType faultType) {
        // Here the elementId is the id of a bus from the bus view.
        super(id, elementId, r, x, connection, faultType);
    }

    public BusFault(String id, String elementId, double r, double x) {
        // Here the elementId is the id of a bus from the bus view.
        this(id, elementId, r, x, ConnectionType.SERIES, FaultType.THREE_PHASE);
    }

    public BusFault(String id, String elementId) {
        // Here the elementId is the id of a bus from the bus view.
        this(id, elementId, 0.0, 0.0, ConnectionType.SERIES, FaultType.THREE_PHASE);
    }

    @Override
    public Type getType() {
        return Type.BUS;
    }
}
