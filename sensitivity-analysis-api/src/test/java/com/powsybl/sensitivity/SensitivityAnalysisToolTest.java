/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisToolTest extends AbstractToolTest {

    private static final String COMMAND_NAME = "sensitivity-analysis";

    private final SensitivityAnalysisTool tool = new SensitivityAnalysisTool();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createFile("test.uct", "");
        createFile("test.csv", "");
        createFile("input.xiidm", "");
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertCommand(tool.getCommand(), COMMAND_NAME, 8, 3);
        assertOption(tool.getCommand().getOptions(), "case-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", true, true);
        assertOption(tool.getCommand().getOptions(), "factors-file", true, true);
        assertOption(tool.getCommand().getOptions(), "contingencies-file", false, true);
        assertOption(tool.getCommand().getOptions(), "parameters-file", false, true);
    }

    @Test
    public void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "wrongFile.uct", "--factors-file", "test.csv", "--output-file", "output.csv"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.uct does not exist or is not a regular file");
    }

    @Test
    public void checkFailsWhenFactorsFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "wrongFile.csv", "--output-file", "output.csv"}, 3, null, "PowsyblException: Unsupported file format or invalid file");
    }

    @Test
    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "test.csv", "--output-file", "out.txt"}, 3, "", "Unsupported output format: out.txt");
    }

    @Test
    public void checkCommand() {
        assertEquals("sensitivity-analysis", tool.getCommand().getName());
        assertEquals("Computation", tool.getCommand().getTheme());
        assertEquals("Run sensitivity analysis", tool.getCommand().getDescription());
    }
}
