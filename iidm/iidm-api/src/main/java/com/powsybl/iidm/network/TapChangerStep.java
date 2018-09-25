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
public interface TapChangerStep<S extends TapChangerStep> {

    /**
     * Get the voltage ratio in per unit.
     */
    double getRho();

    /**
     * Set the voltage ratio in per unit.
     */
    S setRho(double rho);

    /**
     * Get the resistance deviation in percent of nominal value.
     */
    double getRdr();

    /**
     * Set the resistance deviation in percent of nominal value.
     */
    S setRdr(double rdr);

    /**
     * Get the reactance deviation in percent of nominal value.
     */
    double getRdx();

    /**
     * Set the reactance deviation in percent of nominal value.
     */
    S setRdx(double rdx);

    /**
     * Get the susceptance deviation in percent of nominal value.
     */
    double getRdb();

    /**
     * Set the susceptance deviation in percent of nominal value.
     */
    S setRdb(double rdb);

    /**
     * Get the conductance deviation in percent of nominal value.
     */
    double getRdg();

    /**
     * Set the conductance deviation in percent of nominal value.
     */
    S setRdg(double rdg);

    @Deprecated
    /**
     * @deprecated Use {@link #getRdr()} instead.
     */
    default double getR() {
        return getRdr();
    }

    @Deprecated
    /**
     * @deprecated Use {@link #setRdr(double)} instead.
     */
    default S setR(double r) {
        return setRdr(r);
    }

    @Deprecated
    /**
     * @deprecated Use {@link #getRdx()} instead.
     */
    default double getX() {
        return getRdx();
    }

    @Deprecated
    /**
     * @deprecated Use {@link #setRdx(double)} instead.
     */
    default S setX(double x) {
        return setRdx(x);
    }

    @Deprecated
    /**
     * @deprecated Use {@link #getRdg()} instead.
     */
    default double getG() {
        return getRdg();
    }

    @Deprecated
    /**
     * @deprecated Use {@link #setRdg(double)} instead.
     */
    default S setG(double g) {
        return setRdg(g);
    }

    @Deprecated
    /**
     * @deprecated Use {@link #getRdb()} instead.
     */
    default double getB() {
        return getRdb();
    }

    @Deprecated
    /**
     * @deprecated Use {@link #setRdb(double)} instead.
     */
    default S setB(double b) {
        return setRdb(b);
    }
}
