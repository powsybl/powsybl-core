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

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisChecksTest {

    Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithCurrentLimits();
    }

    @Test
    //test current limit violation
    public void checkCurrentLimits() {
        Line line = network.getLine("NHV1_NHV2_1");

        List<LimitViolation> violations = new ArrayList<>();
        LimitViolationDetector detector = new DefaultLimitViolationDetector(Collections.singleton(Security.CurrentLimitType.PATL));
        detector.checkCurrent(line, Branch.Side.TWO, 1101, violations::add);

        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(l -> assertEquals(1100, l.getLimit(), 0d));

        violations = new ArrayList<>();
        detector = new DefaultLimitViolationDetector(Collections.singleton(Security.CurrentLimitType.TATL));
        detector.checkCurrent(line, Branch.Side.TWO, 1201, violations::add);

        Assertions.assertThat(violations)
                .hasSize(1)
                .allSatisfy(l -> assertEquals(1200, l.getLimit(), 0d));
    }

    @Test
    //test voltage limit violation
    public void checkVoltage() {
        VoltageLevel vl = network.getVoltageLevel("VLHV1");

        LimitViolationDetector detector = new DefaultLimitViolationDetector();

        List<LimitViolation> violations = new ArrayList<>();
        vl.getBusView().getBusStream().forEach(b -> detector.checkVoltage(b, 380, violations::add));

        assertEquals(1, violations.size());
        LimitViolation violation = violations.get(0);
        //check that is LimitViolationType.LOW_VOLTAGE limit violation
        assertEquals(LimitViolationType.LOW_VOLTAGE, violation.getLimitType());

        violations.clear();
        vl.getBusView().getBusStream().forEach(b -> detector.checkVoltage(b, 520, violations::add));

        assertEquals(1, violations.size());
        violation = violations.get(0);
        //check that is LimitViolationType.HIGH_VOLTAGE limit violation
        assertEquals(LimitViolationType.HIGH_VOLTAGE, violation.getLimitType());
    }
}
