/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.config.classic;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ClasspathModuleConfigRepository;
import com.powsybl.commons.config.DefaultConfigProvider;
import com.powsybl.commons.config.InMemoryModuleConfigRepository;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.ModuleConfigRepository;
import com.powsybl.commons.config.StackedModuleConfigRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at gmail.com>}
 */
class BundledDefaultConfigTest {

    private static final String RESOURCE_A = "/META-INF/powsybl/test-default-config-a.yml";
    private static final String RESOURCE_B = "/META-INF/powsybl/test-default-config-b.yml";

    private static DefaultConfigProvider provider(String name, int priority, String resource) {
        return new DefaultConfigProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getResourceName() {
                return resource;
            }
        };
    }

    @Test
    void classpathRepositoryLoadsBundledResource() {
        ClasspathModuleConfigRepository repository =
                new ClasspathModuleConfigRepository(RESOURCE_A, getClass().getClassLoader());
        assertTrue(repository.getModuleNames().containsAll(List.of("moduleA", "shared")));
        assertEquals("valueA", repository.getModuleConfig("moduleA").orElseThrow().getStringProperty("propA"));
    }

    @Test
    void classpathRepositoryMissingResourceThrows() {
        assertThrows(PowsyblException.class,
            () -> new ClasspathModuleConfigRepository("/META-INF/powsybl/does-not-exist.yml", getClass().getClassLoader()));
    }

    @Test
    void distinctModulesCompose() {
        ModuleConfigRepository repository = ClassicPlatformConfigProvider.buildBundledDefaultConfigRepository(
                List.of(provider("a", 100, RESOURCE_A), provider("b", 50, RESOURCE_B)),
                getClass().getClassLoader(), FileSystems.getDefault());
        assertEquals("valueA", repository.getModuleConfig("moduleA").orElseThrow().getStringProperty("propA"));
        assertEquals("valueB", repository.getModuleConfig("moduleB").orElseThrow().getStringProperty("propB"));
    }

    @Test
    void overlappingPropertyResolvedByPriority() {
        // 'b' has higher priority despite being declared second
        ModuleConfigRepository repository = ClassicPlatformConfigProvider.buildBundledDefaultConfigRepository(
                List.of(provider("a", 50, RESOURCE_A), provider("b", 100, RESOURCE_B)),
                getClass().getClassLoader(), FileSystems.getDefault());
        assertEquals("fromB", repository.getModuleConfig("shared").orElseThrow().getStringProperty("common"));
    }

    @Test
    void userConfigOverridesBundledDefault() {
        // Reproduce the production stacking order: higher-precedence sources come first.
        InMemoryModuleConfigRepository userRepository = new InMemoryModuleConfigRepository(FileSystems.getDefault());
        MapModuleConfig sharedModule = userRepository.createModuleConfig("shared");
        sharedModule.setStringProperty("common", "fromUser");

        ModuleConfigRepository bundled = ClassicPlatformConfigProvider.buildBundledDefaultConfigRepository(
                List.of(provider("a", 100, RESOURCE_A)), getClass().getClassLoader(), FileSystems.getDefault());

        ModuleConfigRepository stacked = new StackedModuleConfigRepository(userRepository, bundled);
        assertEquals("fromUser", stacked.getModuleConfig("shared").orElseThrow().getStringProperty("common"));
        // a property only present in the bundled defaults is still visible
        assertEquals("valueA", stacked.getModuleConfig("moduleA").orElseThrow().getStringProperty("propA"));
    }
}
