/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class StaticVarCompensatorActionBuilder implements ActionBuilder<StaticVarCompensatorActionBuilder> {

    private String id;
    private String staticVarCompensatorId;
    private StaticVarCompensator.RegulationMode regulationMode;
    private Double voltageSetpoint;
    private Double reactivePowerSetpoint;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public StaticVarCompensatorActionBuilder withNetworkElementId(String staticVarCompensatorId) {
        this.staticVarCompensatorId = staticVarCompensatorId;
        return this;
    }

    @Override
    public StaticVarCompensatorAction build() {
        return new StaticVarCompensatorAction(id, staticVarCompensatorId, regulationMode, voltageSetpoint, reactivePowerSetpoint);
    }

    @Override
    public String getType() {
        return StaticVarCompensatorAction.NAME;
    }

    public StaticVarCompensatorActionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public StaticVarCompensatorActionBuilder withStaticVarCompensatorId(String staticVarCompensatorId) {
        return withNetworkElementId(staticVarCompensatorId);
    }

    public StaticVarCompensatorActionBuilder withRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    public StaticVarCompensatorActionBuilder withVoltageSetpoint(Double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    public StaticVarCompensatorActionBuilder withReactivePowerSetpoint(Double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
        return this;
    }
}
