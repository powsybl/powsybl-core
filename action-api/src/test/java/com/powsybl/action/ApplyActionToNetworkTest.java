/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.action;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControl;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.test.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.action.PercentChangeLoadAction.QModificationStrategy.CONSTANT_PQ_RATIO;
import static com.powsybl.action.PercentChangeLoadAction.QModificationStrategy.CONSTANT_Q;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
class ApplyActionToNetworkTest {

    @Test
    void switchAction() {
        Network network = TwoVoltageLevelNetworkFactory.create();
        Switch sw = network.getSwitch("BR_LOAD");

        assertFalse(sw.isOpen());
        SwitchAction action = new SwitchAction("id", "BR_LOAD", true);
        action.toModification().apply(network);
        assertTrue(sw.isOpen());

        assertTrue(sw.isOpen());
        SwitchAction action2 = new SwitchAction("id", "BR_LOAD", false);
        action2.toModification().apply(network);
        assertFalse(sw.isOpen());
    }

    @Test
    void terminalConnectionAction() {
        Network network = EurostagTutorialExample1Factory.create();
        Connectable<?> connectable = network.getConnectable("NHV1_NHV2_2");

        assertTrue(connectable.getTerminals().stream().allMatch(Terminal::isConnected));
        TerminalsConnectionAction action = new TerminalsConnectionAction("id", "NHV1_NHV2_2", true);
        action.toModification().apply(network);
        assertTrue(connectable.getTerminals().stream().noneMatch(Terminal::isConnected));

        Branch<?> branch = network.getBranch("NHV1_NHV2_2");
        assertFalse(branch.getTerminal(TwoSides.TWO).isConnected());
        assertFalse(branch.getTerminal(TwoSides.ONE).isConnected());
        TerminalsConnectionAction action2 = new TerminalsConnectionAction("id", "NHV1_NHV2_2", ThreeSides.TWO, false);
        action2.toModification().apply(network);
        assertTrue(branch.getTerminal(TwoSides.TWO).isConnected());
        assertFalse(branch.getTerminal(TwoSides.ONE).isConnected());
    }

    @Test
    void terminalConnectionActionOnTieLine() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine tieLine = network.getTieLine("NHV1_NHV2_1");

