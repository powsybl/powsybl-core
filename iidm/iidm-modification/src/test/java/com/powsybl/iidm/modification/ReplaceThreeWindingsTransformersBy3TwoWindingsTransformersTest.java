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
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static com.powsybl.iidm.modification.TransformersTestUtils.*;
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
        Network expectedNetwork = ThreeWindingsTransformerNetworkFactory.create();
        network = ThreeWindingsTransformerNetworkFactory.create();
        assertEquals(1, expectedNetwork.getThreeWindingsTransformerCount());
        t3w = expectedNetwork.getThreeWindingsTransformer("3WT");
        assertNotNull(t3w);
    }

    @Test
    void replaceBasicTest() {
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
    void replaceRatioTapChangerTest() {
        assertNull(t3w.getLeg1().getRatioTapChanger());
        assertNotNull(t3w.getLeg2().getRatioTapChanger());
        assertNotNull(t3w.getLeg3().getRatioTapChanger());

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);

        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        assertNull(t2w1.getRatioTapChanger());
        assertNotNull(t2w2.getRatioTapChanger());
        assertNotNull(t2w3.getRatioTapChanger());

        assertTrue(compareRatioTapChanger(t3w.getLeg2().getRatioTapChanger(), t2w2.getRatioTapChanger()));
        assertTrue(compareRatioTapChanger(t3w.getLeg3().getRatioTapChanger(), t2w3.getRatioTapChanger()));
    }

    @Test
    void replacePhaseTapChangerTest() {
        modifyNetworkForPhaseTapChangerTest();

        assertNull(t3w.getLeg1().getPhaseTapChanger());
        assertNotNull(t3w.getLeg2().getPhaseTapChanger());
        assertNull(t3w.getLeg3().getPhaseTapChanger());

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);

        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        assertNull(t2w1.getPhaseTapChanger());
        assertNotNull(t2w2.getPhaseTapChanger());
        assertNull(t2w3.getPhaseTapChanger());

        assertTrue(comparePhaseTapChanger(t3w.getLeg2().getPhaseTapChanger(), t2w2.getPhaseTapChanger()));
    }

    private void modifyNetworkForPhaseTapChangerTest() {
        addPhaseTapChanger(t3w.getLeg2()); // modify expected network
        addPhaseTapChanger(network.getThreeWindingsTransformer(t3w.getId()).getLeg2()); // modify network
    }

    @Test
    void replaceLoadingLimitsTest() {
        modifyNetworkForLoadingLimitsTest();

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);

        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        assertTrue(compareOperationalLimitsGroups(t3w.getLeg1().getOperationalLimitsGroups(), t2w1.getOperationalLimitsGroups1()));
        assertTrue(compareOperationalLimitsGroups(t3w.getLeg2().getOperationalLimitsGroups(), t2w2.getOperationalLimitsGroups1()));
        assertTrue(compareOperationalLimitsGroups(t3w.getLeg3().getOperationalLimitsGroups(), t2w3.getOperationalLimitsGroups1()));
    }

    private void modifyNetworkForLoadingLimitsTest() {
        addLoadingLimits(t3w.getLeg1());
        addLoadingLimits(t3w.getLeg2());
        addLoadingLimits(t3w.getLeg3());
        addLoadingLimits(network.getThreeWindingsTransformer(t3w.getId()).getLeg1());
        addLoadingLimits(network.getThreeWindingsTransformer(t3w.getId()).getLeg2());
        addLoadingLimits(network.getThreeWindingsTransformer(t3w.getId()).getLeg3());
    }

    @Test
    void testReportNode() throws IOException {
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("t3w property1", "t3w-value1");
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("t3w property2", "t3w-value2");
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("t3w-alias1");
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("t3w-alias2");
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
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("unknown property", "unknown value");
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
