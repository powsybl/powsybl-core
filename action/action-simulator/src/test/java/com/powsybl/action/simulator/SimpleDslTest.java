/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChangerStep;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleDslTest extends AbstractLoadFlowRulesEngineTest {

    @Override
    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(300).setQ(100);
        return network;
    }

    @Override
    protected String getDslFile() {
        return "/simple-dsl.groovy";
    }

    @Test
    public void test() {
        assertEquals(600.0, network.getLoad("LOAD").getP0(), 0.0);
        engine.start(actionDb);
        assertEquals(601.0, network.getLoad("LOAD").getP0(), 0.0);
        RatioTapChangerStep step = network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().getCurrentStep();
        assertEquals(3.3, step.getRdr(), 0.0);
        assertEquals(4.4, step.getRdx(), 0.0);
        assertEquals(5.5, step.getRdg(), 0.0);
        assertEquals(6.6, step.getRdb(), 0.0);
        assertEquals(5.0, step.getRatio(), 0.0);
    }
}
