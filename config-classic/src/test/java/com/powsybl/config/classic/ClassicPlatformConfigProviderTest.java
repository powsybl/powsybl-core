/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.config.classic;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.ModuleConfigRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Jon Harper <jon.harper at rte-france.com>
 */
public class ClassicPlatformConfigProviderTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    private List<String> getAbsolutePaths(String configDirs) {
        Path[] paths = ClassicPlatformConfigProvider.getDefaultConfigDirs(fileSystem, configDirs, "/", ":");
        return Arrays.stream(paths).map(Path::toAbsolutePath).map(Path::normalize).map(Path::toString)
                .collect(Collectors.toList());
    }

    @Test
    public void testNoUserHome() {
        assertEquals(Arrays.asList("/.itools"), getAbsolutePaths(null));
    }

    @Test
    public void testEdgeCaseEmptyAfterSplit() {
        assertEquals(Arrays.asList("/.itools"), getAbsolutePaths(":"));
    }

    @Test
    public void workDir() {
        assertEquals(Arrays.asList("/work"), getAbsolutePaths("."));
    }

    @Test
    public void testEmptyConfigDirs() {
        assertEquals(Arrays.asList("/.itools"), getAbsolutePaths(""));
    }

    @Test
    public void testNormalConfigDirs() {
        assertEquals(Arrays.asList("/foo", "/bar"), getAbsolutePaths("/foo:/bar/"));
    }

    @Test
    public void testModuleRepository() throws IOException {
        try (BufferedWriter newBufferedWriter = Files.newBufferedWriter(fileSystem.getPath("/config.yml"))) {
            newBufferedWriter.write("foo:\n bar: baz");
        }
        ModuleConfigRepository loadModuleRepository = ClassicPlatformConfigProvider
                .loadModuleRepository(new Path[] {fileSystem.getPath("/") }, "config");
        assertEquals("baz", loadModuleRepository.getModuleConfig("foo").get().getStringProperty("bar"));
    }
}
