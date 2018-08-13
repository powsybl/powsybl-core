/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorObserver;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AlternativeTest extends AbstractLoadFlowRulesEngineTest {

    private final LoadFlowActionSimulatorObserver obs = Mockito.mock(LoadFlowActionSimulatorObserver.class);

    @Override
    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(300).setQ(100);
        return network;
    }

    @Override
    protected String getDslFile() {
        return "/rule-with-test.groovy";
    }

    @Test
    public void test() {
        Generator generator = network.getGenerator("GEN");
        Load load = network.getLoad("LOAD");
        double targetP = generator.getTargetP();
        double loadP0 = load.getP0();

        engine.start(actionDb, "contingency1"); // life = 3
        assertEquals(targetP + 2.0, generator.getTargetP(), 0.0);
        assertEquals(loadP0, load.getP0(), 0.0);

        // once for action1, once for action2
        verify(obs, times(2)).beforeTest(Mockito.any(), Mockito.any());
    }

    @Override
    protected LoadFlowActionSimulatorObserver createObserver() {
        return obs;
    }

}
