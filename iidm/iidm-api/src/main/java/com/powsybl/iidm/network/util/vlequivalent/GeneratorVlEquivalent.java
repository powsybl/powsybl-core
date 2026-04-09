/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.vlequivalent;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Networks;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GeneratorVlEquivalent extends AbstractInjectionVlEquivalent {
    private final EnergySource energySource;
    private final double minP;
    private final double maxP;
    private final Terminal regulatingTerminal;
    private final boolean voltageRegulatorOn;
    private final double targetV;
    private final double ratedS;
    private final ReactiveLimits reactiveLimits;

    /**
     * Calculate the characteristics of an equivalent generator for a voltage level containing only a generator and linked to another voltage level with a two-winding transformer.
     * Only use this if those conditions are met, otherwise the result might be unpredictable. See {@link Networks#getSingleConnectableReducibleVoltageLevelStream(Network)}
     * @param voltageLevel the voltage level containing a single battery and a side of a two-winding transformer
     */
    public GeneratorVlEquivalent(VoltageLevel voltageLevel) {
        this(voltageLevel.getGenerators().iterator().next(), voltageLevel.getTwoWindingsTransformers().iterator().next());
    }

    public GeneratorVlEquivalent(Generator generator, TwoWindingsTransformer transformer) {
        super(generator.getId(), generator.getOptionalName().orElse(null), generator.getTargetP(), generator.getTargetQ(), transformer);
        this.energySource = generator.getEnergySource();
        this.minP = generator.getMinP();
        this.maxP = generator.getMaxP();
        this.regulatingTerminal = generator.getRegulatingTerminal();
        this.voltageRegulatorOn = generator.isVoltageRegulatorOn();
        double voltage1 = transformer.getRatedU1();
        double voltage2 = transformer.getRatedU2();
        //find the ratio to apply to the generator's voltage depending on the generator side
        double voltageRatio = transformer.getTerminal1().getVoltageLevel().getId().equals(
                                generator.getTerminal().getVoltageLevel().getId())
                                ? voltage2 / voltage1 : voltage1 / voltage2;
        this.targetV = generator.getTargetV() * voltageRatio;
        this.ratedS = generator.getRatedS();
        this.reactiveLimits = generator.getReactiveLimits();
    }

    public EnergySource getEnergySource() {
        return energySource;
    }

    public double getMinP() {
        return minP;
    }

    public double getMaxP() {
        return maxP;
    }

    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn;
    }

    public double getTargetV() {
        return targetV;
    }

    public double getRatedS() {
        return ratedS;
    }

    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits;
    }

    public double getTargetP() {
        return activePower;
    }

    public double getTargetQ() {
        return reactivePower;
    }
}
