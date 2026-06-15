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

    private Double localTargetQ = Double.NaN;

    private Double localTargetV = Double.NaN;

    private Boolean regulating;

    private VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes = null;

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
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    @Override
    public StaticVarCompensatorAdder setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
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
        return new VoltageRegulationAdderImpl<>(StaticVarCompensator.class, this, this, getNetworkRef(), this::setVoltageRegulationAttributes);
    }

    @Override
    public StaticVarCompensatorImpl add() {
        NetworkImpl network = getNetwork();
        String id = checkAndGetUniqueId();
        String name = getName();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && regulating == null) {
            regulating = false;
        }
        if (voltageRegulationAttributes == null && regulating != null) {
            if (regulationMode == null) {
                regulationMode = RegulationMode.VOLTAGE;
            }
            double targetValue = Double.NaN;
            if (regulatingTerminal != null) {
                if (regulationMode == RegulationMode.VOLTAGE) {
                    targetValue = voltageSetpoint;
                    localTargetQ = reactivePowerSetpoint;
                } else {
                    targetValue = reactivePowerSetpoint;
                    localTargetV = voltageSetpoint;
                }
            } else {
                localTargetV = voltageSetpoint;
                localTargetQ = reactivePowerSetpoint;
            }
            newVoltageRegulation()
                .withMode(regulationMode)
                .withTargetValue(targetValue)
                .withTerminal(regulatingTerminal)
                .withRegulating(regulating)
                .add();
        }

        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkLocalTargetQandV(this, StaticVarCompensator.class, localTargetV, localTargetQ, voltageRegulationAttributes, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        StaticVarCompensatorImpl svc = new StaticVarCompensatorImpl(id, name, isFictitious(), bMin, bMax, voltageRegulationAttributes, getNetworkRef(), localTargetQ, localTargetV);
        svc.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(svc);
        network.getListeners().notifyCreation(svc);
        return svc;
    }

}
