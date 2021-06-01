/**
 * Copyright (c) 2018-1019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationType;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class DefaultLimitViolationDetectorTest {

    private Network network;
    private Line line1;
    private Line line2;
    private VoltageLevel voltageLevelHv1;
    private LimitViolationDetector detector;
    private List<LimitViolation> violationsCollector;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");
        voltageLevelHv1 = network.getVoltageLevel("VLHV1");

        detector = new DefaultLimitViolationDetector();
        violationsCollector = new ArrayList<>();
    }

    @Test
    public void detectPermanentLimitOverloadOnSide2OfLine1() {

        detector.checkCurrent(line1, Branch.Side.TWO, 1101, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(600, l.getAcceptableDuration());
                });
    }

    @Test
    public void detectTemporaryLimitOverloadOnSide2OfLine1() {

        detector.checkCurrent(line1, Branch.Side.TWO, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    public void detectHighestTemporaryLimitOverloadOnSide1OfLine2() {

        detector.checkCurrent(line2, Branch.Side.ONE, 1250, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(Branch.Side.ONE, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    public void detectHighVoltageViolation() {

        voltageLevelHv1.getBusView().getBusStream()
                .forEach(b -> detector.checkVoltage(b, 520, violationsCollector::add));

        Assertions.assertThat(violationsCollector)
                .hasSize(1)
                .allSatisfy(v -> {
                    Assert.assertEquals(LimitViolationType.HIGH_VOLTAGE, v.getLimitType());
                    assertEquals(500, v.getLimit(), 0d);
                    assertEquals(520, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }

    @Test
    public void detectLowVoltageViolation() {

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
    public void detectPermanentActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");

        detector.checkPermanentLimit(line1, Branch.Side.ONE, 1101, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }

    @Test
    public void detectPermanentApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");
        line2 = network.getLine("NHV1_NHV2_2");

        detector.checkPermanentLimit(line1, Branch.Side.ONE, 1101, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1100, l.getLimit(), 0d);
                      assertEquals(1101, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }

    @Test
    public void detectTemporaryActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, Branch.Side.ONE, 1201, violationsCollector::add, LimitType.ACTIVE_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }

    @Test
    public void detectTemporaryApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        detector.checkTemporary(line1, Branch.Side.ONE, 1201, violationsCollector::add, LimitType.APPARENT_POWER);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }

    @Test
    public void detectAllActivePowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(DefaultLimitViolationDetector.CurrentLimitType.class));
        cdetector.checkActivePower(line1, Branch.Side.ONE, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }

    @Test
    public void detectAllApparentPowerLimitOnSide2OfLine1() {

        network = EurostagTutorialExample1Factory.createWithFixedLimits();
        line1 = network.getLine("NHV1_NHV2_1");

        DefaultLimitViolationDetector cdetector = new DefaultLimitViolationDetector(1.0f, EnumSet.allOf(DefaultLimitViolationDetector.CurrentLimitType.class));
        cdetector.checkApparentPower(line1, Branch.Side.ONE, 1201, violationsCollector::add);

        Assertions.assertThat(violationsCollector)
                  .hasSize(1)
                  .allSatisfy(l -> {
                      assertEquals(1200, l.getLimit(), 0d);
                      assertEquals(1201, l.getValue(), 0d);
                      assertSame(Branch.Side.ONE, l.getSide());
                  });
    }
}
