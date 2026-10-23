/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class RuntimeConfigRepositoriesTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        System.clearProperty(RuntimeConfigRepositories.ENABLED_PROPERTY);
        fileSystem.close();
    }

    private InMemoryModuleConfigRepository repositoryWith(String module, String property, String value) {
        InMemoryModuleConfigRepository repository = new InMemoryModuleConfigRepository(fileSystem);
        repository.createModuleConfig(module).setStringProperty(property, value);
        return repository;
    }

    private static RuntimeConfigProvider provider(String name, int priority,
                                                  Supplier<Optional<ModuleConfigRepository>> repository) {
        return new RuntimeConfigProvider() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public Optional<ModuleConfigRepository> getModuleConfigRepository() {
                return repository.get();
            }
        };
    }

    @Test
    void noProviderContributesNothing() {
        assertTrue(RuntimeConfigRepositories.load(List.of()).isEmpty());
    }

    @Test
    void providerReturningNothingIsIgnored() {
        assertTrue(RuntimeConfigRepositories.load(
                List.of(provider("empty", 0, Optional::empty))).isEmpty());
    }

    @Test
    void singleProviderContributesItsValues() {
        Optional<ModuleConfigRepository> repository = RuntimeConfigRepositories.load(
                List.of(provider("only", 0, () -> Optional.of(repositoryWith("foo", "bar", "baz")))));

        assertEquals("baz", repository.orElseThrow().getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
    }

    @Test
    void higherPriorityWinsAndValuesAreMergedPerProperty() {
        InMemoryModuleConfigRepository low = repositoryWith("foo", "shared", "fromLow");
        low.createModuleConfig("onlyLow").setStringProperty("k", "fromLow");
        InMemoryModuleConfigRepository high = repositoryWith("foo", "shared", "fromHigh");

        ModuleConfigRepository repository = RuntimeConfigRepositories.load(List.of(
                provider("low", 10, () -> Optional.of(low)),
                provider("high", 20, () -> Optional.of(high)))).orElseThrow();

        assertEquals("fromHigh", repository.getModuleConfig("foo").orElseThrow().getStringProperty("shared"));
        assertEquals("fromLow", repository.getModuleConfig("onlyLow").orElseThrow().getStringProperty("k"));
    }

    @Test
    void equalPrioritiesAreBrokenByNameSoOrderDoesNotDependOnTheClasspath() {
        ModuleConfigRepository first = repositoryWith("foo", "bar", "fromAaa");
        ModuleConfigRepository second = repositoryWith("foo", "bar", "fromBbb");

        // declared in the order bbb, aaa: the name tie-break must still select aaa
        ModuleConfigRepository repository = RuntimeConfigRepositories.load(List.of(
                provider("bbb", 0, () -> Optional.of(second)),
                provider("aaa", 0, () -> Optional.of(first)))).orElseThrow();

        assertEquals("fromAaa", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
    }

    @Test
    void systemPropertyDisablesTheWholeLayer() {
        System.setProperty(RuntimeConfigRepositories.ENABLED_PROPERTY, "false");

        assertTrue(RuntimeConfigRepositories.load(
                List.of(provider("only", 0, () -> Optional.of(repositoryWith("foo", "bar", "baz"))))).isEmpty());
    }

    @Test
    void whenDisabledProvidersAreNotConsultedAtAll() {
        System.setProperty(RuntimeConfigRepositories.ENABLED_PROPERTY, "false");

        RuntimeConfigProvider throwing = provider("broken", 0, () -> {
            throw new IllegalStateException("should never be called: the layer is disabled");
        });

        // the escape hatch must short-circuit before any provider is instantiated or consulted,
        // otherwise a throwing provider constructor or getModuleConfigRepository() defeats the
        // very rescue -Dpowsybl.config.runtime.enabled=false is documented to provide
        assertTrue(RuntimeConfigRepositories.load(List.of(throwing)).isEmpty());
    }

    @Test
    void publicLoadContributesNothingOnThisModuleOwnClassPath() {
        // Guards the load-bearing guarantee of the whole feature: no module of powsybl-core
        // ships a META-INF/powsybl/config.yml resource nor a RuntimeConfigProvider that
        // contributes anything, so the real ServiceLoader path (as opposed to the package-private
        // load(List) seam every other test in this class uses) must resolve to nothing on the
        // commons module's own classpath. If someone later added such a resource here, this is
        // the test that would fail.
        assertTrue(RuntimeConfigRepositories.load().isEmpty());
    }

    @Test
    void failingProviderFailsFastAndIsNamed() {
        RuntimeConfigProvider failing = provider("broken", 0, () -> {
            throw new IllegalStateException("boom");
        });

        PowsyblException e = assertThrows(PowsyblException.class,
            () -> RuntimeConfigRepositories.load(List.of(failing)));
        assertTrue(e.getMessage().contains("broken"));
    }

    @Test
    void overlappingPropertyIsReportedWithWinnerAndLoserAndValueIsUnchanged() {
        InMemoryModuleConfigRepository low = repositoryWith("foo", "shared", "fromLow");
        InMemoryModuleConfigRepository high = repositoryWith("foo", "shared", "fromHigh");

        assertEquals(List.of(new RuntimeConfigRepositories.PropertyOverlap("foo", "shared", "high", "low")),
                RuntimeConfigRepositories.findOverlappingProperties(List.of("high", "low"), List.of(high, low)));

        ModuleConfigRepository loaded = RuntimeConfigRepositories.load(List.of(
                provider("low", 10, () -> Optional.of(low)),
                provider("high", 20, () -> Optional.of(high)))).orElseThrow();
        assertEquals("fromHigh", loaded.getModuleConfig("foo").orElseThrow().getStringProperty("shared"));
    }

    @Test
    void thirdProviderIsReportedAgainstTheWinnerOnly() {
        List<ModuleConfigRepository> repositories = List.of(
                repositoryWith("foo", "p", "1"), repositoryWith("foo", "p", "2"), repositoryWith("foo", "p", "3"));

        assertEquals(List.of(new RuntimeConfigRepositories.PropertyOverlap("foo", "p", "a", "b"),
                        new RuntimeConfigRepositories.PropertyOverlap("foo", "p", "a", "c")),
                RuntimeConfigRepositories.findOverlappingProperties(List.of("a", "b", "c"), repositories));
    }

    @Test
    void nonOverlappingPropertiesAreNotReported() {
        InMemoryModuleConfigRepository first = repositoryWith("foo", "a", "1");
        InMemoryModuleConfigRepository second = repositoryWith("foo", "b", "2");
        second.createModuleConfig("other").setStringProperty("a", "3");

        assertTrue(RuntimeConfigRepositories.findOverlappingProperties(
                List.of("second", "first"), List.of(second, first)).isEmpty());
    }

    @Test
    void repositoryWithoutEnumerableModulesIsSkippedWithoutFailing() {
        ModuleConfigRepository lambda = name -> Optional.empty();
        InMemoryModuleConfigRepository other = repositoryWith("foo", "bar", "baz");

        assertTrue(RuntimeConfigRepositories.findOverlappingProperties(
                List.of("lambda", "other"), List.of(lambda, other)).isEmpty());

        List<ModuleConfigRepository> loaded = new ArrayList<>();
        assertDoesNotThrow(() -> loaded.add(RuntimeConfigRepositories.load(List.of(
                provider("lambda", 20, () -> Optional.of(lambda)),
                provider("other", 10, () -> Optional.of(other)))).orElseThrow()));
        assertEquals("baz", loaded.get(0).getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
    }

    @Test
    void misbehavingRepositoryDoesNotBreakDetectionNorLoading() {
        ModuleConfigRepository broken = new ModuleConfigRepository() {
            @Override
            public Optional<ModuleConfig> getModuleConfig(String name) {
                return Optional.empty();
            }

            @Override
            public Set<String> getModuleNames() {
                throw new IllegalStateException("boom");
            }
        };

        assertTrue(RuntimeConfigRepositories.findOverlappingProperties(List.of("broken"), List.of(broken)).isEmpty());
        assertTrue(RuntimeConfigRepositories.load(List.of(provider("broken", 0, () -> Optional.of(broken)))).isPresent());
    }
}
