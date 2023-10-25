/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.tool;

import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DynamicSimulationToolTest extends AbstractToolTest {

    private final DynamicSimulationTool tool = new DynamicSimulationTool();

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new DynamicSimulationTool());
    }

    @Override
    public void assertCommand() {
        Command command = tool.getCommand();

        assertCommand(command, "dynamic-simulation", 8, 2);
        assertEquals("Computation", command.getTheme());
        assertEquals("Run dynamic simulation", command.getDescription());
        assertNull(command.getUsageFooter());
        assertOption(command.getOptions(), "case-file", true, true);
        assertOption(command.getOptions(), "dynamic-models-file", true, true);
        assertOption(command.getOptions(), "event-models-file", false, true);
        assertOption(command.getOptions(), "curves-file", false, true);
        assertOption(command.getOptions(), "output-file", false, true);
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Files.copy(getClass().getResourceAsStream("/network.xiidm"), fileSystem.getPath("/network.xiidm"));
        Files.createFile(fileSystem.getPath("/dynamicModels.groovy"));
        Files.createFile(fileSystem.getPath("/eventModels.groovy"));
        Files.createFile(fileSystem.getPath("/curves.groovy"));
        Files.createFile(fileSystem.getPath("/curves.json"));
    }

    @Test
    void testDynamicSimulation() throws IOException {
        String expectedOut = String.join(System.lineSeparator(),
                "Loading network '/network.xiidm'",
                "dynamic simulation results:",
                "+--------+",
                "| Result |",
                "+--------+",
                "| true   |",
                "+--------+");
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy"}, 0, expectedOut, "");

        // Run with curves
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--curves-file", "/curves.groovy"}, 0, expectedOut, "");
    }

    @Test
    void testDynamicSimulationWithEvents() throws IOException {
        // Run with events in groovy
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--event-models-file", "/eventModels.groovy"}, 0, null, "");

        // Run with events in JSON (not supported)
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--event-models-file", "/eventModels.json"}, 3, null, "Unsupported events format: json");
    }

    @Test
    void testDynamicSimulationWithCurves() throws IOException {
        // Run with curves in groovy
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--curves-file", "/curves.groovy"}, 0, null, "");

        // Run with curves in JSON (not supported)
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--curves-file", "/curves.json"}, 3, null, "Unsupported curves format: json");
    }

}
