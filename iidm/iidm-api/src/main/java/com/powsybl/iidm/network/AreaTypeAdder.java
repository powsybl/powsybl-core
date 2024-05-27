/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * To create an AreaType, from a <code>Network</code> instance call
 * the {@link Network#newAreaType()} method to get an AreaType builder instance.
 * <p>
 * Example:
 *<pre>
 *    Network n = ...
 *    Substation s = n.newAreaType()
 *            .setId("BiddingZone")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 * @see AreaType
 * @see Network
 */
public interface AreaTypeAdder extends IdentifiableAdder<AreaType, AreaTypeAdder> {

    AreaTypeAdder copy(AreaType otherAreaType);

    @Override
    AreaType add();

}
