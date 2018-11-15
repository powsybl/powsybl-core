/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisChecksTest {

    AbstractSecurityAnalysis securityAnalysis;
    Network network;

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithCurrentLimits();
    }

    @Test
    //test current limit violation
    public void checkCurrentLimits() {
        Line line = network.getLine("NHV1_NHV2_1");
        LimitViolation limitViolation;

        limitViolation = AbstractSecurityAnalysis.checkCurrentLimits(line, line.getSide(line.getTerminal2()), EnumSet.of(Security.CurrentLimitType.PATL), 1101);

        assertNotNull(limitViolation);
        //check th  that is permaenent limit violation
        assertEquals(1100, limitViolation.getLimit(), 0d);

        limitViolation = AbstractSecurityAnalysis.checkCurrentLimits(line, line.getSide(line.getTerminal2()), EnumSet.of(Security.CurrentLimitType.TATL), 1201);

        assertNotNull(limitViolation);
        //check th  that is temporary limit violation
        assertEquals(1200, limitViolation.getLimit(), 0d);
    }

    @Test
    //test voltage limit violation
    public void checkLimits() {
        VoltageLevel vl = network.getVoltageLevel("VLHV1");
        double lvl = vl.getLowVoltageLimit();
        double hvl = vl.getHighVoltageLimit();

        LimitViolation limitViolation = AbstractSecurityAnalysis.checkLimits(vl, 380);

        assertNotNull(limitViolation);
        //check that is LimitViolationType.LOW_VOLTAGE limit violation
        assertEquals(LimitViolationType.LOW_VOLTAGE, limitViolation.getLimitType());

        limitViolation = null;
        limitViolation = AbstractSecurityAnalysis.checkLimits(vl, 520);

        assertNotNull(limitViolation);
        //check that is LimitViolationType.HIGH_VOLTAGE limit violation
        assertEquals(LimitViolationType.HIGH_VOLTAGE, limitViolation.getLimitType());
    }
}
