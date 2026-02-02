/**
 * Copyright (c) 2026, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.regulation.VoltageRegulationImpl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationMsaAdder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class VoltageRegulationMsaAdderImpl<O extends VoltageRegulationOwner> implements VoltageRegulationMsaAdder {

    private final O owner;

    private RegulationMode mode = null;
    private double targetValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private double slope = Double.NaN;
    private Terminal terminal;
    private boolean regulating;

    VoltageRegulationMsaAdderImpl(O owner) {
        this.owner = owner;
    }

    @Override
    public VoltageRegulationMsaAdder setTargetValue(double targetValue) {
        this.targetValue = targetValue;
        return this;
    }

    @Override
    public VoltageRegulationMsaAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public VoltageRegulationMsaAdder setSlope(double slope) {
        this.slope = slope;
        return this;
    }

    @Override
    public VoltageRegulationMsaAdder setMode(RegulationMode regulationMode) {
        this.mode = regulationMode;
        return this;
    }

    @Override
    public VoltageRegulationMsaAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public VoltageRegulationMsaAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public VoltageRegulation add() {
        // TODO MSA add validation
        VoltageRegulation voltageRegulation = new VoltageRegulationImpl(owner.getNetwork().getRef(), targetValue, targetDeadband, slope, terminal, mode, regulating);
        owner.setVoltageRegulation(voltageRegulation);
        return voltageRegulation;
    }

}
