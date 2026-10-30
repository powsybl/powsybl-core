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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class YamlModuleConfigRepositoryTest {

    private static final String YAML = String.join(System.lineSeparator(),
            "module1:",
            "    s: a",
            "    i: 3",
            "    p: /work/a");

    @Test
    void readerConstructorIsEquivalentToPathConstructor() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path configFile = fileSystem.getPath("/config.yml");
            Files.writeString(configFile, YAML);

            ModuleConfig fromPath = new YamlModuleConfigRepository(configFile)
                    .getModuleConfig("module1").orElseThrow();
            ModuleConfig fromReader;
            try (Reader reader = new StringReader(YAML)) {
                fromReader = new YamlModuleConfigRepository(reader, fileSystem)
                        .getModuleConfig("module1").orElseThrow();
            }

            assertEquals(fromPath.getStringProperty("s"), fromReader.getStringProperty("s"));
            assertEquals(fromPath.getIntProperty("i"), fromReader.getIntProperty("i"));
            assertEquals(fromPath.getPathProperty("p"), fromReader.getPathProperty("p"));
        }
    }

    @Test
    void readerConstructorResolvesPathsOnTheGivenFileSystem() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
             Reader reader = new StringReader(YAML)) {
            ModuleConfig config = new YamlModuleConfigRepository(reader, fileSystem)
                    .getModuleConfig("module1").orElseThrow();
            assertEquals(fileSystem.getPath("/work/a"), config.getPathProperty("p"));
        }
    }

    @Test
    void readerConstructorRejectsContentThatIsNotAMapOfModules() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
             Reader reader = new StringReader("not a map")) {
            PowsyblException e = assertThrows(PowsyblException.class,
                () -> new YamlModuleConfigRepository(reader, fileSystem));
            assertEquals("Named modules are expected at the first level of the YAML", e.getMessage());
        }
    }

    @Test
    void emptyOrCommentOnlyDocumentHasNoModules() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            for (String content : new String[] {"", "# nothing here" + System.lineSeparator()}) {
                try (Reader reader = new StringReader(content)) {
                    assertTrue(new YamlModuleConfigRepository(reader, fileSystem).getModuleNames().isEmpty());
                }
            }
        }
    }
}
