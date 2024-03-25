/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.HvdcLine;

import java.util.Objects;

/**
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcActionBuilder implements ActionBuilder {

    private String id;
    private String hvdcId;
    private Boolean acEmulationEnabled;
    private Double activePowerSetpoint = null;
    private HvdcLine.ConvertersMode converterMode = null;
    private Double droop = null;
    private Double p0 = null;
    private Boolean relativeValue = null;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public HvdcAction build() {
        return new HvdcAction(id, hvdcId, acEmulationEnabled, activePowerSetpoint, converterMode, droop, p0, relativeValue);
    }

    @Override
    public HvdcActionBuilder withNetworkElementId(String hvdcId) {
        this.hvdcId = hvdcId;
        return this;
    }

    public HvdcActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public HvdcActionBuilder withHvdcId(String hvdcId) {
        this.hvdcId = hvdcId;
        return this;
    }

    public HvdcActionBuilder withAcEmulationEnabled(Boolean acEmulationEnabled) {
        this.acEmulationEnabled = acEmulationEnabled;
        return this;
    }

    public HvdcActionBuilder withActivePowerSetpoint(Double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    public HvdcActionBuilder withDroop(Double droop) {
        this.droop = droop;
        return this;
    }

    public HvdcActionBuilder withP0(Double p0) {
        this.p0 = p0;
        return this;
    }

    public HvdcActionBuilder withRelativeValue(Boolean relativeValue) {
        this.relativeValue = relativeValue;
        return this;
    }

    public HvdcActionBuilder withConverterMode(HvdcLine.ConvertersMode converterMode) {
        this.converterMode = converterMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HvdcActionBuilder that = (HvdcActionBuilder) o;
        return Objects.equals(id, that.id) && Objects.equals(hvdcId, that.hvdcId) && Objects.equals(acEmulationEnabled, that.acEmulationEnabled) && Objects.equals(activePowerSetpoint, that.activePowerSetpoint) && converterMode == that.converterMode && Objects.equals(droop, that.droop) && Objects.equals(p0, that.p0) && Objects.equals(relativeValue, that.relativeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, hvdcId, acEmulationEnabled, activePowerSetpoint, converterMode, droop, p0, relativeValue);
    }
}
