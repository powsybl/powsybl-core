/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see Generator
 * @see VoltageLevel
 */
public interface GeneratorAdder extends InjectionAdder<Generator, GeneratorAdder> {

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

    /**
     * Set whether the generator may behave as a condenser, for instance if it may control voltage even if its targetP is equal to zero.
     */
    GeneratorAdder setCondenser(boolean isCondenser);

    /**
     * Build the Generator object.
     * <br>These are the checks that are performed before creating the object :
     * <ul> <li>energySource is set</li>
     *      <li>minP is not equal to Double.NaN -> minP is set</li>
     *      <li>maxP is not equal to Double.NaN -> maxP is set</li>
     *      <li>regulatingTerminal is set</li>
     *      <li>network of regulatingTerminal's voltageLevel is the network of the generator</li>
     *      <li>targetP is not equal to Double.NaN -> targetP is set</li>
     *      <li>targetP is not equal to Double.NaN -> targetP is set</li>
     *      <li>minP <= maxP</li>
     *      <li>ratedS is set and ratedS > 0</li>
     *      </ul>
     * @return {@link Generator}
     */
    @Override
    Generator add();
}
