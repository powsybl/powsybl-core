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

    private float minP = Float.NaN;

    private float maxP = Float.NaN;

    private TerminalExt regulatingTerminal;

    private Boolean voltageRegulatorOn;

    private float targetP = Float.NaN;

    private float targetQ = Float.NaN;

    private float targetV = Float.NaN;

    private float ratedS = Float.NaN;

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
    public GeneratorAdderImpl setMaxP(float maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public GeneratorAdderImpl setMinP(float minP) {
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
    public GeneratorAdderImpl setTargetP(float targetP) {
        this.targetP = targetP;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetQ(float targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetV(float targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public GeneratorAdder setRatedS(float ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public GeneratorImpl add() {
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal(id);
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
