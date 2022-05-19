/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolations;
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
    private Branch line1;
    private Branch line2;
    private Branch transformer;
    private Contingency contingency1;
    private Contingency contingency2;
    private VoltageLevel voltageLevel1;
    private VoltageLevel genVoltageLevel;
    private VoltageLevel loadVoltageLevel;
    private List<LimitViolation> collectedViolations;

    private void doCheckCurrent(Contingency contingency, Branch branch, Branch.Side side, Consumer<LimitViolation> consumer) {

        if (branch == line1) {
            consumer.accept(LimitViolations.current()
                    .subject(branch.getId())
                    .side(side)
                    .duration(1200)
                    .value(1500)
                    .limit(1200)
                    .build());
        }

        if (branch == line2 && side == Branch.Side.TWO) {
            consumer.accept(LimitViolations.current()
                    .subject(branch.getId())
                    .duration(1200)
                    .side(side)
                    .value(1500)
                    .limit(1200)
                    .build());
        }

        if (contingency == contingency1
                && branch == transformer && side == Branch.Side.ONE) {
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

    private LimitViolationDetector contingencyBlindDetector() {
        return new AbstractContingencyBlindDetector() {
            @Override
            public void checkCurrent(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(null, branch, side, consumer);
            }

            @Override
            public void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(null, bus, consumer);
            }

            @Override
            public void checkActivePower(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkApparentPower(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
            }
        };
    }

    private LimitViolationDetector contingencyBasedDetector() {
        return new AbstractLimitViolationDetector() {
            @Override
            public void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
                doCheckCurrent(contingency, branch, side, consumer);
            }

            @Override
            public void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
                doCheckVoltage(contingency, bus, consumer);
            }

            @Override
            public void checkActivePower(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkApparentPower(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
            }

            @Override
            public void checkPermanentLimit(Branch<?> branch, Branch.Side side, double value, Consumer<LimitViolation> consumer, LimitType type) {

            }
        };
    }

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        line1 = network.getBranch("NHV1_NHV2_1");
        line2 = network.getBranch("NHV1_NHV2_2");
        transformer = network.getBranch("NGEN_NHV1");
        voltageLevel1 = network.getVoltageLevel("VLHV1");
        genVoltageLevel = network.getVoltageLevel("VLGEN");
        loadVoltageLevel = network.getVoltageLevel("VLLOAD");
        contingency1 = new Contingency("cont1");
        contingency2 = new Contingency("cont2");

        collectedViolations = new ArrayList<>();
    }

    @Test
    public void networkHas5Violations() {
        contingencyBasedDetector().checkAll(network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getSubjectId().equals("NHV1_NHV2_1")).count());
    }

    @Test
    public void branch1Has2Violations() {
        contingencyBasedDetector().checkCurrent(network.getLine("NHV1_NHV2_1"), collectedViolations::add);
        assertEquals(2, collectedViolations.size());
    }

    @Test
    public void branch2Has1Violation() {
        contingencyBasedDetector().checkCurrent(network.getLine("NHV1_NHV2_2"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    public void voltageLevel1Has1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VLHV1"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.HIGH_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    public void voltageLevelGenHas1Violation() {
        contingencyBasedDetector().checkVoltage(network.getVoltageLevel("VLGEN"), collectedViolations::add);
        assertEquals(1, collectedViolations.size());
        assertSame(LimitViolationType.LOW_VOLTAGE, collectedViolations.get(0).getLimitType());
    }

    @Test
    public void networkHas7ViolationsOnContingency1() {
        contingencyBasedDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(7, collectedViolations.size());
        assertEquals(4, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(2, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    public void networkHas5ViolationsOnContingency2() {
        contingencyBasedDetector().checkAll(contingency2, network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    public void networkHas5ViolationsOnContingency1WithContingencyBlindDetector() {
        contingencyBlindDetector().checkAll(contingency1, network, collectedViolations::add);
        assertEquals(5, collectedViolations.size());
        assertEquals(3, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.CURRENT).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.LOW_VOLTAGE).count());
        assertEquals(1, collectedViolations.stream().filter(l -> l.getLimitType() == LimitViolationType.HIGH_VOLTAGE).count());
    }

    @Test
    public void transformerHas1ViolationOnContingency1() {
        contingencyBasedDetector().checkCurrent(contingency1, transformer, collectedViolations::add);
        assertEquals(1, collectedViolations.size());
    }

    @Test
    public void transformerHasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkCurrent(contingency1, transformer, collectedViolations::add);
        detector.checkCurrent(contingency1, transformer, Branch.Side.ONE, collectedViolations::add);
        detector.checkCurrent(contingency1, transformer, Branch.Side.ONE, 5000, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }

    @Test
    public void loadVoltageLevelHasNoViolationOnContingency1WithContingencyBlindDetector() {
        LimitViolationDetector detector = contingencyBlindDetector();
        detector.checkVoltage(contingency1, loadVoltageLevel, collectedViolations::add);
        Bus loadBus = loadVoltageLevel.getBusView().getBusStream().findFirst().orElseThrow(AssertionError::new);
        detector.checkVoltage(contingency1, loadBus, collectedViolations::add);
        detector.checkVoltage(contingency1, loadBus, 500, collectedViolations::add);
        assertEquals(0, collectedViolations.size());
    }

}
