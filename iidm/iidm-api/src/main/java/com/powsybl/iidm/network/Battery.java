/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A battery system.
 * <p>
 * To create a battery, see {@link BatteryAdder}
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 * @see BatteryAdder
 * @see MinMaxReactiveLimits
 * @see ReactiveCapabilityCurve
 */
public interface Battery extends Injection<Battery>, ReactiveLimitsHolder {

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
    Battery setP0(double p0);

    /**
     * Get the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    Battery setQ0(double q0);

    /**
     * Get the minimal active power in MW.
     */
    double getMinP();

    /**
     * Set the minimal active power in MW.
     */
    Battery setMinP(double minP);

    /**
     * Get the maximal active power in MW.
     */
    double getMaxP();

    /**
     * Set the maximal active power in MW.
     */
    Battery setMaxP(double maxP);
}
