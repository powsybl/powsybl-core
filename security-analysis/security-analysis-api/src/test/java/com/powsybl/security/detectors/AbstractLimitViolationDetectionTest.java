/**
 * Copyright (c) 2018-2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.powsybl.iidm.network.util.LimitViolationUtils.PERMANENT_LIMIT_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public abstract class AbstractLimitViolationDetectionTest {
    protected static Network networkWithFixedLimits;
    protected static Network networkWithFixedLimitsOnDanglingLines;
    protected static Network networkWithCurrentLimitsOn3WT;
    protected static Network networkWithApparentLimitsOn3WT;
    protected static Network networkWithActiveLimitsOn3WT;
    private static Network networkWithFixedCurrentLimits;
    private static Network networkWithFixedCurrentLimitsOnDanglingLines;
    private static Network networkWithVoltageAngleLimit;
    protected List<LimitViolation> violationsCollector;

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

    protected abstract void checkLimitViolation(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer,
                                                LimitType limitType, double limitReduction);

    protected abstract void checkCurrent(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer);

    protected abstract void checkCurrent(ThreeWindingsTransformer transfo, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer);

    protected abstract void checkActivePower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer);

    protected abstract void checkActivePower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer);

    protected abstract void checkApparentPower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer);

    protected abstract void checkApparentPower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer);

    protected abstract void checkVoltage(Bus b, int voltageValue, Consumer<LimitViolation> consumer);

    protected abstract void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer);

    @Test
    void detectPermanentLimitOverloadOnSide2OfLine1() {
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        checkCurrent(line1, TwoSides.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void testLimitReductionOnCurrentPermanentLimit() {
        final double i = 460;
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        Optional<? extends LoadingLimits> line1Limits = line1.getLimits(LimitType.CURRENT, TwoSides.ONE);
        assertTrue(line1Limits.isPresent()
                && line1Limits.get().getTemporaryLimits().isEmpty()
                && line1Limits.get().getPermanentLimit() > i); // no overload expected

        // no violation if limitReductionValue is 1
        checkLimitViolation(line1, TwoSides.ONE, i, violationsCollector::add, LimitType.CURRENT, 1.0);
        assertTrue(violationsCollector.isEmpty());

        // violation reported if limitReductionValue is 0.9
        checkLimitViolation(line1, TwoSides.ONE, i, violationsCollector::add, LimitType.CURRENT, 0.9);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(500, l.getLimit(), 0);
                    assertEquals(460, l.getValue(), 0);
                    assertEquals(0.9, l.getLimitReduction(), 0.001);
                });
    }

    @Test
    void detectTemporaryLimitOverloadOnSide2OfLine1() {
        Line line1 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_1");
        checkCurrent(line1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighestTemporaryLimitOverloadOnSide1OfLine2() {
        Line line2 = networkWithFixedCurrentLimits.getLine("NHV1_NHV2_2");
        checkCurrent(line2, TwoSides.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(TwoSides.ONE, l.getSideAsTwoSides());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectPermanentLimitOverloadOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        checkCurrent(tieLine1, TwoSides.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void testLimitReductionOnCurrentPermanentLimitOnTieLine() {
        final double i = 460;
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        Optional<? extends LoadingLimits> line1Limits = tieLine1.getDanglingLine(TwoSides.ONE).getCurrentLimits();
        assertTrue(line1Limits.isPresent()
                && line1Limits.get().getTemporaryLimits().isEmpty()
                && line1Limits.get().getPermanentLimit() > i); // no overload expected

        // no violation if limitReduction is 1
        checkLimitViolation(tieLine1, TwoSides.ONE, i, violationsCollector::add, LimitType.CURRENT, 1.0);
        assertTrue(violationsCollector.isEmpty());

        // violation reported if limitReduction is 0.9
        checkLimitViolation(tieLine1, TwoSides.ONE, i, violationsCollector::add, LimitType.CURRENT, 0.9);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(500, l.getLimit(), 0);
                    assertEquals(460, l.getValue(), 0);
                    assertEquals(0.9, l.getLimitReduction(), 0.001);
                });
    }

    @Test
    void detectTemporaryLimitOverloadOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        checkCurrent(tieLine1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighestTemporaryLimitOverloadOnSide1OfTieLine2() {
        TieLine tieLine2 = networkWithFixedCurrentLimitsOnDanglingLines.getTieLine("NHV1_NHV2_2");
        checkCurrent(tieLine2, TwoSides.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(TwoSides.ONE, l.getSideAsTwoSides());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    void detectHighVoltageViolation() {
        VoltageLevel voltageLevelHv1 = networkWithFixedCurrentLimits.getVoltageLevel("VLHV1");
        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> checkVoltage(b, 520, violationsCollector::add));

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
                .forEach(b -> checkVoltage(b, 380, violationsCollector::add));

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
        checkVoltageAngle(voltageAngleLimit, 0.30, violationsCollector::add);

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
        checkVoltageAngle(voltageAngleLimit, -0.30, violationsCollector::add);

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
        checkVoltageAngle(voltageAngleLimit, -0.40, violationsCollector::add);

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
        checkVoltageAngle(voltageAngleLimit, 0.40, violationsCollector::add);

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
    void detectAllActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        checkActivePower(line1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                });
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfAThreeWindingsTransformer() {
        ThreeWindingsTransformer transformer = networkWithActiveLimitsOn3WT.getThreeWindingsTransformer("3WT");

        checkActivePower(transformer, ThreeSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(0, l.getAcceptableDuration());
                    assertEquals(140, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(ThreeSides.TWO, l.getSide());
                });
    }

    @Test
    void detectAllApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        checkApparentPower(line1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                });
    }

    @Test
    void detectAllApparentPowerLimitOnSide3OfAThreeWindingsTransformer() {
        ThreeWindingsTransformer transformer = networkWithApparentLimitsOn3WT.getThreeWindingsTransformer("3WT");

        checkApparentPower(transformer, ThreeSides.THREE, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(0, l.getAcceptableDuration());
                    assertEquals(14, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(ThreeSides.THREE, l.getSide());
                });
    }

    @Test
    void testCheckLimitViolationUnsupportedVoltage() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        assertThrows(UnsupportedOperationException.class, () -> checkLimitViolation(line1, TwoSides.ONE, 1201,
                violationsCollector::add, LimitType.VOLTAGE, 1.0)
        );
    }

    @Test
    void detectAllActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        checkActivePower(tieLine1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                });
    }

    @Test
    void detectAllApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        checkApparentPower(tieLine1, TwoSides.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                });
    }

    @Test
    void testCheckLimitViolationUnsupportedVoltageOnTieLine() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        assertThrows(UnsupportedOperationException.class, () -> checkLimitViolation(tieLine1, TwoSides.ONE, 1201, violationsCollector::add, LimitType.VOLTAGE, 1.0));
    }

    @Test
    void detectOverLastTemporaryCurrentLimitOverloadOn3WT() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        checkCurrent(transformer, ThreeSides.THREE, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(14, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeSides.THREE, l.getSide());
                    assertEquals(0, l.getAcceptableDuration());
                    assertEquals("10'", l.getLimitName());
                });
    }

    @Test
    void detectTemporaryCurrentLimitOverloadOn3WT2() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        checkCurrent(transformer, ThreeSides.THREE, 13, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(12, l.getLimit(), 0d);
                    assertEquals(13, l.getValue(), 0d);
                    assertSame(ThreeSides.THREE, l.getSide());
                    assertEquals(600, l.getAcceptableDuration());
                    assertEquals("20'", l.getLimitName());
                });
    }

    @ParameterizedTest
    @MethodSource("provideDetectCurrentLimitArguments")
    void detectCurrentLimit(Network network, String limitsSetId, double currentValue, ExpectedResults expected) {
        Line line = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        line.setSelectedOperationalLimitsGroup1(limitsSetId);
        checkCurrent(line, TwoSides.ONE, currentValue, violationsCollector::add);

        if (expected != null) {
            Assertions.assertThat(violationsCollector)
                    .hasSize(1)
                    .allSatisfy(l -> {
                        assertEquals(expected.limit(), l.getLimit(), 0d);
                        assertEquals(currentValue, l.getValue(), 0d);
                        assertSame(ThreeSides.ONE, l.getSide());
                        assertEquals(expected.acceptableDuration(), l.getAcceptableDuration());
                        assertEquals(expected.limitName(), l.getLimitName());
                    });
        } else {
            assertEquals(0, violationsCollector.size());
        }
    }

    public record ExpectedResults(String limitName, double limit, int acceptableDuration) { }

    private static Stream<Arguments> provideDetectCurrentLimitArguments() {
        String case1 = "CASE1";
        String case2 = "CASE2";
        String case3 = "OneTempCase";

        Network network = EurostagTutorialExample1Factory.create();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1)
                .newOperationalLimitsGroup1(case1).newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                    .setName("TL1")
                    .setValue(120.0)
                    .setAcceptableDuration(20 * 60)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("TL2")
                    .setValue(140.0)
                    .setAcceptableDuration(10 * 60)
                .endTemporaryLimit()
                .add();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1)
                .newOperationalLimitsGroup1(case2).newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                    .setName("IT20")
                    .setValue(120.0)
                    .setAcceptableDuration(20 * 60)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("IT10")
                    .setValue(140.0)
                    .setAcceptableDuration(10 * 60)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("IT1")
                    .setValue(Double.POSITIVE_INFINITY)
                    .setAcceptableDuration(60)
                .endTemporaryLimit()
                .add();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1)
                .newOperationalLimitsGroup1(case3).newCurrentLimits()
                .setPermanentLimit(100.0)
                .beginTemporaryLimit()
                    .setName("TL")
                    .setValue(120.0)
                    .setAcceptableDuration(20 * 60)
                .endTemporaryLimit()
                .add();

        return Stream.of(
                // Case 1: no upper infinite limit
                Arguments.of(network, case1, 90., null), // below the permanent limit
                Arguments.of(network, case1, 110., new ExpectedResults(PERMANENT_LIMIT_NAME, 100., 1200)), // between permanent and TL1
                Arguments.of(network, case1, 130., new ExpectedResults("TL1", 120., 600)), // between TL1 and TL2
                Arguments.of(network, case1, 150., new ExpectedResults("TL2", 140., 0)), // over the highest temp limit (TL2)
                // Case 2: with an upper infinite limit
                Arguments.of(network, case2, 90., null), // below the permanent limit
                Arguments.of(network, case2, 110., new ExpectedResults(PERMANENT_LIMIT_NAME, 100., 1200)), // between permanent and IT20
                Arguments.of(network, case2, 130., new ExpectedResults("IT20", 120., 600)), // between IT20 and IT10
                Arguments.of(network, case2, 150., new ExpectedResults("IT10", 140., 60)), // between IT10 and IT1 (over IT1 is not possible)
                // Case 3: same as 1 but with a single temp limit
                Arguments.of(network, case3, 90., null), // below the permanent limit
                Arguments.of(network, case3, 110., new ExpectedResults(PERMANENT_LIMIT_NAME, 100., 1200)), // between permanent and TL
                Arguments.of(network, case3, 130., new ExpectedResults("TL", 120., 0)) // over the highest temp limit (TL)
        );
    }

}
