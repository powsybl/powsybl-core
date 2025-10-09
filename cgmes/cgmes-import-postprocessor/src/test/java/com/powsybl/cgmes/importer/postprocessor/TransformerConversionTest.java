/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.importer.postprocessor;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImportPostProcessor;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerConversionTest {

    private static final String EQUALS_LINE = "======================";

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

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        config.setConvertSvInjections(true);
        Network n = ConversionUtil.networkModel(testGridModel, config, postProcessors);

        double threshold = 0.01;
        ValidationConfig vconfig = loadFlowValidationConfig(threshold);
        LoadFlowParameters lfParameters = defineLoadflowParameters(vconfig.getLoadFlowParameters(), config);
        ConversionTester.computeMissingFlows(n, lfParameters, false);

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
            case END1, END2, END1_END2:
                break;
            case SPLIT:
                t2wtSplitShuntAdmittance = true;
                break;
        }

        boolean t3wtSplitShuntAdmittance = false;
        switch (config.getXfmr3Shunt()) {
            case NETWORK_SIDE, STAR_BUS_SIDE:
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
