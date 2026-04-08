/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteAngleRegulation {

    private double du;
    private double theta;
    private Integer n; // FIXME: should not be null (strictly positive?)
    private Integer np; // FIXME: should not be null (should be in [-n, n])
    private double p;
    private UcteAngleRegulationType type; // FIXME: should not be null (see. UcteRegulation.fix())

    public UcteAngleRegulation(double du, double theta, Integer n, Integer np, double p, UcteAngleRegulationType type) {
        this.du = du;
        this.theta = theta;
        this.n = n;
        this.np = np;
        this.p = p;
        this.type = type;
    }

    /**
     * Gets δu (%).
     * @return δu (%)
     */
    public double getDu() {
        return du;
    }

    /**
     * Sets δu (%).
     * @param du δu (%)
     */
    public void setDu(double du) {
        this.du = du;
    }

    /**
     * Gets Θ (°).
     * @return Θ (°)
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Sets Θ (°).
     * @param theta Θ (°)
     */
    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Gets number of taps.
     * <p>Counted the following way: it is the difference between the intermediate
     * position (neutral) and the positive or negative ultimate position (e.g. a
     * transformer with total 27 taps (+13,neutral,-13) is given as n = 13 in the
     * UCTE format).
     * @return number of taps
     */
    public Integer getN() {
        return n;
    }

    /**
     * Sets number of taps.
     * <p>Counted the following way: it is the difference between the intermediate
     * position (neutral) and the positive or negative ultimate position (e.g. a
     * transformer with total 27 taps (+13,neutral,-13) is given as n = 13 in the
     * UCTE format).
     * @param n number of taps
     */
    public void setN(Integer n) {
        this.n = n;
    }

    /**
     * Gets tap position.
     * @return tap position
     */
    public Integer getNp() {
        return np;
    }

    /**
     * Sets tap position.
     * @param np tap position
     */
    public void setNp(Integer np) {
        this.np = np;
    }

    /**
     * Gets on load tap changer active power flow target (MW).
     * <p>(optional).
     * @return on load tap changer active power flow target (MW)
     */
    public double getP() {
        return p;
    }

    /**
     * Sets on load tap changer active power flow target (MW).
     * <p>(optional).
     * @param p on load tap changer active power flow target (MW)
     */
    public void setP(double p) {
        this.p = p;
    }

    /**
     * Gets regulation type.
     * @return regulation type
     */
    public UcteAngleRegulationType getType() {
        return type;
    }

    /**
     * Sets regulation type.
     * @param type regulation type
     */
    public void setType(UcteAngleRegulationType type) {
        this.type = type;
    }

}
