/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * To create an Area, from a <code>Network</code> instance call
 * the {@link Network#newArea()} method to get an Area builder instance.
 * <p>
 * Example:
 *<pre>
 *    Network n = ...
 *    Area a = n.newArea()
 *            .setId("FR")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 * @see Area
 * @see Network
 */
public interface AreaAdder extends IdentifiableAdder<Area, AreaAdder> {

    /**
     * Set the Area type
     */
    AreaAdder setAreaType(String areaType);

    /**
     * Set the target AC Interchange of this area in MW, in load sign convention (negative is export, positive is import).
     */
    AreaAdder setAcInterchangeTarget(double acInterchangeTarget);

    /**
     * add a VoltageLevel to the Area
     */
    AreaAdder addVoltageLevel(VoltageLevel voltageLevel);

    /**
     * add a Terminal as an area boundary
     */
    AreaAdder addAreaBoundary(Terminal terminal, boolean ac);

    /**
     * add a DanglingLine boundary as an area boundary
     */
    AreaAdder addAreaBoundary(Boundary boundary, boolean ac);

    /**
     * Build the Area object.
     * <p>These are the checks that are performed before creating the object :</p>
     * <ul>
     *     <li>areaType is not null;</li>
     * </ul>
     * @return {@link Area}
     */
    @Override
    Area add();
}
