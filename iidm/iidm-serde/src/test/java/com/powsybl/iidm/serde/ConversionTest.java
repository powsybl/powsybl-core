/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.tools.ConversionTool;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ConversionTest {

    protected FileSystem fileSystem;

    protected InMemoryPlatformConfig platformConfig;

    private CommandLineTools tools;

    @BeforeEach
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        tools = new CommandLineTools(Collections.singletonList(createConversionTool()));
    }

    private ConversionTool createConversionTool() {
        return new ConversionTool() {

            @Override
            protected ImportConfig createImportConfig() {
                return ImportConfig.load(platformConfig);
            }
        };
    }

    @Test
    void testConversionZip() throws IOException {
        // Prepare the firectory and file
        Files.createDirectory(fileSystem.getPath("/tmp"));
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/foo.xiidm")), fileSystem.getPath("/tmp/foo.xiidm"));

        // Prepare the command
        String[] commandLine = new String[] {
            "convert-network",
            "--input-file", "/tmp/foo.xiidm",
            "--output-format", "XIIDM",
            "--output-file", "/tmp/bar.zip"
        };

        // Assert the command is successful
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int status = AbstractToolTest.runCommand(commandLine, bout, berr, tools, fileSystem);

        // Assert the command worked
        assertEquals(0, status);
        assertEquals("Generating file /tmp/bar.zip:bar.xiidm...\n", bout.toString(StandardCharsets.UTF_8));
        assertEquals("", berr.toString(StandardCharsets.UTF_8));

        // Assert the right files are created
        Path zipFilePath = fileSystem.getPath("/tmp/bar.zip");
        assertTrue(Files.isRegularFile(zipFilePath));
        try (ZipFile zipFile = ZipFile.builder()
            .setSeekableByteChannel(Files.newByteChannel(zipFilePath))
            .get()) {
            assertNotNull(zipFile.getEntry("bar.xiidm"));
        } catch (IOException e) {
            fail();
        }
    }
}
