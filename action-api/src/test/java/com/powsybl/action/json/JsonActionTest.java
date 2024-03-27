package com.powsybl.action.json;

import com.powsybl.action.*;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.identifiers.IdBasedNetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.iidm.network.identifiers.VoltageLevelAndOrderNetworkElementIdentifier;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;

import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JsonActionTest extends AbstractSerDeTest {

    @Test
    void actionRoundTrip() throws IOException {
        List<Action> actions = new ArrayList<>();
        actions.add(new SwitchAction("id1", "switchId1", true));
        actions.add(new MultipleActionsAction("id2", Collections.singletonList(new SwitchAction("id3", "switchId2", true))));
        actions.add(new TerminalsConnectionAction("id3", "lineId3", true)); // both sides.
        actions.add(new TerminalsConnectionAction("id4", "lineId4", false)); // both sides.
        actions.add(new PhaseTapChangerTapPositionAction("id5", "transformerId1", true, 5, ThreeSides.TWO));
        actions.add(new PhaseTapChangerTapPositionAction("id6", "transformerId2", false, 12));
        actions.add(new PhaseTapChangerTapPositionAction("id7", "transformerId3", true, -5, ThreeSides.ONE));
        actions.add(new PhaseTapChangerTapPositionAction("id8", "transformerId3", false, 2, ThreeSides.THREE));
        actions.add(new GeneratorActionBuilder().withId("id9").withGeneratorId("generatorId1").withActivePowerRelativeValue(true).withActivePowerValue(100.0).build());
        actions.add(new GeneratorActionBuilder().withId("id10").withGeneratorId("generatorId2").withVoltageRegulatorOn(true).withTargetV(225.0).build());
        actions.add(new GeneratorActionBuilder().withId("id11").withGeneratorId("generatorId2").withVoltageRegulatorOn(false).withTargetQ(400.0).build());
        actions.add(new LoadActionBuilder().withId("id12").withLoadId("loadId1").withRelativeValue(false).withActivePowerValue(50.0).build());
        actions.add(new LoadActionBuilder().withId("id13").withLoadId("loadId1").withRelativeValue(true).withReactivePowerValue(5.0).build());
        actions.add(new DanglingLineActionBuilder().withId("id17").withDanglingLineId("dlId1").withRelativeValue(true).withReactivePowerValue(5.0).build());
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
        actions.add(new TerminalsConnectionAction("id4", "transformerId25", ThreeSides.THREE, true)); // only one side.
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

    @Test
    void identifierActionList() throws IOException {
        Map<ActionBuilder, NetworkElementIdentifier> elementIdentifierMap = new HashMap<>();
        elementIdentifierMap.put(new TerminalsConnectionActionBuilder().withId("lineConnectionActionId").withOpen(true).withSide(ThreeSides.ONE),
            new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1'));
        elementIdentifierMap.put(new SwitchActionBuilder().withId("switchActionId").withOpen(true),
            new IdBasedNetworkElementIdentifier("switch 1 id"));
        IdentifierActionList identifierActionList = new IdentifierActionList(Collections.emptyList(), elementIdentifierMap);
        roundTripTest(identifierActionList, ActionList::writeJsonFile, ActionList::readJsonFile, "/IdentifierActionListTest.json");
    }
}
