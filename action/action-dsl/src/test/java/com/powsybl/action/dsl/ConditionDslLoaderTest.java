/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.action.dsl.ast.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConditionDslLoaderTest {

    private Network network;
    private Line line1;
    private Line line2;

    @Before
    public void setUp() throws Exception {
        network = EurostagTutorialExample1Factory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").setV(380).setAngle(0);
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");

    }

    private void loadAndAssert(String expected, String script) throws IOException {
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(network);
        assertNotNull(node);
        assertEquals(expected, ExpressionPrinter.toString(node));
    }

    private void evalAndAssert(Object expected, String script) throws IOException {
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(network);
        assertNotNull(node);
        assertEquals(expected, ExpressionEvaluator.evaluate(node, new EvaluationContext() {
            @Override
            public Network getNetwork() {
                return network;
            }

            @Override
            public Contingency getContingency() {
                return null;
            }

            @Override
            public boolean isActionTaken(String actionId) {
                return actionId.equals("action");
            }
        }));
    }

    @Test
    public void testCondition() throws IOException {
        loadAndAssert("line('NHV1_NHV2_1')", "line('NHV1_NHV2_1')");
        loadAndAssert("line('NHV1_NHV2_1').terminal1.p", "line('NHV1_NHV2_1').terminal1.p");
        loadAndAssert("transformer('NGEN_NHV1')", "transformer('NGEN_NHV1')");
        loadAndAssert("branch('NGEN_NHV1')", "branch('NGEN_NHV1')");
        loadAndAssert("load('LOAD')", "load('LOAD')");

        loadAndAssert("1", "1"); // integer
        loadAndAssert("1.0", "1f"); // float
        loadAndAssert("1.0", "1d"); // double
        loadAndAssert("1.0", "1.0"); // big decimal
        for (String op : Arrays.asList("+", "-", "*", "/", "==", "<", ">", ">=", "<=", "!=")) {
            // integer
            loadAndAssert("(line('NHV1_NHV2_1').terminal1.p " + op + " 1)", "line('NHV1_NHV2_1').terminal1.p " + op + " 1");
            loadAndAssert("(1 " + op + " line('NHV1_NHV2_1').terminal1.p)", "1 " + op + " line('NHV1_NHV2_1').terminal1.p");

            // float
            loadAndAssert("(line('NHV1_NHV2_1').terminal1.p " + op + " 1.0)", "line('NHV1_NHV2_1').terminal1.p " + op + " 1f");
            loadAndAssert("(1.0 " + op + " line('NHV1_NHV2_1').terminal1.p)", "1f " + op + " line('NHV1_NHV2_1').terminal1.p");

            // double
            loadAndAssert("(line('NHV1_NHV2_1').terminal1.p " + op + " 1.0)", "line('NHV1_NHV2_1').terminal1.p " + op + " 1d");
            loadAndAssert("(1.0 " + op + " line('NHV1_NHV2_1').terminal1.p)", "1d " + op + " line('NHV1_NHV2_1').terminal1.p");

            // big decimal
            loadAndAssert("(line('NHV1_NHV2_1').terminal1.p " + op + " 1.0)", "line('NHV1_NHV2_1').terminal1.p " + op + " 1.0");
            loadAndAssert("(1.0 " + op + " line('NHV1_NHV2_1').terminal1.p)", "1.0 " + op + " line('NHV1_NHV2_1').terminal1.p");
        }

        loadAndAssert("true", "true");
        loadAndAssert("false", "true && false");
        for (String op : Arrays.asList("&&", "||")) {
            loadAndAssert("(line('NHV1_NHV2_1').overloaded " + op + " true)", "line('NHV1_NHV2_1').overloaded " + op + " true");
            loadAndAssert("(true " + op + " line('NHV1_NHV2_1').overloaded)", "true " + op + " line('NHV1_NHV2_1').overloaded");
        }
        loadAndAssert("false", "!true");
        loadAndAssert("!(line('NHV1_NHV2_1').overloaded)", "!line('NHV1_NHV2_1').overloaded");

        loadAndAssert("actionTaken('action1')", "actionTaken('action1')");
        evalAndAssert(true, "actionTaken('action')");
        loadAndAssert("contingencyOccurred('contingency1')", "contingencyOccurred('contingency1')");
        loadAndAssert("contingencyOccurred()", "contingencyOccurred()");
        evalAndAssert(false, "contingencyOccurred()");
        loadAndAssert("mostLoaded([NHV1_NHV2_1, NHV1_NHV2_2])", "mostLoaded(['NHV1_NHV2_1','NHV1_NHV2_2'])");
        loadAndAssert("isOverloaded([NHV1_NHV2_1, NHV1_NHV2_2])", "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'])");
    }

    @Test
    public void testExpressionEvaluator() throws IOException {

        Terminal terminal = network.getLoad("LOAD").getTerminal();
        double old = terminal.getP();
        terminal.setP(400);
        evalAndAssert(100.0, "(load('LOAD').p0 - load('LOAD').terminal.p) / 2");
        evalAndAssert(true, "(load('LOAD').p0 - load('LOAD').terminal.p) > 2");
        terminal.setP(old);

        // visitComparisonOperator
        evalAndAssert(true, "load('LOAD').p0 == 600.0");
        evalAndAssert(true, "load('LOAD').p0 != 300.0");
        evalAndAssert(true, "load('LOAD').p0 > 100.0");
        evalAndAssert(true, "load('LOAD').p0 < 1000.0");
        evalAndAssert(true, "load('LOAD').p0 >= 100.0");
        evalAndAssert(true, "load('LOAD').p0 <= 1000.0");
        evalAndAssert(599.0, "load('LOAD').p0 - 1");
        evalAndAssert(1200.0, "load('LOAD').p0 * 2");
        evalAndAssert(300.0, "load('LOAD').p0 / 2");

        // visitNotOperator
        evalAndAssert(false, "! load('LOAD').terminal.connected");

        // visitLogicalOperator
        evalAndAssert(true, "load('LOAD').terminal.connected || false");
        evalAndAssert(false, "load('LOAD').terminal.connected && false");

        // visitArithmeticOperator
        evalAndAssert(601.0, "load('LOAD').p0 + 1");
        evalAndAssert(599.0, "load('LOAD').p0 - 1");
        evalAndAssert(1200.0, "load('LOAD').p0 * 2");
        evalAndAssert(300.0, "load('LOAD').p0 / 2");
    }

    @Test
    public void testIsOverloadedNode() throws IOException {
        line1.getTerminal1().setP(100.0).setQ(50.0);
        evalAndAssert(false, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'])");

        line1.newCurrentLimits1().setPermanentLimit(0.00001).add();
        assertNotNull(line1.getCurrentLimits1());
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'])");

        line1.getTerminal1().setP(600.0).setQ(300.0); // i = 1019.2061
        double current = line1.getTerminal1().getI();
        line1.newCurrentLimits1().setPermanentLimit(current - 100).add();
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'])");
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'], 0.05)");
        line1.newCurrentLimits1().setPermanentLimit(current).add();
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'])"); // permanent = real current
        line1.newCurrentLimits1().setPermanentLimit(current * 2).add();
        evalAndAssert(false, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'], 0.9)");
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'], 0.1)");

        addCurrentLimitsOnLine1();
        line1.getTerminal1().setP(400.0).setQ(150.0); // i = 649.06
        evalAndAssert(true, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2'], 1)");

        try {
            evalAndAssert(false, "isOverloaded(['NHV1_NHV2_1','NHV1_NHV2_2', 'UNKNOWN'])");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Branch 'UNKNOWN' not found", e.getMessage());
        }
    }

    private void addCurrentLimitsOnLine1() {
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("10")
                    .setAcceptableDuration(10 * 60)
                    .setValue(700)
                    .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("5")
                    .setAcceptableDuration(5 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
    }

    @Test
    public void testNetworkAccess() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                .setName("20")
                .setAcceptableDuration(20 * 60)
                .setValue(800)
                .endTemporaryLimit()
                .add();

        // IIDM method call
        evalAndAssert(800.0, "line('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)");
        evalAndAssert(false, "line('NHV1_NHV2_1').overloaded");

        evalAndAssert(800.0, "branch('NHV1_NHV2_1').currentLimits1.getTemporaryLimitValue(1200)");
        evalAndAssert(0.0, "branch('NHV2_NLOAD').g");
    }

    @Test
    public void testLoadingRank() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
        line2.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();

        // both are 20' overloaded, but line2 is more overloaded than line1
        line1.getTerminal1().setP(300).setQ(100); // line1.i1 = 480
        line2.getTerminal1().setP(400).setQ(100); // line2.i1 = 626
        evalAndAssert(2, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(1, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // both are 20' overloaded, but line1 is more overloaded than line2
        line1.getTerminal1().setP(400); // line1.i1 = 626
        line2.getTerminal1().setP(300); // line2.i1 = 480
        evalAndAssert(1, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(2, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // none are overloaded, but line2 % of permanent limit is greater than line1 one
        line1.getTerminal1().setP(100); // line1.i1 = 214
        line2.getTerminal1().setP(150); // line2.i1 = 273
        evalAndAssert(2, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(1, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // none are overloaded, but line1 % of permanent limit is greater than line2 one
        line1.getTerminal1().setP(150); // line1.i1 = 273
        line2.getTerminal1().setP(100); // line2.i1 = 214
        evalAndAssert(1, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(2, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
    }

    @Test
    public void testLoadingRankWithDifferentAcceptableDuration() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
        line2.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("5")
                    .setAcceptableDuration(5 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();

        // line1 current is greater than line2 one but acceptable duration of line2 is less than line1
        line1.getTerminal1().setP(410).setQ(100); // line1.i1 = 641
        line2.getTerminal1().setP(400).setQ(100); // line2.i1 = 626
        evalAndAssert(2, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(1, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
    }

    @Test
    public void testLoadingRankWithUndefinedCurrentLimitsForLine2() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();

        // line2 current is greater than line1 one but line2 has not temporary limits
        line1.getTerminal1().setP(400).setQ(100); // line1.i1 = 626
        line2.getTerminal1().setP(500).setQ(100); // line2.i1 = 774
        evalAndAssert(1, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(2, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
    }

    @Test
    public void testLoadingRankWithCurrentLimitsAtBothSides() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
        line1.newCurrentLimits2()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(600)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1")
                    .setAcceptableDuration(1 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();
        line2.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                    .setName("20")
                    .setAcceptableDuration(20 * 60)
                    .setValue(800)
                .endTemporaryLimit()
                .add();

        // line2 current is greater than line2 one but acceptable duration of side 2 of line1 is less than line2 one
        line1.getTerminal1().setP(400).setQ(100); // line1.i1 = 626
        line1.getTerminal2().setP(400).setQ(100); // line1.i2 = 626
        line2.getTerminal1().setP(410).setQ(100); // line2.i1 = 641
        line2.getTerminal2().setP(410).setQ(100); // line2.i2 = 641
        evalAndAssert(1, "loadingRank('NHV1_NHV2_1', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(2, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // handle unknown branch
        try {
            evalAndAssert(null, "loadingRank('NHV1_NHV2_2', ['NHV1_NHV2_1', 'NHV1_NHV2_2', 'UNKNOWN'])");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Branch 'UNKNOWN' not found", e.getMessage());
        }
    }

    @Test
    public void testMostLoaded() throws IOException {
        // add temporary limits
        line1.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                .setName("20")
                .setAcceptableDuration(20 * 60)
                .setValue(800)
                .endTemporaryLimit()
                .add();
        line2.newCurrentLimits1()
                .setPermanentLimit(400)
                .beginTemporaryLimit()
                .setName("20")
                .setAcceptableDuration(20 * 60)
                .setValue(800)
                .endTemporaryLimit()
                .add();

        // line2 is more overloaded than line1
        line1.getTerminal1().setP(300).setQ(100); // line1.i1 = 480
        line2.getTerminal1().setP(400).setQ(100); // line2.i1 = 626
        evalAndAssert("NHV1_NHV2_2", "mostLoaded(['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // line1 is more overloaded than line2
        line1.getTerminal1().setP(400).setQ(100); // line1.i1 = 626
        line2.getTerminal1().setP(300).setQ(100); // line2.i1 = 480
        evalAndAssert("NHV1_NHV2_1", "mostLoaded(['NHV1_NHV2_1', 'NHV1_NHV2_2'])");

        // combine with loadingRank
        evalAndAssert(1, "loadingRank(mostLoaded(['NHV1_NHV2_1', 'NHV1_NHV2_2']), ['NHV1_NHV2_1', 'NHV1_NHV2_2'])");
        evalAndAssert(1, "loadingRank('NHV1_NHV2_1', [mostLoaded(['NHV1_NHV2_1', 'NHV1_NHV2_2']), 'NHV1_NHV2_2'])");

        // handle unknown branch
        try {
            evalAndAssert(null, "mostLoaded(['NHV1_NHV2_1', 'UNKNOWN'])");
            fail();
        } catch (PowsyblException e) {
            assertEquals("Branch 'UNKNOWN' not found", e.getMessage());
        }
    }

    @Test
    public void testExpressionVariableLister() {
        String script = "line('NHV1_NHV2_1').terminal1.p > 0 || line('NHV1_NHV2_1').getTerminal2().getP() > 0 && actionTaken('action1')";
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(network);
        assertNotNull(node);
        List<NetworkNode> nodes = ExpressionVariableLister.list(node);
        assertEquals(2, nodes.size());
    }

    @Test
    public void testActionTakenLister() {
        String script = "actionTaken('action1') && line('NHV1_NHV2_1').terminal1.p > 0 && actionTaken('action2')";
        ExpressionNode node = (ExpressionNode) new ConditionDslLoader(script).load(network);
        assertNotNull(node);
        List<String> actions = ExpressionActionTakenLister.list(node);
        assertEquals(2, actions.size());
        assertTrue(actions.contains("action1"));
        assertTrue(actions.contains("action2"));
    }
}
