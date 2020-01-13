/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.Conversion.Xfmr2RatioPhaseInterpretationAlternative;
import com.powsybl.cgmes.conversion.Conversion.Xfmr2ShuntInterpretationAlternative;
import com.powsybl.cgmes.conversion.Conversion.Xfmr2StructuralRatioInterpretationAlternative;
import com.powsybl.cgmes.conversion.Conversion.Xfmr3RatioPhaseInterpretationAlternative;
import com.powsybl.cgmes.conversion.Conversion.Xfmr3ShuntInterpretationAlternative;
import com.powsybl.cgmes.conversion.Conversion.Xfmr3StructuralRatioInterpretationAlternative;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TransformerConversionTest {

    @Test
    public void microGridBaseCaseBExfmr2ShuntDefault() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2ShuntEnd1() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2ShuntEnd2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END2);
        // Same result as End1, IIDM model and LoadFlowParameters does not allow this configuration
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2ShuntEnd1End2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.END1_END2);
        // Same result as End1, IIDM model and LoadFlowParameters does not allow this configuration
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2ShuntSplit() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.970891, -15.839366, 94.275697, 20.952066);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2RatioPhaseDefault() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2RatioPhaseEnd1() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -91.807775, 98.389959, 92.184500, -89.747219);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2RatioPhaseEnd2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 58.877292, -201.626411, -58.176878, 205.382102);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2RatioPhaseEnd1End2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.END1_END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2RatioPhaseX() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative.X);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 58.877292, -201.626411, -58.176878, 205.382102);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2Ratio0Default() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2Ratio0End1() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -0.849849, -0.138409, 0.852591, 0.184615);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 70.106993, -25.657663, -68.935391, 31.939905);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2Ratio0End2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr2Ratio0X() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative.X);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        // RatioTapChanger
        boolean ok = t2xCompareFlow(n, "_e482b89a-fa84-4ea9-8e70-a83d44790957", -93.855301, -15.285520, 94.158074, 20.388478);
        assertTrue(ok);
        // PhaseTapChanger
        ok = t2xCompareFlow(n, "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0", 927.034612, -339.274880, -911.542354, 422.345850);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3ShuntDefault() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3ShuntNetworkSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3ShuntStarBusSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3ShuntSplit() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative.SPLIT);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3RatioPhaseDefault() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3RatioPhaseNetworkSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3RatioPhaseStarBusSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0Default() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0StarBusSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", 99.227288, 2.747147, -216.195867, -85.490493, 117.988318, 92.500849);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0NetworkSide() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.NETWORK_SIDE);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", -0.000166, -0.000842, -0.006802, -0.004135, 0.006989, 0.005353);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0End1() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END1);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", -26.608872, -134.702971, -1088.277421, -661.570093, 1118.294368, 856.437794);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0End2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END2);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", -8.049184, -40.747649, -329.203920, -200.124953, 338.284046, 259.072433);
        assertTrue(ok);
    }

    @Test
    public void microGridBaseCaseBExfmr3Ratio0End3() throws IOException {
        Conversion.Config config = new Conversion.Config();
        config.setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative.END3);
        Network n = networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);

        // RatioTapChanger
        boolean ok = t3xCompareFlow(n, "_84ed55f4-61f5-4d9d-8755-bba7b877a246", -0.073341, -0.371275, -2.999565, -1.823453, 3.082299, 2.360557);
        assertTrue(ok);
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
            LOG.error(String.format("======================", id));
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
            LOG.error(String.format("======================", id));
        }
        return ok;
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        config.setProfileUsedForInitialStateValues(Conversion.Config.StateProfile.SSH.name());
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

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
        if (Math.abs(expected.p1 - actual.p1) > tol ||
            Math.abs(expected.q1 - actual.q1) > tol ||
            Math.abs(expected.p2 - actual.p2) > tol ||
            Math.abs(expected.q2 - actual.q2) > tol) {
            return false;
        }
        return true;
    }

    private boolean sameFlow(T3xFlow expected, T3xFlow actual) {
        double tol = 0.00001;
        if (Math.abs(expected.p1 - actual.p1) > tol ||
            Math.abs(expected.q1 - actual.q1) > tol ||
            Math.abs(expected.p2 - actual.p2) > tol ||
            Math.abs(expected.q2 - actual.q2) > tol ||
            Math.abs(expected.p3 - actual.p3) > tol ||
            Math.abs(expected.q3 - actual.q3) > tol) {
            return false;
        }
        return true;
    }

    private LoadFlowParameters defineLoadflowParameters(LoadFlowParameters loadFlowParameters, Conversion.Config config) {
        LoadFlowParameters copyLoadFlowParameters = loadFlowParameters.copy();

        boolean splitShuntAdmittanceXfmr2 = false;
        switch (config.getXfmr2Shunt()) {
            case END1:
            case END2:
            case END1_END2:
                break;
            case SPLIT:
                splitShuntAdmittanceXfmr2 = true;
                break;
        }

        boolean splitShuntAdmittanceXfmr3 = false;
        switch (config.getXfmr3Shunt()) {
            case NETWORK_SIDE:
            case STAR_BUS_SIDE:
                break;
            case SPLIT:
                splitShuntAdmittanceXfmr3 = true;
        }

        copyLoadFlowParameters.setSpecificCompatibility(splitShuntAdmittanceXfmr2);
        //copyLoadFlowParameters.setSplitShuntAdmittanceXfmr2(splitShuntAdmittanceXfmr2);
        // copyLoadFlowParameters.setSplitShuntAdmittanceXfmr3(splitShuntAdmittanceXfmr3);

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

    private static void logAllTwoWindingsTransformer(Network n) {
        n.getTwoWindingsTransformerStream().forEach(TransformerConversionTest::logTwoWindingsTransformerAttributes);
    }

    private static void logTwoWindingsTransformerAttributes(TwoWindingsTransformer twt) {
        LOG.info("TwoWindingsTransformer {} ============", twt.getId());
        LOG.info("G {}", twt.getG());
        LOG.info("B {}", twt.getB());
        RatioTapChanger rtc = twt.getRatioTapChanger();
        logRatioTapChangerAttributes(rtc);
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        logPhaseTapChangerAttributes(ptc);
    }

    private static void logAllThreeWindingsTransformer(Network n) {
        n.getThreeWindingsTransformerStream().forEach(TransformerConversionTest::logThreeWindingsTransformerAttributes);
    }

    private static void logThreeWindingsTransformerAttributes(ThreeWindingsTransformer twt) {
        LOG.info("ThreeWindingsTransformer {} ============", twt.getId());
        LOG.info("RatedU0 {}", twt.getRatedU0());
        LOG.info("Leg1 ------");
        logLegAttributes(twt.getLeg1());
        LOG.info("Leg2 ------");
        logLegAttributes(twt.getLeg2());
        LOG.info("Leg3 ------");
        logLegAttributes(twt.getLeg3());
    }

    private static void logLegAttributes(Leg leg) {
        LOG.info("G {}", leg.getG());
        LOG.info("B {}", leg.getB());
        RatioTapChanger rtc = leg.getRatioTapChanger();
        logRatioTapChangerAttributes(rtc);
        PhaseTapChanger ptc = leg.getPhaseTapChanger();
        logPhaseTapChangerAttributes(ptc);
    }

    private static void logRatioTapChangerAttributes(RatioTapChanger rtc) {
        if (rtc == null) {
            LOG.info("RatioTapChanger: null");
            return;
        }

        int tapPosition = rtc.getTapPosition();
        RatioTapChangerStep step = rtc.getStep(tapPosition);
        LOG.info("RatioTapChanger: TapPosition {}", tapPosition);
        LOG.info("RatioTapChanger: Step Ratio {}", 1 / step.getRho());
        LOG.info("RatioTapChanger: Step R {}", step.getR());
        LOG.info("RatioTapChanger: Step X {}", step.getX());
        LOG.info("RatioTapChanger: Step G {}", step.getG());
        LOG.info("RatioTapChanger: Step B {}", step.getB());
    }

    private static void logPhaseTapChangerAttributes(PhaseTapChanger ptc) {
        if (ptc == null) {
            LOG.info("PhaseTapChanger: null");
            return;
        }

        int tapPosition = ptc.getTapPosition();
        PhaseTapChangerStep step = ptc.getStep(tapPosition);
        LOG.info("PhaseTapChanger: TapPosition {}", tapPosition);
        LOG.info("PhaseTapChanger: Step Ratio {}", 1 / step.getRho());
        LOG.info("PhaseTapChanger: Step Angle {}", -step.getAlpha());
        LOG.info("PhaseTapChanger: Step R {}", step.getR());
        LOG.info("PhaseTapChanger: Step X {}", step.getX());
        LOG.info("PhaseTapChanger: Step G {}", step.getG());
        LOG.info("PhaseTapChanger: Step B {}", step.getB());
    }

    private static final Logger LOG = LoggerFactory.getLogger(TransformerConversionTest.class);
}
