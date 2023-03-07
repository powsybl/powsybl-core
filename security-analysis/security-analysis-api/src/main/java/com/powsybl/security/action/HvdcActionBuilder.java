/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.HvdcLine;

/**
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class HvdcActionBuilder {

    private String id;
    private String hvdcId;
    private boolean acEmulationEnabled;
    private Double activePowerSetpoint = null;
    private HvdcLine.ConvertersMode converterMode = null;
    private Double droop = null;
    private Double p0 = null;
    private Boolean relativeValue = null;

    public HvdcAction build() {
        return new HvdcAction(id, hvdcId, acEmulationEnabled, activePowerSetpoint, converterMode, droop, p0, relativeValue);
    }

    public HvdcActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public HvdcActionBuilder withHvdcId(String hvdcId) {
        this.hvdcId = hvdcId;
        return this;
    }

    public HvdcActionBuilder withAcEmulationEnabled(boolean acEmulationEnabled) {
        this.acEmulationEnabled = acEmulationEnabled;
        return this;
    }

    public HvdcActionBuilder withActivePowerSetpoint(double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    public HvdcActionBuilder withDroop(double droop) {
        this.droop = droop;
        return this;
    }

    public HvdcActionBuilder withP0(double p0) {
        this.p0 = p0;
        return this;
    }

    public HvdcActionBuilder withRelativeValue(boolean relativeValue) {
        this.relativeValue = relativeValue;
        return this;
    }

    public HvdcActionBuilder withConverterMode(HvdcLine.ConvertersMode converterMode) {
        this.converterMode = converterMode;
        return this;
    }
}
