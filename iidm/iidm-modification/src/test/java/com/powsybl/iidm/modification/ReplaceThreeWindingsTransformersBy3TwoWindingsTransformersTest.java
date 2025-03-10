/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import static com.powsybl.iidm.modification.TransformersTestUtils.*;
import static com.powsybl.iidm.modification.util.ModificationReports.lostThreeWindingsTransformerExtensions;
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
        assertEquals(1, expectedNetwork.getThreeWindingsTransformerCount());
        t3w = expectedNetwork.getThreeWindingsTransformer("3WT");
        network = ThreeWindingsTransformerNetworkFactory.create();
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
    void replaceSelectedThreeWindingsTransformerTest() {
        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());

        String t3wId = network.getThreeWindingsTransformers().iterator().next().getId();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers(Collections.singletonList(t3wId));
        replace.apply(network);

        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
    }

    @Test
    void nonReplacementTest() {
        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());

        String t3wId = "unknown threeWindingsTransformer";
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers(Collections.singletonList(t3wId));
        replace.apply(network);

        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
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
    void replacePropertiesTest() {
        modifyNetworkForPropertiesTest();
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("v", "132.5");
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("angle", "2.5");
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-summer", "summer");
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-winter", "winter");

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");

        assertEquals(132.5, t2w1.getTerminal2().getBusView().getBus().getV());
        assertEquals(2.5, t2w1.getTerminal2().getBusView().getBus().getAngle());
        assertEquals("summer", t2w1.getProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-summer"));
        assertEquals("winter", t2w1.getProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-winter"));
    }

    private void modifyNetworkForPropertiesTest() {
        addLoadingLimits(t3w.getLeg1());
        addLoadingLimits(network.getThreeWindingsTransformer(t3w.getId()).getLeg1());
    }

    @Test
    void replaceExtensionsTest() {
        modifyNetworkForExtensionsTest();

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        assertEquals(createThreeWindingsTransformerFortescueToString(t3w), createTwoWindingsTransformerFortescueToString(t2w1, t2w2, t2w3));
        assertEquals(createThreeWindingsTransformerPhaseAngleClockToString(t3w), createTwoWindingsTransformerPhaseAngleClockToString(t2w2, t2w3));
        assertEquals(createThreeWindingsTransformerToBeEstimatedToString(t3w), createTwoWindingsTransformerToBeEstimatedToString(t2w1, t2w2, t2w3));
    }

    private void modifyNetworkForExtensionsTest() {
        addExtensions(t3w);
        addExtensions(network.getThreeWindingsTransformer(t3w.getId()));
    }

    @Test
    void replaceAliasesTest() {
        addLegAliases("1");
        addLegAliases("2");
        addLegAliases("3");

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        assertTrue(compareAliases(t2w1, "1"));
        assertTrue(compareAliases(t2w2, "2"));
        assertTrue(compareAliases(t2w3, "3"));
    }

    private void addLegAliases(String leg) {
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("transformerEnd-Leg" + leg, "CGMES.TransformerEnd" + leg);
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("terminal-Leg" + leg, "CGMES.Terminal" + leg);
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("ratioTapChanger-Leg" + leg, "CGMES.RatioTapChanger" + leg);
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("phaseTapChanger-Leg" + leg, "CGMES.PhaseTapChanger" + leg);
    }

    private boolean compareAliases(TwoWindingsTransformer t2w, String leg) {
        return t2w.getAliasFromType("CGMES.TransformerEnd1").orElseThrow().equals("transformerEnd-Leg" + leg)
                && t2w.getAliasFromType("CGMES.Terminal1").orElseThrow().equals("terminal-Leg" + leg)
                && t2w.getAliasFromType("CGMES.RatioTapChanger1").orElseThrow().equals("ratioTapChanger-Leg" + leg)
                && t2w.getAliasFromType("CGMES.PhaseTapChanger1").orElseThrow().equals("phaseTapChanger-Leg" + leg);
    }

    @Test
    void reportNodeTest() throws IOException {
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("t3w property1", "t3w-value1");
        network.getThreeWindingsTransformer(t3w.getId()).setProperty("t3w property2", "t3w-value2");
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("t3w-alias1");
        network.getThreeWindingsTransformer(t3w.getId()).addAlias("t3w-alias2");
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("test", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network, reportNode);

        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
                + test reportNode
                   + Replaced ThreeWindingsTransformer by 3 TwoWindingsTransformers
                      ThreeWindingsTransformer 3WT removed
                      Alias [t3w-alias1,t3w-alias2] of threeWindingsTransformer 3WT will be lost
                      VoltageLevel 3WT-Star-VL created
                      TwoWindingsTransformer 3WT-Leg1 created
                      TwoWindingsTransformer 3WT-Leg2 created
                      TwoWindingsTransformer 3WT-Leg3 created
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void reportNodeExtensionsTest() throws IOException {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("test", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        lostThreeWindingsTransformerExtensions(reportNode, "unknownExtension1, unknownExtension2", t3w.getId());

        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
                + test reportNode
                   Extension [unknownExtension1, unknownExtension2] of threeWindingsTransformer 3WT will be lost
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void replaceFlowsTest() {
        modifyNetworkForFlowsTest();

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);

        TwoWindingsTransformer t2w1 = network.getTwoWindingsTransformer("3WT-Leg1");
        TwoWindingsTransformer t2w2 = network.getTwoWindingsTransformer("3WT-Leg2");
        TwoWindingsTransformer t2w3 = network.getTwoWindingsTransformer("3WT-Leg3");

        TwtData t3wData = new TwtData(t3w, 0.0, false);
        setStarBusVoltage(t3wData, t2w1.getTerminal2().getBusView().getBus());

        BranchData t2w1Data = new BranchData(t2w1, 0.0, false, false);
        BranchData t2w2Data = new BranchData(t2w2, 0.0, false, false);
        BranchData t2w3Data = new BranchData(t2w3, 0.0, false, false);

        double tol = 0.000001;
        assertEquals(t3wData.getComputedP(ThreeSides.ONE), t2w1Data.getComputedP1(), tol);
        assertEquals(t3wData.getComputedP(ThreeSides.TWO), t2w2Data.getComputedP1(), tol);
        assertEquals(t3wData.getComputedP(ThreeSides.THREE), t2w3Data.getComputedP1(), tol);
    }

    private void modifyNetworkForFlowsTest() {
        addVoltages(t3w.getLeg1().getTerminal().getBusView().getBus(), t3w.getLeg2().getTerminal().getBusView().getBus(), t3w.getLeg3().getTerminal().getBusView().getBus());
        addVoltages(network.getThreeWindingsTransformer(t3w.getId()).getLeg1().getTerminal().getBusView().getBus(),
                network.getThreeWindingsTransformer(t3w.getId()).getLeg2().getTerminal().getBusView().getBus(),
                network.getThreeWindingsTransformer(t3w.getId()).getLeg3().getTerminal().getBusView().getBus());
    }

    @Test
    void replaceNodeBreakerTest() {
        modifyNetworkForNodeBreakerTest();

        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());

        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);

        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
    }

    private void modifyNetworkForNodeBreakerTest() {
        Network expectedNetwork = createThreeWindingsTransformerNodeBreakerNetwork();
        t3w = expectedNetwork.getThreeWindingsTransformer("3WT");
        network = createThreeWindingsTransformerNodeBreakerNetwork();
    }

    @Test
    void getNameTest() {
        AbstractNetworkModification networkModification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals("ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers", networkModification.getName());
    }

    @Test
    void hasImpactTest() {
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers modification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }
}
