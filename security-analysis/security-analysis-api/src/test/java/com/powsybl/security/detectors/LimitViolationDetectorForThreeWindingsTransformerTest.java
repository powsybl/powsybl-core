/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LimitViolationDetectorForThreeWindingsTransformerTest {

    private Network network;
    private ThreeWindingsTransformer threeWindingsTransformer;
    private Contingency contingency1;
    private Contingency contingency2;
    private VoltageLevel genVoltageLevel1;
    private VoltageLevel loadVoltageLevel2;
    private VoltageLevel loadVoltageLevel3;
    private List<LimitViolation> collectedViolations;

    private void doCheckCurrent(Contingency contingency, ThreeWindingsTransformer transformer, ThreeSides side, Consumer<LimitViolation> consumer) {

        if (transformer == threeWindingsTransformer && side == ThreeSides.TWO) {
            consumer.accept(LimitViolations.current()
                    .subject(transformer.getId())
                    .duration(1200)
                    .side(side)
                    .value(1500)
                    .limit(1200)
                    .build());
        }

        if (contingency == contingency1
                && transformer == threeWindingsTransformer && side == ThreeSides.THREE) {
            consumer.accept(LimitViolations.current()
                    .subject(transformer.getId())
                    .duration(1200)
                    .side(side)
                    .value(1500)
                    .limit(1200)
                    .build());
        }
    }

    private void doCheckVoltage(Contingency contingency, Bus bus, Consumer<LimitViolation> consumer) {
        if (bus.getVoltageLevel() == loadVoltageLevel2) {
            consumer.accept(LimitViolations.highVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(500)
                    .limit(420)
                    .build());
        }

        if (bus.getVoltageLevel() == genVoltageLevel1) {
            consumer.accept(LimitViolations.lowVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(350)
                    .limit(380)
                    .build());
        }

        if (contingency == contingency1 && bus.getVoltageLevel() == loadVoltageLevel3) {
            consumer.accept(LimitViolations.highVoltage()
                    .subject(bus.getVoltageLevel().getId())
                    .value(500)
                    .limit(420)
                    .build());
        }
    }

    private LimitViolationDetector contingencyBlindDetector() {
        return new AbstractContingencyBlindDetector() {
            @Override
            public void checkCurrent(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(null, transformer, side, consumer);
            }

            @Override
            public void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(null, bus, consumer);
            }

            @Override
            public void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkActivePower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkApparentPower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
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
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkCurrent(Contingency contingency, ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(contingency, transformer, side, consumer);
            }

            @Override
            public void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(contingency, bus, consumer);
            }

            @Override
            public void checkVoltageAngle(Contingency contingency, VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");
            }

            @Override
            public void checkActivePower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");

            }

            @Override
            public void checkApparentPower(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
                throw new UnsupportedOperationException("Not used in this test!");

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
                throw new UnsupportedOperationException("Not used in this test!");

            }
        };
    }

    @BeforeEach
    void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits();
        threeWindingsTransformer = network.getThreeWindingsTransformer("3WT");
        genVoltageLevel1 = network.getVoltageLevel("VL_132");
        loadVoltageLevel2 = network.getVoltageLevel("VL_33");
        loadVoltageLevel3 = network.getVoltageLevel("VL_11");
        contingency1 = new Contingency("cont1");
        contingency2 = new Contingency("cont2");

        collectedViolations = new ArrayList<>();
    }

    @Test
    void networkHas3Violations() {
        contingencyBasedDetector().checkAll(network, collectedViolations::add);
        assertEquals(3, collectedViolations.size());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getSubjectId().equals("3WT")).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    void transformerHas1Violations() {
        contingencyBasedDetector().checkCurrent(network.getThreeWindingsTransformer("3WT"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    void genVoltageLevel1Has1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VL_132"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.LOW_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    void loadVoltageLeve2Has1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VL_33"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.HIGH_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    void networkHas5ViolationsOnContingency1() {
        contingencyBasedDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    void networkHas6ViolationsOnContingency2() {
        contingencyBasedDetector().checkAll(contingency2, network, collectedViolations::add);
        assertEquals(3, collectedViolations.size());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    void networkHas6ViolationsOnContingency1WithContingencyBlindDetector() {
        contingencyBlindDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(3, collectedViolations.size());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    void transformerHas2ViolationOnContingency1() {
        contingencyBasedDetector().checkCurrent(contingency1, threeWindingsTransformer, collectedViolations::add);
        assertEquals(2, collectedViolations.size());
    }

    @Test
    void transformerSide1HasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkCurrent(contingency1, threeWindingsTransformer, ThreeSides.ONE, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
        detector.checkCurrent(contingency1, threeWindingsTransformer, ThreeSides.ONE, 5000, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
        detector.checkCurrent(contingency1, threeWindingsTransformer, collectedViolations::add);
        assertEquals(0, collectedViolations.stream().filter(l -> l.getSide() == ThreeSides.ONE).count());
    }

    @Test
    void loadVoltageLevelHasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkVoltage(contingency1, loadVoltageLevel3, collectedViolations::add);
        Bus loadBus = loadVoltageLevel3.getBusView().getBusStream().findFirst().orElseThrow(IllegalStateException::new);
        detector.checkVoltage(contingency1, loadBus, collectedViolations::add);
        detector.checkVoltage(contingency1, loadBus, 500, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }

}
