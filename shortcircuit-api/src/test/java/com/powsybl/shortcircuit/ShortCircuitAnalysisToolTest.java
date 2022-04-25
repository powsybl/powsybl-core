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
        createFile("test.uct", "");
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
        assertCommand(shortCircuitTool.getCommand(), COMMAND_NAME, 4, 1);
        assertOption(shortCircuitTool.getCommand().getOptions(), "case-file", true, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-file", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "output-format", false, true);
        assertOption(shortCircuitTool.getCommand().getOptions(), "parameters-file", false, true);
    }

    @Test
    public void checkFailsWhenNetworkFileNotFound() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "wrongFile.uct"}, 3, null, "com.powsybl.commons.PowsyblException: File wrongFile.uct does not exist or is not a regular file");
    }

    @Test
    public void checkThrowsWhenOutputFileAndNoFormat() throws IOException {
        assertCommand(new String[] {COMMAND_NAME, "--case-file", "test.uct", "--output-file", "out.txt"}, 2, "", "error: Missing required option: output-format");
    }
}
