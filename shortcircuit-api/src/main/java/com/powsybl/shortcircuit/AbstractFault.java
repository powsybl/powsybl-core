/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import java.util.Objects;

/**
 * Abstract class to describe the characteristics of the fault to be simulated.
 * Used for elementary short-circuit analysis only.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
abstract class AbstractFault implements Fault {

    private final String id;
    private final String elementId;
    private final double r;
    private final double x;
    private final ConnectionType connection;
    private final FaultType faultType;

    protected AbstractFault(String id, String elementId, double r, double x, ConnectionType connection,
                            FaultType faultType) {
        this.id = Objects.requireNonNull(id);
        this.elementId = Objects.requireNonNull(elementId);
        this.r = r;
        this.x = x;
        this.connection = Objects.requireNonNull(connection);
        this.faultType = Objects.requireNonNull(faultType);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getElementId() {
        return this.elementId;
    }

    @Override
    public double getRToGround() {
        return this.r;
    }

    @Override
    public double getXToGround() {
        return this.x;
    }

    @Override
    public ConnectionType getConnectionType() {
        return this.connection;
    }

    @Override
    public FaultType getFaultType() {
        return this.faultType;
    }
}
