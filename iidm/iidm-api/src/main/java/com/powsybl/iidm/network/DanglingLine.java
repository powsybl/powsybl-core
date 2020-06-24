/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A dangling line to model boundaries (X nodes).
 * <p>A dangling line is a component that aggregates a line chunk and a constant
 * power injection (fixed p0, q0).
 * <div>
 *    <object data="doc-files/danglingLine.svg" type="image/svg+xml"></object>
 * </div>
 * Electrical characteritics (r, x, g, b) corresponding to a percent of the
 * orginal line.
 * <p>r, x, g, b have to be consistent with the declared length of the dangling
 * line.
 * <p>To create a dangling line, see {@link DanglingLineAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 * @see DanglingLineAdder
 */
public interface DanglingLine extends Injection<DanglingLine>, ReactiveLimitsHolder {

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setP0(double p0);

    /**
     * Get the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setQ0(double q0);

    /**
     * Get the series resistance in &#937;.
     */
    double getR();

    /**
     * Set the series resistance in &#937;.
     */
    DanglingLine setR(double r);

    /**
     * Get the series reactance in &#937;.
     */
    double getX();

    /**
     * Set the series reactance in &#937;.
     */
    DanglingLine setX(double x);

    /**
     * Get the shunt conductance in S.
     */
    double getG();

    /**
     * Set the shunt conductance in S.
     */
    DanglingLine setG(double g);

    /**
     * Get the shunt susceptance in S.
     */
    double getB();

    /**
     * Set the shunt susceptance in S.
     */
    DanglingLine setB(double b);

    /**
     * <p>Get the generator active power target in MW.</p>
     * <p>The active power target follows a generator sign convention.</p>
     * <p>Depends on the working variant.</p>
     * @return the generator active power target
     */
    double getGeneratorTargetP();

    /**
     * <p>Set the generator active power target in MW.</p>
     * <p>Depends on the working variant.</p>
     * @param generatorTargetP the generator active power target
     * @return this to allow method chaining
     */
    DanglingLine setGeneratorTargetP(double generatorTargetP);

    /**
     * Get the generator maximal active power in MW.
     */
    double getGeneratorMaxP();

    /**
     * Set the generator maximal active power in MW.
     */
    DanglingLine setGeneratorMaxP(double generatorMaxP);

    /**
     * Get the generator minimal active power in MW.
     */
    double getGeneratorMinP();

    /**
     * Set the generator minimal active power in MW.
     */
    DanglingLine setGeneratorMinP(double generatorMinP);

    /**
     * <p>Get the generator reactive power target in MVAR.</p>
     * <p>The generator reactive power target follows a generator sign convention.</p>
     * <p>Depends on the working variant.</p>
     * @return the generator reactive power target
     */
    double getGeneratorTargetQ();

    /**
     * <p>Set the generator reactive power target in MVAR.</p>
     * <p>Depends on the working variant.</p>
     * @param generatorTargetQ the generator reactive power target
     * @return this to allow method chaining
     */
    DanglingLine setGeneratorTargetQ(double generatorTargetQ);

    /**
     * Get the generator voltage regulation status.
     */
    boolean isGeneratorVoltageRegulationOn();

    /**
     * Set the generator voltage regulation status.
     */
    DanglingLine setGeneratorVoltageRegulationOn(boolean generatorVoltageRegulationOn);

    /**
     * <p>Get the generator voltage target in Kv.</p>
     * <p>Depends on the working variant.</p>
     * @return the generator voltage target
     */
    double getGeneratorTargetV();

    /**
     * <p>Set the generator voltage target in Kv.</p>
     * <p>Depends on the working variant.</p>
     * @param generatorTargetV the generator voltage target
     * @return this to allow method chaining
     */
    DanglingLine setGeneratorTargetV(double generatorTargetV);

    /**
     * Get the UCTE Xnode code corresponding to this dangling line in the case
     * where the line is a boundary, return null otherwise.
     */
    String getUcteXnodeCode();

    CurrentLimits getCurrentLimits();

    CurrentLimitsAdder newCurrentLimits();

}
