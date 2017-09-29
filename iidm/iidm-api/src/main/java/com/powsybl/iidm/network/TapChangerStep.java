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
    float getRho();

    /**
     * Set the voltage ratio in per unit.
     */
    TCS setRho(float rho);

    /**
     * Get the resistance deviation in percent of nominal value.
     */
    float getR();

    /**
     * Set the resistance deviation in percent of nominal value.
     */
    TCS setR(float r);

    /**
     * Get the reactance deviation in percent of nominal value.
     */
    float getX();

    /**
     * Set the reactance deviation in percent of nominal value.
     */
    TCS setX(float x);

    /**
     * Get the susceptance deviation in percent of nominal value.
     */
    float getB();

    /**
     * Set the susceptance deviation in percent of nominal value.
     */
    TCS setB(float b);

    /**
     * Get the conductance deviation in percent of nominal value.
     */
    float getG();

    /**
     * Set the conductance deviation in percent of nominal value.
     */
    TCS setG(float g);

}
