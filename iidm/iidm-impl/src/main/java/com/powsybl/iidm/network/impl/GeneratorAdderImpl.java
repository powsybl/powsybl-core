/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.regulation.VoltageRegulationImpl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.impl.regulation.VoltageRegulationAdderImpl;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorAdderImpl extends AbstractInjectionAdder<GeneratorAdderImpl> implements GeneratorAdder {

    private EnergySource energySource = EnergySource.OTHER;

    private double minP = Double.NaN;

    private double maxP = Double.NaN;

    private TerminalExt regulatingTerminal;

    private Boolean voltageRegulatorOn;

    private VoltageRegulationImpl voltageRegulation;

    private double targetP = Double.NaN;

    private double targetQ = Double.NaN;

    private double targetV = Double.NaN;

    private double equivalentLocalTargetQ = Double.NaN;

    private double equivalentLocalTargetV = Double.NaN;

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
    public GeneratorAdderImpl setEquivalentLocalTargetQ(double equivalentLocalTargetQ) {
        this.equivalentLocalTargetQ = equivalentLocalTargetQ;
        return this;
    }

    @Override
    public GeneratorAdderImpl setEquivalentLocalTargetV(double equivalentLocalTargetV) {
        this.equivalentLocalTargetV = equivalentLocalTargetV;
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
        this.equivalentLocalTargetV = Double.NaN;
        return this;
    }

    @Override
    public GeneratorAdderImpl setTargetV(double targetV, double equivalentLocalTargetV) {
        this.targetV = targetV;
        this.equivalentLocalTargetV = equivalentLocalTargetV;
        return this;
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
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV, targetQ,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        ValidationUtil.checkRatedS(this, ratedS);
        ValidationUtil.checkEquivalentLocalTargetV(this, equivalentLocalTargetV);
        ValidationUtil.checkEquivalentLocalTargetQ(this, equivalentLocalTargetQ, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        if (this.voltageRegulation == null) {
            this.voltageRegulation = createVoltageRegulation();
        }
        ValidationUtil.checkVoltageRegulation(this.voltageRegulation);
        GeneratorImpl generator
                = new GeneratorImpl(getNetworkRef(),
                                    id, getName(), isFictitious(), energySource,
                                    minP, maxP,
                                    voltageRegulation,
                                    targetP, equivalentLocalTargetQ, equivalentLocalTargetV,
                                    ratedS, isCondenser);
        generator.addTerminal(terminal);
        voltageLevel.getTopologyModel().attach(terminal, false);
        network.getIndex().checkAndAdd(generator);
        network.getListeners().notifyCreation(generator);
        return generator;
    }

    private VoltageRegulationImpl createVoltageRegulation() {
        // Common attributes
        VoltageRegulationImpl.Builder builder = VoltageRegulationImpl.builder()
            .setNetwork(getNetworkRef())
            .setTerminal(this.regulatingTerminal);
        // VOLTAGE case
        if (this.voltageRegulatorOn && !Double.isNaN(this.targetV)) {
            builder.setRegulating(true)
                .setMode(RegulationMode.VOLTAGE)
                .setTargetValue(this.targetV);
            this.equivalentLocalTargetQ = this.targetQ;
            return builder.build();
            // REACTIVE Power case
        } else if (!Double.isNaN(this.targetQ)) {
            builder.setRegulating(true)
                .setMode(RegulationMode.REACTIVE_POWER)
                .setTargetValue(this.targetQ);
            this.equivalentLocalTargetV = this.targetV;
            return builder.build();
        }
        return null;
    }

    @Override
    public VoltageRegulationBuilder<GeneratorAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(this, this.getNetworkRef());
    }

    public VoltageRegulation getVoltageRegulation() {
        return voltageRegulation;
    }

    @Override
    public void setVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.voltageRegulation = (VoltageRegulationImpl) voltageRegulation;
    }
}
