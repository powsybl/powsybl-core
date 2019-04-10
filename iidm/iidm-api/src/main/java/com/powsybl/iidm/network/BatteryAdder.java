/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a battery, from a <code>VoltageLevel</code> instance call
 * the {@link VoltageLevel#newBattery()} method to get a battery builder
 * instance.
 * <p>
 * Example:
 *<pre>
 *    VoltageLevel vl = ...
 *    Battery b = vl.newBattery()
 *            .setId("b1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 * @see Battery
 * @see VoltageLevel
 */
public interface BatteryAdder extends InjectionAdder<BatteryAdder> {

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    BatteryAdder setP0(double p0);

    /**
     * Set the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    BatteryAdder setQ0(double q0);

    /**
     * Set the minimal active power in MW.
     */
    BatteryAdder setMinP(double minP);

    /**
     * Set the maximal active power in MW.
     */
    BatteryAdder setMaxP(double maxP);

    /**
     * Build the Battery object.
     * This are the checks that are performed before creating the object :
     *      - p0 is not equal to Double.NaN -> p0 is set
     *      - q0 is not equal to Double.NaN -> q0 is set
     *      - minP is not equal to Double.NaN -> minP is set
     *      - maxP is not equal to Double.NaN -> maxP is set
     *      - minP is less than maxP
     * @return {@link Battery}
     */
    Battery add();
}
