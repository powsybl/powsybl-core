/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;

class ExtensionAdderProvidersTest {

    private interface SimpleExtendable extends Extendable<SimpleExtendable> {
    }

    private static class SimpleExtendableImpl2
            extends AbstractExtendable<SimpleExtendable> implements SimpleExtendable {
        @Override
        public String getImplementationName() {
            return "Custom";
        }
    }

    private interface SimpleExtension extends Extension<SimpleExtendable> {

        String NAME = "SimpleExtension";

        default String getName() {
            return NAME;
        }
    }

    private static class SimpleExtensionImpl extends AbstractExtension<SimpleExtendable>
            implements SimpleExtension {
    }

    private static class SimpleExtensionImpl2 extends AbstractExtension<SimpleExtendable>
            implements SimpleExtension {
    }

    private interface SimpleExtensionAdder extends ExtensionAdder<SimpleExtendable, SimpleExtension> {
        default Class<SimpleExtension> getExtensionClass() {
            return SimpleExtension.class;
        }
    }

    private static class SimpleExtensionAdderImpl
            extends AbstractExtensionAdder<SimpleExtendable, SimpleExtension>
            implements SimpleExtensionAdder {
        protected SimpleExtensionAdderImpl(SimpleExtendable extendable) {
            super(extendable);
        }

        @Override
        protected SimpleExtensionImpl createExtension(SimpleExtendable extendable) {
            return new SimpleExtensionImpl();
        }
    }

    private static class SimpleExtensionAdderImpl2
            extends AbstractExtensionAdder<SimpleExtendable, SimpleExtension>
            implements SimpleExtensionAdder {
        protected SimpleExtensionAdderImpl2(SimpleExtendable extendable) {
            super(extendable);
        }

        @Override
        protected SimpleExtensionImpl2 createExtension(SimpleExtendable extendable) {
            return new SimpleExtensionImpl2();
        }
    }

    private static class SimpleExtensionAdderImplProvider
            implements
            ExtensionAdderProvider<SimpleExtendable, SimpleExtension, SimpleExtensionAdderImpl> {
        @Override
        public String getImplementationName() {
            return "Default";
        }

        @Override
        public String getExtensionName() {
            return SimpleExtension.NAME;
        }

        @Override
        public Class<SimpleExtensionAdderImpl> getAdderClass() {
            return SimpleExtensionAdderImpl.class;
        }

        @Override
        public SimpleExtensionAdderImpl newAdder(SimpleExtendable extendable) {
            return new SimpleExtensionAdderImpl(extendable);
        }
    }

    @AutoService(ExtensionAdderProvider.class)
    public static class SimpleExtensionAdderImpl2Provider implements
            ExtensionAdderProvider<SimpleExtendable, SimpleExtension, SimpleExtensionAdderImpl2> {
        @Override
        public String getImplementationName() {
            return "Custom";
        }

        @Override
        public String getExtensionName() {
            return SimpleExtension.NAME;
        }

        @Override
        public Class<SimpleExtensionAdderImpl2> getAdderClass() {
            return SimpleExtensionAdderImpl2.class;
        }

        @Override
        public SimpleExtensionAdderImpl2 newAdder(SimpleExtendable extendable) {
            return new SimpleExtensionAdderImpl2(extendable);
        }
    }

    private interface GenericExtendable<G extends GenericExtendable<G>> extends Extendable<G> {
    }

    private static class GenericExtendableImpl2<G extends GenericExtendable<G>>
            extends AbstractExtendable<G> implements GenericExtendable<G> {
        @Override
        public String getImplementationName() {
            return "Custom";
        }
    }

    private interface SpecificExtendable extends GenericExtendable<SpecificExtendable> {
    }

    private static class SpecificExtendableImpl2
            extends AbstractExtendable<SpecificExtendable>
            implements SpecificExtendable {
        @Override
        public String getImplementationName() {
            return "Custom";
        }
    }

    private interface GenericExtension<G extends GenericExtendable<G>> extends Extension<G> {

        String NAME = "SimpleExtension";

