/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.action.*;
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

import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
import static org.junit.jupiter.api.Assertions.*;
import static com.powsybl.security.LimitViolationType.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class JsonActionAndOperatorStrategyTest extends AbstractSerDeTest {

    @Test
    void actionRoundTrip() throws IOException {
        List<Action> actions = new ArrayList<>();
        actions.add(new SwitchAction("id1", "switchId1", true));
        actions.add(new MultipleActionsAction("id2", Collections.singletonList(new SwitchAction("id3", "switchId2", true))));
        actions.add(new LineConnectionAction("id3", "lineId3", true, true));
        actions.add(new LineConnectionAction("id4", "lineId4", false));
        actions.add(new PhaseTapChangerTapPositionAction("id5", "transformerId1", true, 5, ThreeSides.TWO));
        actions.add(new PhaseTapChangerTapPositionAction("id6", "transformerId2", false, 12));
        actions.add(new PhaseTapChangerTapPositionAction("id7", "transformerId3", true, -5, ThreeSides.ONE));
        actions.add(new PhaseTapChangerTapPositionAction("id8", "transformerId3", false, 2, ThreeSides.THREE));
        actions.add(new GeneratorActionBuilder().withId("id9").withGeneratorId("generatorId1").withActivePowerRelativeValue(true).withActivePowerValue(100.0).build());
        actions.add(new GeneratorActionBuilder().withId("id10").withGeneratorId("generatorId2").withVoltageRegulatorOn(true).withTargetV(225.0).build());
        actions.add(new GeneratorActionBuilder().withId("id11").withGeneratorId("generatorId2").withVoltageRegulatorOn(false).withTargetQ(400.0).build());
        actions.add(new LoadActionBuilder().withId("id12").withLoadId("loadId1").withRelativeValue(false).withActivePowerValue(50.0).build());
        actions.add(new LoadActionBuilder().withId("id13").withLoadId("loadId1").withRelativeValue(true).withReactivePowerValue(5.0).build());
        actions.add(new RatioTapChangerTapPositionAction("id14", "transformerId4", false, 2, ThreeSides.THREE));
        actions.add(new RatioTapChangerTapPositionAction("id15", "transformerId5", true, 1));
        actions.add(RatioTapChangerRegulationAction.activateRegulation("id16", "transformerId5", ThreeSides.THREE));
        actions.add(PhaseTapChangerRegulationAction.activateAndChangeRegulationMode("id17", "transformerId5", ThreeSides.ONE,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 10.0));
        actions.add(PhaseTapChangerRegulationAction.deactivateRegulation("id18",
                "transformerId6", ThreeSides.ONE));
        actions.add(PhaseTapChangerRegulationAction.activateAndChangeRegulationMode("id19",
                "transformerId6", ThreeSides.ONE,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 15.0));
        actions.add(RatioTapChangerRegulationAction.activateRegulationAndChangeTargetV("id20", "transformerId5", 90.0));
        actions.add(RatioTapChangerRegulationAction.deactivateRegulation("id21", "transformerId5", ThreeSides.THREE));
        actions.add(new HvdcActionBuilder()
                .withId("id22")
                .withHvdcId("hvdc1")
                .withAcEmulationEnabled(false)
                .build());
        actions.add(new HvdcActionBuilder()
                .withId("id23")
                .withHvdcId("hvdc2")
                .withAcEmulationEnabled(true)
                .build());
        actions.add(new HvdcActionBuilder()
                .withId("id24")
                .withHvdcId("hvdc2")
                .withAcEmulationEnabled(true)
                .withDroop(121.0)
                .withP0(42.0)
                .withConverterMode(SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .withRelativeValue(false)
                .build());
        actions.add(new HvdcActionBuilder()
                .withId("id25")
                .withHvdcId("hvdc1")
                .withAcEmulationEnabled(false)
                .withActivePowerSetpoint(12.0)
                .withRelativeValue(true)
                .build());
        actions.add(new ShuntCompensatorPositionActionBuilder().withId("id22").withShuntCompensatorId("shuntId1").withSectionCount(5).build());
        actions.add(new StaticVarCompensatorActionBuilder().withId("id23")
                .withStaticVarCompensatorId("svc").withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .withVoltageSetpoint(56.0).build());
        actions.add(new StaticVarCompensatorActionBuilder().withId("id24")
                .withStaticVarCompensatorId("svc").withRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER)
                .withReactivePowerSetpoint(120.0).build());
        ActionList actionList = new ActionList(actions);
        roundTripTest(actionList, ActionList::writeJsonFile, ActionList::readJsonFile, "/ActionFileTest.json");
    }

    @Test
    void actionsReadV10() throws IOException {
        ActionList actionList = ActionList.readJsonInputStream(getClass().getResourceAsStream("/ActionFileTestV1.0.json"));
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            actionList.writeJsonOutputStream(bos);
            ComparisonUtils.compareTxt(getClass().getResourceAsStream("/ActionFileTest.json"), new ByteArrayInputStream(bos.toByteArray()));
        }
    }

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

    @Test
    void wrongActions() {
        final InputStream inputStream = getClass().getResourceAsStream("/WrongActionFileTest.json");
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: for phase tap changer tap position action relative value field can't be null\n" +
                " at [Source: (BufferedInputStream); line: 8, column: 3] (through reference chain: java.util.ArrayList[0])", assertThrows(UncheckedIOException.class, () ->
                ActionList.readJsonInputStream(inputStream)).getMessage());

        final InputStream inputStream3 = getClass().getResourceAsStream("/ActionFileTestWrongVersion.json");
        assertTrue(assertThrows(UncheckedIOException.class, () -> ActionList
                .readJsonInputStream(inputStream3))
                .getMessage()
                .contains("actions. Tag: value is not valid for version 1.1. Version should be <= 1.0"));
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
    }

    @Test
    void testJsonPlugins() throws JsonProcessingException {

        Module jsonModule = new SimpleModule()
                .registerSubtypes(DummyAction.class);
        SecurityAnalysisJsonPlugin plugin = () -> List.of(jsonModule);
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule(List.of(plugin)));

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
