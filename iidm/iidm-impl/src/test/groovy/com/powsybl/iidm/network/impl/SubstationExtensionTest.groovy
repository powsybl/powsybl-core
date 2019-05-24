/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl

import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationExtensionTest {

    @Test
    void nullCountryTest() {
        def network = NetworkFactory.create("test", "")
        def s = network.newSubstation()
                .setId("s")
                .add()
        assertNull(s.country);
    }

    @Test
    void nonNullCountryTest() {
        def network = NetworkFactory.create("test", "")
        def s = network.newSubstation()
                .setId("s")
                .setCountry(Country.BD)
                .add()
        assertEquals(Country.BD, s.country);
    }
}
