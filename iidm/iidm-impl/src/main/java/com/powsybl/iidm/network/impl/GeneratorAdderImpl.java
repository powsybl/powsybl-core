/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.*;

import static com.powsybl.iidm.network.util.VoltageRegulationUtils.createVoltageRegulationBackwardCompatibility;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorAdderImpl extends AbstractInjectionAdder<GeneratorAdderImpl> implements GeneratorAdder {

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private TerminalExt regulatingTerminal;

    private VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes = null;

    private Boolean voltageRegulatorOn;

    private double targetP = Double.NaN;

    private double localTargetQ = Double.NaN;

    private double targetValue = Double.NaN;

    private double localTargetV = Double.NaN;

    private double ratedS = Double.NaN;

    private boolean isCondenser = false;

    GeneratorAdderImpl(VoltageLevelExt voltageLevel) {
        this.voltageLevel = voltageLevel;
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
    public GeneratorAdder setTargetQ(double targetQ) {
        return this.setLocalTargetQ(targetQ);
    }

    @Override
    public GeneratorAdderImpl setLocalTargetQ(double localTargetQ) {
        this.localTargetQ = localTargetQ;
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ;
    }

    public GeneratorAdderImpl setTargetV(double targetV) {
        return this.setTargetV(targetV, targetV);
    }

    @Override
    public GeneratorAdderImpl setTargetV(double targetV, double equivalentLocalTargetV) {
        this.targetValue = targetV;
        return this.setLocalTargetV(equivalentLocalTargetV);
    }

    @Override
    public GeneratorAdderImpl setLocalTargetV(double localTargetV) {
        this.localTargetV = localTargetV;
        return this;
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public GeneratorAdder setRatedS(double ratedS) {
        this.ratedS = ratedS;
        return this;
    }

    @Override
    public GeneratorAdder setCondenser(boolean isCondenser) {
        this.isCondenser = isCondenser;
        return this;
    }

    @Override
    public GeneratorImpl add() {
        NetworkImpl network = getNetwork();
        if (network.getMinValidationLevel() == ValidationLevel.EQUIPMENT && voltageRegulatorOn == null) {
            voltageRegulatorOn = false;
        }
        String id = checkAndGetUniqueId();
        TerminalExt terminal = checkAndGetTerminal();
        ValidationUtil.checkEnergySource(this, energySource);
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkActivePowerSetpoint(this, targetP, network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);
        // Backward compatibility : If a generator with old setters is added and voltageRegulation does not exist,
        // the new voltageRegulation will be created from the old attributes.
        if (voltageRegulationAttributes == null && voltageRegulatorOn != null) {
            createVoltageRegulationBackwardCompatibility(this, targetValue, localTargetV, localTargetQ, voltageRegulatorOn, regulatingTerminal);
        }

        network.setValidationLevelIfGreaterThan(ValidationUtil.checkLocalTargetQandV(this, Generator.class, localTargetV, localTargetQ, voltageRegulationAttributes, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        GeneratorImpl generator
                = new GeneratorImpl(getNetworkRef(),
                                    id, getName(), isFictitious(), energySource,
                                    minP, maxP,
                                    voltageRegulationAttributes,
                                    targetP, localTargetQ, localTargetV,
                                    ratedS, isCondenser);
        generator.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        // put targetValue in localtargetV if localTerminal
        network.getIndex().checkAndAdd(generator);
        network.getListeners().notifyCreation(generator);
        return generator;
    }

    @Override
    public VoltageRegulationAdder<GeneratorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(Generator.class, this, this, getNetworkRef(), this::setVoltageRegulationAttributes);
    }
}
