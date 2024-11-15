/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.action;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
class EqualsActionTest {

    @Test
    void switchAction() {
        SwitchAction action1 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .build();
        SwitchAction action2 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .build();
        SwitchAction action3 = new SwitchActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withOpen(true)
                .build();
        SwitchAction action4 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withOpen(true)
                .build();
        SwitchAction action5 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(false)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void terminalConnectionAction() {
        Action action1 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .withSide(ThreeSides.ONE)
                .build();
        TerminalsConnectionAction action2 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .withSide(ThreeSides.ONE)
                .build();
        Action action3 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .build();
        Action action4 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .build();
        TerminalsConnectionAction action5 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true)
                .withSide(ThreeSides.THREE)
                .build();
        Action action6 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withOpen(true)
                .withSide(ThreeSides.ONE)
                .build();
        Action action7 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(false)
                .withSide(ThreeSides.ONE)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .addEqualityGroup(action7)
                .testEquals();
    }

    @Test
    void dangingLineAction() {
        DanglingLineAction action1 = new DanglingLineActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        DanglingLineAction action2 = new DanglingLineActionBuilder().withId("id")
                .withDanglingLineId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        DanglingLineAction action3 = new DanglingLineActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        DanglingLineAction action4 = new DanglingLineActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(true)
                .build();
        DanglingLineAction action5 = new DanglingLineActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withReactivePowerValue(100)
                .withRelativeValue(false)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void loadAction() {
        Action action1 = new LoadActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        LoadAction action2 = new LoadActionBuilder().withId("id")
                .withLoadId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        Action action3 = new LoadActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(101)
                .withRelativeValue(false)
                .build();
        Action action4 = new DanglingLineActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        LoadAction action5 = new LoadActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withRelativeValue(false)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void shuntCompensatorAction() {
        ShuntCompensatorPositionAction action1 = new ShuntCompensatorPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withSectionCount(10)
                .build();
        ShuntCompensatorPositionAction action2 = new ShuntCompensatorPositionActionBuilder().withId("id")
                .withShuntCompensatorId("neId")
                .withSectionCount(10)
                .build();
        ShuntCompensatorPositionAction action3 = new ShuntCompensatorPositionActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withSectionCount(11)
                .build();
        ShuntCompensatorPositionAction action4 = new ShuntCompensatorPositionActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withSectionCount(10)
                .build();
        ShuntCompensatorPositionAction action5 = new ShuntCompensatorPositionActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withSectionCount(10)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void staticVarCompensatorAction() {
        Action action1 = new StaticVarCompensatorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withReactivePowerSetpoint(10.)
                .withVoltageSetpoint(5.)
                .withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .build();
        StaticVarCompensatorAction action2 = new StaticVarCompensatorActionBuilder().withId("id")
                .withStaticVarCompensatorId("neId")
                .withReactivePowerSetpoint(10.)
                .withVoltageSetpoint(5.)
                .withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .build();
        Action action3 = new StaticVarCompensatorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withReactivePowerSetpoint(10.)
                .withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .build();
        Action action4 = new StaticVarCompensatorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withReactivePowerSetpoint(10.)
                .withVoltageSetpoint(5.)
                .build();
        StaticVarCompensatorAction action5 = new StaticVarCompensatorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withVoltageSetpoint(5.)
                .withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .build();
        StaticVarCompensatorAction action6 = new StaticVarCompensatorActionBuilder().withId("id")
                .withStaticVarCompensatorId("neId2")
                .withReactivePowerSetpoint(10.)
                .withVoltageSetpoint(5.)
                .withRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .testEquals();
    }

    @Test
    void phaseTapChangerTapPositionAction() {
        PhaseTapChangerTapPositionAction action1 = new PhaseTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(true)
                .build();
        PhaseTapChangerTapPositionAction action2 = new PhaseTapChangerTapPositionActionBuilder().withId("id")
                .withTransformerId("neId")
                .withTapPosition(2)
                .withRelativeValue(true)
                .build();
        PhaseTapChangerTapPositionAction action3 = new PhaseTapChangerTapPositionActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(true)
                .withSide(ThreeSides.ONE)
                .build();
        PhaseTapChangerTapPositionAction action4 = new PhaseTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withTapPosition(2)
                .withRelativeValue(true)
                .withRelativeValue(false)
                .build();
        PhaseTapChangerTapPositionAction action5 = new PhaseTapChangerTapPositionActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withRelativeValue(true)
                .withTapPosition(3)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void phaseTapChangerRegulationAction() {
        Action action1 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withRegulationValue(2.)
                .withRegulating(true)
                .build();
        PhaseTapChangerRegulationAction action2 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withTransformerId("neId")
                .withRegulationValue(2.)
                .withRegulating(true)
                .build();
        Action action3 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withRegulationValue(2.)
                .withSide(ThreeSides.ONE)
                .build();
        Action action4 = new PhaseTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .build();
        PhaseTapChangerRegulationAction action5 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withRegulationValue(2.)
                .withRegulating(true)
                .withRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .build();
        PhaseTapChangerRegulationAction action6 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withRegulationValue(2.)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .testEquals();
    }

    @Test
    void ratioTapChangerRegulationAction() {
        Action action1 = new RatioTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTargetV(2.)
                .build();
        RatioTapChangerRegulationAction action2 = new RatioTapChangerRegulationActionBuilder().withId("id")
                .withTransformerId("neId")
                .withTargetV(2.)
                .build();
        Action action3 = new RatioTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTargetV(2.)
                .withSide(ThreeSides.ONE)
                .build();
        Action action4 = new PhaseTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withRegulationValue(2.)
                .build();
        RatioTapChangerRegulationAction action5 = new RatioTapChangerRegulationActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTargetV(2.)
                .withRegulating(true)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void ratioTapChangerTapPositionAction() {
        Action action1 = new RatioTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .build();
        RatioTapChangerTapPositionAction action2 = new RatioTapChangerTapPositionActionBuilder().withId("id")
                .withTransformerId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .build();
        Action action3 = new RatioTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .withSide(ThreeSides.ONE)
                .build();
        Action action4 = new PhaseTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .build();
        RatioTapChangerTapPositionAction action5 = new RatioTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(true)
                .build();
        RatioTapChangerTapPositionAction action6 = new RatioTapChangerTapPositionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withTapPosition(2)
                .withRelativeValue(false)
                .withTapPosition(0)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .testEquals();
    }

    @Test
    void generatorAction() {
        GeneratorAction action1 = new GeneratorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .build();
        GeneratorAction action2 = new GeneratorActionBuilder().withId("id")
                .withGeneratorId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .build();
        GeneratorAction action3 = new GeneratorActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(true)
                .withTargetQ(50)
                .build();
        GeneratorAction action4 = new GeneratorActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withVoltageRegulatorOn(true)
                .withTargetQ(50)
                .build();
        GeneratorAction action5 = new GeneratorActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .build();
        GeneratorAction action6 = new GeneratorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(110)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .build();
        GeneratorAction action7 = new GeneratorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .withVoltageRegulatorOn(false)
                .build();
        GeneratorAction action8 = new GeneratorActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withActivePowerValue(100)
                .withActivePowerRelativeValue(false)
                .withTargetQ(50)
                .withTargetV(60)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .addEqualityGroup(action7)
                .addEqualityGroup(action8)
                .testEquals();
    }

    @Test
    void hvdcAction() {
        HvdcAction action1 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withAcEmulationEnabled(true)
                .build();
        HvdcAction action2 = new HvdcActionBuilder().withId("id")
                .withHvdcId("neId")
                .withAcEmulationEnabled(true)
                .build();
        HvdcAction action3 = new HvdcActionBuilder().withId("id2")
                .withNetworkElementId("neId")
                .build();
        HvdcAction action4 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId2")
                .withAcEmulationEnabled(true)
                .build();
        HvdcAction action5 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .build();
        HvdcAction action6 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withConverterMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .build();
        HvdcAction action7 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withAcEmulationEnabled(true)
                .withP0(2.)
                .build();
        HvdcAction action8 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withAcEmulationEnabled(true)
                .withDroop(2.)
                .build();
        HvdcAction action9 = new HvdcActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withAcEmulationEnabled(true)
                .withActivePowerSetpoint(2.)
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .addEqualityGroup(action6)
                .addEqualityGroup(action7)
                .addEqualityGroup(action8)
                .addEqualityGroup(action9)
                .testEquals();
    }

    @Test
    void multipleActionsAction() {
        SwitchActionBuilder subaction1 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true);
        TerminalsConnectionActionBuilder subaction2 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true);
        SwitchActionBuilder subaction3 = new SwitchActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true);
        TerminalsConnectionActionBuilder subaction4 = new TerminalsConnectionActionBuilder().withId("id")
                .withNetworkElementId("neId")
                .withOpen(true);
        MultipleActionsAction action1 = new MultipleActionsActionBuilder().withId("id")
                .withActionBuilders(List.of(subaction1, subaction2))
                .build();
        MultipleActionsAction action2 = new MultipleActionsActionBuilder().withId("id")
                .withActionBuilders(List.of(subaction3, subaction4))
                .build();
        MultipleActionsAction action3 = new MultipleActionsActionBuilder().withId("id")
                .withActionBuilders(List.of(subaction1))
                .build();
        MultipleActionsAction action4 = new MultipleActionsActionBuilder().withId("id2")
                .withActionBuilders(List.of(subaction1, subaction2))
                .build();
        MultipleActionsAction action5 = new MultipleActionsActionBuilder().withId("id")
                .withActionBuilders(List.of())
                .build();
        new EqualsTester()
                .addEqualityGroup(action1, action2)
                .addEqualityGroup(action3)
                .addEqualityGroup(action4)
                .addEqualityGroup(action5)
                .testEquals();
    }

    @Test
    void interchangeTargetAction() {
        AreaInterchangeTargetAction action1 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area1")
            .withTarget(1.0)
            .build();
        AreaInterchangeTargetAction action2 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area1")
            .withTarget(1.0)
            .build();
        AreaInterchangeTargetAction action3 = new AreaInterchangeTargetActionBuilder().withId("id2")
            .withAreaId("area1")
            .withTarget(1.0)
            .build();
        AreaInterchangeTargetAction action4 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area2")
            .withTarget(1.0)
            .build();
        AreaInterchangeTargetAction action5 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area1")
            .withTarget(2.0)
            .build();

        AreaInterchangeTargetAction action6 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area1")
            .withTarget(Double.NaN)
            .build();

        AreaInterchangeTargetAction action7 = new AreaInterchangeTargetActionBuilder().withId("id")
            .withAreaId("area1")
            .withTarget(Double.NaN)
            .build();

        new EqualsTester()
            .addEqualityGroup(action1, action2)
            .addEqualityGroup(action3)
            .addEqualityGroup(action4)
            .addEqualityGroup(action5)
            .addEqualityGroup(action6, action7)
            .testEquals();
    }

}
