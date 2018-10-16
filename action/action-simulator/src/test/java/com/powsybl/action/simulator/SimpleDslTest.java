/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerTap;
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
        RatioTapChanger rtc = network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger();
        RatioTapChangerTap tap = rtc.getCurrentTap();
        assertEquals(3.3, tap.getRdr(), 0.0);
        assertEquals(4.4, tap.getRdx(), 0.0);
        assertEquals(5.5, tap.getRdg(), 0.0);
        assertEquals(6.6, tap.getRdb(), 0.0);
        assertEquals(5.0, tap.getRatio(), 0.0);

        RatioTapChangerTap tap0 = rtc.getTap(0);
        assertEquals(13.3, tap0.getRdr(), 0.0);
        assertEquals(24.4, tap0.getRdx(), 0.0);
        assertEquals(25.5, tap0.getRdg(), 0.0);
        assertEquals(36.6, tap0.getRdb(), 0.0);
        assertEquals(55.0, tap0.getRatio(), 0.0);

    }
}
