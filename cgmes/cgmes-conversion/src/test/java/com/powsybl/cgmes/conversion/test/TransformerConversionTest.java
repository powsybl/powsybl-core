/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.Conversion.*;
import com.powsybl.cgmes.conversion.PhaseAngleClock;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class TransformerConversionTest {

    private static final String EQUALS_LINE = "======================";
    private static final String DIR = "/issues/transformers/";

    @Test
    void t2wShuntEnd1Test() {
        // All shunt admittances to ground (g, b) at end1 (before transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
    }

    @Test
    void t2wShuntEnd2Test() {
        // All shunt admittances to ground (g, b) at end2 (after transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END2);
        Network n = networkModel(twoWindingsTransformers(), config);

        // Same result as end1, IIDM model and LoadFlowParameters don't allow this configuration
        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
    }

    @Test
    void t2wShuntEnd1End2Test() {
        // Shunt admittances to ground (g, b) at the end where they are defined in CGMES model
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1_END2);
        Network n = networkModel(twoWindingsTransformers(), config);

        // Same result as end1, IIDM model and LoadFlowParameters don't allow this configuration
        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
    }

    @Test
    void t2wShuntSplitTest() {
        // Split shunt admittances to ground (g, b) between end1 and end2.
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.SPLIT);
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.970891, -15.839366, 94.275697, 20.952066));
    }

    @Test
    void t2wRatioPhaseEnd1Test() {
        // All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end1 (before transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -91.807775, 98.389959, 92.184500, -89.747219));
        assertTrue(t2xCompareFlow(n, "PST", 927.034612, -339.274880, -911.542354, 422.345850));
    }

    @Test
    void t2wRatioPhaseEnd2Test() {
        // All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end2 (after transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END2);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
        assertTrue(t2xCompareFlow(n, "PST", 58.877292, -201.626411, -58.176878, 205.382102));
    }

    @Test
    void t2wRatioPhaseEnd1End2Test() {
        // TapChangers (ratioTapChanger and phaseTapChanger) are considered at the end where they are defined in CGMES
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1_END2);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
        assertTrue(t2xCompareFlow(n, "PST", 927.034612, -339.274880, -911.542354, 422.345850));
    }

    @Test
    void t2wRatioPhaseXTest() {
        // If x1 == 0 all tapChangers (ratioTapChanger and phaseTapChanger) are considered at the end1
        // otherwise they are considered at end2
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.X);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
        assertTrue(t2xCompareFlow(n, "PST", 58.877292, -201.626411, -58.176878, 205.382102));
    }

    @Test
    void t2wRatio0End1Test() {
        // Structural ratio always at end1 (before transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -0.849849, -0.138409, 0.852591, 0.184615));
        assertTrue(t2xCompareFlow(n, "PST", 70.106993, -25.657663, -68.935391, 31.939905));
    }

    @Test
    void t2wRatio0End2Test() {
        // Structural ratio always at end2 (after transmission impedance)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
        assertTrue(t2xCompareFlow(n, "PST", 927.034612, -339.274880, -911.542354, 422.345850));
    }

    @Test
    void t2wRatio0XTest() {
        // If x1 == 0 structural ratio at end1, otherwise at end2
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.X);
        Network n = networkModel(twoWindingsTransformers(), config);

        assertTrue(t2xCompareFlow(n, "T2W", -93.855301, -15.285520, 94.158074, 20.388478));
        assertTrue(t2xCompareFlow(n, "PST", 927.034612, -339.274880, -911.542354, 422.345850));
    }

    @Test
    void t3wShuntNetworkSideTest() {
        // Shunt admittances to ground at the network side (end1 of the leg)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849));
    }

    @Test
    void t3wShuntStarBusSideTest() {
        // Shunt admittances to ground at the start bus side (end2 of the leg)
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849));
    }

    @Test
    void t3wShuntSplitTest() {
        // Split shunt admittances to ground between two ends of the leg
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.SPLIT);
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.231950, 2.876479, -216.194348, -85.558437, 117.981856, 92.439531));
    }

    @Test
    void t3wRatioPhaseNetworkSideTest() {
        // All tapChangers (ratioTapChanger and phaseTapChanger) at the network side
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849));
    }

    @Test
    void t3wRatioPhaseStarBusSideTest() {
        // All tapChangers (ratioTapChanger and phaseTapChanger) at the star bus side
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849));
    }

    @Test
    void t3wRatio0StarBusSideTest() {
        // Structural ratio at the star bus side of all legs and RatedU0 = RatedU1
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849));
    }

    @Test
    void t3wRatio0NetworkSideTest() {
        // Structural ratio at the network side of all legs. RatedU0 = 1 kv
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", -0.000166, -0.000842, -0.006802, -0.004135, 0.006989, 0.005353));
    }

    @Test
    void t3wRatio0End1Test() {
        // Structural ratio at the network side of legs 2 and 3. RatedU0 = RatedU1
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", -26.608872, -134.702971, -1088.277421, -661.570093, 1118.294368, 856.437794));
    }

    @Test
    void t3wRatio0End2Test() {
        // Structural ratio at the network side of legs 1 and 3. RatedU0 = RatedU2
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", -8.049184, -40.747649, -329.203920, -200.124953, 338.284046, 259.072433));
    }

    @Test
    void t3wRatio0End3Test() {
        // Structural ratio at the network side of legs 1 and 2. RatedU0 = RatedU2
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END3);
        Network n = networkModel(threeWindingsTransformer(), config);

        assertTrue(t3xCompareFlow(n, "T3W", -0.073341, -0.371275, -2.999565, -1.823453, 3.082299, 2.360557));
    }

    @Test
    void phaseAngleClockTest() {
        // A 2w- and a 3w-transformer with non-null phase angle clock value on respectively 2nd and 3rd winding
        Network n = networkModel(phaseAngleClock("phaseAngleClock_EQ.xml"), new PhaseAngleClock());

        // Phase angle clock values have been correctly read
        assertEquals(5, n.getTwoWindingsTransformer("T2W").getExtension(TwoWindingsTransformerPhaseAngleClock.class).getPhaseAngleClock());
        assertEquals(0, n.getThreeWindingsTransformer("T3W").getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg2());
        assertEquals(5, n.getThreeWindingsTransformer("T3W").getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg3());

        // Power flows are calculated from SV voltages
        assertTrue(t2xCompareFlow(n, "T2W", -318.691633, 1424.484145, 436.204160, 1393.367311));
        assertTrue(t3xCompareFlow(n, "T3W", -7.505045, -1.896561, -288.380946, 1216.566903, 351.090362, 1199.878285));
    }

    @Test
    void phaseAngleClockAllZeroTest() {
        // A 2w- and a 3w-transformer with all phase angle clock equal to 0
        Network n = networkModel(phaseAngleClock("phaseAngleClock_EQ_AllZero.xml"), new PhaseAngleClock());

        // No phase angle clock extension has been created since all values are equal to 0
        assertNull(n.getTwoWindingsTransformer("T2W").getExtension(TwoWindingsTransformerPhaseAngleClock.class));
        assertNull(n.getThreeWindingsTransformer("T3W").getExtension(ThreeWindingsTransformerPhaseAngleClock.class));

        // Power flows differ from the ones in phaseAngleClockTest()
        assertTrue(t2xCompareFlow(n, "T2W", -0.087780, -0.178561, 0.087782, 0.178613));
        assertTrue(t3xCompareFlow(n, "T3W", -0.000001, -0.000022, 0.000002, 0.000068, -0.000001, -0.000045));
    }

    @Test
    void phaseAngleClockAllNonZeroTest() {
        // A 2w- and a 3w-transformer with non-null phase angle clock value on all windings
        Network n = networkModel(phaseAngleClock("phaseAngleClock_EQ_AllNonZero.xml"), new PhaseAngleClock());

        // Non-null phase angle clock values on 1st winding are discarded, the other ones are correctly read
        assertEquals(5, n.getTwoWindingsTransformer("T2W").getExtension(TwoWindingsTransformerPhaseAngleClock.class).getPhaseAngleClock());
        assertEquals(3, n.getThreeWindingsTransformer("T3W").getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg2());
        assertEquals(5, n.getThreeWindingsTransformer("T3W").getExtension(ThreeWindingsTransformerPhaseAngleClock.class).getPhaseAngleClockLeg3());

        // Power flows differ from the ones in phaseAngleClockTest() when the pac values differ (3w-transformer)
        assertTrue(t2xCompareFlow(n, "T2W", -318.691633, 1424.484145, 436.204160, 1393.367311));
        assertTrue(t3xCompareFlow(n, "T3W", -1494.636083, 1530.638656, 981.686099, 1826.870720, 562.199867, 309.289551));
    }

    @Test
    void microGridBaseCaseBEPhaseTapChangerXMin() {
        Network n = networkModel(Cgmes3ModifiedCatalog.microGridBaseCasePhaseTapChangerXMin(), new Conversion.Config());

        TwoWindingsTransformer twt1 = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        TwoWindingsTransformer twt2 = n.getTwoWindingsTransformer("b94318f6-6d24-4f56-96b9-df2531ad6543");

        assertEquals(1.10949, obtainXcurrentStep(twt1), 0.00001);
        assertEquals(2.796323, obtainXcurrentStep(twt2), 0.00001);
    }

    private static double obtainXcurrentStep(TwoWindingsTransformer twt) {
        double xtx = twt.getX();
        double ptcStepX = twt.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getX()).orElse(0d);
        return xtx * (1 + ptcStepX / 100);
    }

    private boolean t2xCompareFlow(Network n, String id, double p1, double q1, double p2, double q2) {
        TwoWindingsTransformer twt = n.getTwoWindingsTransformer(id);
        T2xFlow actual = twoWindingsTransformerFlow(twt);
        T2xFlow expected = new T2xFlow();
        expected.p1 = p1;
        expected.q1 = q1;
        expected.p2 = p2;
        expected.q2 = q2;
        boolean ok = sameFlow(expected, actual);

        if (!ok && LOG.isErrorEnabled()) {
            LOG.error(String.format("TransformerConversionTest: TwoWindingsTransformer id %s ===", id));
            LOG.error(String.format("Expected P1 %12.6f Q1 %12.6f P2 %12.6f Q2 %12.6f", expected.p1, expected.q1, expected.p2, expected.q2));
            LOG.error(String.format("Actual   P1 %12.6f Q1 %12.6f P2 %12.6f Q2 %12.6f", actual.p1, actual.q1, actual.p2, actual.q2));
            LOG.error(EQUALS_LINE);
        }
        return ok;
    }

    private boolean t3xCompareFlow(Network n, String id, double p1, double q1, double p2, double q2, double p3, double q3) {
        ThreeWindingsTransformer twt = n.getThreeWindingsTransformer(id);
        T3xFlow actual = threeWindingsTransformerFlow(twt);
        T3xFlow expected = new T3xFlow();
        expected.p1 = p1; expected.q1 = q1;
        expected.p2 = p2; expected.q2 = q2;
        expected.p3 = p3; expected.q3 = q3;
        boolean ok = sameFlow(expected, actual);

        if (!ok && LOG.isErrorEnabled()) {
            LOG.error(String.format("TransformerConversionTest: ThreeWindingsTransformer id %s ===", id));
            LOG.error(String.format("Expected P1 %12.6f Q1 %12.6f P2 %12.6f Q2 %12.6f P3 %12.6f Q3 %12.6f", expected.p1,
                expected.q1, expected.p2, expected.q2, expected.p3, expected.q3));
            LOG.error(String.format("Actual   P1 %12.6f Q1 %12.6f P2 %12.6f Q2 %12.6f P3 %12.6f Q3 %12.6f", actual.p1,
                actual.q1, actual.p2, actual.q2, actual.p3, actual.q3));
            LOG.error(EQUALS_LINE);
        }
        return ok;
    }

    private Network networkModel(GridModelReference gridModelReference, CgmesImportPostProcessor postProcessor) {
        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(postProcessor);

        return networkModel(gridModelReference, new Conversion.Config(), postProcessors);
    }

    private Network networkModel(GridModelReference gridModelReference, Conversion.Config config) {
        return networkModel(gridModelReference, config, Collections.emptyList());
    }

    private Network networkModel(GridModelReference gridModelReference, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        config.setConvertSvInjections(true);
        Network n = ConversionUtil.networkModel(gridModelReference, config, postProcessors);

        double threshold = 0.01;
        ValidationConfig vconfig = loadFlowValidationConfig(threshold);
        LoadFlowParameters lfParameters = defineLoadflowParameters(vconfig.getLoadFlowParameters(), config);
        ConversionTester.computeMissingFlows(n, lfParameters);

        return n;
    }

    private GridModelReferenceResources twoWindingsTransformers() {
        return new GridModelReferenceResources(
                "TwoWindingsTransformers",
                null,
                new ResourceSet(DIR,
                        "twoWindingsTransformers_EQ.xml",
                        "twoWindingsTransformers_SSH.xml",
                        "twoWindingsTransformers_SV.xml",
                        "twoWindingsTransformers_TP.xml"));
    }

    private GridModelReferenceResources threeWindingsTransformer() {
        return new GridModelReferenceResources(
                "ThreeWindingsTransformer",
                null,
                new ResourceSet(DIR,
                        "threeWindingsTransformer_EQ.xml",
                        "threeWindingsTransformer_SSH.xml",
                        "threeWindingsTransformer_SV.xml",
                        "threeWindingsTransformer_TP.xml"));
    }

    private GridModelReferenceResources phaseAngleClock(String phaseAngleClockEQ) {
        return new GridModelReferenceResources(
                "PhaseAngleClock",
                null,
                new ResourceSet(DIR,
                        phaseAngleClockEQ,
                        "phaseAngleClock_SSH.xml",
                        "phaseAngleClock_TP.xml",
                        "phaseAngleClock_SV.xml"));
    }

    private static ValidationConfig loadFlowValidationConfig(double threshold) {
        ValidationConfig config = ValidationConfig.load();
        config.setVerbose(true);
        config.setThreshold(threshold);
        config.setOkMissingValues(false);
        config.setLoadFlowParameters(new LoadFlowParameters());
        return config;
    }

    private static T2xFlow twoWindingsTransformerFlow(TwoWindingsTransformer twt) {
        T2xFlow f = new T2xFlow();
        if (twt == null) {
            return f;
        }

        Terminal t1 = twt.getTerminal1();
        if (t1 != null) {
            f.p1 = t1.getP();
            f.q1 = t1.getQ();
        }
        Terminal t2 = twt.getTerminal2();
        if (t2 != null) {
            f.p2 = t2.getP();
            f.q2 = t2.getQ();
        }
        return f;
    }

    private static T3xFlow threeWindingsTransformerFlow(ThreeWindingsTransformer twt) {
        T3xFlow f = new T3xFlow();
        if (twt == null) {
            return f;
        }

        Terminal t1 = twt.getLeg1().getTerminal();
        if (t1 != null) {
            f.p1 = t1.getP();
            f.q1 = t1.getQ();
        }
        Terminal t2 = twt.getLeg2().getTerminal();
        if (t2 != null) {
            f.p2 = t2.getP();
            f.q2 = t2.getQ();
        }
        Terminal t3 = twt.getLeg3().getTerminal();
        if (t3 != null) {
            f.p3 = t3.getP();
            f.q3 = t3.getQ();
        }

        return f;
    }

    private boolean sameFlow(T2xFlow expected, T2xFlow actual) {
        double tol = 0.00001;
        // Comparison fails if actual has NaN values
        return Math.abs(expected.p1 - actual.p1) <= tol &&
            Math.abs(expected.q1 - actual.q1) <= tol &&
            Math.abs(expected.p2 - actual.p2) <= tol &&
            Math.abs(expected.q2 - actual.q2) <= tol;
    }

    private boolean sameFlow(T3xFlow expected, T3xFlow actual) {
        double tol = 0.00001;
        // Comparison fails if actual has NaN values
        return Math.abs(expected.p1 - actual.p1) <= tol &&
            Math.abs(expected.q1 - actual.q1) <= tol &&
            Math.abs(expected.p2 - actual.p2) <= tol &&
            Math.abs(expected.q2 - actual.q2) <= tol &&
            Math.abs(expected.p3 - actual.p3) <= tol &&
            Math.abs(expected.q3 - actual.q3) <= tol;
    }

    private LoadFlowParameters defineLoadflowParameters(LoadFlowParameters loadFlowParameters, Conversion.Config config) {
        LoadFlowParameters copyLoadFlowParameters = loadFlowParameters.copy();

        boolean t2wtSplitShuntAdmittance = false;
        switch (config.getXfmr2Shunt()) {
            case END1:
            case END2:
            case END1_END2:
                break;
            case SPLIT:
                t2wtSplitShuntAdmittance = true;
                break;
        }

        boolean t3wtSplitShuntAdmittance = false;
        switch (config.getXfmr3Shunt()) {
            case NETWORK_SIDE:
            case STAR_BUS_SIDE:
                break;
            case SPLIT:
                t3wtSplitShuntAdmittance = true;
        }

        if (t2wtSplitShuntAdmittance != t3wtSplitShuntAdmittance) {
            throw new PowsyblException(String.format("Unexpected SplitShuntAdmittance configuration %s %s",
                t2wtSplitShuntAdmittance, t3wtSplitShuntAdmittance));
        }
        boolean twtSplitShuntAdmittance = t2wtSplitShuntAdmittance;

        copyLoadFlowParameters.setTwtSplitShuntAdmittance(twtSplitShuntAdmittance);

        return copyLoadFlowParameters;
    }

    static class T2xFlow {
        double p1 = Double.NaN;
        double q1 = Double.NaN;
        double p2 = Double.NaN;
        double q2 = Double.NaN;
    }

    static class T3xFlow {
        double p1 = Double.NaN;
        double q1 = Double.NaN;
        double p2 = Double.NaN;
        double q2 = Double.NaN;
        double p3 = Double.NaN;
        double q3 = Double.NaN;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TransformerConversionTest.class);
}
