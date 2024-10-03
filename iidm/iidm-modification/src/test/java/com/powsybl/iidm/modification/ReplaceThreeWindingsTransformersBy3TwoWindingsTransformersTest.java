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
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

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
        assertEquals(1, network.getThreeWindingsTransformerCount());
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
    void testReportNode() throws IOException {
        t3w.setProperty("t3w property1", "t3w-value1");
        t3w.setProperty("t3w property2", "t3w-value2");
        t3w.addAlias("t3w-alias1");
        t3w.addAlias("t3w-alias2");
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("test", "test reportNode").build();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network, reportNode);

        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
                + test reportNode
                   + Replaced ThreeWindingsTransformer by 3 TwoWindingsTransformers
                      ThreeWindingsTransformer 3WT removed
                      Property [t3w property2,t3w property1] of threeWindingsTransformer 3WT will be lost
                      Alias [t3w-alias1,t3w-alias2] of threeWindingsTransformer 3WT will be lost
                      VoltageLevel 3WT-Star-VL created
                      TwoWindingsTransformer 3WT-Leg1 created
                      TwoWindingsTransformer 3WT-Leg2 created
                      TwoWindingsTransformer 3WT-Leg3 created
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
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
