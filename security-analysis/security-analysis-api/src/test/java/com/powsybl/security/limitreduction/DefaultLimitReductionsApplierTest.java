/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.criteria.NetworkElementIdListCriterion;
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        LimitReduction reduction5 = LimitReduction.builder(LimitType.CURRENT, 0.2)
                .withMonitoringOnly(true)
                .build();
        applier = new DefaultLimitReductionsApplier(List.of(reduction1, reduction2, reduction3, reduction4, reduction5));
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
        Optional<LimitsContainer<LoadingLimits>> optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE, false);
        assertTrue(optLimits.isPresent());
        assertEquals(450, optLimits.get().getLimits().getPermanentLimit(), 0.01);
        optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getLimits();
        assertEquals(990, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200, reducedLimits.getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        // - Same reductions as before apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2(0.5, false);
        computeAndCheckLimitsOnLine2(0.2, true);
    }

    private static void computeAndCheckLimitsOnLine1WithoutReductions() {
        Optional<LimitsContainer<LoadingLimits>> optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE, false);
        assertTrue(optLimits.isPresent());
        assertEquals(500, optLimits.get().getLimits().getPermanentLimit(), 0.01);
        assertEquals(500, optLimits.get().getOriginalLimits().getPermanentLimit(), 0.01);

        optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertTrue(optLimits.isPresent());
        checkOriginalLimitsOnLine1(optLimits.get().getLimits());
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
        assertFalse(optLimits.get().hasChanged());
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
        Optional<LimitsContainer<LoadingLimits>> optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.ONE, monitoringOnly);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getLimits();
        assertEquals(1100 * expectedReduction, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200 * expectedReduction, reducedLimits.getTemporaryLimitValue(20 * 60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(60), 0.01);
        checkOriginalLimitsOnLine2(optLimits.get().getOriginalLimits());
        assertTrue(optLimits.get().hasChanged());

        optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.TWO, monitoringOnly);
        assertTrue(optLimits.isPresent());
        assertEquals(500 * expectedReduction, optLimits.get().getLimits().getPermanentLimit(), 0.01);
        assertEquals(500, optLimits.get().getOriginalLimits().getPermanentLimit(), 0.01);
        assertTrue(optLimits.get().hasChanged());
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
        Optional<LimitsContainer<LoadingLimits>> optLimits = applier.computeLimits(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO, false);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getLimits();
        assertEquals(1100, reducedLimits.getPermanentLimit(), 0.01);
        assertTrue(Double.isNaN(reducedLimits.getTemporaryLimitValue(10 * 60))); // removed since the 1' limit's reduced value is < 1200
        assertEquals(1125, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
        assertTrue(optLimits.get().hasChanged());
    }

    @Test
    void noLimitsToReduceTest() {
        Optional<LimitsContainer<LoadingLimits>> optLimits = applier.computeLimits(network.getTwoWindingsTransformer("NGEN_NHV1"),
                LimitType.CURRENT, ThreeSides.ONE, false);
        // There are no limits on "NGEN_NHV1" => no reduced limits.
        assertTrue(optLimits.isEmpty());
    }

    @Test
    void sameReductionsAsPreviousContingencyTest() {
        applier.setWorkingContingency(null); // pre-contingency
        boolean sameAsBefore = applier.setWorkingContingency("contingency2");
        // No specific reductions were defined for contingency2 => the same reductions apply.
        assertTrue(sameAsBefore);
        assertTrue(applier.isSameReductionsAsForPreviousContingencyId());
        sameAsBefore = applier.setWorkingContingency("contingency1");
        // A specific reduction was defined for contingency1 => the reductions which apply are not the same.
        assertFalse(sameAsBefore);
        assertFalse(applier.isSameReductionsAsForPreviousContingencyId());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getNoChangesComputers")
    void noChangesTest(String desc, DefaultLimitReductionsApplier noChangesComputer) {
        // In this test, no effective reductions were defined (either no reductions were used in the computer or
        // their values are all equal to 1.0).
        Optional<LimitsContainer<LoadingLimits>> optLimits = noChangesComputer.computeLimits(network.getLine("NHV1_NHV2_1"),
                LimitType.CURRENT, ThreeSides.TWO, false);
        assertTrue(optLimits.isPresent());
        checkOriginalLimitsOnLine1(optLimits.get().getLimits());
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
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
        assertEquals(applicableForPreContingency, AbstractLimitReductionsApplier.isContingencyContextListApplicable(contingencyContext, null));
        assertEquals(applicableForContingency1, AbstractLimitReductionsApplier.isContingencyContextListApplicable(contingencyContext, "contingency1"));
        assertEquals(applicableForContingency2, AbstractLimitReductionsApplier.isContingencyContextListApplicable(contingencyContext, "contingency2"));
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
