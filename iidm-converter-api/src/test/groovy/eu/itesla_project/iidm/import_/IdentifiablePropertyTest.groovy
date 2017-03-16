/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.import_

import eu.itesla_project.iidm.network.Country
import eu.itesla_project.iidm.network.Network
import eu.itesla_project.iidm.network.NetworkFactory
import eu.itesla_project.iidm.network.Substation
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiablePropertyTest {

    @Before
    void setUp() throws Exception {
        // to force static init loading
        ImportGroovyScriptExtension.toString()
    }

    @Test
    void test() {
        Network network = NetworkFactory.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        assertFalse(s.hasProperty())
        assertNull(s.greeting)
        s.greeting = "hello"
        assertEquals("hello", s.getProperties().getProperty("greeting"))
        assertEquals("hello", s.greeting)
    }
}
