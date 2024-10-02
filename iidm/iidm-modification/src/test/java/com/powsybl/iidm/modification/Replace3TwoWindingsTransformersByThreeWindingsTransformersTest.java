/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class Replace3TwoWindingsTransformersByThreeWindingsTransformersTest {

    private Network network;
    private TwoWindingsTransformer t2w1;
    private TwoWindingsTransformer t2w2;
    private TwoWindingsTransformer t2w3;

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        assertTrue(network.getThreeWindingsTransformerCount() == 1);
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        assertTrue(network.getTwoWindingsTransformerCount() == 3);
        t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        assertNotNull(t2w1);
        t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        assertNotNull(t2w2);
        t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");
        assertNotNull(t2w3);
    }

    @Test
    void replaceTest() {
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
    }

    @Test
    void testApplyChecks() {
        t2w1.setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertEquals("Replace3TwoWindingsTransformersByThreeWindingsTransformers", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers modification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }
}
