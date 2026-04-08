/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class PlatformConfigNamedProviderTest {

    private static final String BAD_NAME = "xxxx";

    private static final String LEGACY_IMPL_NAME_PROPERTY = "default";

    private static final String GOOD_NAME = "b";

    private static final String DEFAULT_IMPL_NAME_PROPERTY = "default-impl-name";

    private static final ImmutableList<PlatformConfigNamedProvider> MULTIPLE_PROVIDERS = ImmutableList
            .of(new TestProvider("a"), new TestProvider(GOOD_NAME),
                    new TestProvider("c"));

    private static final ImmutableList<String> DEFAULT_PROPERTY_LIST = ImmutableList
            .of(DEFAULT_IMPL_NAME_PROPERTY, LEGACY_IMPL_NAME_PROPERTY);
    private static final ImmutableList<String> LEGACY_PROPERTY_LIST = ImmutableList
            .of(DEFAULT_IMPL_NAME_PROPERTY, LEGACY_IMPL_NAME_PROPERTY);

    private static final String MODULE = "module";

    private FileSystem fileSystem;

    private InMemoryPlatformConfig platformConfig;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static class TestProvider implements PlatformConfigNamedProvider {
        String name;

        TestProvider(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    void testDefaultNoProvider() {
        // case without any provider, an exception is expected
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(null, MODULE, ImmutableList.of(),
                ImmutableList.of(), platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testOneProvider() {
        // case with multiple providers without any config
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                null, MODULE, ImmutableList.of(), ImmutableList.of(new TestProvider("b")),
                platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testDefaultTwoProviders() {
        // case with 2 providers without any config, an exception is expected
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(null, MODULE, ImmutableList.of(),
                MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testDefaultMultipleProvidersPlatformConfig() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, GOOD_NAME);
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                null, MODULE, DEFAULT_PROPERTY_LIST, MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testOneProviderAndMistakeInPlatformConfig() {
        // case with 1 provider with config but with a name that is not the one of
        // provider.
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, BAD_NAME);
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(null, MODULE, DEFAULT_PROPERTY_LIST,
                ImmutableList.of(new TestProvider("a")), platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testNamedMultipleProviders() {
        // case with multiple providers and specifying programmatically
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                GOOD_NAME, MODULE, DEFAULT_PROPERTY_LIST, MULTIPLE_PROVIDERS,
                platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testBadNamedMultipleProviders() {
        // case with multiple providers and specifying programmatically with an error
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(BAD_NAME, MODULE, ImmutableList.of(),
                MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testDefaultMultipleProvidersPlatformConfigBackwardsCompatible1() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config in backwards compatible mode with the new property
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, GOOD_NAME);
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                null, MODULE, LEGACY_PROPERTY_LIST, MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testDefaultMultipleProvidersPlatformConfigBackwardsCompatible2() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config in backwards compatible mode with the old property
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(LEGACY_IMPL_NAME_PROPERTY, GOOD_NAME);
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                null, MODULE, LEGACY_PROPERTY_LIST, MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testDefaultMultipleProvidersPlatformConfigBackwardsCompatiblePriority1() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config in backwards compatible mode with the new property
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(MODULE);
        moduleConfig.setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, GOOD_NAME);
        moduleConfig.setStringProperty(LEGACY_IMPL_NAME_PROPERTY, BAD_NAME);
        PlatformConfigNamedProvider provider = PlatformConfigNamedProvider.Finder.find(
                null, MODULE, LEGACY_PROPERTY_LIST, MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class);
        assertEquals(GOOD_NAME, provider.getName());
    }

    @Test
    void testDefaultMultipleProvidersPlatformConfigBackwardsCompatiblePriority2() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config in backwards compatible mode with the new property
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig(MODULE);
        moduleConfig.setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, BAD_NAME);
        moduleConfig.setStringProperty(LEGACY_IMPL_NAME_PROPERTY, GOOD_NAME);
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(
                null, MODULE, LEGACY_PROPERTY_LIST, MULTIPLE_PROVIDERS, platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testOneProviderAndMistakeInPlatformConfigBackwardsCompatible1() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config with error
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(LEGACY_IMPL_NAME_PROPERTY, BAD_NAME);
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(null, MODULE, LEGACY_PROPERTY_LIST,
                ImmutableList.of(new TestProvider("a")), platformConfig, PlatformConfigNamedProvider.class));
    }

    @Test
    void testOneProviderAndMistakeInPlatformConfigBackwardsCompatible2() {
        // case with multiple providers without any config but specifying which one to
        // use in platform config with error
        platformConfig.createModuleConfig(MODULE)
                .setStringProperty(DEFAULT_IMPL_NAME_PROPERTY, BAD_NAME);
        assertThrows(PowsyblException.class, () -> PlatformConfigNamedProvider.Finder.find(
                null, MODULE, LEGACY_PROPERTY_LIST,
                ImmutableList.of(new TestProvider("a")), platformConfig, PlatformConfigNamedProvider.class));
    }

}
