/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * To create a substation, from a <code>Network</code> instance call
 * the {@link Network#newSubstation() } method to get a substation builder
 * instance.
 * <p>
 * Example:
 *<pre>
 *    Network n = ...
 *    Substation s = n.newSubstation()
 *            .setId("s1")
 *            ...
 *        .add();
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see Substation
 * @see Network
 */
public interface SubstationAdder extends IdentifiableAdder<SubstationAdder> {

    SubstationAdder setCountry(Country country);

    SubstationAdder setTso(String tso);

    SubstationAdder setGeographicalTags(String... tags);

    Substation add();
}
