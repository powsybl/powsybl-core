/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ActionBuilderTest {

    @Test
    void phaseTapChangerRegulationActionBuilderTest() {
        PhaseTapChangerRegulationAction phaseTapChangerRegulationAction = new PhaseTapChangerRegulationAction("actionId",
            "transformerId", ThreeSides.TWO, true, ACTIVE_POWER_CONTROL, 1.0);
        PhaseTapChangerRegulationActionBuilder phaseTapChangerRegulationActionBuilder = phaseTapChangerRegulationAction.convertToBuilder();
        Assertions.assertEquals(new PhaseTapChangerRegulationActionBuilder()
                .withId("actionId")
                .withTransformerId("transformerId")
                .withSide(ThreeSides.TWO)
                .withRegulating(true)
                .withRegulationMode(ACTIVE_POWER_CONTROL)
                .withRegulationValue(1.0),
            phaseTapChangerRegulationActionBuilder);
    }

    @Test
    void ratioTapChangerRegulationActionBuilderTest() {
        RatioTapChangerRegulationAction ratioTapChangerRegulationAction = new RatioTapChangerRegulationAction("actionId",
            "transformerId", ThreeSides.TWO, true, 1.0);
        RatioTapChangerRegulationActionBuilder ratioTapChangerRegulationActionBuilder = ratioTapChangerRegulationAction.convertToBuilder();
        Assertions.assertEquals(new RatioTapChangerRegulationActionBuilder()
                .withId("actionId")
                .withTransformerId("transformerId")
                .withSide(ThreeSides.TWO)
                .withRegulating(true)
                .withTargetV(1.0),
            ratioTapChangerRegulationActionBuilder);
    }

    @Test
    void phaseTapChangerTapPositionActionBuilderTest() {
        PhaseTapChangerTapPositionAction phaseTapChangerTapPositionAction = new PhaseTapChangerTapPositionAction("actionId",
            "transformerId", false, 3, ThreeSides.TWO);
        PhaseTapChangerTapPositionActionBuilder phaseTapChangerRegulationActionBuilder = phaseTapChangerTapPositionAction.convertToBuilder();
        Assertions.assertEquals(new PhaseTapChangerTapPositionActionBuilder()
                .withId("actionId")
                .withTransformerId("transformerId")
                .withSide(ThreeSides.TWO)
                .withTapPosition(3)
                .withRelativeValue(false),
            phaseTapChangerRegulationActionBuilder);
    }

    @Test
    void ratioTapChangerTapPositionActionBuilderTest() {
        RatioTapChangerTapPositionAction ratioTapChangerTapPositionAction = new RatioTapChangerTapPositionAction("actionId",
            "transformerId", false, 3, ThreeSides.TWO);
        RatioTapChangerTapPositionActionBuilder ratioTapChangerTapPositionActionBuilder = ratioTapChangerTapPositionAction.convertToBuilder();
        Assertions.assertEquals(new RatioTapChangerTapPositionActionBuilder()
                .withId("actionId")
                .withTransformerId("transformerId")
                .withSide(ThreeSides.TWO)
                .withTapPosition(3)
                .withRelativeValue(false),
            ratioTapChangerTapPositionActionBuilder);
    }

    @Test
    void shuntCompensatorPositionActionBuilderTest() {
        ShuntCompensatorPositionAction shuntCompensatorPositionAction = new ShuntCompensatorPositionAction("actionId",
            "shuntCompensatorId", 4);
        ShuntCompensatorPositionActionBuilder shuntCompensatorPositionActionBuilder = shuntCompensatorPositionAction.convertToBuilder();
        Assertions.assertEquals(new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("blabla")
            .withNetworkElementId("shuntCompensatorId").withSectionCount(4), shuntCompensatorPositionActionBuilder);

        String message1 = Assertions.assertThrows(IllegalArgumentException.class, () -> new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId").build()).getMessage();
        Assertions.assertEquals("sectionCount is undefined", message1);

        String message2 = Assertions.assertThrows(IllegalArgumentException.class, () -> new ShuntCompensatorPositionActionBuilder()
            .withId("actionId").withShuntCompensatorId("shuntCompensatorId").withSectionCount(-1).build()).getMessage();
        Assertions.assertEquals("sectionCount should be positive for a shunt compensator", message2);
    }

    @Test
    void hvdcActionBuilderTest() {
        HvdcAction hvdcAction = new HvdcAction("actionId",
            "hvdcId", true, 2.0,
            HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, 3.0, 10.0, false);
        HvdcActionBuilder hvdcActionBuilder = hvdcAction.convertToBuilder();
        Assertions.assertEquals(new HvdcActionBuilder().withId("actionId")
                .withHvdcId("hvdcId")
                .withAcEmulationEnabled(true)
                .withActivePowerSetpoint(2.0)
                .withDroop(3.0)
                .withConverterMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .withP0(10.0)
                .withRelativeValue(false),
            hvdcActionBuilder);
    }

    @Test
    void generatorActionBuilderTest() {
        GeneratorAction generatorAction = new GeneratorAction("actionId", "generatorId", true,
            1.0, false, 3.0, 4.0);
        GeneratorActionBuilder generatorActionBuilder = generatorAction.convertToBuilder();
        Assertions.assertEquals(new GeneratorActionBuilder().withId("actionId")
                .withGeneratorId("generatorId")
                .withActivePowerRelativeValue(true)
                .withActivePowerValue(1.0)
                .withVoltageRegulatorOn(false)
                .withTargetV(3.0)
                .withTargetQ(4.0),
            generatorActionBuilder);
    }

    @Test
    void loadActionBuilderTest() {
        LoadAction loadAction = new LoadAction("actionId", "loadId", true, 1.0, 2.0);
        LoadActionBuilder loadActionBuilder = loadAction.convertToBuilder();
        Assertions.assertEquals(new LoadActionBuilder().withId("actionId")
                .withLoadId("loadId")
                .withActivePowerValue(1.0)
                .withRelativeValue(true)
                .withReactivePowerValue(2.0),
            loadActionBuilder);
    }

    @Test
    void staticVarCompensatorActionBuilderTest() {
        StaticVarCompensatorAction staticVarCompensatorAction = new StaticVarCompensatorAction("actionId",
            "StaticVarCompensatorId", StaticVarCompensator.RegulationMode.OFF, 2.0, 4.0);
        StaticVarCompensatorActionBuilder staticVarCompensatorActionBuilder = staticVarCompensatorAction.convertToBuilder();
        Assertions.assertEquals(new StaticVarCompensatorActionBuilder().withId("actionId")
                .withStaticVarCompensatorId("StaticVarCompensatorId")
                .withVoltageSetpoint(2.0)
                .withRegulationMode(StaticVarCompensator.RegulationMode.OFF)
                .withReactivePowerSetpoint(4.0),
            staticVarCompensatorActionBuilder);
    }

    @Test
    void danglingLineActionBuilderTest() {
        DanglingLineAction danglingLineAction = new DanglingLineAction("actionId",
            "danglingLineId", true, 1.0, 2.0);
        DanglingLineActionBuilder danglingLineActionBuilder = danglingLineAction.convertToBuilder();
        Assertions.assertEquals(new DanglingLineActionBuilder().withId("actionId")
                .withDanglingLineId("danglingLineId")
                .withReactivePowerValue(2.0)
                .withRelativeValue(true)
                .withActivePowerValue(1.0),
            danglingLineActionBuilder);
    }
}
