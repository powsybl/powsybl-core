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
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;

import java.util.Objects;

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

    private VoltageRegulationImpl voltageRegulation;

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
    public StaticVarCompensatorAdder setTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public StaticVarCompensatorAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
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

    private void setVoltageRegulation(VoltageRegulationImpl voltageRegulation) {
        this.voltageRegulation = voltageRegulation;
    }

    @Override
    public VoltageRegulationAdder<StaticVarCompensatorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(StaticVarCompensator.class, this, getNetworkRef(), this::setVoltageRegulation);
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
        if (voltageRegulation == null) {
            double targetValue;
            if (regulationMode == RegulationMode.VOLTAGE) {
                targetValue = voltageSetpoint;
            } else {
                targetValue = reactivePowerSetpoint;
            }
            newVoltageRegulation()
                .withMode(regulationMode)
                .withTerminal(regulatingTerminal)
                .withTargetValue(targetValue)
                .withRegulating(Boolean.TRUE.equals(regulating))
                .add();
        }

        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
//        network.setValidationLevelIfGreaterThan(ValidationUtil.checkSvcRegulator(this, regulating, voltageSetpoint, reactivePowerSetpoint, regulationMode,
//                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        StaticVarCompensatorImpl svc = new StaticVarCompensatorImpl(id, name, isFictitious(), bMin, bMax, voltageRegulation, getNetworkRef(), targetQ, targetV);
        svc.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(svc);
        network.getListeners().notifyCreation(svc);
        return svc;
    }

}
