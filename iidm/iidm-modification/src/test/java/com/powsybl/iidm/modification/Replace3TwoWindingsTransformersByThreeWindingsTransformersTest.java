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
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static com.powsybl.iidm.modification.TransformersTestUtils.*;
import static com.powsybl.iidm.modification.TransformersTestUtils.addPhaseTapChanger;
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
        Network expectedNetwork = createSetUpNetwork();
        network = createSetUpNetwork();

        assertEquals(3, network.getTwoWindingsTransformerCount());
        t2w1 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg1");
        assertNotNull(t2w1);
        t2w2 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg2");
        assertNotNull(t2w2);
        t2w3 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg3");
        assertNotNull(t2w3);
    }

    private static Network createSetUpNetwork() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        return network;
    }

    @Test
    void replaceBasicTest() {
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
    void replaceRatioTapChangerTest() {
        assertNull(t2w1.getRatioTapChanger());
        assertNotNull(t2w2.getRatioTapChanger());
        assertNotNull(t2w3.getRatioTapChanger());

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        assertNull(t3w.getLeg1().getRatioTapChanger());
        assertNotNull(t3w.getLeg2().getRatioTapChanger());
        assertNotNull(t3w.getLeg3().getRatioTapChanger());

        assertTrue(compareRatioTapChanger(t2w2.getRatioTapChanger(), t3w.getLeg2().getRatioTapChanger()));
        assertTrue(compareRatioTapChanger(t2w3.getRatioTapChanger(), t3w.getLeg3().getRatioTapChanger()));
    }

    @Test
    void replacePhaseTapChangerTest() {
        modifyNetworkForPhaseTapChangerTest();

        assertNotNull(t2w1.getPhaseTapChanger());
        assertNull(t2w2.getPhaseTapChanger());
        assertNull(t2w3.getPhaseTapChanger());

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        assertNotNull(t2w1.getPhaseTapChanger());
        assertNull(t2w2.getPhaseTapChanger());
        assertNull(t2w3.getPhaseTapChanger());

        assertTrue(comparePhaseTapChanger(t2w1.getPhaseTapChanger(), t3w.getLeg1().getPhaseTapChanger()));
    }

    private void modifyNetworkForPhaseTapChangerTest() {
        addPhaseTapChanger(t2w1); // modify expected network
        addPhaseTapChanger(network.getTwoWindingsTransformer(t2w1.getId())); // modify network
    }

    @Test
    void replaceLoadingLimitsTest() {
        modifyNetworkForLoadingLimitsTest();

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        assertTrue(compareOperationalLimitsGroups(t2w1.getOperationalLimitsGroups1(), t3w.getLeg1().getOperationalLimitsGroups()));
        assertTrue(compareOperationalLimitsGroups(t2w2.getOperationalLimitsGroups1(), t3w.getLeg2().getOperationalLimitsGroups()));
        assertTrue(compareOperationalLimitsGroups(t2w3.getOperationalLimitsGroups1(), t3w.getLeg3().getOperationalLimitsGroups()));
    }

    private void modifyNetworkForLoadingLimitsTest() {
        addLoadingLimits(t2w1);
        addLoadingLimits(t2w2);
        addLoadingLimits(t2w3);
        addLoadingLimits(network.getTwoWindingsTransformer(t2w1.getId()));
        addLoadingLimits(network.getTwoWindingsTransformer(t2w2.getId()));
        addLoadingLimits(network.getTwoWindingsTransformer(t2w3.getId()));
    }

    @Test
    void replaceAliasTest() {
        addTwoWindingsTransformerAliases(network.getTwoWindingsTransformer(t2w1.getId()));
        addTwoWindingsTransformerAliases(network.getTwoWindingsTransformer(t2w2.getId()));
        addTwoWindingsTransformerAliases(network.getTwoWindingsTransformer(t2w3.getId()));

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        assertTrue(compareAliases(t3w, "1", t2w1));
        assertTrue(compareAliases(t3w, "2", t2w2));
        assertTrue(compareAliases(t3w, "3", t2w3));
    }

    private void addTwoWindingsTransformerAliases(TwoWindingsTransformer t2w) {
        t2w.addAlias("transformerEnd-" + t2w.getId(), "CGMES.TransformerEnd1");
        t2w.addAlias("terminal-" + t2w.getId(), "CGMES.Terminal1");
        t2w.addAlias("ratioTapChanger-" + t2w.getId(), "CGMES.RatioTapChanger1");
        t2w.addAlias("phaseTapChanger-" + t2w.getId(), "CGMES.PhaseTapChanger1");
    }

    private boolean compareAliases(ThreeWindingsTransformer t3w, String leg, TwoWindingsTransformer t2w) {
        return t3w.getAliasFromType("CGMES.TransformerEnd" + leg).orElseThrow().equals("transformerEnd-" + t2w.getId())
                && t3w.getAliasFromType("CGMES.Terminal" + leg).orElseThrow().equals("terminal-" + t2w.getId())
                && t3w.getAliasFromType("CGMES.RatioTapChanger" + leg).orElseThrow().equals("ratioTapChanger-" + t2w.getId())
                && t3w.getAliasFromType("CGMES.PhaseTapChanger" + leg).orElseThrow().equals("phaseTapChanger-" + t2w.getId());
    }

    @Test
    void testReportNode() throws IOException {
        network.getTwoWindingsTransformer(t2w1.getId()).setProperty("t2w1 property1", "t2w1-value1");
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("t2w2 property1", "t2w2-value1");
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("t2w2 property2", "t2w2-value2");
        network.getTwoWindingsTransformer(t2w3.getId()).setProperty("t2w3 property1", "t2w3-value1");
        network.getTwoWindingsTransformer(t2w1.getId()).addAlias("t2w1-alias1");
        network.getTwoWindingsTransformer(t2w1.getId()).addAlias("t2w1-alias2");
        network.getTwoWindingsTransformer(t2w2.getId()).addAlias("t2w2-alias1");
        network.getTwoWindingsTransformer(t2w3.getId()).addAlias("t2w3-alias1");
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
        network.getTwoWindingsTransformer(t2w1.getId()).setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testApplyChecks2() {
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void testApplyChecks3() {
        network.getTwoWindingsTransformer(t2w3.getId()).setProperty("unknown property", "unknown value");
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
