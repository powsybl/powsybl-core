/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.elementmerging;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.vlequivalent.GeneratorVlEquivalent;

import java.util.function.Function;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public class GeneratorTwoWindingTransformerMergeModification extends AbstractInjectionTwoWindingTransformerMergeModification<Generator, GeneratorAdder, GeneratorVlEquivalent> {

    public GeneratorTwoWindingTransformerMergeModification(String voltageLevelId) {
        super(voltageLevelId);
    }

    @Override
    public String getName() {
        return "GeneratorTwoWindingTransformerMergeModification";
    }

    @Override
    int getInjectionCount(VoltageLevel voltageLevel) {
        return voltageLevel.getGeneratorCount();
    }

    @Override
    String getInjectionName() {
        return "generator";
    }

    @Override
    Function<Double, GeneratorAdder> getActivePowerSetter(GeneratorAdder adder) {
        return adder::setTargetP;
    }

    @Override
    Function<Double, GeneratorAdder> getReactivePowerSetter(GeneratorAdder adder) {
        return adder::setTargetQ;
    }

    @Override
    GeneratorAdder setSpecificInjectionParameters(GeneratorAdder adder, GeneratorVlEquivalent equivalent) {
        return adder
            .setCondenser(equivalent.isCondenser())
            .setMinP(equivalent.getMinP())
            .setMaxP(equivalent.getMaxP())
            .setTargetV(equivalent.getTargetV())
            .setRatedS(equivalent.getRatedS())
            .setEnergySource(equivalent.getEnergySource())
            .setRegulatingTerminal(equivalent.getRegulatingTerminal())
            .setVoltageRegulatorOn(equivalent.isVoltageRegulatorOn()); //TODO how to set regulating limits ?
    }

    @Override
    GeneratorVlEquivalent createEquivalent(Generator injection, TwoWindingsTransformer transformer) {
        return new GeneratorVlEquivalent(injection, transformer);
    }

    @Override
    GeneratorAdder createAdder(VoltageLevel voltageLevel) {
        return voltageLevel.newGenerator();
    }

    @Override
    Generator getInjection(VoltageLevel voltageLevel) {
        return voltageLevel.getGenerators().iterator().next();
    }
}
