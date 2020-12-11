/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.iidm.network.Network;
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
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationToolTest extends AbstractToolTest {

    @AutoService(DynamicSimulationProvider.class)
    public static class DynamicSimulationProviderMock implements DynamicSimulationProvider {

        @Override
        public CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters) {
            return CompletableFuture.completedFuture(new DynamicSimulationResultImpl(true, null, Collections.emptyMap(), DynamicSimulationResult.emptyTimeLine()));
        }

        @Override
        public String getName() {
            return "Mock";
        }

        @Override
        public String getVersion() {
            return null;
        }
    }

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

    @Before
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
    public void testDynamicSimulation() throws IOException {
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
    public void testDynamicSimulationWithEvents() throws IOException {
        // Run with events in groovy
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--event-models-file", "/eventModels.groovy"}, 0, null, "");

        // Run with events in JSON (not supported)
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--event-models-file", "/eventModels.json"}, 3, null, "Unsupported events format: json");
    }

    @Test
    public void testDynamicSimulationWithCurves() throws IOException {
        // Run with curves in groovy
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--curves-file", "/curves.groovy"}, 0, null, "");

        // Run with curves in JSON (not supported)
        assertCommand(new String[]{"dynamic-simulation", "--case-file", "/network.xiidm", "--dynamic-models-file", "/dynamicModels.groovy", "--curves-file", "/curves.json"}, 3, null, "Unsupported curves format: json");
    }

}
