/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.config.classic;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryModuleConfigRepository;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.ModuleConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class ClassicPlatformConfigProviderTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private List<String> getAbsolutePaths(String configDirs) {
        Path[] paths = ClassicPlatformConfigProvider.getDefaultConfigDirs(fileSystem, configDirs, "/", ":");
        return Arrays.stream(paths).map(Path::toAbsolutePath).map(Path::normalize).map(Path::toString)
                .collect(Collectors.toList());
    }

    @Test
    void testNoUserHome() {
        assertEquals(List.of("/.itools"), getAbsolutePaths(null));
    }

    @Test
    void testEdgeCaseEmptyAfterSplit() {
        assertEquals(List.of("/.itools"), getAbsolutePaths(":"));
    }

    @Test
    void workDir() {
        assertEquals(List.of("/work"), getAbsolutePaths("."));
    }

    @Test
    void testEmptyConfigDirs() {
        assertEquals(List.of("/.itools"), getAbsolutePaths(""));
    }

    @Test
    void testNormalConfigDirs() {
        assertEquals(Arrays.asList("/foo", "/bar"), getAbsolutePaths("/foo:/bar/"));
    }

    @Test
    void testModuleRepository() throws IOException {
        try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            newBufferedWriter.write("foo:\n bar: baz");
        }
        ModuleConfigRepository loadModuleRepository = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config");
        assertEquals("baz", loadModuleRepository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
    }

    @Test
    void testRuntimeRepositoryHasLowerPrecedenceThanTheConfigurationFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            writer.write(String.join(System.lineSeparator(),
                    "foo:",
                    " shared: fromUser",
                    " onlyUser: fromUser"));
        }
        InMemoryModuleConfigRepository runtimeRepository = new InMemoryModuleConfigRepository(fileSystem);
        MapModuleConfig foo = runtimeRepository.createModuleConfig("foo");
        foo.setStringProperty("shared", "fromRuntime");
        foo.setStringProperty("onlyRuntime", "fromRuntime");

        ModuleConfigRepository repository = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config", runtimeRepository);

        ModuleConfig config = repository.getModuleConfig("foo").orElseThrow();
        assertEquals("fromUser", config.getStringProperty("shared"));
        assertEquals("fromUser", config.getStringProperty("onlyUser"));
        assertEquals("fromRuntime", config.getStringProperty("onlyRuntime"));
    }

    @Test
    void testRuntimeRepositoryProvidesModulesAbsentFromTheConfigurationFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            writer.write("other:\n bar: baz");
        }
        InMemoryModuleConfigRepository runtimeRepository = new InMemoryModuleConfigRepository(fileSystem);
        runtimeRepository.createModuleConfig("foo").setStringProperty("bar", "fromRuntime");

        ModuleConfigRepository repository = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config", runtimeRepository);

        assertEquals("fromRuntime", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
    }

    @Test
    void testWithoutRuntimeRepositoryNothingChanges() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            writer.write("foo:\n bar: baz");
        }

        ModuleConfigRepository repository = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config", null);

        assertEquals("baz", repository.getModuleConfig("foo").orElseThrow().getStringProperty("bar"));
        assertTrue(repository.getModuleConfig("absent").isEmpty());
    }

    @Test
    void testEnvironmentHasHigherPrecedenceThanBothTheConfigurationFileAndTheRuntimeRepository() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            writer.write("foo:\n shared: fromUser");
        }
        InMemoryModuleConfigRepository runtimeRepository = new InMemoryModuleConfigRepository(fileSystem);
        runtimeRepository.createModuleConfig("foo").setStringProperty("shared", "fromRuntime");

        Map<String, String> environmentWithTheProperty = Map.of("FOO__SHARED", "fromEnvironment");
        ModuleConfigRepository withEnvironment = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config", environmentWithTheProperty, runtimeRepository);
        assertEquals("fromEnvironment", withEnvironment.getModuleConfig("foo").orElseThrow().getStringProperty("shared"));

        ModuleConfigRepository withoutEnvironment = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config", Map.of(), runtimeRepository);
        assertEquals("fromUser", withoutEnvironment.getModuleConfig("foo").orElseThrow().getStringProperty("shared"));
    }
}
