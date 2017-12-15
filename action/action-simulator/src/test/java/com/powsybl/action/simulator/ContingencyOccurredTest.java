/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.action.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.action.simulator.loadflow.RunningContext;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyOccurredTest extends AbstractLoadFlowRulesEngineTest {

    private final List<String> preContActions = new ArrayList<>();
    private final List<String> postContActions = new ArrayList<>();

    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        return network;
    }

    @Override
    protected LoadFlowActionSimulatorObserver createObserver() {
        return new DefaultLoadFlowActionSimulatorObserver() {

            private Network preContNetwork;
            private Network postContNetwork;

            private void overload(Network network) {
                // set the current so that line is overloaded
                Line l2 = network.getLine("NHV1_NHV2_2");
                l2.getTerminal1().setP(300).setQ(100);
            }

            private void underload(Network network) {
                // after first loadflow in pre-contingency or post-contingency mode decrease the current on overloaded
                // line to simulate action effect
                Line l2 = network.getLine("NHV1_NHV2_2");
                l2.getTerminal1().setP(100).setQ(100);
            }

            @Override
            public void beforePreContingencyAnalysis(RunningContext runningContext) {
                preContNetwork = runningContext.getNetwork();
                overload(preContNetwork);
            }

            @Override
            public void postContingencyAnalysisNetworkLoaded(RunningContext runningContext) {
                postContNetwork = runningContext.getNetwork();
                overload(postContNetwork);
            }

            @Override
            public void beforeAction(RunningContext runningContext, String actionId) {
                if (runningContext.getContingency() == null) {
                    preContActions.add(actionId);
                    underload(preContNetwork);
                } else {
                    postContActions.add(actionId);
                    underload(postContNetwork);
                }
            }
        };
    }

    @Override
    protected String getDslFile() {
        return "/contingency-occurred.groovy";
    }

    @Test
    public void test() throws Exception {
        engine.start(actionDb, "contingency1");

        // check action1 is activated in pre-contingency state and action2 in post-contingency state
        assertEquals(preContActions, Collections.singletonList("action1"));
        assertEquals(postContActions, Collections.singletonList("action2"));
    }
}
