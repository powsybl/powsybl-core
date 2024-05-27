/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.action;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.network.test.TwoVoltageLevelNetworkFactory;
import org.junit.jupiter.api.Test;

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
        ValidationException e = assertThrows(ValidationException.class, () -> modif.apply(network));
        assertEquals("Shunt compensator 'SHUNT': the current number (2) of section should be lesser than the maximum number of section (1)", e.getMessage());
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
        PowsyblException e = assertThrows(PowsyblException.class, () -> modif.apply(network, true, null));
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

}
