/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TapChangerStep<TCS extends TapChangerStep> {

    /**
     * Get the voltage ratio in per unit.
     */
    double getRho();

    /**
     * Set the voltage ratio in per unit.
     */
    TCS setRho(double rho);

    /**
     * Get the resistance deviation in percent of nominal value.
     */
    double getR();

    /**
     * Set the resistance deviation in percent of nominal value.
     */
    TCS setR(double r);

    /**
     * Get the reactance deviation in percent of nominal value.
     */
    double getX();

    /**
     * Set the reactance deviation in percent of nominal value.
     */
    TCS setX(double x);

    /**
     * Get the susceptance deviation in percent of nominal value.
     */
    double getB();

    /**
     * Set the susceptance deviation in percent of nominal value.
     */
    TCS setB(double b);

    /**
     * Get the conductance deviation in percent of nominal value.
     */
    double getG();

    /**
     * Set the conductance deviation in percent of nominal value.
     */
    TCS setG(double g);

}