        default String getName() {
            return NAME;
        }
    }

    private static class GenericExtensionImpl<G extends GenericExtendable<G>>
            extends AbstractExtension<G>
            implements GenericExtension<G> {
    }

    private static class GenericExtensionImpl2<G extends GenericExtendable<G>>
            extends AbstractExtension<G>
            implements GenericExtension<G> {
    }

    private interface GenericExtensionAdder<G extends GenericExtendable<G>> extends ExtensionAdder<G, GenericExtension<G>> {

        default Class<GenericExtension> getExtensionClass() {
            return GenericExtension.class;
        }
    }

    private static class GenericExtensionAdderImpl<G extends GenericExtendable<G>>
            extends AbstractExtensionAdder<G, GenericExtension<G>>
            implements GenericExtensionAdder<G> {
        protected GenericExtensionAdderImpl(G extendable) {
            super(extendable);
        }

        @Override
        protected GenericExtensionImpl<G> createExtension(G genericExtendable) {
            return new GenericExtensionImpl<>();
        }
    }

    private static class GenericExtensionAdderImpl2<G extends GenericExtendable<G>>
            extends AbstractExtensionAdder<G, GenericExtension<G>>
            implements GenericExtensionAdder<G> {
        protected GenericExtensionAdderImpl2(G extendable) {
            super(extendable);
        }

        @Override
        protected GenericExtensionImpl2<G> createExtension(G genericExtendable) {
            return new GenericExtensionImpl2<>();
        }
    }

    private static class GenericExtensionAdderImplProvider<G extends GenericExtendable<G>>
            implements
            ExtensionAdderProvider<G, GenericExtension<G>, GenericExtensionAdderImpl<G>> {
        @Override
        public String getImplementationName() {
            return "Default";
        }

        @Override
        public String getExtensionName() {
            return GenericExtension.NAME;
        }

        @Override
        public Class<GenericExtensionAdderImpl> getAdderClass() {
            return GenericExtensionAdderImpl.class;
        }

        @Override
        public GenericExtensionAdderImpl<G> newAdder(G extendable) {
            return new GenericExtensionAdderImpl<>(extendable);
        }
    }

    @AutoService(ExtensionAdderProvider.class)
    public static class GenericExtensionAdderImpl2Provider<G extends GenericExtendable<G>>
            implements
            ExtensionAdderProvider<G, GenericExtension<G>, GenericExtensionAdderImpl2<G>> {
        @Override
        public String getImplementationName() {
            return "Custom";
        }

        @Override
        public String getExtensionName() {
            return GenericExtension.NAME;
        }

        @Override
        public Class<GenericExtensionAdderImpl2> getAdderClass() {
            return GenericExtensionAdderImpl2.class;
        }

        @Override
        public GenericExtensionAdderImpl2<G> newAdder(G extendable) {
            return new GenericExtensionAdderImpl2<>(extendable);
        }
    }

    @Test
    void test() {
        List<ExtensionAdderProvider> listProviders = Arrays.asList(
                new SimpleExtensionAdderImplProvider(),
                new SimpleExtensionAdderImpl2Provider(),
                new GenericExtensionAdderImplProvider(),
                new GenericExtensionAdderImpl2Provider());
        ConcurrentMap<Pair<String, Class>, ExtensionAdderProvider> cache = new ConcurrentHashMap<>();
        ConcurrentMap<String, List<ExtensionAdderProvider>> mapProviders = ExtensionAdderProviders.groupProvidersByName(listProviders);
        ExtensionAdderProvider a = ExtensionAdderProviders.findCachedProvider("Default",
                SimpleExtensionAdder.class, mapProviders, cache);
        assertEquals("Default", a.getImplementationName());
        assertEquals(SimpleExtensionAdderImplProvider.class, a.getClass());
        assertEquals(1, cache.size());
        ExtensionAdderProvider b = ExtensionAdderProviders.findCachedProvider("Custom",
                SimpleExtensionAdder.class, mapProviders, cache);
        assertEquals("Custom", b.getImplementationName());
        assertEquals(SimpleExtensionAdderImpl2Provider.class, b.getClass());
        assertEquals(2, cache.size());

        // Do it again, it should work from the cache
        assertEquals("Default", ExtensionAdderProviders.findCachedProvider("Default", SimpleExtensionAdder.class,
                new ConcurrentHashMap<>(), cache).getImplementationName());
        assertEquals("Custom", ExtensionAdderProviders.findCachedProvider("Custom", SimpleExtensionAdder.class,
                                new ConcurrentHashMap<>(), cache)
                        .getImplementationName());
    }

