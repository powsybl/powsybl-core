/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

import java.util.Objects;
import java.util.function.Supplier;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StaticVarCompensatorAdderImpl extends AbstractInjectionAdder<StaticVarCompensatorAdderImpl> implements StaticVarCompensatorAdder {

    private double bMin = Double.NaN;

    private double bMax = Double.NaN;

    private double voltageSetpoint = Double.NaN;

    private double reactivePowerSetpoint = Double.NaN;

    private RegulationMode regulationMode;

    private TerminalExt regulatingTerminal;

    private Double targetQ = Double.NaN;

    private Double targetV = Double.NaN;

    private Boolean regulating;

    private Supplier<VoltageRegulation> voltageRegulationCreator = null;

    StaticVarCompensatorAdderImpl(VoltageLevelExt vl) {
        this.voltageLevel = Objects.requireNonNull(vl);
    }

    @Override
    protected String getTypeDescription() {
        return StaticVarCompensatorImpl.TYPE_DESCRIPTION;
    }

    @Override
    public StaticVarCompensatorAdderImpl setBmin(double bMin) {
        this.bMin = bMin;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setBmax(double bMax) {
        this.bMax = bMax;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetQ(double localTargetQ) {
        this.targetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.targetQ;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetV(double localTargetV) {
        this.targetV = localTargetV;
        return this;
    }

    private void setVoltageRegulationCreator(Supplier<VoltageRegulation> voltageRegulationCreator) {
        this.voltageRegulationCreator = voltageRegulationCreator;
    }

    @Override
    public StaticVarCompensatorAdderImpl setVoltageSetpoint(double voltageSetpoint) {
        this.voltageSetpoint = voltageSetpoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        this.reactivePowerSetpoint = reactivePowerSetpoint;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulationMode(RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public VoltageRegulationAdder<StaticVarCompensatorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(StaticVarCompensator.class, this, this, getNetworkRef(), this::setVoltageRegulationCreator);
    }

    @Override
    public StaticVarCompensatorImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        String name = getName();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && regulating == null) {
            regulating = false;
        }
        if (regulationMode == null) {
            regulationMode = RegulationMode.VOLTAGE;
        }
        if (voltageRegulationCreator == null && regulating != null) {
            createVoltageRegulationBackwardCompatibility(this, voltageSetpoint, reactivePowerSetpoint, regulating, regulatingTerminal);
        }
        VoltageRegulationExt voltageRegulation = voltageRegulationCreator != null ? (VoltageRegulationExt) voltageRegulationCreator.get() : null;

        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
        StaticVarCompensatorImpl svc = new StaticVarCompensatorImpl(id, name, isFictitious(), bMin, bMax, voltageRegulation, getNetworkRef(), targetQ, targetV);
        svc.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(svc);
        network.getListeners().notifyCreation(svc);
        return svc;
    }

}
