/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Branch;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitViolationBuilderTest {

    @Test
    public void insufficientInfoThrows() {
        LimitViolationBuilder builder = new LimitViolationBuilder();
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.subject("id");
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.current();
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.duration(1200);
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.limit(1500);
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.value(2000);
        Assertions.assertThatNullPointerException().isThrownBy(builder::build);
        builder.side1()
                .build();
    }

    @Test
    public void buildCurrentViolation() {
        LimitViolationBuilder builder = LimitViolations.current()
                .subject("id")
                .duration(20, TimeUnit.MINUTES)
                .limit(1500)
                .value(2000)
                .side1();

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.CURRENT, violation.getLimitType());
        assertEquals(Branch.Side.ONE, violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
        assertEquals(1200, violation.getAcceptableDuration(), 0);

        LimitViolation violation2 = builder
                .subjectName("name")
                .limitName("limitName")
                .reduction(0.9f)
                .side2()
                .duration(60)
                .build();
        assertEquals(Branch.Side.TWO, violation2.getSide());
        assertEquals("name", violation2.getSubjectName());
        assertEquals("limitName", violation2.getLimitName());
        assertEquals(0.9f, violation2.getLimitReduction(), 0);
        assertEquals(60, violation2.getAcceptableDuration(), 0);
    }

    @Test
    public void buildLowVoltageViolation() {
        LimitViolationBuilder builder = LimitViolations.lowVoltage()
                .subject("id")
                .limit(1500)
                .value(2000);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.LOW_VOLTAGE, violation.getLimitType());
        assertNull(violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void buildHighVoltageViolation() {
        LimitViolationBuilder builder = LimitViolations.highVoltage()
                .subject("id")
                .limit(1500)
                .value(2000);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.HIGH_VOLTAGE, violation.getLimitType());
        assertNull(violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void builLowShortCircuitCurrentViolation() {
        LimitViolationBuilder builder = LimitViolations.lowShortCircuitCurrent()
                .subject("id")
                .limit(1500)
                .value(2000);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.LOW_SHORT_CIRCUIT_CURRENT, violation.getLimitType());
        assertNull(violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void buildHighShortCircuitCurrentViolation() {
        LimitViolationBuilder builder = LimitViolations.highShortCircuitCurrent()
                .subject("id")
                .limit(1500)
                .value(2000);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, violation.getLimitType());
        assertNull(violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void buildActivePowerViolation() {
        LimitViolationBuilder builder = LimitViolations.activePower()
                                                       .subject("id")
                                                       .limit(1500)
                                                       .value(2000)
                                                       .duration(20, TimeUnit.MINUTES)
                                                       .side(Branch.Side.ONE);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.ACTIVE_POWER, violation.getLimitType());
        assertSame(Branch.Side.ONE, violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void buildApparentPowerViolation() {
        LimitViolationBuilder builder = LimitViolations.apparentPower()
                                                       .subject("id")
                                                       .limit(1500)
                                                       .value(2000)
                                                       .duration(20, TimeUnit.MINUTES)
                                                       .side(Branch.Side.TWO);

        LimitViolation violation = builder.build();
        assertEquals("id", violation.getSubjectId());
        assertSame(LimitViolationType.APPARENT_POWER, violation.getLimitType());
        assertSame(Branch.Side.TWO, violation.getSide());
        assertNull(violation.getLimitName());
        assertEquals(1500, violation.getLimit(), 0);
        assertEquals(2000, violation.getValue(), 0);
        assertEquals(1.0, violation.getLimitReduction(), 0);
    }

    @Test
    public void testComparator() {
        LimitViolationBuilder builder = LimitViolations.highVoltage()
                .subject("id")
                .limit(1500)
                .value(2000);

        LimitViolation violation1 = builder.build();
        LimitViolation violation2 = builder.build();
        assertNotSame(violation1, violation2);
        assertEquals(0, LimitViolations.comparator().compare(violation1, violation2));

        violation2 = builder.subject("id2").build();
        assertTrue(LimitViolations.comparator().compare(violation1, violation2) < 0);
    }

    @Test
    public void testLimitNameInVoltageLimitViolation() {
        LimitViolationBuilder builder = LimitViolations.highVoltage()
                .subject("id")
                .type(LimitViolationType.HIGH_VOLTAGE)
                .limit(420)
                .value(500)
                .limitName("high");
        assertEquals("high", builder.build().getLimitName());
    }
}
