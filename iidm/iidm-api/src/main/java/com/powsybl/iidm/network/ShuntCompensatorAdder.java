/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a shunt compensator, from a <code>VoltageLevel</code> instance call
 * the {@link VoltageLevel#newShuntCompensator()} method to get a shunt compensator builder
 * instance.
 * <p>
 * Example:
 *<pre>
 *    VoltageLevel vl = ...
 *    ShuntCompensator s = vl.newShunt()
 *            .setId("s1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see ShuntCompensator
 * @see VoltageLevel
 */
public interface ShuntCompensatorAdder extends InjectionAdder<ShuntCompensatorAdder> {

    ShuntCompensatorAdder setbPerSection(double bPerSection);

    ShuntCompensatorAdder setMaximumSectionCount(int maximumSectionCount);

    ShuntCompensatorAdder setCurrentSectionCount(int currentSectionCount);

    ShuntCompensator add();

}