    @Test
    void testMissing() {
        try {
            ExtensionAdderProviders.findCachedProvider("Default",
                    SimpleExtensionAdder.class, new ConcurrentHashMap<>(),
                    new ConcurrentHashMap<>());
            fail("Should throw Missing Provider exception");
        } catch (PowsyblException e) {
            assertTrue(e.getMessage().contains("not found"), "Should throw Missing Provider exception");
        }
    }

    @Test
    void testMissingAlternate() {
        try {
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap = ExtensionAdderProviders
                    .groupProvidersByName(Arrays.asList(new SimpleExtensionAdderImplProvider()));
            ExtensionAdderProviders.findCachedProvider("Custom",
                    SimpleExtensionAdder.class, providersMap,
                    new ConcurrentHashMap<>());
            fail("Should throw Missing Provider exception");
        } catch (PowsyblException e) {
            assertTrue(e.getMessage().contains("not found"), "Should throw Missing Provider exception");
        }
    }

    @Test
    void testMissingAlternate2() {
        try {
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap = ExtensionAdderProviders
                    .groupProvidersByName(Arrays.asList(new GenericExtensionAdderImpl2Provider()));
            ExtensionAdderProviders.findCachedProvider("Custom",
                    SimpleExtensionAdder.class, providersMap,
                    new ConcurrentHashMap<>());
            fail("Should throw Missing Provider exception");
        } catch (PowsyblException e) {
            assertTrue(e.getMessage().contains("not found"), "Should throw Missing Provider exception");
        }
    }

    @Test
    void testMultiple() {
        List<ExtensionAdderProvider> listProviders = Arrays.asList(
                new SimpleExtensionAdderImplProvider(),
                new SimpleExtensionAdderImplProvider());
        ConcurrentMap<String, List<ExtensionAdderProvider>> mapProviders = ExtensionAdderProviders
                .groupProvidersByName(listProviders);
        try {
            ExtensionAdderProviders.findCachedProvider("Default",
                    SimpleExtensionAdder.class, mapProviders, new ConcurrentHashMap<>());
            fail("Should throw Multiple Provider exception");
        } catch (PowsyblException e) {
            assertTrue(e.getMessage().contains("Multiple"), "Should Mutliple Missing Provider exception");
        }
    }

    @Test
    void testAddingBase() {
        Extendable<SimpleExtendable> a = new SimpleExtendableImpl2();
        a.newExtension(SimpleExtensionAdder.class).add();
        assertNotNull(a.getExtension(SimpleExtension.class));
    }

    @Test
    void testAddingSimple() {
        SimpleExtendable a = new SimpleExtendableImpl2();
        a.newExtension(SimpleExtensionAdder.class).add();
        assertNotNull(a.getExtension(SimpleExtension.class));
    }

    @Test
    void testAddingGeneric1() {
        SpecificExtendable b = new SpecificExtendableImpl2();
        b.newExtension(GenericExtensionAdder.class).add();
        assertNotNull(b.getExtension(GenericExtension.class));
    }

    @Test
    void testAddingGeneric2() {
        GenericExtendable<SpecificExtendable> b = new SpecificExtendableImpl2();
        b.newExtension(GenericExtensionAdder.class).add();
        assertNotNull(b.getExtension(GenericExtension.class));
    }

    @Test
    void testAddingGeneric3() {
        GenericExtendable<SpecificExtendable> b = new GenericExtendableImpl2();
        b.newExtension(GenericExtensionAdder.class).add();
        assertNotNull(b.getExtension(GenericExtension.class));
    }

}
