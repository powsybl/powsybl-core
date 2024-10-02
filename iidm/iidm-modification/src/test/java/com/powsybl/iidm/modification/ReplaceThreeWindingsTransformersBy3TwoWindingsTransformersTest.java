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
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ReplaceThreeWindingsTransformersBy3TwoWindingsTransformersTest {

    private Network network;
    private ThreeWindingsTransformer t3w;

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        assertTrue(network.getThreeWindingsTransformerCount() == 1);
        t3w = network.getThreeWindingsTransformer("3WT");
        assertNotNull(t3w);
    }

    @Test
    void replaceTest() {
        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
    }

    @Test
    void testApplyChecks() {
        t3w.setProperty("unknown property", "unknown value");
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals("ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers modification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }
}
