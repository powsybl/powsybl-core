/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ext.TieLineExt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class TieLineDslTest extends AbstractLoadFlowRulesEngineTest {

    @Override
    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(300).setQ(100);
        addTieLine(network);
        return network;
    }

    private void addTieLine(Network network) {
        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 30.0;
        double hl1g2 = 35.0;
        double hl1b1 = 40.0;
        double hl1b2 = 45.0;
        double hl2g1 = 130.0;
        double hl2g2 = 135.0;
        double hl2b1 = 140.0;
        double hl2b2 = 145.0;
        double xnodeP = 50.0;
        double xnodeQ = 60.0;

        // adder
        Line tieLine = network.newLine().setId("tie")
                .setName("testNameTie")
                .setVoltageLevel1("VLHV1")
                .setBus1("NHV1")
                .setConnectableBus1("NHV1")
                .setVoltageLevel2("VLHV2")
                .setBus2("NHV2")
                .setConnectableBus2("NHV2")
                .setR(r + r2)
                .setX(x + x2)
                .setG1(hl1g1 + hl2g1)
                .setG2(hl1g2 + hl2g2)
                .setB1(hl1b1 + hl2b1)
                .setB2(hl1b2 + hl2b2)
                .add();
        TieLineExt.HalfLineImpl h1 = new TieLineExt.HalfLineImpl();
        h1.setId("hl1")
                .setName("half1_name")
                .setR(r)
                .setX(x)
                .setB1(hl1b1)
                .setB2(hl1b2)
                .setG1(hl1g1)
                .setG2(hl1g2)
                .setXnodeQ(xnodeQ)
                .setXnodeP(xnodeP);
        TieLineExt.HalfLineImpl h2 = new TieLineExt.HalfLineImpl();
        h2.setId("hl2")
                .setId("hl2")
                .setR(r2)
                .setX(x2)
                .setB1(hl2b1)
                .setB2(hl2b2)
                .setG1(hl2g1)
                .setG2(hl2g2)
                .setXnodeP(xnodeP)
                .setXnodeQ(xnodeQ);
        TieLineExt ext = new TieLineExt(tieLine, "ucte", h1, h2);
        tieLine.addExtension(TieLineExt.class, ext);
    }

    @Override
    protected String getDslFile() {
        return "/tieline-dsl.groovy";
    }

    @Test
    public void test() {
        engine.start(actionDb);
        Load load = network.getLoad("LOAD");
        assertEquals(20.0, load.getP0(), 0.0);
        assertEquals(11.0, load.getQ0(), 0.0);
    }
}
