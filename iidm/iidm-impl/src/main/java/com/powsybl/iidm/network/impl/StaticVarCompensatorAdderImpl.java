/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StaticVarCompensatorAdderImpl extends AbstractInjectionAdder<StaticVarCompensatorAdderImpl> implements StaticVarCompensatorAdder {

    private final VoltageLevelExt vl;

    private double bMin = Double.NaN;

    private double bMax = Double.NaN;

    private double voltageSetpoint = Double.NaN;

    private double reactivePowerSetpoint = Double.NaN;

    private StaticVarCompensator.RegulationMode regulationMode = StaticVarCompensator.RegulationMode.OFF;

    private boolean useLocalRegulation = false;

    private TerminalExt regulatingTerminal;

    StaticVarCompensatorAdderImpl(VoltageLevelExt vl) {
        this.vl = Objects.requireNonNull(vl);
    }

    @Override
    protected NetworkImpl getNetwork() {
        return vl.getNetwork();
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
    public StaticVarCompensatorAdderImpl setRegulationMode(StaticVarCompensator.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public StaticVarCompensatorAdderImpl useLocalRegulation(boolean use) {
        this.useLocalRegulation = use;
        return this;
    }

    @Override
    public StaticVarCompensatorImpl add() {
        String id = checkAndGetUniqueId();
        String name = getName();
        TerminalExt terminal = checkAndGetTerminal();
        boolean validateRegulatingTerminal = true;
        if (useLocalRegulation) {
            regulatingTerminal = terminal;
            validateRegulatingTerminal = false;
        }

        ValidationUtil.checkBmin(this, bMin);
        ValidationUtil.checkBmax(this, bMax);

        // The validation method of the regulating terminal (validation.validRegulatingTerminal)
        // checks that the terminal is not null and its network is the same as the object being added.
        // The network for the terminal is obtained from its voltage level but the terminal voltage level
        // is set after the validation is performed by the method voltageLevel.attach(terminal, false).
        // As we do not want to move the order of validation and terminal attachment
        // we do not check the regulating terminal if useLocalRegulation is true.
        // We assume the terminal will be ok since it will be the one of the equipment.
        ValidationUtil.checkSvcRegulatingControl(this, regulatingTerminal, voltageSetpoint,
            reactivePowerSetpoint, regulationMode, getNetwork(), validateRegulatingTerminal);
        StaticVarCompensatorImpl svc = new StaticVarCompensatorImpl(id, name, isFictitious(), bMin, bMax,
            voltageSetpoint, reactivePowerSetpoint, regulationMode, regulatingTerminal, getNetwork().getRef());
        svc.addTerminal(terminal);
        vl.attach(terminal, false);
        getNetwork().getIndex().checkAndAdd(svc);
        getNetwork().getListeners().notifyCreation(svc);
        return svc;
    }

}
