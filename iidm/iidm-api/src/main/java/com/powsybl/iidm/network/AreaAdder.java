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
 *    Substation s = n.newArea()
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

    AreaAdder setAreaType(String areaType);

    AreaAdder setAcNetInterchangeTarget(Double acNetInterchangeTarget);

    AreaAdder setAcNetInterchangeTolerance(Double acNetInterchangeTolerance);

    AreaAdder addVoltageLevel(VoltageLevel voltageLevel);

    AreaAdder addBoundaryTerminal(Terminal terminal, boolean ac);

    @Override
    Area add();
}
