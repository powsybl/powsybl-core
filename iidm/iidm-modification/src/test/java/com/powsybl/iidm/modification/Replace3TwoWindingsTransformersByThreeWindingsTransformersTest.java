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
import com.powsybl.iidm.network.TwoWindingsTransformer;
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
class Replace3TwoWindingsTransformersByThreeWindingsTransformersTest {

    private Network network;
    private TwoWindingsTransformer t2w1;
    private TwoWindingsTransformer t2w2;
    private TwoWindingsTransformer t2w3;

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(1, network.getThreeWindingsTransformerCount());
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        assertEquals(3, network.getTwoWindingsTransformerCount());
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
    void testReportNode() throws IOException {
        t2w1.setProperty("t2w1 property1", "t2w1-value1");
        t2w2.setProperty("t2w2 property1", "t2w2-value1");
        t2w2.setProperty("t2w2 property2", "t2w2-value2");
        t2w3.setProperty("t2w3 property1", "t2w3-value1");
        t2w1.addAlias("t2w1-alias1");
        t2w1.addAlias("t2w1-alias2");
        t2w2.addAlias("t2w2-alias1");
        t2w3.addAlias("t2w3-alias1");
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("test", "test reportNode").build();
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network, reportNode);

        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
                + test reportNode
                   + Replaced 3 TwoWindingsTransformers by ThreeWindingsTransformer
                      TwoWindingsTransformer 3WT-Leg1 removed
                      TwoWindingsTransformer 3WT-Leg2 removed
                      TwoWindingsTransformer 3WT-Leg3 removed
                      Voltage level 3WT-Star-VL, its equipments and the branches it is connected to have been removed
                      Property [t2w1 property1] of twoWindingsTransformer 3WT-Leg1 will be lost
                      Property [t2w2 property1,t2w2 property2] of twoWindingsTransformer 3WT-Leg2 will be lost
                      Property [t2w3 property1] of twoWindingsTransformer 3WT-Leg3 will be lost
                      Alias [t2w1-alias2,t2w1-alias1] of twoWindingsTransformer 3WT-Leg1 will be lost
                      Alias [t2w2-alias1] of twoWindingsTransformer 3WT-Leg2 will be lost
                      Alias [t2w3-alias1] of twoWindingsTransformer 3WT-Leg3 will be lost
                      ThreeWindingsTransformer 3WT-Leg1-3WT-Leg2-3WT-Leg3 created
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void testApplyChecks1() {
        t2w1.setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testApplyChecks2() {
        t2w2.setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testApplyChecks3() {
        t2w3.setProperty("unknown property", "unknown value");
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
