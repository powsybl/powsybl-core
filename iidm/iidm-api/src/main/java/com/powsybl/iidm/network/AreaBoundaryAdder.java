/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * To create an AreaBoundary, from a <code>Area</code> instance call
 * the {@link Area#newAreaBoundary()} method to get an AreaBoundary builder instance.
 * <p>
 * Example:
 *<pre>
 *    Area a = ...
 *    AreaBoundary ab = a.newAreaBoundary()
 *            .setAc(true)
 *            ...
 *        .add();
 *</pre>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */

public interface AreaBoundaryAdder {
    AreaBoundaryAdder setBoundary(Boundary boundary);

    AreaBoundaryAdder setTerminal(Terminal terminal);

    AreaBoundaryAdder setAc(boolean ac);

    void add();
}
