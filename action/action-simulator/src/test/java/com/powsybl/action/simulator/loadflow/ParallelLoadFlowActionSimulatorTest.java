/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.simulator.tools.ActionSimulatorToolConstants;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ParallelLoadFlowActionSimulatorTest {

    private ComputationManager computationManager;

    private ParallelLoadFlowActionSimulator parallelLoadFlowActionSimulator;

    private ActionDb actionDb;

    private List<String> contingencies;

    @Before
    public void setup() {
        Network network = mock(Network.class);
        computationManager = mock(ComputationManager.class);
        ToolRunningContext context = mock(ToolRunningContext.class);
        when(context.getLongTimeExecutionComputationManager()).thenReturn(computationManager);

        CommandLine commandLine = mock(CommandLine.class);
        when(commandLine.getOptionValue(ActionSimulatorToolConstants.TASK_COUNT)).thenReturn("7");

        LoadFlowActionSimulatorConfig config = mock(LoadFlowActionSimulatorConfig.class);

        parallelLoadFlowActionSimulator = new ParallelLoadFlowActionSimulator(network, context, commandLine, config, false, Collections.emptyList());

        actionDb = mock(ActionDb.class);
        contingencies = mock(List.class);
    }

    @Test
    public void test() {
        assertEquals("parallel loadflow action-simulator", parallelLoadFlowActionSimulator.getName());
        when(contingencies.size()).thenReturn(11);
        try {
            parallelLoadFlowActionSimulator.start(actionDb, contingencies);
        } catch (Exception e) {
            // do nothing
        }
        verify(computationManager, times(7)).execute(any(), any());
    }

    @Test
    public void testContingenySizeSmallerThanTasks() {
        when(contingencies.size()).thenReturn(3);
        try {
            parallelLoadFlowActionSimulator.start(actionDb, contingencies);
        } catch (Exception e) {
            // do nothing
        }
        verify(computationManager, times(3)).execute(any(), any());
    }
}
