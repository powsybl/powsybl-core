/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * Class to describe the characteristics of the fault to be simulated.
 * Used for elementary short-circuit calculation only.
 *
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class BusFault extends AbstractFault {

    public BusFault(String id, double r, double x, ConnectionType connection, FaultType faultType) {
        // Here the id is the id of a bus from the bus view.
        super(id, r, x, connection, faultType);
    }

    public BusFault(String id, double r, double x) {
        // Here the id is the id of a bus from the bus view.
        this(id, r, x, ConnectionType.SERIES, FaultType.THREE_PHASE);
    }

    public BusFault(String id) {
        // Here the id is the id of a bus from the bus view.
        this(id, 0.0, 0.0, ConnectionType.SERIES, FaultType.THREE_PHASE);
    }

    @Override
    public Type getType() {
        return Type.BUS;
    }
}
