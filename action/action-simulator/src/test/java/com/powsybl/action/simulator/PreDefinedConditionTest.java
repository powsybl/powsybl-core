/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator;

import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PreDefinedConditionTest extends AbstractLoadFlowRulesEngineTest {
    @Override
    protected Network createNetwork() {
        Network network = EurostagTutorialExample1WithTemporaryLimitFactory.create();

        // overload the line
        network.getVoltageLevel("VLHV1").getBusBreakerView().getBus("NHV1").setV(380).setAngle(0);
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(300).setQ(100);

        return network;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        initialize();
        engine = new LoadFlowActionSimulator(network, computationManager, new LoadFlowActionSimulatorConfig(LoadFlowFactory.class, 3, false), Arrays.asList(observer)) {
            @Override
            protected LoadFlowFactory newLoadFlowFactory() {
                return loadFlowFactory;
            }
        };
    }

    @Override
    protected String getDslFile() {
        return "/pre-defined-condition.groovy";
    }

    @Test
    public void test() {
        engine.start(actionDb);
    }
}
