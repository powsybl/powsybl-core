/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.limitsreduction.criterion.duration.PermanentDurationCriterion;
import com.powsybl.security.limitsreduction.criterion.network.LineCriterion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class ReducedLimitsComputerTest {
    private static ReducedLimitsComputer computer;
    private static Network network;

    @BeforeAll
    static void init() {
        network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

        LimitReductionDefinitionList.LimitReductionDefinition definition1 = new LimitReductionDefinitionList.LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.9)
                .setNetworkElementCriteria(new LineCriterion(Set.of("NHV1_NHV2_1")))
                .setContingencyContexts(ContingencyContext.specificContingency("contingency1"))
                .setDurationCriteria(new PermanentDurationCriterion());
        LimitReductionDefinitionList.LimitReductionDefinition definition2 = new LimitReductionDefinitionList.LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.5)
                .setNetworkElementCriteria(new LineCriterion(Set.of("NHV1_NHV2_2")));
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList()
                .setLimitReductionDefinitions(definition1, definition2);
        computer = new ReducedLimitsComputer(definitionList);
    }

    @Test
    void applyReductionsTest() {
        // pre-contingency
        computer.changeContingencyId(null);
        // - No reductions apply for "NHV1_NHV2_1"
        computeAndCheckLimitsOnLine1WithoutReductions();
        // - Some reductions apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2();

        // contingency0
        computer.changeContingencyId("contingency0");
        // - Same reductions as before apply for both network elements => the cache is used.
        computeAndCheckLimitsOnLine1WithoutReductions();
        computeAndCheckLimitsOnLine2();

        // contingency1
        computer.changeContingencyId("contingency1");
        // - Some reductions apply for "NHV1_NHV2_1", but only for permanent limits
        Optional<LoadingLimits> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(450, optLimits.get().getPermanentLimit(), 0.01);
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(990, optLimits.get().getPermanentLimit(), 0.01);
        assertEquals(1200, optLimits.get().getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, optLimits.get().getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, optLimits.get().getTemporaryLimitValue(0), 0.01);
        // - Same reductions as before apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2();
    }

    private static void computeAndCheckLimitsOnLine1WithoutReductions() {
        Optional<LoadingLimits> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(500, optLimits.get().getPermanentLimit(), 0.01);
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(1100, optLimits.get().getPermanentLimit(), 0.01);
        assertEquals(1200, optLimits.get().getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, optLimits.get().getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, optLimits.get().getTemporaryLimitValue(0), 0.01);
    }

    private static void computeAndCheckLimitsOnLine2() {
        Optional<LoadingLimits> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(550, optLimits.get().getPermanentLimit(), 0.01);
        assertEquals(600, optLimits.get().getTemporaryLimitValue(20 * 60), 0.01);
        assertEquals(Double.MAX_VALUE, optLimits.get().getTemporaryLimitValue(60), 0.01);
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_2"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(250, optLimits.get().getPermanentLimit(), 0.01);
    }

    @Test
    void noLimitsToReduceTest() {
        Optional<LoadingLimits> optLimits = computer.getLimitsWithAppliedReduction(network.getTwoWindingsTransformer("NGEN_NHV1"),
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
    void noChangesTest(String desc, ReducedLimitsComputer noChangesComputer) {
        // In this test, no effective reductions were defined (either no definitions were used in the computer or
        // their values are all equal to 1.0).
        Optional<LoadingLimits> optLimits = noChangesComputer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"),
                LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(1100, optLimits.get().getPermanentLimit(), 0.01);
        assertEquals(1200, optLimits.get().getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, optLimits.get().getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, optLimits.get().getTemporaryLimitValue(0), 0.01);
    }

    static Stream<Arguments> getNoChangesComputers() {
        ReducedLimitsComputer noDefComputer = new ReducedLimitsComputer(new LimitReductionDefinitionList());
        LimitReductionDefinitionList.LimitReductionDefinition definition1 = new LimitReductionDefinitionList.LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(1.)
                .setNetworkElementCriteria(new LineCriterion(Set.of("NHV1_NHV2_1")))
                .setContingencyContexts(ContingencyContext.all());
        LimitReductionDefinitionList.LimitReductionDefinition definition2 = new LimitReductionDefinitionList.LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(1.)
                .setNetworkElementCriteria(new LineCriterion(Set.of("NHV1_NHV2_1")))
                .setContingencyContexts(ContingencyContext.all());
        LimitReductionDefinitionList reductionsTo1List = new LimitReductionDefinitionList()
                .setLimitReductionDefinitions(definition1, definition2);
        ReducedLimitsComputer reductionsTo1Computer = new ReducedLimitsComputer(reductionsTo1List);
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
        assertEquals(applicableForPreContingency, computer.isContingencyContextListApplicable(contingencyContexts, null));
        assertEquals(applicableForContingency1, computer.isContingencyContextListApplicable(contingencyContexts, "contingency1"));
        assertEquals(applicableForContingency2, computer.isContingencyContextListApplicable(contingencyContexts, "contingency2"));
        assertEquals(applicableForContingency3, computer.isContingencyContextListApplicable(contingencyContexts, "contingency3"));
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
