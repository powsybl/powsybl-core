/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

import static com.powsybl.iidm.network.util.LimitViolationUtils.PERMANENT_LIMIT_NAME;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class DefaultLimitViolationDetectorTest extends AbstractLimitViolationDetectionTest {

    private LimitViolationDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DefaultLimitViolationDetector();
        violationsCollector = new ArrayList<>();
    }

    @Override
    protected void checkLimitViolation(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer,
                                       LimitType limitType, double limitReduction) {
        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(limitReduction, EnumSet.allOf(LoadingLimitType.class));
        cdetector.checkLimitViolation(branch, side, currentValue, consumer, limitType);
    }

    @Override
    protected void checkCurrent(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
        detector.checkCurrent(branch, side, currentValue, consumer);
    }

    @Override
    protected void checkCurrent(ThreeWindingsTransformer transfo, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
        detector.checkCurrent(transfo, side, currentValue, consumer);
    }

    @Override
    protected void checkActivePower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        detector.checkActivePower(branch, side, value, consumer);
    }

    @Override
    protected void checkActivePower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        detector.checkActivePower(transfo, side, value, consumer);
    }

    @Override
    protected void checkApparentPower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        detector.checkApparentPower(branch, side, value, consumer);
    }

    @Override
    protected void checkApparentPower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        detector.checkApparentPower(transfo, side, value, consumer);
    }

    @Override
    protected void checkVoltage(Bus b, int voltageValue, Consumer<LimitViolation> consumer) {
        detector.checkVoltage(b, voltageValue, consumer);
    }

    @Override
    protected void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
        detector.checkVoltageAngle(voltageAngleLimit, voltageAngleDifference, consumer);
    }

    @Test
    void detectPermanentActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(line1, TwoSides.TWO, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectPermanentApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(line1, TwoSides.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                      assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                      assertEquals(1.0f, l.getLimitReduction());
                  });
    }

    @Test
    void detectTemporaryActivePowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");
        boolean ok = detector.checkTemporary(line1, TwoSides.TWO, 1.0f, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);
        assertFalse(ok); // A violation was detected
        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
        // No violation detected
        assertTrue(detector.checkTemporary(line1, TwoSides.TWO, 1.0f, 100., violationsCollector::add, LimitType.ACTIVE_POWER));
    }

    @Test
    void detectTemporaryApparentPowerLimitOnSide2OfLine1() {
        Line line1 = networkWithFixedLimits.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, TwoSides.TWO, 1.0f, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                      assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                  });
    }

    @Test
    void detectPermanentActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(tieLine1, TwoSides.TWO, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectPermanentApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkPermanentLimit(tieLine1, TwoSides.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(1.0f, l.getLimitReduction());
                });
    }

    @Test
    void detectTemporaryActivePowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");
        detector.checkTemporary(tieLine1, TwoSides.TWO, 1.0f, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectTemporaryApparentPowerLimitOnSide2OfTieLine1() {
        TieLine tieLine1 = networkWithFixedLimitsOnDanglingLines.getTieLine("NHV1_NHV2_1");

        detector.checkTemporary(tieLine1, TwoSides.TWO, 1.0f, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(TwoSides.TWO, l.getSideAsTwoSides());
                    assertNotEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }

    @Test
    void detectTemporaryCurrentLimitOverloadOn3WT() {
        ThreeWindingsTransformer transformer = networkWithCurrentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        // also test with branch one or two
        boolean ok = detector.checkTemporary(transformer, ThreeSides.THREE, 1.0f, 12, violationsCollector::add, LimitType.CURRENT);
        assertFalse(ok); // A violation was detected
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(12, l.getLimit(), 0d);
                    assertEquals(12, l.getValue(), 0d);
                    assertSame(ThreeSides.THREE, l.getSide());
                    assertEquals(600, l.getAcceptableDuration());
                });
        // No violation detected
        assertTrue(detector.checkTemporary(transformer, ThreeSides.THREE, 1.0f, 0., violationsCollector::add, LimitType.CURRENT));
    }

    @Test
    void detectPermanentApparentPowerLimitOn3WT() {
        ThreeWindingsTransformer transformer = networkWithApparentLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkPermanentLimit(transformer, ThreeSides.TWO, 1.0f, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeSides.TWO, l.getSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                    assertEquals(1.0f, l.getLimitReduction());
                });
    }

    @Test
    void detectPermanentActivePowerLimitOn3WT() {
        ThreeWindingsTransformer transformer = networkWithActiveLimitsOn3WT.getThreeWindingsTransformer("3WT");
        detector.checkPermanentLimit(transformer, ThreeSides.ONE, 1.0f, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1000, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(ThreeSides.ONE, l.getSide());
                    assertEquals(PERMANENT_LIMIT_NAME, l.getLimitName());
                });
    }
}
