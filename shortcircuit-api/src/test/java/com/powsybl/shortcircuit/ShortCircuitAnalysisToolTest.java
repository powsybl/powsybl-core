/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.shortcircuit.tools.ShortCircuitAnalysisTool;
import com.powsybl.tools.test.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Boubakeur Brahimi
 */
class ShortCircuitAnalysisToolTest extends AbstractToolTest {

    private ShortCircuitAnalysisTool shortCircuitTool = new ShortCircuitAnalysisTool();
    private static final String COMMAND_NAME = "shortcircuit";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Files.copy(getClass().getResourceAsStream("/network.xiidm"), fileSystem.getPath("network.xiidm"));
        createFile("test.uct", "");
        createFile("out.txt", "");
        createFile("emptyInput.txt", "{ }");
        Files.copy(getClass().getResourceAsStream("/FaultsFile.json"), fileSystem.getPath("faults.json"));
        Files.copy(getClass().getResourceAsStream("/ShortCircuitParameters.json"), fileSystem.getPath("parameters.json"));
        Files.copy(getClass().getResourceAsStream("/FaultParametersFile.json"), fileSystem.getPath("faultParameters.json"));
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(shortCircuitTool);
    }

    @Test
    void testGetCommand() {
        assertCommand();
    }

    @Override
    public void assertCommand() {
        assertEquals("shortcircuit", shortCircuitTool.getCommand().getName());
        assertEquals("Computation", shortCircuitTool.getCommand().getTheme());
        assertEquals("Run short circuit analysis", shortCircuitTool.getCommand().getDescription());

        assertCommand(shortCircuitTool.getCommand(), COMMAND_NAME, 6, 2);
        assertOption(shortCircuitTool.getCommand().getOptions(), "input-file", true, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "case-file", true, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-file", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-format", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "parameters-file", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "fault-parameters-file", false, true);
    }

    @Test
    void checkFailsWhenInputFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "wrongFile.txt", "--case-file", "network.xiidm"}, 3, null, "java.io.UncheckedIOException: java.nio.file.NoSuchFileException: wrongFile.txt");
    }

    @Test
    void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "wrongFile.uct"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.uct does not exist or is not a regular file");
    }

    @Test
    void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "faults.json", "--case-file", "test.uct", "--output-file", "out.txt"}, 2, null, "error: Missing required option: output-format");
    }

    @Test
    void checkThrowsWhenNetworkFileIsEmpty() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "faults.json", "--case-file", "test.uct"}, 3, null, "com.powsybl.commons.PowsyblException: Unsupported file format or invalid file.");
    }

    @Test
    void checkFailsWhenParametersFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "faults.json", "--case-file", "network.xiidm", "--fault-parameters-file", "wrongFile.txt"}, 3, null, "java.io.UncheckedIOException: java.nio.file.NoSuchFileException: wrongFile.txt");
    }

    @Test
    void checkFailsWhenFaultParametersFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "faults.json", "--case-file", "network.xiidm", "--fault-parameters-file", "wrongFile.txt"}, 3, null, "java.io.UncheckedIOException: java.nio.file.NoSuchFileException: wrongFile.txt");
    }

    @Test
    void test() throws IOException {
        String expectedOut = "Loading fault list 'faultsFile.json'\n" +
                "Loading network 'network.xiidm'\n" +
                "Short circuit analysis:\n" +
                "+----+---------------------------+\n" +
                "| ID | Three Phase Fault Current |\n" +
                "+----+---------------------------+\n" +
                "Limit violations:\n" +
                "+----+---------------+---------+--------------+------------+-------+-------+\n" +
                "| ID | Voltage level | Country | Base voltage | Limit type | Limit | Value |\n" +
                "+----+---------------+---------+--------------+------------+-------+-------+\n";

        assertCommand(new String[] {COMMAND_NAME, "--input-file", "faults.json", "--case-file", "network.xiidm", "--parameters-file", "parameters.json", "--fault-parameters-file", "faultParameters.json"}, 0, expectedOut, null);
    }
}
