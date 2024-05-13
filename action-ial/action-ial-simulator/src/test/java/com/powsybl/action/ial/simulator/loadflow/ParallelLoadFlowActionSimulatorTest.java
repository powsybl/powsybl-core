/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class ParallelLoadFlowActionSimulatorTest {

    private ComputationManager computationManager;

    private ParallelLoadFlowActionSimulator parallelLoadFlowActionSimulator;

    private List<String> contingencies;

    @BeforeEach
    void setup() {
        Network network = mock(Network.class);
        computationManager = mock(ComputationManager.class);

        LoadFlowActionSimulatorConfig config = mock(LoadFlowActionSimulatorConfig.class);

        parallelLoadFlowActionSimulator = new ParallelLoadFlowActionSimulator(network, computationManager, 7, config, false, Collections.emptyList());

        contingencies = mock(List.class);
    }

    @Test
    void test() {
        when(contingencies.size()).thenReturn(11);

        String script = "";
        try {
            parallelLoadFlowActionSimulator.run(script, contingencies);
        } catch (Exception e) {
            // do nothing
        }
        verify(computationManager, times(1)).execute(any(), any());
    }

    @Test
    void testContingencySizeSmallerThanTasks() {
        when(contingencies.size()).thenReturn(3);

        String script = "";
        try {
            parallelLoadFlowActionSimulator.run(script, contingencies);
        } catch (Exception e) {
            // do nothing
        }
        verify(computationManager, times(1)).execute(any(), any());
    }
}
