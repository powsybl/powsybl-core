/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhenConditionWithBooleanTest extends AbstractLoadFlowRulesEngineTest {

    @Override
    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(300).setQ(100);
        return network;
    }

    @Override
    protected String getDslFile() {
        return "/golden_rule.groovy";
    }

    @Test
    public void test() {
        Load load = network.getLoad("LOAD");
        assertEquals(600.0, load.getP0(), 0.0);
        engine.start(actionDb);
        assertEquals(601.0, load.getP0(), 0.0);
    }
}
