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
 * @see DanglingLineAdder
 */
public interface DanglingLine extends Injection<DanglingLine> {

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working state.
     * @see StateManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working state.
     * @see StateManager
     */
    DanglingLine setP0(double p0);

    /**
     * Get the constant reactive power in MW.
     * <p>Depends on the working state.
     * @see StateManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MW.
     * <p>Depends on the working state.
     * @see StateManager
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
     * Get the UCTE Xnode code corresponding to this dangling line in the case
     * where the line is a boundary, return null otherwise.
     */
    String getUcteXnodeCode();

    CurrentLimits getCurrentLimits();

    CurrentLimitsAdder newCurrentLimits();

}
