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
class DefaultReducedLimitsComputerImplTest {
    private static DefaultReducedLimitsComputerImpl computer;
    private static Network network;

    @BeforeAll
    static void init() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

        LimitReductionDefinition definition1 = new LimitReductionDefinition(LimitType.CURRENT, 0.9f,
                List.of(ContingencyContext.specificContingency("contingency1")),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"))),
                List.of(new PermanentDurationCriterion()));
        LimitReductionDefinition definition2 = new LimitReductionDefinition(LimitType.CURRENT, 0.5f,
                Collections.emptyList(),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2"))),
                Collections.emptyList());
        LimitReductionDefinition definition3 = new LimitReductionDefinition(LimitType.CURRENT, 0.1f,
                List.of(ContingencyContext.specificContingency("contingency3")),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2"))),
                Collections.emptyList());
        LimitReductionDefinition definition4 = new LimitReductionDefinition(LimitType.CURRENT, 0.75f,
                List.of(ContingencyContext.specificContingency("contingency4")),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"))),
                List.of(new EqualityTemporaryDurationCriterion(60)));
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList(List.of(definition1, definition2, definition3, definition4));
        computer = new DefaultReducedLimitsComputerImpl(definitionList);
    }

    @Test
    void applyReductionsTest() {
        // pre-contingency
        computer.changeContingencyId(null);
        // - No reductions apply for "NHV1_NHV2_1"
        computeAndCheckLimitsOnLine1WithoutReductions();
        // - Some reductions apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2(0.5);

        // contingency0
        computer.changeContingencyId("contingency0");
        // - Same reductions as before apply for both network elements => the cache is used.
        computeAndCheckLimitsOnLine1WithoutReductions();
        computeAndCheckLimitsOnLine2(0.5);

        // contingency1
        computer.changeContingencyId("contingency1");
        // - Some reductions apply for "NHV1_NHV2_1", but only for permanent limits
        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(450, optLimits.get().getReducedLimits().getPermanentLimit(), 0.01);
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getReducedLimits();
        assertEquals(990, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200, reducedLimits.getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        // - Same reductions as before apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2(0.5);
    }

    private static void computeAndCheckLimitsOnLine1WithoutReductions() {
        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(500, optLimits.get().getReducedLimits().getPermanentLimit(), 0.01);
        assertEquals(500, optLimits.get().getOriginalLimits().getPermanentLimit(), 0.01);

        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        checkOriginalLimitsOnLine1(optLimits.get().getReducedLimits());
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
        assertTrue(optLimits.get().isReducedSameAsOriginal());
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

    private static void computeAndCheckLimitsOnLine2(double expectedReduction) {
        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getReducedLimits();
        assertEquals(1100 * expectedReduction, reducedLimits.getPermanentLimit(), 0.01);
        assertEquals(1200 * expectedReduction, reducedLimits.getTemporaryLimitValue(20 * 60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(60), 0.01);
        checkOriginalLimitsOnLine2(optLimits.get().getOriginalLimits());
        assertFalse(optLimits.get().isReducedSameAsOriginal());

        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(500 * expectedReduction, optLimits.get().getReducedLimits().getPermanentLimit(), 0.01);
        assertEquals(500, optLimits.get().getOriginalLimits().getPermanentLimit(), 0.01);
        assertFalse(optLimits.get().isReducedSameAsOriginal());
    }

    @Test
    void severalApplicableDefinitionsTest() {
        computer.changeContingencyId("contingency3");
        // Several definitions apply for line2 (with 0.5 and 0.1 reductions), only the last is used.
        computeAndCheckLimitsOnLine2(0.1);
    }

    @Test
    void temporaryLimitToRemoveTest() {
        computer.changeContingencyId("contingency4");
        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        LoadingLimits reducedLimits = optLimits.get().getReducedLimits();
        assertEquals(1100, reducedLimits.getPermanentLimit(), 0.01);
        assertTrue(Double.isNaN(reducedLimits.getTemporaryLimitValue(10 * 60))); // removed since the 1' limit's reduced value is < 1200
        assertEquals(1125, reducedLimits.getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, reducedLimits.getTemporaryLimitValue(0), 0.01);
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
        assertFalse(optLimits.get().isReducedSameAsOriginal());
    }

    @Test
    void noLimitsToReduceTest() {
        Optional<LimitsContainer<LoadingLimits>> optLimits = computer.getLimitsWithAppliedReduction(network.getTwoWindingsTransformer("NGEN_NHV1"),
                LimitType.CURRENT, ThreeSides.ONE);
        // There are no limits on "NGEN_NHV1" => no reduced limits.
        assertTrue(optLimits.isEmpty());
    }

    @Test
    void sameDefinitionsAsPreviousContingencyTest() {
        computer.changeContingencyId(null); // pre-contingency
        boolean sameAsBefore = computer.changeContingencyId("contingency2");
        // No specific definitions were defined for contingency2 => the same reductions apply.
        assertTrue(sameAsBefore);
        assertTrue(computer.isSameDefinitionsAsForPreviousContingencyId());
        sameAsBefore = computer.changeContingencyId("contingency1");
        // A specific definition was defined for contingency1 => the reductions which apply are not the same.
        assertFalse(sameAsBefore);
        assertFalse(computer.isSameDefinitionsAsForPreviousContingencyId());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getNoChangesComputers")
    void noChangesTest(String desc, DefaultReducedLimitsComputerImpl noChangesComputer) {
        // In this test, no effective reductions were defined (either no definitions were used in the computer or
        // their values are all equal to 1.0).
        Optional<LimitsContainer<LoadingLimits>> optLimits = noChangesComputer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"),
                LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        checkOriginalLimitsOnLine1(optLimits.get().getReducedLimits());
        checkOriginalLimitsOnLine1(optLimits.get().getOriginalLimits());
    }

    static Stream<Arguments> getNoChangesComputers() {
        DefaultReducedLimitsComputerImpl noDefComputer = new DefaultReducedLimitsComputerImpl(new LimitReductionDefinitionList(Collections.emptyList()));
        LimitReductionDefinition definition1 = new LimitReductionDefinition(LimitType.CURRENT, 1.f,
                List.of(ContingencyContext.all()),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"))),
                Collections.emptyList());
        LimitReductionDefinition definition2 = new LimitReductionDefinition(LimitType.CURRENT, 1.f,
                List.of(ContingencyContext.all()),
                List.of(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1"))),
                Collections.emptyList());
        LimitReductionDefinitionList reductionsTo1List = new LimitReductionDefinitionList(List.of(definition1, definition2));
        DefaultReducedLimitsComputerImpl reductionsTo1Computer = new DefaultReducedLimitsComputerImpl(reductionsTo1List);
        return Stream.of(
                Arguments.of("No definitions", noDefComputer),
                Arguments.of("Reductions to 1.0", reductionsTo1Computer)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getContingencyContextListsData")
    void isContingencyContextListApplicableTest(String desc, List<ContingencyContext> contingencyContexts,
                                                boolean applicableForPreContingency,
                                                boolean applicableForContingency1, boolean applicableForContingency2,
                                                boolean applicableForContingency3) {
        assertEquals(applicableForPreContingency, AbstractContingencyWiseReducedLimitsComputer.isContingencyContextListApplicable(contingencyContexts, null));
        assertEquals(applicableForContingency1, AbstractContingencyWiseReducedLimitsComputer.isContingencyContextListApplicable(contingencyContexts, "contingency1"));
        assertEquals(applicableForContingency2, AbstractContingencyWiseReducedLimitsComputer.isContingencyContextListApplicable(contingencyContexts, "contingency2"));
        assertEquals(applicableForContingency3, AbstractContingencyWiseReducedLimitsComputer.isContingencyContextListApplicable(contingencyContexts, "contingency3"));
    }

    static Stream<Arguments> getContingencyContextListsData() {
        ContingencyContext c1 = ContingencyContext.specificContingency("contingency1");
        ContingencyContext c2 = ContingencyContext.specificContingency("contingency2");
        ContingencyContext c3 = ContingencyContext.specificContingency("contingency3");
        return Stream.of(
                Arguments.of("(empty)", List.of(), true, true, true, true),
                Arguments.of("all", List.of(ContingencyContext.all()), true, true, true, true),
                Arguments.of("none", List.of(ContingencyContext.none()), true, false, false, false),
                Arguments.of("only contingencies", List.of(ContingencyContext.onlyContingencies()), false, true, true, true),
                Arguments.of("c1", List.of(c1), false, true, false, false),
                Arguments.of("c2", List.of(c2), false, false, true, false),
                Arguments.of("c3", List.of(c3), false, false, false, true),
                Arguments.of("none + c1", List.of(ContingencyContext.none(), c1), true, true, false, false),
                Arguments.of("c1 + c2", List.of(c1, c2), false, true, true, false)
        );
    }

}
