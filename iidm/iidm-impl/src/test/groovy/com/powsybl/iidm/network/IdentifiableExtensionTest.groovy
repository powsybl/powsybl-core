/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network

import com.powsybl.commons.extensions.AbstractExtension
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableExtensionTest {

    static class Foo extends AbstractExtension<Substation> {

        private float value

        @Override
        String getName() {
            return "foo"
        }
    }

    private Substation s

    @Before
    void setUp() throws Exception {
        Network network = NetworkFactory.create("test", "test")
        s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add()
    }

    @Test
    void testProperty() {
        assertFalse(s.hasProperty())
        assertNull(s.greeting)
        s.greeting = "hello"
        assertEquals("hello", s.getProperties().getProperty("greeting"))
        assertEquals("hello", s.greeting)
    }

    @Test
    void testExtension() {
        assertNull(s.foo)
        s.addExtension(Foo.class, new Foo())
        assertNotNull(s.foo)
        s.foo.value = 3f
        assertEquals(3f, s.foo.value, 0f)
    }

}
