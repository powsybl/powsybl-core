/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.AtLeastOneNominalVoltageCriterion;
import com.powsybl.iidm.criteria.IdentifiableCriterion;
import com.powsybl.iidm.criteria.NetworkElementIdListCriterion;
import com.powsybl.iidm.criteria.VoltageInterval;
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class DefaultLimitReductionsApplierTest {
    private static DefaultLimitReductionsApplier applier;
    private static Network network;

    @BeforeAll
    static void init() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

        LimitReduction reduction1 = LimitReduction.builder(LimitType.CURRENT, 0.9)
                .withMonitoringOnly(false)
                .withContingencyContext(ContingencyContext.specificContingency("contingency1"))
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
                .withLimitDurationCriteria(new PermanentDurationCriterion())
                .build();
        LimitReduction reduction2 = LimitReduction.builder(LimitType.CURRENT, 0.5)
                .withMonitoringOnly(false)
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
                .build();
        LimitReduction reduction3 = LimitReduction.builder(LimitType.CURRENT, 0.1)
                .withMonitoringOnly(false)
                .withContingencyContext(ContingencyContext.specificContingency("contingency3"))
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
                .build();
        LimitReduction reduction4 = LimitReduction.builder(LimitType.CURRENT, 0.75)
                .withMonitoringOnly(false)
                .withContingencyContext(ContingencyContext.specificContingency("contingency4"))
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
                .withLimitDurationCriteria(new EqualityTemporaryDurationCriterion(60))
                .build();
        LimitReduction reduction5 = LimitReduction.builder(LimitType.CURRENT, 0.1)
                .withMonitoringOnly(false)
                .withContingencyContext(ContingencyContext.specificContingency("contingency5"))
                // Applicable only for the 2 winding transformer NHV2_NLOAD on Side 2
                .withNetworkElementCriteria(new IdentifiableCriterion(new AtLeastOneNominalVoltageCriterion(
                        VoltageInterval.between(150., 160., true, true))))
                .build();
        LimitReduction reduction6 = LimitReduction.builder(LimitType.CURRENT, 0.2)
                .withMonitoringOnly(true)
                .build();
        applier = new DefaultLimitReductionsApplier(List.of(reduction1, reduction2, reduction3, reduction4, reduction5, reduction6));
    }

    @Test
    void applyReductionsTest() {
        // pre-contingency
        applier.setWorkingContingency(null);
        // - No reductions apply for "NHV1_NHV2_1"
        computeAndCheckLimitsOnLine1WithoutReductions();
        // - Some reductions apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2(0.5, false);
        computeAndCheckLimitsOnLine2(0.2, true);

        // contingency0
        applier.setWorkingContingency("contingency0");
        // - Same reductions as before apply for both network elements => the cache is used.
        computeAndCheckLimitsOnLine1WithoutReductions();
        computeAndCheckLimitsOnLine2(0.5, false);
        computeAndCheckLimitsOnLine2(0.2, true);

        // contingency1
        applier.setWorkingContingency("contingency1");
        // - Some reductions apply for "NHV1_NHV2_1", but only for permanent limits
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE, false);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        assertEquals(450, container.getLimits().getPermanentLimit(), 0.01);
        limits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertFalse(limits.isEmpty());
        container = limits.stream().findFirst().orElseThrow();
        LoadingLimits reducedLimits = container.getLimits();
        assertEquals(990, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200, reducedLimits.getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        // - Same reductions as before apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2(0.5, false);
        computeAndCheckLimitsOnLine2(0.2, true);
    }

    private static void computeAndCheckLimitsOnLine1WithoutReductions() {
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE, false);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        assertEquals(500, container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(500, container.getOriginalLimits().getPermanentLimit(), 0.01);

        limits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertFalse(limits.isEmpty());
        container = limits.stream().findFirst().orElseThrow();
        checkOriginalLimitsOnLine1(container.getLimits());
        checkOriginalLimitsOnLine1(container.getOriginalLimits());
        assertFalse(container.isDistinct());
    }

    private static void checkOriginalLimitsOnLine1(LoadingLimits limits) {
        assertEquals(1100, limits.getPermanentLimit(), 0.01);
        assertEquals(1200, limits.getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, limits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, limits.getTemporaryLimitValue(0), 0.01);
    }

    private static void checkOriginalLimitsOnLine2(LoadingLimits limits) {
        assertEquals(1100, limits.getPermanentLimit(), 0.01);
        assertEquals(1200, limits.getTemporaryLimitValue(20 * 60), 0.01);
        assertEquals(Double.MAX_VALUE, limits.getTemporaryLimitValue(60), 0.01);
    }

    private static void computeAndCheckLimitsOnLine2(double expectedReduction, boolean monitoringOnly) {
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.ONE, monitoringOnly);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        LoadingLimits reducedLimits = container.getLimits();
        assertEquals(1100 * expectedReduction, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200 * expectedReduction, reducedLimits.getTemporaryLimitValue(20 * 60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(60), 0.01);
        checkOriginalLimitsOnLine2(container.getOriginalLimits());
        assertTrue(container.isDistinct());

        limits = applier.computeLimits(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.TWO, monitoringOnly);
        assertFalse(limits.isEmpty());
        container = limits.stream().findFirst().orElseThrow();
        assertEquals(500 * expectedReduction, container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(500, container.getOriginalLimits().getPermanentLimit(), 0.01);
        assertTrue(container.isDistinct());
    }

    @Test
    void severalApplicableReductionsTest() {
        applier.setWorkingContingency("contingency3");
        // Several reductions apply for line2 (with 0.5 and 0.1 reductions), only the last is used.
        computeAndCheckLimitsOnLine2(0.1, false);
    }

    @Test
    void temporaryLimitToRemoveTest() {
        applier.setWorkingContingency("contingency4");
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        LoadingLimits reducedLimits = container.getLimits();
        assertEquals(1100, reducedLimits.getPermanentLimit(), 0.01);
        assertTrue(Double.isNaN(reducedLimits.getTemporaryLimitValue(10 * 60))); // removed since the 1' limit's reduced value is < 1200
        assertEquals(1125, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        checkOriginalLimitsOnLine1(container.getOriginalLimits());
        assertTrue(container.isDistinct());
    }

    @Test
    void noLimitsToReduceTest() {
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(network.getTwoWindingsTransformer("NGEN_NHV1"),
                LimitType.CURRENT, ThreeSides.ONE, false);
        // There are no limits on "NGEN_NHV1" => no reduced limits.
        assertTrue(limits.isEmpty());
    }

    @Test
    void reduceOnOneSideOnlyTest() {
        applier.setWorkingContingency("contingency5");
        TwoWindingsTransformer nhv2Nload = network.getTwoWindingsTransformer("NHV2_NLOAD");
        nhv2Nload.getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setValue(1200.)
                    .setAcceptableDuration(60)
                    .setName("60'")
                .endTemporaryLimit()
                .add();
        nhv2Nload.getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                    .setValue(1200.)
                    .setAcceptableDuration(60)
                    .setName("60'")
                .endTemporaryLimit()
                .add();
        // The reduction only applies on side 2 for NHV2_NLOAD
        Collection<LimitsContainer<LoadingLimits>> limits = applier.computeLimits(nhv2Nload,
                LimitType.CURRENT, ThreeSides.ONE, false);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        assertEquals(1000., container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(1200., container.getLimits().getTemporaryLimitValue(60), 0.01);
        assertFalse(container.isDistinct());

        // The reduction only applies on side 1 for NHV2_NLOAD
        limits = applier.computeLimits(nhv2Nload, LimitType.CURRENT, ThreeSides.TWO, false);
        assertFalse(limits.isEmpty());
        container = limits.stream().findFirst().orElseThrow();
        assertEquals(1000., container.getOriginalLimits().getPermanentLimit(), 0.01);
        assertEquals(100., container.getLimits().getPermanentLimit(), 0.01);
        assertEquals(1200., container.getOriginalLimits().getTemporaryLimitValue(60), 0.01);
        assertEquals(120., container.getLimits().getTemporaryLimitValue(60), 0.01);
        assertTrue(container.isDistinct());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getNoChangesComputers")
    void noChangesTest(String desc, DefaultLimitReductionsApplier noChangesComputer) {
        // In this test, no effective reductions were defined (either no reductions were used in the computer or
        // their values are all equal to 1.0).
        Collection<LimitsContainer<LoadingLimits>> limits = noChangesComputer.computeLimits(network.getLine("NHV1_NHV2_1"),
                LimitType.CURRENT, ThreeSides.TWO, false);
        assertFalse(limits.isEmpty());
        LimitsContainer<LoadingLimits> container = limits.stream().findFirst().orElseThrow();
        checkOriginalLimitsOnLine1(container.getLimits());
        checkOriginalLimitsOnLine1(container.getOriginalLimits());
    }

    static Stream<Arguments> getNoChangesComputers() {
        DefaultLimitReductionsApplier noReductionComputer = new DefaultLimitReductionsApplier(Collections.emptyList());
        LimitReduction reduction1 = LimitReduction.builder(LimitType.CURRENT, 1.)
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
                .build();
        LimitReduction reduction2 = LimitReduction.builder(LimitType.CURRENT, 1.)
                .withNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")))
                .build();
        DefaultLimitReductionsApplier reductionsTo1Computer = new DefaultLimitReductionsApplier(List.of(reduction1, reduction2));
        return Stream.of(
                Arguments.of("No reductions", noReductionComputer),
                Arguments.of("Reductions to 1.0", reductionsTo1Computer)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getContingencyContextListsData")
    void isContingencyContextListApplicableTest(String desc, ContingencyContext contingencyContext,
                                                boolean applicableForPreContingency,
                                                boolean applicableForContingency1, boolean applicableForContingency2) {
        assertEquals(applicableForPreContingency, AbstractLimitReductionsApplier.isContingencyInContingencyContext(contingencyContext, null));
        assertEquals(applicableForContingency1, AbstractLimitReductionsApplier.isContingencyInContingencyContext(contingencyContext, "contingency1"));
        assertEquals(applicableForContingency2, AbstractLimitReductionsApplier.isContingencyInContingencyContext(contingencyContext, "contingency2"));
    }

    static Stream<Arguments> getContingencyContextListsData() {
        ContingencyContext c1 = ContingencyContext.specificContingency("contingency1");
        ContingencyContext c2 = ContingencyContext.specificContingency("contingency2");
        return Stream.of(
                Arguments.of("(empty)", null, true, true, true),
                Arguments.of("all", ContingencyContext.all(), true, true, true),
                Arguments.of("none", ContingencyContext.none(), true, false, false),
                Arguments.of("only contingencies", ContingencyContext.onlyContingencies(), false, true, true),
                Arguments.of("c1", c1, false, true, false),
                Arguments.of("c2", c2, false, false, true)
        );
    }

}
