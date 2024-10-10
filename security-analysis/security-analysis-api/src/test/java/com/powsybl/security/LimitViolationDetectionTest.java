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
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.security.detectors.AbstractLimitViolationDetectionTest;
import com.powsybl.security.detectors.LoadingLimitType;
import com.powsybl.security.limitreduction.DefaultLimitReductionsApplier;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.limitreduction.SimpleLimitsComputer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Bus mergedBus = getMergedBusFromConfiguredBusId(network, "NHV2");
        LimitViolationDetection.checkVoltage(mergedBus, 620, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(l -> {
                assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
                assertEquals(620.0, violationsCollector.get(0).getValue());
                assertEquals(500, violationsCollector.get(0).getLimit());
                assertEquals("VLHV2", violationsCollector.get(0).getSubjectId());
            });
    }

    @Test
    void testVoltageViolationDetectionWithDetailLimitViolationId() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        // Add a new bus "NHV20" which will be merged with "NHV2" in the bs view
        VoltageLevel vlh2 = network.getVoltageLevel("VLHV2");
        vlh2.getBusBreakerView().newBus()
                .setId("NHV20")
                .add();
        vlh2.getBusBreakerView().newSwitch()
                .setBus1("NHV2").setBus2("NHV20")
                .setOpen(false).setId("DJ")
                .add();
        // Retrieve the merged bus containing "NHV2"
        Bus mergedBus = getMergedBusFromConfiguredBusId(network, "NHV2");
        // Check the voltage on this merged bus
        LimitViolationDetection.checkVoltage(mergedBus, 620, violationsCollector::add);
        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(l -> {
                assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
                assertEquals(620.0, violationsCollector.get(0).getValue());
                assertEquals(500, violationsCollector.get(0).getLimit());
                Assertions.assertThat(violationsCollector.get(0).getViolationLocation())
                    .isPresent()
                    .get()
                    .isInstanceOfSatisfying(BusBreakerViolationLocation.class,
                        vli -> {
                            assertEquals(ViolationLocation.Type.BUS_BREAKER, vli.getType());
                            assertEquals(2, vli.getBusIds().size());
                            assertTrue(vli.getBusIds().containsAll(List.of("NHV2", "NHV20"))); // Both configured buses are present
                            Set<Bus> buses = vli.getBusBreakerView(network).getBusStream().collect(Collectors.toSet());
                            assertEquals(2, buses.size());
                            assertTrue(buses.contains(network.getBusBreakerView().getBus("NHV2")));
                            assertTrue(buses.contains(network.getBusBreakerView().getBus("NHV20")));
                            Set<Bus> mergedBuses = vli.getBusView(network).getBusStream().collect(Collectors.toSet());
                            assertEquals(1, mergedBuses.size());
                        });
            });
    }

    private Bus getMergedBusFromConfiguredBusId(Network network, String busId) {
        Bus b = (Bus) network.getIdentifiable(busId);
        return b.getVoltageLevel().getBusView().getMergedBus(busId);
    }

    @Test
    void testVoltageViolationDetectionWithBusBarIds() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        LimitViolationDetection.checkVoltage(network.getBusView().getBus("S1VL1_0"), 300,
            violationsCollector::add);
        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(l -> {
                assertEquals(LimitViolationType.HIGH_VOLTAGE, violationsCollector.get(0).getLimitType());
                assertEquals(300, violationsCollector.get(0).getValue());
                assertEquals(240, violationsCollector.get(0).getLimit());
                Assertions.assertThat(violationsCollector.get(0).getViolationLocation())
                    .isPresent()
                    .get()
                    .isInstanceOfSatisfying(NodeBreakerViolationLocation.class,
                        vli -> {
                            assertEquals(ViolationLocation.Type.NODE_BREAKER, vli.getType());
                            assertEquals("S1VL1", vli.getVoltageLevelId());
                            Assertions.assertThat(vli.getNodes())
                                .hasSize(5)
                                .isEqualTo(List.of(0, 1, 2, 3, 4));
                            assertThrows(UnsupportedOperationException.class, () -> vli.getBusBreakerView(network));
                            Set<Bus> mergedBuses = vli.getBusView(network).getBusStream().collect(Collectors.toSet());
                            assertEquals(1, mergedBuses.size());
                            assertTrue(mergedBuses.contains(network.getBusView().getBus("S1VL1_0")));
                        });
            });
        // Check corresponding busbar sections
        ViolationLocation violationLocation = violationsCollector.get(0).getViolationLocation().orElseThrow();
        NodeBreakerViolationLocation vloc = (NodeBreakerViolationLocation) violationLocation;
        VoltageLevel vl = network.getVoltageLevel(vloc.getVoltageLevelId());
        List<String> busbarSectionIds = vloc.getNodes().stream()
                .map(node -> vl.getNodeBreakerView().getTerminal(node))
                .filter(Objects::nonNull)
                .map(Terminal::getConnectable)
                .filter(t -> t.getType() == IdentifiableType.BUSBAR_SECTION)
                .map(Identifiable::getId)
                .toList();
        assertEquals(1, busbarSectionIds.size());
        assertEquals("S1VL1_BBS", busbarSectionIds.get(0));
    }

    @Test
    void testPermanentLimitLimitsComputer() {
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
        Assertions.assertThat(violationsCollector)
            .hasSize(1)
            .allSatisfy(l -> {
                assertEquals(0.5, violationsCollector.get(0).getLimitReduction());
                assertEquals(Integer.MAX_VALUE, violationsCollector.get(0).getAcceptableDuration());
                assertEquals(315, violationsCollector.get(0).getValue(), 0.01);
                assertEquals(500., violationsCollector.get(0).getLimit(), 0.01);
                assertEquals(ThreeSides.ONE, violationsCollector.get(0).getSide());
            });
    }

    @Test
    void testTemporaryLimitLimitsComputer() {
        Network network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        List<LimitReduction> limitReductionList = new ArrayList<>();
        LimitReduction reduction1 = LimitReduction.builder(LimitType.CURRENT, 0.5)
            .withMonitoringOnly(false)
            .withContingencyContext(ContingencyContext.none())
            .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
            .withLimitDurationCriteria(new EqualityTemporaryDurationCriterion(60))
            .build();
        limitReductionList.add(reduction1);
        DefaultLimitReductionsApplier computer = new DefaultLimitReductionsApplier(limitReductionList);
        checkCurrent(network.getLine("NHV1_NHV2_1"), TwoSides.TWO, 751, violationsCollector::add, computer);
        assertEquals(1, violationsCollector.size());
        assertEquals(0.5, violationsCollector.get(0).getLimitReduction());
        assertEquals(0, violationsCollector.get(0).getAcceptableDuration());
        assertEquals(751, violationsCollector.get(0).getValue(), 0.01);
        assertEquals(1500, violationsCollector.get(0).getLimit(), 0.01);
        assertEquals(ThreeSides.TWO, violationsCollector.get(0).getSide());
        assertEquals("1'", violationsCollector.get(0).getLimitName());
    }
}
