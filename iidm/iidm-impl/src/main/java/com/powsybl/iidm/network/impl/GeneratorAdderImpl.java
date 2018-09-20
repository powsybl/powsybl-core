/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.GeneratorAdder;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Terminal;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorAdderImpl extends AbstractInjectionAdder<GeneratorAdderImpl> implements GeneratorAdder {

    private final VoltageLevelExt voltageLevel;

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private TerminalExt regulatingTerminal;

    private Boolean voltageRegulatorOn;

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double targetV = Double.NaN;

    private double ratedS = Double.NaN;

    GeneratorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }

    @Override
    public GeneratorAdderImpl setEnergySource(EnergySource energySource) {
        this.energySource = energySource;
        return this;
    }

    @Override
    public GeneratorAdderImpl setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public GeneratorAdderImpl setMinP(double minP) {
        this.minP = minP;
        return this;
    }

    @Override
    public GeneratorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    @Override
    public GeneratorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetP(double targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public GeneratorAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public GeneratorImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkEnergySource(this, energySource);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        ValidationUtil.checkActivePowerSetpoint(this, targetP);
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, targetQ);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);
        GeneratorImpl generator
                = new GeneratorImpl(getNetwork().getRef(),
                                    id, getName(), energySource,
                                    minP, maxP,
                                    voltageRegulatorOn, regulatingTerminal != null ? regulatingTerminal : terminal,
                                    targetP, targetQ, targetV,
                                    ratedS);
        generator.addTerminal(terminal);
        voltageLevel.attach(terminal, false);
        getNetwork().getObjectStore().checkAndAdd(generator);
        getNetwork().getListeners().notifyCreation(generator);
        return generator;
    }

}
