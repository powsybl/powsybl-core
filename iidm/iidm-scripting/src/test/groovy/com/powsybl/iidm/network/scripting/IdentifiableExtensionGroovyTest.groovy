/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.google.auto.service.AutoService
import com.powsybl.commons.extensions.AbstractExtension
import com.powsybl.commons.extensions.AbstractExtensionAdder
import com.powsybl.commons.extensions.ExtensionAdderProvider
import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.Substation
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IdentifiableExtensionGroovyTest {

    static class Foo extends AbstractExtension<Substation> {

        static final String NAME = "foo"

        private float value

        Foo(float value) {
            this.value = value
        }

        @Override
        String getName() {
            return NAME
        }
    }

    static class FooAdder extends AbstractExtensionAdder<Substation, Foo> {

        private float value;

        FooAdder(Substation substation) {
            super(substation);
        }

        @Override
        Class<? super Foo> getExtensionClass() {
            return Foo.class;
        }

        FooAdder setValue(float value) {
            this.value = value
            return this
        }

        @Override
        protected Foo createExtension(Substation extendable) {
            return new Foo(value)
        }
    }

    @AutoService(ExtensionAdderProvider.class)
    static class FooAdderProvider implements ExtensionAdderProvider<Substation, Foo, FooAdder> {

        @Override
        String getExtensionName() {
            return Foo.NAME
        }

        @Override
        String getImplementationName() {
            return "Default"
        }

        @Override
        Class<? super FooAdder> getAdderClass() {
            return FooAdder.class
        }

        @Override
        FooAdder newAdder(Substation substation) {
            return new FooAdder(substation)
        }
    }

    private Substation s

    @BeforeEach
    void setUp() throws Exception {
        Network network = Network.create("test", "test")
        s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add()
    }

    @Test
    void testStringProperty() {
        assertFalse(s.hasProperty())
        assertNull(s.greeting)
        s.greeting = "hello"
        assertEquals("hello", s.getProperty("greeting"))
        assertEquals("hello", s.greeting)
    }

    @Test
    void testBooleanProperty() {
        assertFalse(s.hasProperty())
        assertNull(s.ok)
        s.ok = true
        assertEquals("true", s.getProperty("ok"))
        assertEquals(true, s.ok)
        s.ok = null
        assertNull(s.ok)
    }

    @Test
    void testExtension() {
        assertNull(s.foo)
        s.addExtension(Foo.class, new Foo(1f))
        assertNotNull(s.foo)
        s.foo.value = 3f
        assertEquals(3f, s.foo.value, 0f)
    }

    @Test
    void testCreateExtensionJavaSyntax() {
        s.newExtension(FooAdder.class)
            .setValue(5f)
            .add()
        assertEquals(5f, s.foo.value, 0f)
    }

    @Test
    void testCreateExtensionGroovySyntax() {
        s.newFoo()
            .setValue(5f)
            .add()
        assertEquals(5f, s.foo.value, 0f)
    }

    @Test
    void testCreateExtensionOtherGroovySyntax() {
        s.fooAdder()
            .setValue(5f)
            .add()
        assertEquals(5f, s.foo.value, 0f)
    }

    @Test
    void testCreateExtensionIdiomaticGroovySyntax() {
        s.newFoo {
            value 5f
        }
        assertEquals(5f, s.foo.value, 0f)
    }
}
