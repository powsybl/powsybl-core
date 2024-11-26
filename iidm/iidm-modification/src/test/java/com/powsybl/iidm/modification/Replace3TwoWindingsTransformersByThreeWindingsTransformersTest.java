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
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static com.powsybl.iidm.modification.TransformersTestUtils.*;
import static com.powsybl.iidm.modification.TransformersTestUtils.addPhaseTapChanger;
import static com.powsybl.iidm.modification.util.ModificationReports.lostTwoWindingsTransformerExtensions;
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
        assertEquals(3, expectedNetwork.getTwoWindingsTransformerCount());
        t2w1 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg1");
        t2w2 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg2");
        t2w3 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg3");
        network = createSetUpNetwork();
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

        assertNotNull(t3w.getLeg1().getPhaseTapChanger());
        assertNull(t3w.getLeg2().getPhaseTapChanger());
        assertNull(t3w.getLeg3().getPhaseTapChanger());

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
        addLoadingLimitsEnd1(t2w1);
        addLoadingLimitsEnd1(t2w2);
        addLoadingLimitsEnd1(t2w3);
        addLoadingLimitsEnd1(network.getTwoWindingsTransformer(t2w1.getId()));
        addLoadingLimitsEnd1(network.getTwoWindingsTransformer(t2w2.getId()));
        addLoadingLimitsEnd1(network.getTwoWindingsTransformer(t2w3.getId()));
    }

    @Test
    void replacePropertiesT2w1Test() {
        assertTrue(replacePropertiesTwoWindingsTransformer(t2w1));
    }

    @Test
    void replacePropertiesT2w2Test() {
        assertTrue(replacePropertiesTwoWindingsTransformer(t2w2));
    }

    @Test
    void replacePropertiesT2w3Test() {
        assertTrue(replacePropertiesTwoWindingsTransformer(t2w3));
    }

    private boolean replacePropertiesTwoWindingsTransformer(TwoWindingsTransformer t2w) {
        modifyNetworkForPropertiesTest(t2w);
        network.getTwoWindingsTransformer(t2w.getId()).setProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-summer", "summer");
        network.getTwoWindingsTransformer(t2w.getId()).setProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-winter", "winter");

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        return t3w.getProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-summer").equals("summer") &&
                t3w.getProperty("CGMES.OperationalLimitSet_" + "OperationalLimitsGroup-winter").equals("winter");
    }

    private void modifyNetworkForPropertiesTest(TwoWindingsTransformer t2w) {
        addLoadingLimitsEnd1(t2w);
        addLoadingLimitsEnd1(network.getTwoWindingsTransformer(t2w.getId()));
    }

    @Test
    void replaceExtensionsTest() {
        modifyNetworkForExtensionsTest();

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        assertEquals(createTwoWindingsTransformerFortescueToString(t2w1, t2w2, t2w3), createThreeWindingsTransformerFortescueToString(t3w));
        assertEquals(createTwoWindingsTransformerPhaseAngleClockToString(t2w2, t2w3), createThreeWindingsTransformerPhaseAngleClockToString(t3w));
        assertEquals(createTwoWindingsTransformerToBeEstimatedToString(t2w1, t2w2, t2w3), createThreeWindingsTransformerToBeEstimatedToString(t3w));
    }

    private void modifyNetworkForExtensionsTest() {
        addExtensions(t2w1, 1);
        addExtensions(t2w2, 2);
        addExtensions(t2w3, 3);
        addExtensions(network.getTwoWindingsTransformer(t2w1.getId()), 1);
        addExtensions(network.getTwoWindingsTransformer(t2w2.getId()), 2);
        addExtensions(network.getTwoWindingsTransformer(t2w3.getId()), 3);
    }

    @Test
    void replaceAliasesTest() {
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
    void reportNodeTest() throws IOException {
        network.getTwoWindingsTransformer(t2w1.getId()).setProperty("t2w1 property1", "t2w1-value1");
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("t2w2 property1", "t2w2-value1");
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("t2w2 property2", "t2w2-value2");
        network.getTwoWindingsTransformer(t2w3.getId()).setProperty("t2w3 property1", "t2w3-value1");
        network.getTwoWindingsTransformer(t2w1.getId()).addAlias("t2w1-alias1");
        network.getTwoWindingsTransformer(t2w1.getId()).addAlias("t2w1-alias2");
        network.getTwoWindingsTransformer(t2w2.getId()).addAlias("t2w2-alias1");
        network.getTwoWindingsTransformer(t2w3.getId()).addAlias("t2w3-alias1");
        network.getTwoWindingsTransformer(t2w1.getId()).addAlias("t2w1-alias2");
        addLoadingLimitsEnd2(network.getTwoWindingsTransformer(t2w1.getId()));
        addLoadingLimitsEnd2(network.getTwoWindingsTransformer(t2w2.getId()));
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
                      Alias [t2w1-alias2,t2w1-alias1] of twoWindingsTransformer 3WT-Leg1 will be lost
                      Alias [t2w2-alias1] of twoWindingsTransformer 3WT-Leg2 will be lost
                      Alias [t2w3-alias1] of twoWindingsTransformer 3WT-Leg3 will be lost
                      OperationalLimitsGroups [OperationalLimitsGroup-summer-end2,OperationalLimitsGroup-winter-end2] of twoWindingsTransformer 3WT-Leg1 will be lost
                      OperationalLimitsGroups [OperationalLimitsGroup-summer-end2,OperationalLimitsGroup-winter-end2] of twoWindingsTransformer 3WT-Leg2 will be lost
                      ThreeWindingsTransformer 3WT-Leg1-3WT-Leg2-3WT-Leg3 created
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void reportNodeExtensionsTest() throws IOException {
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("test", "test reportNode").build();
        lostTwoWindingsTransformerExtensions(reportNode, "unknownExtension1, unknownExtension2", t2w1.getId());

        StringWriter sw1 = new StringWriter();
        reportNode.print(sw1);
        assertEquals("""
                + test reportNode
                   Extension [unknownExtension1, unknownExtension2] of twoWindingsTransformer 3WT-Leg1 will be lost
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void replaceFlowsTest() {
        modifyNetworkForFlowsTest();
        assertTrue(checkFlowsTest());
    }

    private void modifyNetworkForFlowsTest() {
        addVoltages(t2w1.getTerminal1().getBusView().getBus(), t2w2.getTerminal1().getBusView().getBus(), t2w3.getTerminal1().getBusView().getBus());
        addVoltages(network.getTwoWindingsTransformer(t2w1.getId()).getTerminal1().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w2.getId()).getTerminal1().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w3.getId()).getTerminal1().getBusView().getBus());
    }

    @Test
    void replaceFlowsRatedU2Test() {
        modifyNetworkForFlowsRatedU2Test();
        assertTrue(checkFlowsTest());
    }

    private void modifyNetworkForFlowsRatedU2Test() {
        t2w1.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.99);
        t2w2.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 1.02);
        t2w3.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.98);

        network.getTwoWindingsTransformer(t2w1.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.99);
        network.getTwoWindingsTransformer(t2w2.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 1.02);
        network.getTwoWindingsTransformer(t2w3.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.98);

        addVoltages(t2w1.getTerminal1().getBusView().getBus(), t2w2.getTerminal1().getBusView().getBus(), t2w3.getTerminal1().getBusView().getBus());
        addVoltages(network.getTwoWindingsTransformer(t2w1.getId()).getTerminal1().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w2.getId()).getTerminal1().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w3.getId()).getTerminal1().getBusView().getBus());
    }

    private boolean checkFlowsTest() {
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-3WT-Leg3");

        TwtData t3wData = new TwtData(t3w, 0.0, false);
        setStarBusVoltage(t3wData, t2w1.getTerminal2().getBusView().getBus());

        BranchData t2w1Data = new BranchData(t2w1, 0.0, false, false);
        BranchData t2w2Data = new BranchData(t2w2, 0.0, false, false);
        BranchData t2w3Data = new BranchData(t2w3, 0.0, false, false);

        double tol = 0.000001;
        assertEquals(t2w1Data.getComputedP1(), t3wData.getComputedP(ThreeSides.ONE), tol);
        assertEquals(t2w2Data.getComputedP1(), t3wData.getComputedP(ThreeSides.TWO), tol);
        assertEquals(t2w3Data.getComputedP1(), t3wData.getComputedP(ThreeSides.THREE), tol);
        return true;
    }

    @Test
    void replaceFlowsNotWellOrientedTest() {
        modifyNetworkForFlowsNotWellOrientedTest();
        assertTrue(checkFlowsTestNotWellOriented());
    }

    private void modifyNetworkForFlowsNotWellOrientedTest() {
        t2w1.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.99);
        t2w2.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 1.02);
        t2w3.setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.98);

        network.getTwoWindingsTransformer(t2w1.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.99);
        network.getTwoWindingsTransformer(t2w2.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 1.02);
        network.getTwoWindingsTransformer(t2w3.getId()).setRatedU2(t2w1.getTerminal2().getVoltageLevel().getNominalV() * 0.98);

        addPhaseTapChanger(t2w2);
        addPhaseTapChanger(network.getTwoWindingsTransformer(t2w2.getId()));

        String tw2Id = t2w2.getId();
        reOrientedTwoWindingsTransformer(t2w2);
        reOrientedTwoWindingsTransformer(network.getTwoWindingsTransformer(tw2Id));
        t2w2 = t2w1.getNetwork().getTwoWindingsTransformer("3WT-Leg2-notWellOriented");

        addVoltages(t2w1.getTerminal1().getBusView().getBus(), t2w2.getTerminal2().getBusView().getBus(), t2w3.getTerminal1().getBusView().getBus());
        addVoltages(network.getTwoWindingsTransformer(t2w1.getId()).getTerminal1().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w2.getId()).getTerminal2().getBusView().getBus(),
                network.getTwoWindingsTransformer(t2w3.getId()).getTerminal1().getBusView().getBus());
    }

    private boolean checkFlowsTestNotWellOriented() {
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT-Leg1-3WT-Leg2-notWellOriented-3WT-Leg3");

        TwtData t3wData = new TwtData(t3w, 0.0, false);
        setStarBusVoltage(t3wData, t2w1.getTerminal2().getBusView().getBus());

        BranchData t2w1Data = new BranchData(t2w1, 0.0, false, false);
        BranchData t2w2Data = new BranchData(t2w2, 0.0, false, false);
        BranchData t2w3Data = new BranchData(t2w3, 0.0, false, false);

        double tol = 0.000001;
        assertEquals(t2w1Data.getComputedP1(), t3wData.getComputedP(ThreeSides.ONE), tol);
        assertEquals(t2w2Data.getComputedP2(), t3wData.getComputedP(ThreeSides.TWO), tol);
        assertEquals(t2w3Data.getComputedP1(), t3wData.getComputedP(ThreeSides.THREE), tol);
        return true;
    }

    @Test
    void replaceNodeBreakerTest() {
        modifyNetworkForNodeBreakerTest();

        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(network);

        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
    }

    private void modifyNetworkForNodeBreakerTest() {
        Network expectedNetwork = createThreeWindingsTransformerNodeBreakerNetwork();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replaceExpected = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replaceExpected.apply(expectedNetwork);
        t2w1 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg1");
        assertNotNull(t2w1);
        t2w2 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg2");
        assertNotNull(t2w2);
        t2w3 = expectedNetwork.getTwoWindingsTransformer("3WT-Leg3");

        network = createThreeWindingsTransformerNodeBreakerNetwork();
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers replace = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        replace.apply(network);
    }

    @Test
    void applyChecks2Test() {
        network.getTwoWindingsTransformer(t2w1.getId()).setProperty("unknown property", "unknown value");
        network.getTwoWindingsTransformer(t2w2.getId()).setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void applyChecks3Test() {
        network.getTwoWindingsTransformer(t2w1.getId()).setProperty("unknown property", "unknown value");
        network.getTwoWindingsTransformer(t2w3.getId()).setProperty("unknown property", "unknown value");
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertThrows(PowsyblException.class, () -> replace.apply(network, true, ReportNode.NO_OP),
                "An unknown property should fail to apply.");
        assertDoesNotThrow(() -> replace.apply(network, false, ReportNode.NO_OP),
                "An unknown property should not throw if throwException is false.");
    }

    @Test
    void getNameTest() {
        AbstractNetworkModification networkModification = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        assertEquals("Replace3TwoWindingsTransformersByThreeWindingsTransformers", networkModification.getName());
    }

    @Test
    void hasImpactTest() {
        ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers modification = new ReplaceThreeWindingsTransformersBy3TwoWindingsTransformers();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification.hasImpactOnNetwork(network));
    }
}
