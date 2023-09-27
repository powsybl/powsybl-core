/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.modelslist;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.test.AbstractToolTest;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
class ListDynamicSimulationModelsToolTest extends AbstractToolTest {

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
    void testAllModels() throws IOException {
        String expectedOut = String.join(System.lineSeparator(), "Dynamic models:", "Event models:");
        assertCommand(new String[]{"list-dynamic-simulation-models"}, 0, expectedOut, "");
    }

    @Test
    void testDynamicModelsOnly() throws IOException {
        String expectedOut = String.join(System.lineSeparator(), "Dynamic models:");
        assertCommand(new String[]{"list-dynamic-simulation-models", "--dynamic-models"}, 0, expectedOut, "");
    }

    @Test
    void testEventModelsOnly() throws IOException {
        String expectedOut = String.join(System.lineSeparator(), "Event models:");
        assertCommand(new String[]{"list-dynamic-simulation-models", "--event-models"}, 0, expectedOut, "");
    }
}
