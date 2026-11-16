/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator;

import com.powsybl.action.ial.simulator.loadflow.*;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ContingencyOccurredTest extends AbstractLoadFlowRulesEngineTest {

    private final List<String> preContActions = new ArrayList<>();
    private final List<String> postContActions = new ArrayList<>();

    private ByteArrayOutputStream bout;
    private ByteArrayOutputStream berr;
    private PrintStream out;
    private PrintStream err;

    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        return network;
    }

    @Override
    protected LoadFlowActionSimulatorObserver createObserver() {
        bout = new ByteArrayOutputStream();
        berr = new ByteArrayOutputStream();
        out = new PrintStream(bout);
        err = new PrintStream(berr);
        return new LoadFlowActionSimulatorLogPrinter(out, err, true) {

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
                super.beforePreContingencyAnalysis(runningContext);
                preContNetwork = runningContext.getNetwork();
                overload(preContNetwork);
            }

            @Override
            public void postContingencyAnalysisNetworkLoaded(RunningContext runningContext) {
                super.postContingencyAnalysisNetworkLoaded(runningContext);
                postContNetwork = runningContext.getNetwork();
                overload(postContNetwork);
            }

            @Override
            public void beforeAction(RunningContext runningContext, String actionId) {
                super.beforeAction(runningContext, actionId);
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
    void test() throws Exception {
        engine.start(actionDb, "contingency1");

        // check action1 is activated in pre-contingency state and action2 in post-contingency state
        assertEquals(Collections.singletonList("action1"), preContActions);
        assertEquals(Collections.singletonList("action2"), postContActions);
        testLogs();
    }

    @Test
    void testWithIgnorePreContingencyViolations() {
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig("LoadFlowMock", 3, true, false),
                applyIfWorks(), new LoadFlowParameters(), createObserver());
        engine.start(actionDb, "contingency1");

        // check action1 is activated in pre-contingency state and action2 in post-contingency state
        assertEquals(Collections.singletonList("action1"), preContActions);
        assertEquals(Collections.singletonList("action2"), postContActions);
    }

    void testLogs() {
        String out = bout.toString();
        assertTrue(out.contains("""
                Starting pre-contingency analysis
                    Round 0
                        Violations:
                """.replaceAll("\n", System.lineSeparator())));
        assertTrue(out.contains("""
                        Rule 'rule1' evaluated to TRUE
                        Applying action 'action1'
                        Rule 'rule2' evaluated to FALSE
                    Round 1
                        No more violation
                Starting post-contingency 'contingency1' analysis
                    Round 0
                        Violations:
                """.replaceAll("\n", System.lineSeparator())));
        assertTrue(out.contains("""
                        Rule 'rule1' evaluated to FALSE
                        Rule 'rule2' evaluated to TRUE
                        Applying action 'action2'
                    Round 1
                        No more violation
                """.replaceAll("\n", System.lineSeparator())));
        assertEquals("", berr.toString());
    }

    @AfterEach
    void clear() throws IOException {
        super.tearDown();
        err.close();
        out.close();
        berr.close();
        bout.close();
    }
}
