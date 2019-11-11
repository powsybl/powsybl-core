/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.tools;

import com.powsybl.dynamicsimulation.tools.DynamicSimulationTool;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationToolTest extends AbstractToolTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final DynamicSimulationTool tool = new DynamicSimulationTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new DynamicSimulationTool());
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "dynamic-simulation", 7, 1);
        assertEquals("Computation", command.getTheme());
        assertEquals("Run dynamic simulation", command.getDescription());
        assertNull(command.getUsageFooter());
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "output-file", false, true);
        assertOption(command.getOptions(), "skip-postproc", false, false);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Files.copy(getClass().getResourceAsStream("/network.xiidm"), fileSystem.getPath("/network.xiidm"));
    }

    @Test
    public void testDynamicSimulation() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network '/network.xiidm'",
                "dynamic simulation results:",
                "+--------+---------+",
                "| Result | Metrics |",
                "+--------+---------+",
                "| true   |         |",
                "+--------+---------+");
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm"}, 0, expectedOut, "");
    }
}
