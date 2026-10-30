/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class ClasspathRuntimeConfigProviderTest {

    @TempDir
    Path tempDir;

    /**
     * Creates a directory holding a com/powsybl/config/config.yml resource and returns its URL,
     * so it can be used as a class path entry.
     */
    private URL classPathEntry(String directoryName, String... yamlLines) throws IOException {
        Path root = Files.createDirectories(tempDir.resolve(directoryName));
        Path resource = root.resolve(ClasspathRuntimeConfigProvider.RESOURCE_PATH);
        Files.createDirectories(resource.getParent());
        Files.writeString(resource, String.join(System.lineSeparator(), yamlLines));
        return root.toUri().toURL();
    }

    @Test
    void noResourceOnTheClassPathContributesNothing() throws IOException {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {}, null)) {
            assertTrue(new ClasspathRuntimeConfigProvider(classLoader).getModuleConfigRepository().isEmpty());
        }
    }

    @Test
    void thereIsNoResourceOnThisModuleOwnClassPath() {
        assertTrue(new ClasspathRuntimeConfigProvider().getModuleConfigRepository().isEmpty());
    }

    @Test
    void singleResourceIsRead() throws IOException {
        URL entry = classPathEntry("corp", "foo:", "    bar: baz");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ModuleConfigRepository repository = new ClasspathRuntimeConfigProvider(classLoader)
                    .getModuleConfigRepository().orElseThrow();
            assertEquals("baz", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
        }
    }

    @Test
    void reservedModuleIsNotExposedAsConfiguration() throws IOException {
        URL entry = classPathEntry("corp",
                "powsybl-runtime-config:",
                "    priority: 5",
                "foo:",
                "    bar: baz");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ModuleConfigRepository repository = new ClasspathRuntimeConfigProvider(classLoader)
                    .getModuleConfigRepository().orElseThrow();
            assertTrue(repository.getModuleConfig("powsybl-runtime-config").isEmpty());
            assertEquals("baz", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
        }
    }

    @Test
    void resourcesAreOrderedByDeclaredPriorityNotByClassPathOrder() throws IOException {
        URL low = classPathEntry("low",
                "powsybl-runtime-config:",
                "    priority: 10",
                "foo:",
                "    shared: fromLow",
                "    onlyLow: fromLow");
        URL high = classPathEntry("high",
                "powsybl-runtime-config:",
                "    priority: 20",
                "foo:",
                "    shared: fromHigh");

        // the higher priority entry comes second on the class path on purpose
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {low, high}, null)) {
            ModuleConfig config = new ClasspathRuntimeConfigProvider(classLoader)
                    .getModuleConfigRepository().orElseThrow()
                    .getModuleConfig("foo").orElseThrow();
            assertEquals("fromHigh", config.getStringProperty("shared"));
            assertEquals("fromLow", config.getStringProperty("onlyLow"));
        }
    }

    @Test
    void equalPrioritiesAreBrokenByResourceUrl() throws IOException {
        URL aaa = classPathEntry("aaa", "foo:", "    bar: fromAaa");
        URL bbb = classPathEntry("bbb", "foo:", "    bar: fromBbb");

        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {bbb, aaa}, null)) {
            ModuleConfigRepository repository = new ClasspathRuntimeConfigProvider(classLoader)
                    .getModuleConfigRepository().orElseThrow();
            assertEquals("fromAaa", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
        }
    }

    @Test
    void malformedResourceFailsFast() throws IOException {
        URL entry = classPathEntry("broken", "not a map");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ClasspathRuntimeConfigProvider provider = new ClasspathRuntimeConfigProvider(classLoader);
            assertThrows(PowsyblException.class, provider::getModuleConfigRepository);
        }
    }

    @Test
    void wrongStructureNamesTheResource() throws IOException {
        // valid YAML syntax, but the second level is not a map of properties
        URL entry = classPathEntry("broken", "foo: bar");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ClasspathRuntimeConfigProvider provider = new ClasspathRuntimeConfigProvider(classLoader);
            PowsyblException e = assertThrows(PowsyblException.class, provider::getModuleConfigRepository);
            assertTrue(e.getMessage().contains(entry.toString()));
        }
    }

    @Test
    void invalidPriorityNamesTheResource() throws IOException {
        URL entry = classPathEntry("broken", "powsybl-runtime-config:", "    priority: high");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ClasspathRuntimeConfigProvider provider = new ClasspathRuntimeConfigProvider(classLoader);
            PowsyblException e = assertThrows(PowsyblException.class, provider::getModuleConfigRepository);
            assertTrue(e.getMessage().contains(entry.toString()));
        }
    }

    @Test
    void yamlSyntaxErrorNamesTheResource() throws IOException {
        // an unclosed flow sequence is a YAML syntax error (SnakeYAML's YAMLException), not a
        // structural one (PowsyblException)
        URL entry = classPathEntry("broken", "foo: [1, 2");
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] {entry}, null)) {
            ClasspathRuntimeConfigProvider provider = new ClasspathRuntimeConfigProvider(classLoader);
            PowsyblException e = assertThrows(PowsyblException.class, provider::getModuleConfigRepository);
            assertTrue(e.getMessage().contains(entry.toString()));
        }
    }

    @Test
    void providerIsNamed() {
        assertEquals("classpath", new ClasspathRuntimeConfigProvider().getName());
    }

    @Test
    void providerHasDefaultPriority() {
        assertEquals(0, new ClasspathRuntimeConfigProvider().getPriority());
    }
}
