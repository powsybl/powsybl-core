/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LineCharacteristics<T> {

    /**
     * Get the series resistance in &#937;.
     */
    double getR();

    /**
     * Set the series resistance in &#937;.
     */
    T setR(double r);

    /**
     * Get the series reactance in &#937;.
     */
    double getX();

    /**
     * Set the series reactance in &#937;.
     */
    T setX(double x);

    /**
     * Get the first side shunt conductance in S.
     */
    double getG1();

    /**
     * Set the first side shunt conductance in S.
     */
    T setG1(double g1);

    /**
     * Get the second side shunt conductance in S.
     */
    double getG2();

    /**
     * Set the second side shunt conductance in S.
     */
    T setG2(double g2);

    /**
     * Get the first side shunt susceptance in S.
     */
    double getB1();

    /**
     * Set the first side shunt susceptance in S.
     */
    T setB1(double b1);

    /**
     * Get the second side shunt susceptance in S.
     */
    double getB2();

    /**
     * Set the second side shunt susceptance in S.
     */
    T setB2(double b2);

}
