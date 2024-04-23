/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cim;

import com.google.common.io.ByteStreams;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CimAnonymizerToolTest extends AbstractToolTest {

    private final CimAnonymizerTool tool = new CimAnonymizerTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertCommand(tool.getCommand(), "cim-anonymizer", 4, 3);
        assertOption(tool.getCommand().getOptions(), "cim-path", true, true);
        assertOption(tool.getCommand().getOptions(), "output-dir", true, true);
        assertOption(tool.getCommand().getOptions(), "mapping-file", true, true);
        assertOption(tool.getCommand().getOptions(), "skip-external-refs", false, false);
    }

    @Test
    void run() throws IOException {
        Path workDir = fileSystem.getPath("work");
        Path cimZipFile = workDir.resolve("sample.zip");
        Path anonymizedCimFileDir = workDir.resolve("result");
        Files.createDirectories(anonymizedCimFileDir);
        Path dictionaryFile = workDir.resolve("dic.csv");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cimZipFile))) {
            zos.putNextEntry(new ZipEntry("sample_EQ.xml"));
            zos.write(ByteStreams.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/sample_EQ.xml"))));
        }

        assertCommandSuccessful(new String[] {"cim-anonymizer", "--cim-path", cimZipFile.toString(),
            "--output-dir", anonymizedCimFileDir.toString(),
            "--mapping-file", dictionaryFile.toString()},
                "Anonymizing work/sample.zip" + System.lineSeparator());

        assertTrue(Files.exists(anonymizedCimFileDir.resolve("sample.zip")));
        assertTrue(Files.exists(dictionaryFile));
        // anonymized content validation has already been done in other unit test
    }
}
