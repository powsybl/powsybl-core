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

    BatteryAdder setP0(double p0);

    BatteryAdder setQ0(double q0);

    BatteryAdder setMinP(double minP);

    BatteryAdder setMaxP(double maxP);

    Battery add();
}
