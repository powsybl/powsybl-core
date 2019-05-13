/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class LoadFlowActionSimulatorTest {

    @Test
    public void test() {
        LoadFlowActionSimulatorConfig loadFlowActionSimulatorConfig = Mockito.mock(LoadFlowActionSimulatorConfig.class);
        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Network network = Mockito.mock(Network.class);
        TableFormatterConfig formatterConfig = Mockito.mock(TableFormatterConfig.class);
        LoadFlowActionSimulatorObserver observer = Mockito.mock(LoadFlowActionSimulatorObserver.class);

        LoadFlowActionSimulator loadFlowActionSimulator = new LoadFlowActionSimulator(network, computationManager, loadFlowActionSimulatorConfig, formatterConfig, true, observer);

        assertEquals("loadflow", loadFlowActionSimulator.getName());
        assertSame(computationManager, loadFlowActionSimulator.getComputationManager());
        assertSame(network, loadFlowActionSimulator.getNetwork());
        assertSame(loadFlowActionSimulatorConfig, loadFlowActionSimulator.getLoadFlowActionSimulatorConfig());
        assertSame(formatterConfig, loadFlowActionSimulator.getTableFormatterConfig());
        assertTrue(loadFlowActionSimulator.isApplyIfSolvedViolations());
    }

}
