/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.action.json.ActionJsonModule;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.action.*;
import com.powsybl.security.condition.AllViolationCondition;
import com.powsybl.security.condition.AnyViolationCondition;
import com.powsybl.security.condition.TrueCondition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.security.strategy.ConditionalActions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.powsybl.security.LimitViolationType.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class JsonActionAndOperatorStrategyTest extends AbstractSerDeTest {

    @Test
    void operatorStrategyReadV10() throws IOException {
        OperatorStrategyList operatorStrategies = OperatorStrategyList.read(getClass().getResourceAsStream("/OperatorStrategyFileTestV1.0.json"));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            operatorStrategies.write(bos);
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/OperatorStrategyFileTest.json"), new ByteArrayInputStream(bos.toByteArray()));
        }
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
        OperatorStrategyList operatorStrategyList = new OperatorStrategyList(operatorStrategies);
        roundTripTest(operatorStrategyList, OperatorStrategyList::write, OperatorStrategyList::read, "/OperatorStrategyFileTest.json");
    }

    @JsonTypeName(DummyAction.NAME)
    static class DummyAction extends AbstractAction {

        static final String NAME = "dummy-action";

        @JsonCreator
        protected DummyAction(@JsonProperty("id") String id) {
            super(id);
        }

        @JsonProperty(value = "type", access = JsonProperty.Access.READ_ONLY)
        @Override
        public String getType() {
            return NAME;
        }

        @Override
        public ActionBuilder<?> convertToBuilder() {
            return null;
        }
    }

    @Test
    void testJsonPlugins() throws JsonProcessingException {

        Module jsonModule = new SimpleModule()
                .registerSubtypes(DummyAction.class);
        SecurityAnalysisJsonPlugin plugin = () -> List.of(jsonModule);
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule(List.of(plugin)))
                .registerModule(new ActionJsonModule());

        DummyAction action = new DummyAction("hello");
        ActionList actions = new ActionList(List.of(action));
        String serialized = mapper.writeValueAsString(actions);
        ActionList parsed = mapper.readValue(serialized, ActionList.class);

        assertEquals(1, parsed.getActions().size());
        Action parsedAction = parsed.getActions().get(0);
        assertTrue(parsedAction instanceof DummyAction);
        assertEquals("hello", parsedAction.getId());
    }
}
