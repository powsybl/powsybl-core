/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.strategy.ConditionalActions;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import com.powsybl.contingency.strategy.condition.*;
import com.powsybl.iidm.network.TerminalNumber;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.powsybl.contingency.violations.LimitViolationType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class JsonActionAndOperatorStrategyTest extends AbstractSerDeTest {

    @Test
    void operatorStrategyReadV10() {
        OperatorStrategyList operatorStrategies = OperatorStrategyList.read(getClass().getResourceAsStream("/OperatorStrategyFileTestV1.0.json"));
        assertEquals(6, operatorStrategies.getOperatorStrategies().size());
        for (var opStrategy : operatorStrategies.getOperatorStrategies()) {
            assertEquals(1, opStrategy.getConditionalActions().size());
            assertEquals("default", opStrategy.getConditionalActions().getFirst().getId());
        }
        assertEquals("contingencyId5", operatorStrategies.getOperatorStrategies().get(5).getContingencyContext().getContingencyId());
    }

    @Test
    void operatorStrategyReadV11() {
        OperatorStrategyList operatorStrategies = OperatorStrategyList.read(getClass().getResourceAsStream("/OperatorStrategyFileTestV1.1.json"));
        assertEquals(6, operatorStrategies.getOperatorStrategies().size());
        for (var opStrategy : operatorStrategies.getOperatorStrategies()) {
            assertEquals(1, opStrategy.getConditionalActions().size());
        }
        assertEquals("stage1", operatorStrategies.getOperatorStrategies().get(5).getConditionalActions().getFirst().getId());
    }

    @Test
    void operatorStrategyRoundTrip() throws IOException {
        List<OperatorStrategy> operatorStrategies = new ArrayList<>();
        operatorStrategies.add(new OperatorStrategy("id1", ContingencyContext.specificContingency("contingencyId1"),
            List.of(new ConditionalActions("stage1", new TrueCondition(), List.of("actionId1", "actionId2", "actionId3")))));
        operatorStrategies.add(new OperatorStrategy("id2", ContingencyContext.specificContingency("contingencyId2"),
            List.of(new ConditionalActions("stage1", new AnyViolationCondition(), List.of("actionId4")))));
        operatorStrategies.add(new OperatorStrategy("id3", ContingencyContext.specificContingency("contingencyId1"),
            List.of(new ConditionalActions("stage1", new AnyViolationCondition(Collections.singleton(CURRENT)),
                List.of("actionId1", "actionId3")))));
        operatorStrategies.add(new OperatorStrategy("id4", ContingencyContext.specificContingency("contingencyId3"),
            List.of(new ConditionalActions("stage1", new AnyViolationCondition(Collections.singleton(LOW_VOLTAGE)),
                List.of("actionId1", "actionId2", "actionId4")))));
        operatorStrategies.add(new OperatorStrategy("id5", ContingencyContext.specificContingency("contingencyId4"),
            List.of(new ConditionalActions("stage1", new AllViolationCondition(List.of("violation1", "violation2"),
                Collections.singleton(HIGH_VOLTAGE)),
                List.of("actionId1", "actionId5")))));
        operatorStrategies.add(new OperatorStrategy("id6", ContingencyContext.specificContingency("contingencyId5"),
            List.of(new ConditionalActions("stage1", new AllViolationCondition(List.of("violation1", "violation2")),
                List.of("actionId3")))));
        operatorStrategies.add(new OperatorStrategy("id7", ContingencyContext.specificContingency("contingencyId5"),
            List.of(
                new ConditionalActions("stage1", new BranchThresholdCondition("Line1", AbstractThresholdCondition.Variable.CURRENT, AbstractThresholdCondition.ComparisonType.GREATER_THAN, 2.0, TwoSides.ONE), List.of("actionId3")),
                new ConditionalActions("stage1", new ThreeWindingsTransformerThresholdCondition("3WTransformer1", AbstractThresholdCondition.Variable.REACTIVE_POWER, AbstractThresholdCondition.ComparisonType.NOT_EQUAL, 52.0, ThreeSides.THREE), List.of("actionId3")),
                new ConditionalActions("stage2", new InjectionThresholdCondition("Gen2", AbstractThresholdCondition.Variable.ACTIVE_POWER, AbstractThresholdCondition.ComparisonType.GREATER_THAN_OR_EQUALS, 2.0), List.of("actionId3", "actionId4")),
                new ConditionalActions("stage3", new AcDcConverterThresholdCondition("Converter1", AbstractThresholdCondition.Variable.CURRENT, AbstractThresholdCondition.ComparisonType.LESS_THAN_OR_EQUALS, 3.0, true, TerminalNumber.TWO), List.of("actionId3", "actionId4", "actionId5")))));
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(operatorStrategies);
        roundTripTest(operatorStrategyList, OperatorStrategyList::write, OperatorStrategyList::read, "/OperatorStrategyFileTest.json");
    }
}
