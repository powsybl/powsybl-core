/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 */
class UcteConverterHelperTest {

    private static Network reference;

    private static Network reference2;

    @BeforeAll
    static void setup() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("expectedExport", new ResourceSet("/", "expectedExport.uct"));
        reference = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        ReadOnlyDataSource dataSource2 = new ResourceDataSource("expectedExport2", new ResourceSet("/", "expectedExport2.uct"));
        reference2 = new UcteImporter().importData(dataSource2, NetworkFactory.findDefault(), null);
    }

    private static TwoWindingsTransformer createTestTransformer(String id) {
        Network network = NetworkFactory.findDefault().createNetwork("test", "test");
        Substation substation = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VL1").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevel1.getBusBreakerView().newBus().setId("B1").add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VL2").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        voltageLevel2.getBusBreakerView().newBus().setId("B2").add();
        return substation.newTwoWindingsTransformer()
                .setId(id)
                .setVoltageLevel1("VL1").setConnectableBus1("B1").setBus1("B1")
                .setVoltageLevel2("VL2").setConnectableBus2("B2").setBus2("B2")
                .setRatedU1(380.0).setRatedU2(380.0)
                .setR(2.0).setX(100.0).setG(0.0).setB(0.0)
                .add();
    }

    private static ReportNode createRootReportNode() {
        return ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("core.ucte.UcteReading")
                .build();
    }

    private static String captureStderr(Runnable action) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    void calculatePhaseDuTest() {
        assertEquals(2.0000, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
        assertNotEquals(2.0001, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
    }

    @Test
    void calculateSymmAngleDuTest() {
        assertEquals(1.573, calculateSymmAngleDu(reference.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1")), 0.0001);
    }

    @Test
    void calculateAsymmAngleDuAndAngleTest() {
        Complex du = calculateAsymmAngleDuAndAngle(reference.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);
        assertEquals(0.9, du.abs(), 0.0001);
        assertEquals(60.0, Math.toDegrees(du.getArgument()), 0.0001);
    }

    @Test
    void calculatePhaseDuTest2() {
        assertEquals(-2.000, calculatePhaseDu(reference2.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
        assertEquals(-1.57, calculateSymmAngleDu(reference2.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1")), 0.00001); // loss of one decimal with sign

        Complex duRef2 = calculateAsymmAngleDuAndAngle(reference2.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);

        double angleExpected = Math.toRadians(-120.0);
        double moduleExpected = -0.900;
        Complex duExpected = new Complex(Math.cos(angleExpected), Math.sin(angleExpected)).multiply(moduleExpected);

        assertEquals(duExpected.getReal(), duRef2.getReal(), 0.0001);
        assertEquals(duExpected.getImaginary(), duRef2.getImaginary(), 0.0001);

        ReadOnlyDataSource dataSource = new ResourceDataSource("expectedExport4", new ResourceSet("/", "expectedExport4.uct"));
        Network reference4 = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        Complex duRef4 = calculateAsymmAngleDuAndAngle(reference4.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);

        double angleExpected4 = Math.toRadians(-120.0);
        double moduleExpected4 = 1.833;
        Complex duExpected4 = new Complex(Math.cos(angleExpected4), Math.sin(angleExpected4)).multiply(moduleExpected4);

        assertEquals(duExpected4.getReal(), duRef4.getReal(), 0.0001);
        assertEquals(duExpected4.getImaginary(), duRef4.getImaginary(), 0.0001);

        ReadOnlyDataSource dataSource6 = new ResourceDataSource("expectedExport5", new ResourceSet("/", "expectedExport5.uct"));
        Network reference6 = new UcteImporter().importData(dataSource6, NetworkFactory.findDefault(), null);

        Complex duRef6 = calculateAsymmAngleDuAndAngle(reference6.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);
        assertEquals(0.990, duRef6.abs(), 0.00001);
        assertEquals(90.00, Math.toDegrees(duRef6.getArgument()), 0.00001); // loss of one decimal with sign
    }

    @Test
    void computeTapPositionRangeTrivialCaseTest() {
        TwoWindingsTransformer twt = createTestTransformer("TRIVIAL_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(-3)
                .setTapPosition(0)
                .setRegulating(false)
                .beginStep().setRho(1.09).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -3
                .beginStep().setRho(1.06).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -2
                .beginStep().setRho(1.03).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                .beginStep().setRho(1.00).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0
                .beginStep().setRho(0.97).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setRho(0.94).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                .beginStep().setRho(0.91).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 3
                .add();
        RatioTapChanger rtc = twt.getRatioTapChanger();

        int neutralPosition = findNeutralTapPosition(rtc);
        assertEquals(0, neutralPosition);

        UcteConverterHelper.TapPositionRange range = computeTapPositionRange(
                rtc.getLowTapPosition(), rtc.getHighTapPosition(), rtc.getTapPosition(), neutralPosition);

        assertEquals(3, range.n());
        assertEquals(0, range.np());
    }

    @Test
    void findNeutralTapPositionClosestNotExactMatchTest() {
        TwoWindingsTransformer twt = createTestTransformer("CLOSEST_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(1)
                .setTapPosition(3)
                .setRegulating(false)
                .beginStep().setRho(0.90).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setRho(0.95).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                .beginStep().setRho(1.05).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 3 (current tap)
                .beginStep().setRho(0.98).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 4 (closest to 1.0, not exact)
                .beginStep().setRho(1.10).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 5
                .add();

        assertEquals(4, findNeutralTapPosition(twt.getRatioTapChanger()));
    }

    @Test
    void findNeutralTapPositionTieBreakTest() {
        TwoWindingsTransformer twt = createTestTransformer("TIEBREAK_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(1)
                .setTapPosition(1)
                .setRegulating(false)
                .beginStep().setRho(0.95).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1: distance 0.05
                .beginStep().setRho(1.05).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2: distance 0.05 (tie)
                .add();

        int neutralPosition = findNeutralTapPosition(twt.getRatioTapChanger());
        assertEquals(1, neutralPosition);
    }

    @Test
    void findNeutralTapPositionPhaseTapChangerClosestNotExactMatchTest() {
        TwoWindingsTransformer twt = createTestTransformer("CLOSEST_PHASE_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(1)
                .setTapPosition(3)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(-10.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setAlpha(-5.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 2
                .beginStep().setAlpha(5.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 3 (current tap)
                .beginStep().setAlpha(2.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 4 (closest to 0.0, not exact)
                .beginStep().setAlpha(-8.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 5
                .add();

        assertEquals(4, findNeutralTapPosition(twt.getPhaseTapChanger()));
    }

    @Test
    void findNeutralTapPositionPhaseTapChangerDegenerateAllZeroAlphaTest() {
        TwoWindingsTransformer twt = createTestTransformer("DEGENERATE_PHASE_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(1)
                .setTapPosition(3)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(0.0).setRho(0.90).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setAlpha(0.0).setRho(0.95).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 2
                .beginStep().setAlpha(0.0).setRho(1.05).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 3 (current tap)
                .beginStep().setAlpha(0.0).setRho(0.98).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 4 (closest to 1.0, not exact)
                .beginStep().setAlpha(0.0).setRho(1.10).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 5
                .add();

        assertEquals(3, findNeutralTapPosition(twt.getPhaseTapChanger()));
    }

    @Test
    void computeTapPositionRangeAsymmetricTest() {
        TwoWindingsTransformer twt = createTestTransformer("ASYMMETRIC_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(1)
                .setTapPosition(3)
                .setRegulating(false)
                .beginStep().setRho(0.90).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setRho(0.95).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                .beginStep().setRho(1.05).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 3 (current tap)
                .beginStep().setRho(0.98).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 4 (neutral)
                .beginStep().setRho(1.10).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 5
                .add();
        RatioTapChanger rtc = twt.getRatioTapChanger();
        int neutralPosition = findNeutralTapPosition(rtc);
        assertEquals(4, neutralPosition);

        // computeTapPositionRange is a pure computation: extending the range to stay symmetric is not its concern
        // to warn about, that's up to the caller (see UcteExporterTest for the warning itself)
        UcteConverterHelper.TapPositionRange range = computeTapPositionRange(
                rtc.getLowTapPosition(), rtc.getHighTapPosition(), rtc.getTapPosition(), neutralPosition);

        assertEquals(3, range.n());
        assertEquals(-1, range.np());
    }

    @Test
    void predictRatioStepTest() {
        assertEquals(1.0, predictRatioStep(100.0, 0), 1e-9);
        assertEquals(0.5, predictRatioStep(100.0, 1), 1e-9);
    }

    @Test
    void predictSymmAngleStepTest() {
        assertEquals(0.0, predictSymmAngleStep(200.0, 0), 1e-9);
        assertEquals(-90.0, predictSymmAngleStep(200.0, 1), 1e-9);
        assertEquals(90.0, predictSymmAngleStep(200.0, -1), 1e-9);
    }

    @Test
    void predictAsymmAngleStepTest() {
        Complex lowPositionComplex = new Complex(1.0, 0.0);
        Complex predicted = predictAsymmAngleStep(lowPositionComplex, 100.0, Math.PI / 2, 1);
        assertEquals(1.0, predicted.getReal(), 1e-9);
        assertEquals(1.0, predicted.getImaginary(), 1e-9);
    }

    @Test
    void checkRatioTapChangerDeviationUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("UNIFORM_RATIO_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(-2)
                .setTapPosition(0)
                .setRegulating(false)
                .beginStep().setRho(1.0416666666666667).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -2
                .beginStep().setRho(1.0204081632653061).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                .beginStep().setRho(1.0).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0
                .beginStep().setRho(0.9803921568627451).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setRho(0.9615384615384616).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkRatioTapChangerDeviation(twt.getRatioTapChanger(), 2.0, 0, twt.getId(), reportNode));
        assertEquals("", warnings);
        assertEquals(0, reportNode.getChildren().size());
    }

    @Test
    void checkRatioTapChangerDeviationNonUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("NONUNIFORM_RATIO_TWT");
        twt.newRatioTapChanger()
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(-2)
                .setTapPosition(0)
                .setRegulating(false)
                .beginStep().setRho(1.0416666666666667).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -2
                .beginStep().setRho(1.0204081632653061).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // -1
                .beginStep().setRho(1.0).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 0
                .beginStep().setRho(1.5).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates
                .beginStep().setRho(0.9615384615384616).setR(0.0).setX(40.0).setG(0.0).setB(0.0).endStep() // 2
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkRatioTapChangerDeviation(twt.getRatioTapChanger(), 2.0, 0, twt.getId(), reportNode));
        assertEquals(1, warnings.lines().filter(line -> line.contains("NONUNIFORM_RATIO_TWT")).count());
        assertTrue(warnings.contains("differences found"));
        assertEquals(1, reportNode.getChildren().size());
        assertTrue(reportNode.getChildren().get(0).getMessage().contains("NONUNIFORM_RATIO_TWT"));
    }

    @Test
    void checkSymmAngleTapChangerDeviationUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("UNIFORM_SYMM_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(-1)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(90.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // -1
                .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0
                .beginStep().setAlpha(-90.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkSymmAngleTapChangerDeviation(twt.getPhaseTapChanger(), 200.0,
                0, twt.getId(), reportNode));
        assertEquals("", warnings);
        assertEquals(0, reportNode.getChildren().size());
    }

    @Test
    void checkSymmAngleTapChangerDeviationNonUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("NONUNIFORM_SYMM_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(-1)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(90.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // -1
                .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0
                .beginStep().setAlpha(-50.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates from -90
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkSymmAngleTapChangerDeviation(twt.getPhaseTapChanger(), 200.0,
                0, twt.getId(), reportNode));
        assertEquals(1, warnings.lines().filter(line -> line.contains("NONUNIFORM_SYMM_TWT")).count());
        assertTrue(warnings.contains("differences found"));
        assertEquals(1, reportNode.getChildren().size());
        assertTrue(reportNode.getChildren().get(0).getMessage().contains("NONUNIFORM_SYMM_TWT"));
    }

    @Test
    void checkAsymmAngleTapChangerDeviationUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("UNIFORM_ASYM_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0 (low anchor)
                .beginStep().setAlpha(0.0).setRho(0.5).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1
                .beginStep().setAlpha(0.0).setRho(1.0 / 3.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 2
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkAsymmAngleTapChangerDeviation(twt.getPhaseTapChanger(), 100.0, 0.0, twt.getId(), reportNode));
        assertEquals("", warnings);
        assertEquals(0, reportNode.getChildren().size());
    }

    @Test
    void checkAsymmAngleTapChangerDeviationNonUniformTest() {
        TwoWindingsTransformer twt = createTestTransformer("NONUNIFORM_ASYM_TWT");
        twt.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .setRegulationValue(200)
                .setRegulationTerminal(twt.getTerminal2())
                .beginStep().setAlpha(0.0).setRho(1.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 0 (low anchor)
                .beginStep().setAlpha(0.0).setRho(0.9).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 1 -- deviates from 0.5
                .beginStep().setAlpha(0.0).setRho(1.0 / 3.0).setR(0.0).setX(0.0).setG(0.0).setB(0.0).endStep() // 2
                .add();

        ReportNode reportNode = createRootReportNode();
        String warnings = captureStderr(() -> checkAsymmAngleTapChangerDeviation(twt.getPhaseTapChanger(), 100.0, 0.0, twt.getId(), reportNode));
        assertEquals(1, warnings.lines().filter(line -> line.contains("NONUNIFORM_ASYM_TWT")).count());
        assertTrue(warnings.contains("differences found"));
        assertEquals(1, reportNode.getChildren().size());
        assertTrue(reportNode.getChildren().get(0).getMessage().contains("NONUNIFORM_ASYM_TWT"));
    }

}
