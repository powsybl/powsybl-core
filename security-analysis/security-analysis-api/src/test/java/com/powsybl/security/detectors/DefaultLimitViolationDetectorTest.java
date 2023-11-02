/**
 * Copyright (c) 2018-1019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static com.powsybl.iidm.network.util.LimitViolationUtils.PERMANENT_LIMIT_NAME;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class DefaultLimitViolationDetectorTest {

    private static Network networkWithFixedCurrentLimits;
    private static Network networkWithFixedLimits;
    private static Network networkWithFixedCurrentLimitsOnDanglingLines;
    private static Network networkWithFixedLimitsOnDanglingLines;
    private static Network networkWithVoltageAngleLimit;
    private static Network networkWithCurrentLimitsOn3WT;
    private static Network networkWithApparentLimitsOn3WT;
    private static Network networkWithActiveLimitsOn3WT;

    private LimitViolationDetector detector;
    private List<LimitViolation> violationsCollector;

    @BeforeAll
    static void setUpClass() {
        networkWithFixedCurrentLimits = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        networkWithFixedLimits = EurostagTutorialExample1Factory.createWithFixedLimits();
        networkWithFixedCurrentLimitsOnDanglingLines = EurostagTutorialExample1Factory.createWithFixedCurrentLimitsOnDanglingLines();
        networkWithFixedLimitsOnDanglingLines = EurostagTutorialExample1Factory.createWithFixedLimitsOnDanglingLines();
        networkWithVoltageAngleLimit = EurostagTutorialExample1Factory.createWithVoltageAngleLimit();
        networkWithCurrentLimitsOn3WT = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        networkWithApparentLimitsOn3WT = ThreeWindingsTransformerNetworkFactory.createWithApparentPowerLimits();
        networkWithActiveLimitsOn3WT = ThreeWindingsTransformerNetworkFactory.createWithActivePowerLimits();
    }

    @BeforeEach
    void setUp() {
        detector = new DefaultLimitViolationDetector();
        violationsCollector = new ArrayList<>();
    }

    @Test
    void detectPermanentLimitOverloadOnSide2OfLine1() {
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        detector.checkCurrent(line1, Branch.Side.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void testLimitReductionOnCurrentPermanentLimit() {
        final double i = 460;
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        Optional<? extends LoadingLimits> line1Limits = line1.getLimits(LimitType.CURRENT, Branch.Side.ONE);
        assertTrue(line1Limits.isPresent()
            && line1Limits.get().getTemporaryLimits().isEmpty()
            && line1Limits.get().getPermanentLimit() > i); // no overload expected

        // no violation if limitReduction is 1
        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(line1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        assertTrue(violationsCollector.isEmpty());

        // violation reported if limitReduction is 0.9
        cdetector = new DefaultLimitViolationDetector(0.9f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(line1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(500, l.getLimit(), 0);
                    assertEquals(460, l.getValue(), 0);
                    assertEquals(0.9f, l.getLimitReduction());
                });
    }

    @Test
    void detectTemporaryLimitOverloadOnSide2OfLine1() {
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        detector.checkCurrent(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighestTemporaryLimitOverloadOnSide1OfLine2() {
        Line line2 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_2");
        detector.checkCurrent(line2, Branch.Side.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(Branch.Side.ONE, l.getBranchSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectPermanentLimitOverloadOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkCurrent(tieLine1, Branch.Side.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void testLimitReductionOnCurrentPermanentLimitOnTieLine() {
        final double i = 460;
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        Optional<? extends LoadingLimits> line1Limits = tieLine1.getDanglingLine(Branch.Side.ONE).getCurrentLimits();
        assertTrue(line1Limits.isPresent()
                && line1Limits.get().getTemporaryLimits().isEmpty()
                && line1Limits.get().getPermanentLimit() > i); // no overload expected

        // no violation if limitReduction is 1
        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(tieLine1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        assertTrue(violationsCollector.isEmpty());

        // violation reported if limitReduction is 0.9
        cdetector = new DefaultLimitViolationDetector(0.9f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(tieLine1, Branch.Side.ONE, i, violationsCollector::add, LimitType.CURRENT);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(500, l.getLimit(), 0);
                    assertEquals(460, l.getValue(), 0);
                    assertEquals(0.9f, l.getLimitReduction());
                });
    }

    @Test
    void detectTemporaryLimitOverloadOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkCurrent(tieLine1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighestTemporaryLimitOverloadOnSide1OfTieLine2() {
        TieLine tieLine2 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_2");
        detector.checkCurrent(tieLine2, Branch.Side.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(Branch.Side.ONE, l.getBranchSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighVoltageViolation() {
        VoltageLevel voltageLevelHv1 = networkWithFixedCurrentLimits.getVoltageLevel("VLHV1");
        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> detector.checkVoltage(b, 520, violationsCollector::add));

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.HIGH_VOLTAGE, v.getLimitType());
                    assertEquals(500, v.getLimit(), 0d);
                    assertEquals(520, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    void detectLowVoltageViolation() {
        VoltageLevel voltageLevelHv1 = networkWithFixedCurrentLimits.getVoltageLevel("VLHV1");
        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> detector.checkVoltage(b, 380, violationsCollector::add));

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.LOW_VOLTAGE, v.getLimitType());
                    assertEquals(400, v.getLimit(), 0d);
                    assertEquals(380, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    void detectVoltageAngleViolationRefToOther() {
        VoltageAngleLimit voltageAngleLimit = networkWithVoltageAngleLimit.getVoltageAngleLimitsStream().toList().get(0);
        detector.checkVoltageAngle(voltageAngleLimit, 0.30, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(v -> {
                assertEquals(LimitViolationType.HIGH_VOLTAGE_ANGLE, v.getLimitType());
                assertEquals(0.25, v.getLimit(), 0d);
                assertEquals(0.30, v.getValue(), 0d);
                assertEquals(null, v.getSide());
                assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
            });
    }

    @Test
    void detectVoltageAngleViolationOtherToRef() {
        VoltageAngleLimit voltageAngleLimit = networkWithVoltageAngleLimit.getVoltageAngleLimitsStream().toList().get(1);
        detector.checkVoltageAngle(voltageAngleLimit, -0.30, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(v -> {
                assertEquals(LimitViolationType.LOW_VOLTAGE_ANGLE, v.getLimitType());
                assertEquals(0.20, v.getLimit(), 0d);
                assertEquals(-0.30, v.getValue(), 0d);
                assertEquals(null, v.getSide());
                assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
            });
    }

    @Test
    void detectVoltageAngleViolationHighAndLow1() {
        VoltageAngleLimit voltageAngleLimit = networkWithVoltageAngleLimit.getVoltageAngleLimitsStream().toList().get(2);
        detector.checkVoltageAngle(voltageAngleLimit, -0.40, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(v -> {
                assertEquals(LimitViolationType.LOW_VOLTAGE_ANGLE, v.getLimitType());
                assertEquals(-0.20, v.getLimit(), 0d);
                assertEquals(-0.40, v.getValue(), 0d);
                assertNull(v.getSide());
                assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
            });
    }

    @Test
    void detectVoltageAngleViolationHighAndLow2() {
        VoltageAngleLimit voltageAngleLimit = networkWithVoltageAngleLimit.getVoltageAngleLimitsStream().toList().get(2);
        detector.checkVoltageAngle(voltageAngleLimit, 0.40, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.HIGH_VOLTAGE_ANGLE, v.getLimitType());
                    assertEquals(0.35, v.getLimit(), 0d);
                    assertEquals(0.40, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    void detectPermanentActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(line1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectPermanentApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(line1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                      assertEquals(1.0f, l.getLimitReduction());
                  });
    }

    @Test
    void detectTemporaryActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        detector.checkTemporary(line1, Branch.Side.TWO, 1.0f, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectTemporaryApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, Branch.Side.TWO, 1.0f, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkActivePower(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                  });
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfAThreeWindingsTransformer() {
        ThreeWindingsTransformer transformer = networkWithActiveLimitsOn3WT.getThreeWindingsTransformer("3WT");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkActivePower(transformer, ThreeWindingsTransformer.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(100, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.TWO, l.getThreeWindingsTransformerSide());
                });
    }

    @Test
    void detectAllApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkApparentPower(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.TWO, l.getBranchSide());
                  });
    }

    @Test
    void detectAllApparentPowerLimitOnSide3OfAThreeWindingsTransformer() {
        ThreeWindingsTransformer transformer = networkWithApparentLimitsOn3WT.getThreeWindingsTransformer("3WT");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkApparentPower(transformer, ThreeWindingsTransformer.Side.THREE, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(10, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.THREE, l.getThreeWindingsTransformerSide());
                });
    }

    @Test
    void testCheckLimitViolationUnsupportedVoltage() {

        assertThrows(UnsupportedOperationException.class, () -> {
            Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

            DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
            cdetector.checkLimitViolation(line1, Branch.Side.ONE, 1201, violationsCollector::add, LimitType.VOLTAGE);
        });
    }

    @Test
    void detectPermanentActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(tieLine1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectPermanentApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(tieLine1, Branch.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(1.0f, l.getLimitReduction());
                });
    }

    @Test
    void detectTemporaryActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkTemporary(tieLine1, Branch.Side.TWO, 1.0f, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectTemporaryApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        detector.checkTemporary(tieLine1, Branch.Side.TWO, 1.0f, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                    assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkActivePower(tieLine1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                });
    }

    @Test
    void detectAllApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkApparentPower(tieLine1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getBranchSide());
                });
    }

    @Test
    void testCheckLimitViolationUnsupportedVoltageOnTieLine() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(LoadingLimitType.class));
        assertThrows(UnsupportedOperationException.class, () -> cdetector.checkLimitViolation(tieLine1, Branch.Side.ONE, 1201, violationsCollector::add, LimitType.VOLTAGE));
    }

    @Test
    void detectPermanentCurrentLimitOverloadOn3WT() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkCurrent(transformer, ThreeWindingsTransformer.Side.THREE, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(10, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.THREE, l.getThreeWindingsTransformerSide());
                    assertEquals(2147483647, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectPermanentCurrentLimitOverloadOn3WT2() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkCurrent(transformer, ThreeWindingsTransformer.Side.THREE, 13, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(12, l.getLimit(), 0d);
                    assertEquals(13, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.THREE, l.getThreeWindingsTransformerSide());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals("20'", l.getLimitName());
                });
    }

    @Test
    void detectTemporaryCurrentLimitOverloadOn3WT() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        // also test with branch one or two
        detector.checkTemporary(transformer, ThreeWindingsTransformer.Side.THREE, 1.0f, 12, violationsCollector::add, LimitType.CURRENT);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(12, l.getLimit(), 0d);
                    assertEquals(12, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.THREE, l.getThreeWindingsTransformerSide());
                    assertEquals(600, l.getAcceptableDuration());
                });
    }

    @Test
    void detectPermanentApparentPowerLimitOn3WT() {
        ThreeWindingsTransformer transformer = networkWithApparentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkPermanentLimit(transformer, ThreeWindingsTransformer.Side.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.TWO, l.getThreeWindingsTransformerSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(1.0f, l.getLimitReduction());
                });
    }

    @Test
    void detectPermanentActivePowerLimitOn3WT() {
        ThreeWindingsTransformer transformer = networkWithActiveLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkPermanentLimit(transformer, ThreeWindingsTransformer.Side.ONE, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1000, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeWindingsTransformer.Side.ONE, l.getThreeWindingsTransformerSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }
}
