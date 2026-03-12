/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class ValidationToolTest extends AbstractToolTest {

    private static final String COMMAND_NAME = "loadflow-validation";
    private final ValidationTool tool = new ValidationTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(tool);
    }

    @Override
    public void assertCommand() {
        assertEquals(COMMAND_NAME, tool.getCommand().getName());
        assertEquals("Computation", tool.getCommand().getTheme());
        assertEquals("Validate load-flow results of a network", tool.getCommand().getDescription());

        assertCommand(tool.getCommand(), COMMAND_NAME, 12, 2);
        assertOption(tool.getCommand().getOptions(), "case-file", true, true);
        assertOption(tool.getCommand().getOptions(), "output-folder", true, true);
        assertOption(tool.getCommand().getOptions(), "load-flow", false, false);
        assertOption(tool.getCommand().getOptions(), "types", false, true);
        assertOption(tool.getCommand().getOptions(), "with-extensions", false, false);
    }

    @Test
    void testCommand() {
        assertCommand();
    }
}
