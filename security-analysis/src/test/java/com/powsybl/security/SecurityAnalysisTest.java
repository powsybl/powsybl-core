/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.BranchContingency;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.api.LoadFlowFactory;
import com.powsybl.loadflow.api.LoadFlowParameters;
import com.powsybl.loadflow.api.mock.LoadFlowFactoryMock;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class SecurityAnalysisTest {

    @Test
    public void run() {
        Network network = EurostagTutorialExample1Factory.create();
        ((Bus) network.getIdentifiable("NHV1")).setV(380f);
        ((Bus) network.getIdentifiable("NHV2")).setV(380f);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(1500f).add();
        network.getLine("NHV1_NHV2_1").newCurrentLimits2()
            .setPermanentLimit(1200f)
            .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(1300)
            .endTemporaryLimit()
            .add();

        ComputationManager computationManager = Mockito.mock(ComputationManager.class);
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable r) {
                r.run();
            }
        };
        Mockito.when(computationManager.getExecutor()).thenReturn(executor);

        LoadFlowFactory loadflowFactory = new LoadFlowFactoryMock();

        ContingenciesProvider contingenciesProvider = Mockito.mock(ContingenciesProvider.class);
        Contingency contingency = Mockito.mock(Contingency.class);
        Mockito.when(contingency.getId()).thenReturn("NHV1_NHV2_2_contingency");
        Mockito.when(contingency.getElements()).thenReturn(Collections.singletonList(new BranchContingency("NHV1_NHV2_2")));
        Mockito.when(contingency.toTask()).thenReturn(new ModificationTask() {
            @Override
            public void modify(Network network, ComputationManager computationManager) {
                network.getLine("NHV1_NHV2_2").getTerminal1().disconnect();
                network.getLine("NHV1_NHV2_2").getTerminal2().disconnect();
                network.getLine("NHV1_NHV2_1").getTerminal2().setP(600f);
            }
        });
        Mockito.when(contingenciesProvider.getContingencies(network)).thenReturn(Collections.singletonList(contingency));

        SecurityAnalysisResult result = new SecurityAnalysisImpl(network, computationManager, loadflowFactory)
                                                .runAsync(contingenciesProvider, StateManager.INITIAL_STATE_ID, new LoadFlowParameters())
                                                .join();

        assertTrue(result.getPreContingencyResult().isComputationOk());
        assertEquals(0, result.getPreContingencyResult().getLimitViolations().size());
        PostContingencyResult postcontingencyResult = result.getPostContingencyResults().get(0);
        assertTrue(postcontingencyResult.getLimitViolationsResult().isComputationOk());
        assertEquals(1, postcontingencyResult.getLimitViolationsResult().getLimitViolations().size());
        LimitViolation violation = postcontingencyResult.getLimitViolationsResult().getLimitViolations().get(0);
        assertEquals(LimitViolationType.CURRENT, violation.getLimitType());
        assertEquals("NHV1_NHV2_1", violation.getSubjectId());
    }

}
