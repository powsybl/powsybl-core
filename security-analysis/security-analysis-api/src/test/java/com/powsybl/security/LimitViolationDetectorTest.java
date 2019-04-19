/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitViolationDetectorTest {

    private Network network;
    private LimitViolationDetector detector;
    private List<LimitViolation> collectedViolations;

    private static class TestViolationDetector extends AbstractLimitViolationDetector {

        @Override
        public void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {

            if (branch.getId().equals("NHV1_NHV2_1")) {
                consumer.accept(LimitViolations.current()
                        .subject("NHV1_NHV2_1")
                        .side(side)
                        .permanent()
                        .value(1500)
                        .limit(1200)
                        .build());
            }

            if (branch.getId().equals("NHV1_NHV2_2") && side == Branch.Side.TWO) {
                consumer.accept(LimitViolations.current()
                        .subject("NHV1_NHV2_2")
                        .duration(1200)
                        .side(side)
                        .value(1500)
                        .limit(1200)
                        .build());
            }

            if (contingency != null && contingency.getId().equals("cont1")
                    && branch.getId().equals("NGEN_NHV1") && side == Branch.Side.ONE) {
                consumer.accept(LimitViolations.current()
                        .subject("NGEN_NHV1")
                        .duration(1200)
                        .side(side)
                        .value(1500)
                        .limit(1200)
                        .build());
            }
        }

        @Override
        public void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
            if (bus.getVoltageLevel().getId().equals("VLHV1")) {
                consumer.accept(LimitViolations.highVoltage()
                        .subject("VLHV1")
                        .value(500)
                        .limit(420)
                        .build());
            }

            if (bus.getVoltageLevel().getId().equals("VLGEN")) {
                consumer.accept(LimitViolations.lowVoltage()
                        .subject("VLGEN")
                        .value(350)
                        .limit(380)
                        .build());
            }

            if (contingency != null && contingency.getId().equals("cont1") && bus.getVoltageLevel().getId().equals("VLLOAD")) {
                consumer.accept(LimitViolations.highVoltage()
                        .subject("VLLOAD")
                        .value(500)
                        .limit(420)
                        .build());
            }
        }
    }

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        detector = new TestViolationDetector();
        collectedViolations = new ArrayList<>();
    }

    @Test
    public void networkHas5Violations() {
        detector.checkAll(network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getSubjectId().equals("NHV1_NHV2_1")).count());
    }

    @Test
    public void branch1Has2Violations() {
        detector.checkCurrent(network.getLine("NHV1_NHV2_1"), collectedViolations::add);
        assertEquals(2, collectedViolations.size());
    }

    @Test
    public void branch2Has1Violation() {
        detector.checkCurrent(network.getLine("NHV1_NHV2_2"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    public void voltageLevel1Has1Violation() {
        detector.checkVoltage(network.getVoltageLevel("VLHV1"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.HIGH_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    public void voltageLevelGenHas1Violation() {
        detector.checkVoltage(network.getVoltageLevel("VLGEN"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.LOW_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    public void networkHas7ViolationsOnContingency1() {
        detector.checkAll(new Contingency("cont1"), network, collectedViolations::add);
        assertEquals(7, collectedViolations.size());
        assertEquals(4, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    public void networkHas5ViolationsOnContingency2() {
        detector.checkAll(new Contingency("cont2"), network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }
}
