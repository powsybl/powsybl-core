/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.modification.generation.dispatch;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class GenerationDispatchTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = Network.read("testGenerationDispatch.xiidm", getClass().getResourceAsStream("/testGenerationDispatch.xiidm"));
    }

    @Test
    void testGeneratorsFullDispatch() {
        // test with loss coefficient = 20%
        GenerationDispatch generationDispatch = new GenerationDispatchBuilder().withLossCoefficient(20.).build();
        generationDispatch.apply(network);

        assertEquals(528., generationDispatch.getTotalDemand(0));

        // check new targetP values on connected generators in connected component
        assertEquals(100., network.getGenerator("GH1").getTargetP());
        assertEquals(70., network.getGenerator("GH2").getTargetP());
        assertEquals(130., network.getGenerator("GH3").getTargetP());
        assertEquals(100., network.getGenerator("GTH1").getTargetP());
        assertEquals(128., network.getGenerator("GTH2").getTargetP());
        assertEquals(0., network.getGenerator("TEST1").getTargetP());
        assertEquals(0., network.getGenerator("ABC").getTargetP());

        // check that the supply-demand balance could be met : no remaining power to dispatch
        assertEquals(0., generationDispatch.getRemainingPowerImbalance(0));
    }

    @Test
    void testGeneratorsPartialDispatch() {
        // test with loss coefficient = 90%
        GenerationDispatch generationDispatch = new GenerationDispatchBuilder().withLossCoefficient(90.).build();
        generationDispatch.apply(network);

        assertEquals(836., generationDispatch.getTotalDemand(0));

        // check new targetP values on connected generators in connected component
        assertEquals(100., network.getGenerator("GH1").getTargetP());
        assertEquals(70., network.getGenerator("GH2").getTargetP());
        assertEquals(130., network.getGenerator("GH3").getTargetP());
        assertEquals(100., network.getGenerator("GTH1").getTargetP());
        assertEquals(150., network.getGenerator("GTH2").getTargetP());
        assertEquals(50., network.getGenerator("TEST1").getTargetP());
        assertEquals(100., network.getGenerator("ABC").getTargetP());

        // check that the supply-demand balance could not be met : there is remaining power to dispatch
        assertEquals(136., generationDispatch.getRemainingPowerImbalance(0));
    }
}

