/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction.criterion.duration;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList;
import com.powsybl.security.limitsreduction.ReducedLimitsComputer;
import com.powsybl.security.limitsreduction.criterion.network.LineCriterion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
                .setContingencyContexts(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC))
                .setDurationCriteria(new PermanentDurationCriterion());
        LimitReductionDefinitionList.LimitReductionDefinition definition2 = new LimitReductionDefinitionList.LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.5)
                .setNetworkElementCriteria(new LineCriterion(Set.of("NHV1_NHV2_2")));
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList();
        definitionList.addLimitReductionDefinitions(definition1, definition2);
        computer = new ReducedLimitsComputer(definitionList);
    }

    @Test
    void applyReductionsTest() {
        // pre-contingency
        computer.changeContingencyId(null);
        // - No reductions apply for "NHV1_NHV2_1"
        Optional<LoadingLimits> optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
        assertTrue(optLimits.isPresent());
        assertEquals(500, optLimits.get().getPermanentLimit(), 0.01);
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.TWO);
        assertTrue(optLimits.isPresent());
        assertEquals(1100, optLimits.get().getPermanentLimit(), 0.01);
        assertEquals(1200, optLimits.get().getTemporaryLimitValue(10 * 60), 0.01);
        assertEquals(1500, optLimits.get().getTemporaryLimitValue(60), 0.01);
        assertEquals(Double.MAX_VALUE, optLimits.get().getTemporaryLimitValue(0), 0.01);
        // - Some reductions apply for "NHV1_NHV2_2"
        computeAndCheckLimitsOnLine2();

        // contingency1
        computer.changeContingencyId("contingency1");
        // - Some reductions apply for "NHV1_NHV2_1", but only for permanent limits
        optLimits = computer.getLimitsWithAppliedReduction(network.getLine("NHV1_NHV2_1"), LimitType.CURRENT, ThreeSides.ONE);
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
                .addLimitReductionDefinitions(definition1, definition2);
        ReducedLimitsComputer reductionsTo1Computer = new ReducedLimitsComputer(reductionsTo1List);
        return Stream.of(
                Arguments.of("No definitions", noDefComputer),
                Arguments.of("Reductions to 1.0", reductionsTo1Computer)
        );
    }
}
