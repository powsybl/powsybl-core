/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisToolTest extends AbstractToolTest {

    private ShortCircuitAnalysisTool shortCircuitTool = new ShortCircuitAnalysisTool();
    private static final String COMMAND_NAME = "shortcircuit";

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Files.copy(getClass().getResourceAsStream("/network.xiidm"), fileSystem.getPath("network.xiidm"));
        createFile("test.uct", "");
        createFile("out.txt", "");
        createFile("emptyInput.txt", "{ }");
        Files.copy(getClass().getResourceAsStream("/ShortCircuitInput.json"), fileSystem.getPath("input.txt"));
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(shortCircuitTool);
    }

    @Test
    public void testGetCommand() {
        assertCommand();
    }

    @Override
    public void assertCommand() {
        assertCommand(shortCircuitTool.getCommand(), COMMAND_NAME, 5, 2);
        assertOption(shortCircuitTool.getCommand().getOptions(), "input-file", true, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "case-file", true, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-file", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-format", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "parameters-file", false, true);
    }

    @Test
    public void checkFailsWhenInputFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "wrongFile.txt", "--case-file", "test.uct"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.txt does not exist or is not a regular file");
    }

    @Test
    public void checkFailsWhenInputFileIsEmpty() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "emptyInput.txt", "--case-file", "test.uct"}, 3, null, "File 'emptyInput.txt' is empty");
    }

    @Test
    public void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "wrongFile.uct"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.uct does not exist or is not a regular file");
    }

    @Test
    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "test.uct", "--output-file", "out.txt"}, 2, null, "error: Missing required option: output-format");
    }

    @Test
    public void checkThrowsWhenNetworkFileIsEmpty() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "test.uct"}, 3, null, "com.powsybl.commons.PowsyblException: Unsupported file format or invalid file.");
    }

    @Test
    public void checkFailsWhenParametersFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "network.xiidm", "--parameters-file", "wrongFile.txt"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.txt does not exist or is not a regular file");
    }

    @Test
    public void test() throws IOException {
        String expectedOut = "Loading fault list 'input.txt'\n" +
                "Loading network 'network.xiidm'\n" +
                "Short circuit analysis:\n" +
                "+----+---------------------------+\n" +
                "| ID | Three Phase Fault Current |\n" +
                "+----+---------------------------+\n" +
                "Limit violations:\n" +
                "+---------------+---------+--------------+------------+-------+-------+\n" +
                "| Voltage level | Country | Base voltage | Limit type | Limit | Value |\n" +
                "+---------------+---------+--------------+------------+-------+-------+\n";

        assertCommand(new String[] {COMMAND_NAME, "--input-file", "input.txt", "--case-file", "network.xiidm"}, 0, expectedOut, null);
    }
}
