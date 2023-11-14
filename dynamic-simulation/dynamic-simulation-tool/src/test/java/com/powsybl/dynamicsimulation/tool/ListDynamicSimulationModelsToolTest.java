/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.tool;

import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class ListDynamicSimulationModelsToolTest extends AbstractToolTest {

    private final ListDynamicSimulationModelsTool tool = new ListDynamicSimulationModelsTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new ListDynamicSimulationModelsTool());
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "list-dynamic-simulation-models", 2, 0);
        assertEquals("Misc", command.getTheme());
        assertEquals("Display dynamic simulation models", command.getDescription());
        assertNull(command.getUsageFooter());
        Options options = command.getOptions();
        assertOption(options, "dynamic-models", false, false);
        assertOption(options, "event-models", false, false);
    }

    @Test
    void testCommand() {
        assertCommand();
    }

    @Test
    void testAllModels() {
        String expectedOut = String.join(System.lineSeparator(), "Dynamic models:", "Event models:" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"list-dynamic-simulation-models"}, expectedOut);
    }

    @Test
    void testDynamicModelsOnly() {
        String expectedOut = String.join(System.lineSeparator(), "Dynamic models:" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"list-dynamic-simulation-models", "--dynamic-models"}, expectedOut);
    }

    @Test
    void testEventModelsOnly() {
        String expectedOut = String.join(System.lineSeparator(), "Event models:" + System.lineSeparator());
        assertCommandSuccessful(new String[]{"list-dynamic-simulation-models", "--event-models"}, expectedOut);
    }
}
