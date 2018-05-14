/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a generator, from a <code>VoltageLevel</code> instance call
 * the {@link VoltageLevel#newGenerator()} method to get a generator builder
 * instance.
 * <p>
 * Example:
 *<pre>
 *    VoltageLevel vl = ...
 *    Generator g = vl.newGenerator()
 *            .setId("g1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see Generator
 * @see VoltageLevel
 */
public interface GeneratorAdder extends InjectionAdder<GeneratorAdder> {

    GeneratorAdder setEnergySource(EnergySource energySource);

    GeneratorAdder setMaxP(double maxP);

    GeneratorAdder setMinP(double minP);

    GeneratorAdder setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Set the regulating terminal, If not set or set to null local terminal is used.
     */
    GeneratorAdder setRegulatingTerminal(Terminal regulatingTerminal);

    GeneratorAdder setTargetP(double targetP);

    GeneratorAdder setTargetQ(double targetQ);

    GeneratorAdder setTargetV(double targetV);

    GeneratorAdder setRatedS(double ratedS);

    Generator add();
}
