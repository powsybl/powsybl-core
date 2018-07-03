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

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityComputationToolTest extends AbstractToolTest {

    private static final String COMMAND_NAME = "sensitivity-computation";

    private final SensitivityComputationTool tool = new SensitivityComputationTool();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createFile("test.uct", "");
        createFile("test.csv", "");
    }

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertCommand(tool.getCommand(), COMMAND_NAME, 5, 2);
        assertOption(tool.getCommand().getOptions(), "case-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-file", false, true);
        assertOption(tool.getCommand().getOptions(), "output-format", false, true);
        assertOption(tool.getCommand().getOptions(), "factors-file", true, true);
        assertOption(tool.getCommand().getOptions(), "skip-postproc", false, false);
    }

    @Test
    public void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "wrongFile.uct", "--factors-file", "test.csv"}, 3, "", "");
    }

    @Test
    public void checkFailsWhenFactorsFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "wrongFile.csv"}, 3, "", "");
    }

    @Test
    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--factors-file", "test.csv", "--output-file", "out.txt"}, 2, "", "");
    }
}