        // Disconnection
        TerminalsConnectionAction disconnectionAction = new TerminalsConnectionAction("id", "NHV1_NHV2_1", true);
        NetworkModification disconnection = disconnectionAction.toModification();
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        ComputationManager computationManager = LocalComputationManager.getDefault();
        assertTrue(tieLine.getDanglingLine1().getTerminal().isConnected());
        assertTrue(tieLine.getDanglingLine2().getTerminal().isConnected());
        disconnection.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP);
        assertFalse(tieLine.getDanglingLine1().getTerminal().isConnected());
        assertFalse(tieLine.getDanglingLine2().getTerminal().isConnected());

        // Connection
        TerminalsConnectionAction connectionAction = new TerminalsConnectionAction("id", "NHV1_NHV2_1", false);
        NetworkModification connection = connectionAction.toModification();
        connection.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP);
        assertTrue(tieLine.getDanglingLine1().getTerminal().isConnected());
        assertTrue(tieLine.getDanglingLine2().getTerminal().isConnected());
    }

    @Test
    void dangingLineAction() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        DanglingLine danglingLine = network.getDanglingLine("NHV1_XNODE1");
        danglingLine.setP0(10.0);
        danglingLine.setQ0(4.0);

        assertEquals(10.0, danglingLine.getP0());
        DanglingLineAction action = new DanglingLineActionBuilder().withId("id")
                .withDanglingLineId("NHV1_XNODE1")
                .withRelativeValue(false)
                .withActivePowerValue(5.0)
                .build();
        action.toModification().apply(network);
        assertEquals(5.0, danglingLine.getP0());

        assertEquals(4.0, danglingLine.getQ0());
        DanglingLineAction action2 = new DanglingLineActionBuilder().withId("id")
                .withDanglingLineId("NHV1_XNODE1")
                .withRelativeValue(true)
                .withReactivePowerValue(2.0)
                .build();
        action2.toModification().apply(network);
        assertEquals(6.0, danglingLine.getQ0());
    }

    @Test
    void loadAction() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");

        assertEquals(200.0, load.getQ0());
        LoadAction action = new LoadActionBuilder().withId("id")
                .withLoadId("LOAD")
                .withRelativeValue(false)
                .withReactivePowerValue(100.0)
                .build();
        action.toModification().apply(network);
        assertEquals(100.0, load.getQ0());

        assertEquals(600.0, load.getP0());
        LoadAction action2 = new LoadActionBuilder().withId("id")
                .withLoadId("LOAD")
                .withRelativeValue(true)
                .withActivePowerValue(-20.0)
                .build();
        action2.toModification().apply(network);
        assertEquals(580.0, load.getP0());
    }

    @Test
    void pctLoadActionShouldNotModifyQ0WhenConstantQ() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(600.0, load.getP0());
        assertEquals(200.0, load.getQ0());
        PercentChangeLoadAction action = (PercentChangeLoadAction) new PercentChangeLoadActionBuilder()
                .withId("id").withLoadId("LOAD").withP0PercentChange(-10d).withQModificationStrategy(CONSTANT_Q).build();
        action.toModification().apply(network);
        assertEquals(540.0, load.getP0());
        assertEquals(200.0, load.getQ0());
    }

    @Test
    void pctLoadActionShouldPreservePQRatioWhenConstantPQRatio() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertEquals(600.0, load.getP0());
        assertEquals(200.0, load.getQ0());
        PercentChangeLoadAction action = (PercentChangeLoadAction) new PercentChangeLoadActionBuilder()
                .withId("id").withLoadId("LOAD").withP0PercentChange(-10d).withQModificationStrategy(CONSTANT_PQ_RATIO).build();
        action.toModification().apply(network);
        assertEquals(540.0, load.getP0());
        assertEquals(180.0, load.getQ0());
    }

    @Test
    void shuntCompensatorAction() {
        Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();
        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");

        assertEquals(1, shuntCompensator.getSectionCount());
        ShuntCompensatorPositionAction action = new ShuntCompensatorPositionActionBuilder().withId("id")
                .withShuntCompensatorId("SHUNT")
                .withSectionCount(0)
                .build();
        action.toModification().apply(network);
        assertEquals(0, shuntCompensator.getSectionCount());

        assertEquals(0, shuntCompensator.getSectionCount());
        assertEquals(1, shuntCompensator.getMaximumSectionCount());
        ShuntCompensatorPositionAction action2 = new ShuntCompensatorPositionActionBuilder().withId("id")
                .withShuntCompensatorId("SHUNT")
                .withSectionCount(2)
                .build();
        NetworkModification modif = action2.toModification();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modif.apply(network, true, ReportNode.NO_OP));
        assertEquals("Shunt compensator 'SHUNT': the current number (2) of section should be lesser than the maximum number of section (1)", e.getMessage());
        assertDoesNotThrow(() -> modif.apply(network));
    }

    @Test
    void phaseTapChangerTapPositionAction() {
        Network network = PhaseShifterTestCaseFactory.create();
        TwoWindingsTransformer twoWT = network.getTwoWindingsTransformer("PS1");

        assertEquals(1, twoWT.getPhaseTapChanger().getTapPosition());
        PhaseTapChangerTapPositionAction action = new PhaseTapChangerTapPositionAction("id", "PS1", false, 2);
        action.toModification().apply(network);
        assertEquals(2, twoWT.getPhaseTapChanger().getTapPosition());

        assertEquals(2, twoWT.getPhaseTapChanger().getTapPosition());
        PhaseTapChangerTapPositionAction action2 = new PhaseTapChangerTapPositionAction("id", "PS1", false, 3);
        NetworkModification modif = action2.toModification();
        PowsyblException e = assertThrows(PowsyblException.class, () -> modif.apply(network, true, ReportNode.NO_OP));
        assertEquals("2 windings transformer 'PS1': incorrect tap position 3 [0, 2]", e.getMessage());

        Network network2 = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer.Leg threeWTLeg = network2.getThreeWindingsTransformer("3WT").getLeg(ThreeSides.THREE);
        addPhaseTapChanger(threeWTLeg);
        assertEquals(0, threeWTLeg.getPhaseTapChanger().getTapPosition());
        PhaseTapChangerTapPositionAction action3 = new PhaseTapChangerTapPositionAction("id", "3WT", false, 1, ThreeSides.THREE);
        action3.toModification().apply(network2);
        assertEquals(1, threeWTLeg.getPhaseTapChanger().getTapPosition());
    }

    private static void addPhaseTapChanger(ThreeWindingsTransformer.Leg threeWTLeg) {
        threeWTLeg.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setR(0.01)
                .setX(0.0001)
                .setB(0)
                .setG(0)
                .setRho(1.1)
                .setAlpha(1)
                .endStep()
                .beginStep()
                .setR(0.02)
                .setX(0.0002)
                .setB(0)
                .setG(0)
                .setRho(1.2)
                .setAlpha(1.1)
                .endStep()
                .add();
    }

    @Test
    void generatorAction() {
        Network network = EurostagTutorialExample1Factory.create();
        Generator generator = network.getGenerator("GEN");

        assertEquals(607.0, generator.getTargetP());
        GeneratorAction action = new GeneratorActionBuilder()
                .withId("id")
                .withGeneratorId("GEN")
                .withActivePowerRelativeValue(false)
                .withActivePowerValue(503.0)
                .build();
        action.toModification().apply(network);
        assertEquals(503.0, generator.getTargetP());

        assertEquals(503.0, generator.getTargetP());
        GeneratorAction action2 = new GeneratorActionBuilder()
                .withId("id")
                .withGeneratorId("GEN")
                .withActivePowerRelativeValue(true)
                .withActivePowerValue(-10)
                .build();
        action2.toModification().apply(network);
        assertEquals(493.0, generator.getTargetP());
    }

    @Test
    void hvdcAction() {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine hvdcLine = network.getHvdcLine("L");
        hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
                .withP0(200.0f)
                .withDroop(0.9f)
                .withEnabled(true)
                .add();

        assertEquals(280.0, hvdcLine.getActivePowerSetpoint());
        HvdcAction action = new HvdcActionBuilder()
                .withId("id")
                .withHvdcId("L")
                .withActivePowerSetpoint(200.0)
                .build();
        action.toModification().apply(network);
        assertEquals(200.0, hvdcLine.getActivePowerSetpoint());

        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
        HvdcAction action2 = new HvdcActionBuilder()
                .withId("id")
                .withHvdcId("L")
                .withActivePowerSetpoint(-20.0)
                .withRelativeValue(true)
                .withConverterMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .build();
        action2.toModification().apply(network);
        assertEquals(180.0, hvdcLine.getActivePowerSetpoint());
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());

        HvdcAngleDroopActivePowerControl hvdcLineExt = hvdcLine.getExtension(HvdcAngleDroopActivePowerControl.class);
        assertEquals(200.0f, hvdcLineExt.getP0());
        assertEquals(0.9f, hvdcLineExt.getDroop());
        HvdcAction action3 = new HvdcActionBuilder()
                .withId("id")
                .withHvdcId("L")
                .withP0(100.0)
                .withDroop(1.0)
                .build();
        action3.toModification().apply(network);
        assertEquals(100.0f, hvdcLineExt.getP0());
        assertEquals(1.0f, hvdcLineExt.getDroop());

        assertTrue(hvdcLineExt.isEnabled());
        HvdcAction action4 = new HvdcActionBuilder()
                .withId("id")
                .withHvdcId("L")
                .withActivePowerSetpoint(220.0)
                .withAcEmulationEnabled(false)
                .build();
        action4.toModification().apply(network);
        assertEquals(220.0, hvdcLine.getActivePowerSetpoint());
        assertFalse(hvdcLineExt.isEnabled());
    }

}
