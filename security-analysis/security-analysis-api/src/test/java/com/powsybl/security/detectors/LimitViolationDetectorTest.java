/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class LimitViolationDetectorTest {

    private Network network;
    private Branch line1;
    private Branch line2;
    private Branch transformer;
    private Contingency contingency1;
    private Contingency contingency2;
    private VoltageLevel voltageLevel1;
    private VoltageLevel genVoltageLevel;
    private VoltageLevel loadVoltageLevel;
    private VoltageAngleLimit voltageAngleLimit0;
    private VoltageAngleLimit voltageAngleLimit1;
    private VoltageAngleLimit voltageAngleLimit2;
    private List<LimitViolation> collectedViolations;

    private void doCheckCurrent(Contingency contingency, Branch branch, TwoSides side, Consumer<LimitViolation> consumer) {

        if (branch == line1) {
            consumer.accept(LimitViolations.current()
                    .subject(branch.getId())
                    .side(side)
                    .duration(1200)
                    .value(1500)
                    .limit(1200)
                    .build());
        }

        if (branch == line2 && side == TwoSides.TWO) {
            consumer.accept(LimitViolations.current()
                    .subject(branch.getId())
                    .duration(1200)
                    .side(side)
                    .value(1500)
                    .limit(1200)
                    .build());
        }

        if (contingency == contingency1
                && branch == transformer && side == TwoSides.ONE) {
            consumer.accept(LimitViolations.current()
                    .subject(branch.getId())
                    .duration(1200)
                    .side(side)
                    .value(1500)
                    .limit(1200)
                    .build());
        }
    }

    private void doCheckVoltage(Contingency contingency, Bus bus, Consumer<LimitViolation> consumer) {
        if (bus.getVoltageLevel() == voltageLevel1) {
            consumer.accept(LimitViolations.highVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(500)
                    .limit(420)
                    .build());
        }

        if (bus.getVoltageLevel() == genVoltageLevel) {
            consumer.accept(LimitViolations.lowVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(350)
                    .limit(380)
                    .build());
        }

        if (contingency == contingency1 && bus.getVoltageLevel() == loadVoltageLevel) {
            consumer.accept(LimitViolations.highVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(500)
                    .limit(420)
                    .build());
        }
    }

    private void doCheckVoltageAngle(Contingency contingency, VoltageAngleLimit voltageAngleLimit, Consumer<LimitViolation> consumer) {
        if (voltageAngleLimit == voltageAngleLimit0) {
            consumer.accept(LimitViolations.highVoltageAngle()
                .subject(voltageAngleLimit.getTerminalFrom().getConnectable().getId())
                .side(TwoSides.ONE)
                .value(0.30)
                .limit(0.25)
                .build());
        }
        if (contingency == contingency1 && voltageAngleLimit == voltageAngleLimit1) {
            consumer.accept(LimitViolations.lowVoltageAngle()
                .subject(voltageAngleLimit.getTerminalFrom().getConnectable().getId())
                .side(TwoSides.ONE)
                .value(-0.22)
                .limit(0.20)
                .build());
        }
    }

    private LimitViolationDetector contingencyBlindDetector() {
        return new AbstractContingencyBlindDetector() {
            @Override
            public void checkCurrent(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(null, branch, side, consumer);
            }

            @Override
            public void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(null, bus, consumer);
            }

            @Override
            public void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
                doCheckVoltageAngle(null, voltageAngleLimit, consumer);
            }

            @Override
            public void checkActivePower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkApparentPower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkActivePower(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkApparentPower(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }
        };
    }

    private LimitViolationDetector contingencyBasedDetector() {
        return new AbstractLimitViolationDetector() {
            @Override
            public void checkCurrent(Contingency contingency, Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(contingency, branch, side, consumer);
            }

            @Override
            public void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(contingency, bus, consumer);
            }

            @Override
            public void checkVoltageAngle(Contingency contingency, VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
                doCheckVoltageAngle(contingency, voltageAngleLimit, consumer);
            }

            @Override
            public void checkActivePower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkApparentPower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkActivePower(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkApparentPower(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkPermanentLimit(Branch<?> branch, TwoSides side, double limitReductionValue, double value, Consumer<LimitViolation> consumer, LimitType type) {

            }
        };
    }

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithVoltageAngleLimit();
        line1 = network.getBranch("NHV1_NHV2_1");
        line2 = network.getBranch("NHV1_NHV2_2");
        transformer = network.getBranch("NGEN_NHV1");
        voltageLevel1 = network.getVoltageLevel("VLHV1");
        genVoltageLevel = network.getVoltageLevel("VLGEN");
        loadVoltageLevel = network.getVoltageLevel("VLLOAD");
        voltageAngleLimit0 = network.getVoltageAngleLimitsStream().toList().get(0);
        voltageAngleLimit1 = network.getVoltageAngleLimitsStream().toList().get(1);
        voltageAngleLimit2 = network.getVoltageAngleLimitsStream().toList().get(2);
        contingency1 = new Contingency("cont1");
        contingency2 = new Contingency("cont2");

        collectedViolations = new ArrayList<>();
    }

    @Test
    void networkHas6Violations() {
        contingencyBasedDetector().checkAll(network, collectedViolations::add);
        assertEquals(6, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getSubjectId().equals("NHV1_NHV2_1")).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE_ANGLE).count());
    }

    @Test
    void branch1Has2Violations() {
        contingencyBasedDetector().checkCurrent(network.getLine("NHV1_NHV2_1"), collectedViolations::add);
        assertEquals(2, collectedViolations.size());
    }

    @Test
    void branch2Has1Violation() {
        contingencyBasedDetector().checkCurrent(network.getLine("NHV1_NHV2_2"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    void voltageLevel1Has1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VLHV1"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.HIGH_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    void voltageLevelGenHas1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VLGEN"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.LOW_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    void voltageAngleLimit0Has1Violation() {
        contingencyBasedDetector().checkVoltageAngle(network.getVoltageAngleLimitsStream().toList().get(0), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.HIGH_VOLTAGE_ANGLE, collectedViolations.get(0).getLimitType());
    }

    @Test
    void networkHas9ViolationsOnContingency1() {
        contingencyBasedDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(9, collectedViolations.size());
        assertEquals(4, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE_ANGLE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE_ANGLE).count());
    }

    @Test
    void networkHas5ViolationsOnContingency2() {
        contingencyBasedDetector().checkAll(contingency2, network, collectedViolations::add);
        assertEquals(6, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE_ANGLE).count());
    }

    @Test
    void networkHas5ViolationsOnContingency1WithContingencyBlindDetector() {
        contingencyBlindDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(6, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE_ANGLE).count());
    }

    @Test
    void transformerHas1ViolationOnContingency1() {
        contingencyBasedDetector().checkCurrent(contingency1, transformer, collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    void transformerHasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkCurrent(contingency1, transformer, collectedViolations::add);
        detector.checkCurrent(contingency1, transformer, TwoSides.ONE, collectedViolations::add);
        detector.checkCurrent(contingency1, transformer, TwoSides.ONE, 5000, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }

    @Test
    void loadVoltageLevelHasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkVoltage(contingency1, loadVoltageLevel, collectedViolations::add);
        Bus loadBus = loadVoltageLevel.getBusView().getBusStream().findFirst().orElseThrow(IllegalStateException::new);
        detector.checkVoltage(contingency1, loadBus, collectedViolations::add);
        detector.checkVoltage(contingency1, loadBus, 500, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }

    @Test
    void voltageAngleLimit1Has1ViolationOnContingency1() {
        contingencyBasedDetector().checkVoltageAngle(contingency1, voltageAngleLimit1, collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    void voltageAngleLimit2HasNoViolationOnContingency1() {
        contingencyBasedDetector().checkVoltageAngle(contingency1, voltageAngleLimit2, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }
}
