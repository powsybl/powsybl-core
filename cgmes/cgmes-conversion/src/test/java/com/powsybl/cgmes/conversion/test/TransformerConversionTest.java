/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.Conversion.*;
import com.powsybl.cgmes.conversion.PhaseAngleClock;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerConversionTest {

    @Test
    void microGridBaseCaseBExfmr2ShuntDefault() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2ShuntEnd1() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2ShuntEnd2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END2);
        // Same result as End1, IIDM model and LoadFlowParameters does not allow this configuration
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2ShuntEnd1End2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1_END2);
        // Same result as End1, IIDM model and LoadFlowParameters does not allow this configuration
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2ShuntSplit() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.SPLIT);
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.970891, -15.839366, 94.275697, 20.952066);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2RatioPhaseDefault() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2RatioPhaseEnd1() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -91.807775, 98.389959, 92.184500, -89.747219);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2RatioPhaseEnd2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 58.877292, -201.626411, -58.176878, 205.382102);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2RatioPhaseEnd1End2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1_END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2RatioPhaseX() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.X);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 58.877292, -201.626411, -58.176878, 205.382102);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2Ratio0Default() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2Ratio0End1() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -0.849849, -0.138409, 0.852591, 0.184615);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 70.106993, -25.657663, -68.935391, 31.939905);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2Ratio0End2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr2Ratio0X() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.X);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3ShuntDefault() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3ShuntNetworkSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3ShuntStarBusSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3ShuntSplit() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.SPLIT);
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.231950, 2.876479, -216.194348, -85.558437, 117.981856, 92.439531);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3RatioPhaseDefault() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3RatioPhaseNetworkSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3RatioPhaseStarBusSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0Default() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0StarBusSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0NetworkSide() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", -0.000166, -0.000842, -0.006802, -0.004135, 0.006989, 0.005353);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0End1() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", -26.608872, -134.702971, -1088.277421, -661.570093, 1118.294368, 856.437794);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0End2() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", -8.049184, -40.747649, -329.203920, -200.124953, 338.284046, 259.072433);
        assertTrue(ok);
    }

    @Test
    void microGridBaseCaseBExfmr3Ratio0End3() {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END3);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "84ed55f4-61f5-4d9d-8755-bba7b877a246", -0.073341, -0.371275, -2.999565, -1.823453, 3.082299, 2.360557);
        assertTrue(ok);
    }

    @Test
    void miniBusBranchPhaseAngleClock() {
        Conversion.Config config = new Conversion.Config();
        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new PhaseAngleClock());

        Network n = networkModel(CgmesConformity1Catalog.miniBusBranch(), config, postProcessors);

        boolean ok = t2xCompareFlow(n, "f1e72854-ec35-46e9-b614-27db354e8dbb", -318.691633, 1424.484145, 436.204160, 1393.367311);
        assertTrue(ok);
        ok = t3xCompareFlow(n, "5d38b7ed-73fd-405a-9cdb-78425e003773", -7.505045, -1.896561, -288.380946, 1216.566903, 351.090362, 1199.878285);
        assertTrue(ok);
    }

    @Test
    void miniBusBranchPhaseAngleClockZero() {
        Conversion.Config config = new Conversion.Config();
        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new PhaseAngleClock());

        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniBusBranchPhaseAngleClockZero(), config, postProcessors);

        boolean ok = t2xCompareFlow(n, "f1e72854-ec35-46e9-b614-27db354e8dbb", -0.087780, -0.178561, 0.087782, 0.178613);
        assertTrue(ok);
        ok = t3xCompareFlow(n, "5d38b7ed-73fd-405a-9cdb-78425e003773", -0.000001, -0.000022, 0.000002, 0.000068, -0.000001, -0.000045);
        assertTrue(ok);
    }

    @Test
    void miniBusBranchT2xPhaseAngleClock1NonZero() {
        Conversion.Config config = new Conversion.Config();
        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new PhaseAngleClock());

        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniBusBranchT2xPhaseAngleClock1NonZero(), config, postProcessors);

        boolean ok = t2xCompareFlow(n, "f1e72854-ec35-46e9-b614-27db354e8dbb", -318.691633, 1424.484145, 436.204160, 1393.367311);
        assertTrue(ok);
    }

    @Test
    void miniBusBranchT3xAllPhaseAngleClockNonZero() {
        Conversion.Config config = new Conversion.Config();
        List<CgmesImportPostProcessor> postProcessors = new ArrayList<>();
        postProcessors.add(new PhaseAngleClock());

        Network n = networkModel(CgmesConformity1ModifiedCatalog.miniBusBranchT3xAllPhaseAngleClockNonZero(), config, postProcessors);

        boolean ok = t3xCompareFlow(n, "5d38b7ed-73fd-405a-9cdb-78425e003773", -1494.636083, 1530.638656, 981.686099, 1826.870720, 562.199867, 309.289551);
        assertTrue(ok);
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
            LOG.error("======================");
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
            LOG.error("======================");
        }
        return ok;
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        return networkModel(testGridModel, config, Collections.emptyList());
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        config.setConvertSvInjections(true);
        Network n = ConversionUtil.networkModel(testGridModel, config, postProcessors);

        double threshold = 0.01;
        ValidationConfig vconfig = loadFlowValidationConfig(threshold);
        LoadFlowParameters lfParameters = defineLoadflowParameters(vconfig.getLoadFlowParameters(), config);
        ConversionTester.computeMissingFlows(n, lfParameters);

        return n;
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
