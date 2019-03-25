/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisChecksTest {

    private Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
    }

    @Test
    //test current limit violation
    public void checkCurrentLimits() {
        Line line1 = network.getLine("NHV1_NHV2_1");

        // Permanent limit violation
        List<LimitViolation> violations = new ArrayList<>();
        LimitViolationDetector detector = new DefaultLimitViolationDetector(Collections.singleton(Security.CurrentLimitType.PATL));
        detector.checkCurrent(line1, Branch.Side.TWO, 1101, violations::add);

        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1100, l.getLimit(), 0d);
                    assertEquals(1101, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(Integer.MAX_VALUE, l.getAcceptableDuration());
                });

        violations.clear();
        detector = new DefaultLimitViolationDetector(Collections.singleton(Security.CurrentLimitType.TATL));
        detector.checkCurrent(line1, Branch.Side.TWO, 1201, violations::add);

        // Temporary limit violation
        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1201, l.getValue(), 0d);
                    assertSame(Branch.Side.TWO, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });

        // Highest temporary limit violation, on side 1
        Line line2 = network.getLine("NHV1_NHV2_2");
        violations = new ArrayList<>();
        detector = new DefaultLimitViolationDetector();
        detector.checkCurrent(line2, Branch.Side.ONE, 1250, violations::add);
        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(l -> {
                    assertEquals(1200, l.getLimit(), 0d);
                    assertEquals(1250, l.getValue(), 0d);
                    assertSame(Branch.Side.ONE, l.getSide());
                    assertEquals(60, l.getAcceptableDuration());
                });
    }

    @Test
    //test voltage limit violation
    public void checkVoltage() {
        VoltageLevel vl = network.getVoltageLevel("VLHV1");

        LimitViolationDetector detector = new DefaultLimitViolationDetector();

        List<LimitViolation> violations = new ArrayList<>();
        vl.getBusView().getBusStream().forEach(b -> detector.checkVoltage(b, 380, violations::add));

        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.LOW_VOLTAGE, v.getLimitType());
                    assertEquals(400, v.getLimit(), 0d);
                    assertEquals(380, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });

        violations.clear();
        vl.getBusView().getBusStream().forEach(b -> detector.checkVoltage(b, 520, violations::add));

        //check that is LimitViolationType.HIGH_VOLTAGE limit violation
        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(v -> {
                    assertEquals(LimitViolationType.HIGH_VOLTAGE, v.getLimitType());
                    assertEquals(500, v.getLimit(), 0d);
                    assertEquals(520, v.getValue(), 0d);
                    assertNull(v.getSide());
                    assertEquals(Integer.MAX_VALUE, v.getAcceptableDuration());
                });
    }
}
