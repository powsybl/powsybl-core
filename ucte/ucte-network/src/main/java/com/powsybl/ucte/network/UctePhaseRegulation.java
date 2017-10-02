/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UctePhaseRegulation {

    private float du;
    private Integer n; // FIXME: should not be null
    private Integer np; // FIXME: should not be null
    private float u;

    public UctePhaseRegulation(float du, Integer n, Integer np, float u) {
        this.du = du;
        this.n = n;
        this.np = np;
        this.u = u;
    }

    /**
     * Gets δu (%).
     * @return δu (%)
     */
    public float getDu() {
        return du;
    }

    /**
     * Sets δu (%).
     * @param du δu (%)
     */
    public void setDu(float du) {
        this.du = du;
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
     * Gets on load tap changer voltage target for node 2 (V2 or UL) (kV) (optional).
     * @return on load tap changer voltage target for node 2
     */
    public float getU() {
        return u;
    }

    /**
     * Sets on load tap changer voltage target for node 2 (V2 or UL) (kV) (optional).
     * @param u on load tap changer voltage target for node 2
     */
    public void setU(float u) {
        this.u = u;
    }


}
