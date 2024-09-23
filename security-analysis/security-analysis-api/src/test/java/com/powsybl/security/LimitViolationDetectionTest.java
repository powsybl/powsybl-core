/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.NetworkElementIdListCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.detectors.AbstractLimitViolationDetectionTest;
import com.powsybl.security.detectors.LoadingLimitType;
import com.powsybl.security.limitreduction.DefaultLimitReductionsApplier;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.limitreduction.SimpleLimitsComputer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitViolationDetectionTest extends AbstractLimitViolationDetectionTest {

    @BeforeEach
    void setUp() {
        violationsCollector = new ArrayList<>();
    }

    @Override
    protected void checkLimitViolation(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer,
                                       LimitType limitType, double limitReduction) {
        LimitViolationDetection.checkLimitViolation(branch, side, currentValue, limitType, EnumSet.allOf(LoadingLimitType.class), new SimpleLimitsComputer(limitReduction), consumer
        );
    }

    private void checkLimitViolation(ThreeWindingsTransformer transfo, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer,
                                       LimitType limitType) {
        LimitViolationDetection.checkLimitViolation(transfo, side, currentValue, limitType, EnumSet.allOf(LoadingLimitType.class), new LimitsComputer.NoModificationsImpl(), consumer);
    }

    private void checkLimitViolation(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer,
                                     LimitType limitType, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer) {
        LimitViolationDetection.checkLimitViolation(branch, side, currentValue, limitType, EnumSet.allOf(LoadingLimitType.class), limitsComputer, consumer);
    }

    protected void checkCurrent(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer) {
        checkLimitViolation(branch, side, currentValue, consumer, LimitType.CURRENT, limitsComputer);
    }

    @Override
    protected void checkCurrent(Branch<?> branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
        checkLimitViolation(branch, side, currentValue, consumer, LimitType.CURRENT, 1.0);
    }

    @Override
    protected void checkCurrent(ThreeWindingsTransformer transfo, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
        checkLimitViolation(transfo, side, currentValue, consumer, LimitType.CURRENT);
    }

    @Override
    protected void checkActivePower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(branch, side, value, consumer, LimitType.ACTIVE_POWER, 1.0);
    }

    @Override
    protected void checkActivePower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(transfo, side, value, consumer, LimitType.ACTIVE_POWER);
    }

    @Override
    protected void checkApparentPower(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(branch, side, value, consumer, LimitType.APPARENT_POWER, 1.0);
    }

    @Override
    protected void checkApparentPower(ThreeWindingsTransformer transfo, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(transfo, side, value, consumer, LimitType.APPARENT_POWER);
    }

    @Override
    protected void checkVoltage(Bus b, int voltageValue, Consumer<LimitViolation> consumer) {
        LimitViolationDetection.checkVoltage(b, voltageValue, consumer);
    }

    @Override
    protected void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
        LimitViolationDetection.checkVoltageAngle(voltageAngleLimit, voltageAngleDifference, consumer);
    }

    @Test
    void testVoltageViolationDetection() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        LimitViolationDetection.checkVoltage(((Bus) network.getIdentifiable("NHV2")), 620, violationsCollector::add);
        Assertions.assertEquals(1, violationsCollector.size());
        Assertions.assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
        Assertions.assertEquals(620.0, violationsCollector.get(0).getValue());
        Assertions.assertEquals(500, violationsCollector.get(0).getLimit());
        Assertions.assertEquals("VLHV2", violationsCollector.get(0).getSubjectId());
    }

    @Test
    void testVoltageViolationDetectionWithBusId() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        LimitViolationDetection.checkVoltage(((Bus) network.getIdentifiable("NHV2")), 620, violationsCollector::add,
            LimitViolationUtils.VoltageLimitViolationIdType.BUS_ID);
        Assertions.assertEquals(1, violationsCollector.size());
        Assertions.assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
        Assertions.assertEquals(620.0, violationsCollector.get(0).getValue());
        Assertions.assertEquals(500, violationsCollector.get(0).getLimit());
        Assertions.assertEquals("NHV2", violationsCollector.get(0).getSubjectId());
    }

    @Test
    void testVoltageViolationDetectionWithBusBarIds() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        LimitViolationDetection.checkVoltage(network.getBusView().getBus("S1VL1_0"), 300,
            violationsCollector::add, LimitViolationUtils.VoltageLimitViolationIdType.BUSBAR_IDS);
        Assertions.assertEquals(1, violationsCollector.size());
        Assertions.assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
        Assertions.assertEquals(300, violationsCollector.get(0).getValue());
        Assertions.assertEquals(240, violationsCollector.get(0).getLimit());
        Assertions.assertEquals("S1VL1_BBS", violationsCollector.get(0).getSubjectId());
    }

    @Test
    void testLimitsComputer() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        List<LimitReduction> limitReductionList = new ArrayList<>();
        LimitReduction reduction1 = LimitReduction.builder(LimitType.CURRENT, 0.5)
            .withMonitoringOnly(false)
            .withContingencyContext(ContingencyContext.none())
            .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
            .withLimitDurationCriteria(new PermanentDurationCriterion())
            .build();
        limitReductionList.add(reduction1);
        DefaultLimitReductionsApplier computer = new DefaultLimitReductionsApplier(limitReductionList);
        checkCurrent(network.getLine("NHV1_NHV2_1"), TwoSides.ONE, 315, violationsCollector::add, computer);
        Assertions.assertEquals(1, violationsCollector.size());
        Assertions.assertEquals(0.5, violationsCollector.get(0).getLimitReduction());
        Assertions.assertEquals(Integer.MAX_VALUE, violationsCollector.get(0).getAcceptableDuration());
        Assertions.assertEquals(315, violationsCollector.get(0).getValue(), 0.01);
        Assertions.assertEquals(500., violationsCollector.get(0).getLimit(), 0.01);
        Assertions.assertEquals(ThreeSides.ONE, violationsCollector.get(0).getSide());
    }
}
