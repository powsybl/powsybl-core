/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
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
    void replaceSelectedTwoWindingsTransformerOneTest() {
        assertTrue(replaceSelectedTwoWindingsTransformerTest("3WT-Leg1"));
    }

    @Test
    void replaceSelectedTwoWindingsTransformerTwoTest() {
        assertTrue(replaceSelectedTwoWindingsTransformerTest("3WT-Leg2"));
    }

    @Test
    void replaceSelectedTwoWindingsTransformerThreeTest() {
        assertTrue(replaceSelectedTwoWindingsTransformerTest("3WT-Leg3"));
    }

    private boolean replaceSelectedTwoWindingsTransformerTest(String t2wId) {
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers(Collections.singletonList(t2wId));
        replace.apply(network);

        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(0, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        return true;
    }

    @Test
    void nonReplacementTest() {
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());

        String t2wId = "unknown twoWindingsTransformer";
        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers(Collections.singletonList(t2wId));
        replace.apply(network);

        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getTwoWindingsTransformerCount());
        assertEquals(0, network.getThreeWindingsTransformerCount());
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
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
    void replaceDisconnectedNodeBreakerTest() {
        // In node/breaker topology:
        // One of the 3 two-winding transformers ("3WT-Leg3") is inverted + its switch is opened and not retained
        // => no bus in the bus view on side 2 for this transformer. The "well oriented" detection should not fail.
        Network n = NetworkFactory.findDefault().createNetwork("test", "test");
        Substation substation = n.newSubstation().setId("SUBSTATION").setCountry(Country.FR).add();
        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("vl1")
                .setNominalV(132.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("bbsVl1")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView().newSwitch()
                .setId("sw1")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(false)
                .setNode1(0).setNode2(1)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("vl2")
                .setNominalV(33.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("bbsVl2")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("sw2")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(false)
                .setNode1(0).setNode2(1)
                .add();

        VoltageLevel vl3 = substation.newVoltageLevel()
                .setId("vl3")
                .setNominalV(11.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl3.getNodeBreakerView().newBusbarSection()
                .setId("bbsVl3")
                .setNode(0)
                .add();
        vl3.getNodeBreakerView().newSwitch()
                .setId("sw3")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(true) // Opened and not retained
                .setNode1(0).setNode2(1)
                .add();

        VoltageLevel vlStar = substation.newVoltageLevel()
                .setId("vlStar")
                .setNominalV(20.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vlStar.getNodeBreakerView().newBusbarSection()
                .setId("bbsStar")
                .setNode(0)
                .add();
        vlStar.getNodeBreakerView().newSwitch()
                .setId("swStar1")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(false)
                .setNode1(0).setNode2(1)
                .add();
        vlStar.getNodeBreakerView().newSwitch()
                .setId("swStar2")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(false)
                .setNode1(0).setNode2(2)
                .add();
        vlStar.getNodeBreakerView().newSwitch()
                .setId("swStar3")
                .setKind(SwitchKind.BREAKER).setRetained(false)
                .setOpen(false)
                .setNode1(0).setNode2(3)
                .add();

        substation.newTwoWindingsTransformer()
                .setId("3WT-Leg1")
                .setR(17.424).setX(1.7424).setG(0.00573921028466483).setB(0.000573921028466483)
                .setVoltageLevel1(vl1.getId()).setRatedU1(132.0).setNode1(1)
                .setVoltageLevel2(vlStar.getId()).setRatedU2(20.0).setNode2(1)
                .add();
        substation.newTwoWindingsTransformer()
                .setId("3WT-Leg2")
                .setR(1.089).setX(0.1089).setG(0.0).setB(0.0)
                .setVoltageLevel1(vl2.getId()).setRatedU1(33.0).setNode1(1)
                .setVoltageLevel2(vlStar.getId()).setRatedU2(20.0).setNode2(2)
                .add();
        substation.newTwoWindingsTransformer()
                .setId("3WT-Leg3")
                .setR(0.121).setX(0.0121).setG(0.0).setB(0.0)
                // Inverted
                .setVoltageLevel1(vlStar.getId()).setRatedU1(20.0).setNode1(3)
                .setVoltageLevel2(vl3.getId()).setRatedU2(11.0).setNode2(1)
                .add();

        Replace3TwoWindingsTransformersByThreeWindingsTransformers replace = new Replace3TwoWindingsTransformersByThreeWindingsTransformers();
        replace.apply(n);

        assertEquals(3, n.getVoltageLevelCount());
        assertEquals(0, n.getTwoWindingsTransformerCount());
        assertEquals(1, n.getThreeWindingsTransformerCount());
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
