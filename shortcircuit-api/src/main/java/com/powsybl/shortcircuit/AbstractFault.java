/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * Abstract class to describe the characteristics of the fault to be simulated.
 * Used for elementary short-circuit calculation only.
 *
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
abstract class AbstractFault implements Fault {

    private final String id;
    private final double r;
    private final double x;
    private final Fault.ConnectionType connection;
    private final Fault.FaultType faultType;
    private final boolean withLimitViolations;
    private final boolean withVoltageMap;

    protected AbstractFault(String id, double r, double x, Fault.ConnectionType connection,
                         Fault.FaultType faultType, boolean withLimitViolations, boolean withVoltageMap) {
        this.id = id;
        this.r = r;
        this.x = x;
        this.connection = connection;
        this.faultType = faultType;
        this.withLimitViolations = withLimitViolations;
        this.withVoltageMap = withVoltageMap;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public double getR() {
        return this.r;
    }

    @Override
    public double getX() {
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

    @Override
    public boolean withLimitViolations() {
        return this.withLimitViolations;
    }

    @Override
    public boolean withVoltageMap() {
        return this.withVoltageMap;
    }
}
