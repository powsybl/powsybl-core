/**
 * Copyright (c) 2024, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.FIXED_TAP;
import static com.powsybl.iidm.network.RatioTapChanger.RegulationMode.VOLTAGE;
import static com.powsybl.iidm.network.TopologyKind.BUS_BREAKER;
import static com.powsybl.iidm.network.TwoSides.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet at rte-france.com>}
 */
public abstract class AbstractTapChangerHolderTest {

    @Test
    void shouldReuseCopiedPhaseTapChangerPropertiesFixedTapExample() {
        Network network = exampleNetwork();
        PhaseTapChanger existingPhaseTapChanger = network.getTwoWindingsTransformer("transformer")
                .newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationValue(12)
                .setRegulationMode(FIXED_TAP)
                .setLowTapPosition(0)
                .setRegulating(false)
                .setTargetDeadband(3)
                .beginStep().setAlpha(1).setRho(2).setR(3).setG(4).setB(5).setX(6)
                .endStep()
                .beginStep().setAlpha(2).setRho(3).setR(4).setG(5).setB(6).setX(7)
                .endStep()
                .beginStep().setAlpha(3).setRho(4).setR(5).setG(6).setB(7).setX(8)
                .endStep()
                .add();

        PhaseTapChanger newPhaseTapChanger = network.getTwoWindingsTransformer("transformer2")
                .newPhaseTapChanger(existingPhaseTapChanger)
                .add();

        assertEquals(existingPhaseTapChanger.getTapPosition(), newPhaseTapChanger.getTapPosition());
        assertEquals(existingPhaseTapChanger.getLowTapPosition(), newPhaseTapChanger.getLowTapPosition());
        assertEquals(existingPhaseTapChanger.getRegulationValue(), newPhaseTapChanger.getRegulationValue());
        assertEquals(existingPhaseTapChanger.getRegulationMode(), newPhaseTapChanger.getRegulationMode());
        assertEquals(existingPhaseTapChanger.isRegulating(), newPhaseTapChanger.isRegulating());
        assertEquals(existingPhaseTapChanger.getTargetDeadband(), newPhaseTapChanger.getTargetDeadband());

        newPhaseTapChanger.getAllSteps().forEach((tap, newStep) -> {
            PhaseTapChangerStep existingStep = existingPhaseTapChanger.getStep(tap);
            assertEquals(existingStep.getAlpha(), newStep.getAlpha());
            assertEquals(existingStep.getRho(), newStep.getRho());
            assertEquals(existingStep.getR(), newStep.getR());
            assertEquals(existingStep.getG(), newStep.getG());
            assertEquals(existingStep.getB(), newStep.getB());
            assertEquals(existingStep.getX(), newStep.getX());
        });
    }

    @Test
    void shouldReuseCopiedPhaseTapChangerPropertiesActivePowerControlExample() {
        Network network = exampleNetwork();
        PhaseTapChanger existingPhaseTapChanger = network.getTwoWindingsTransformer("transformer")
                .newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationValue(12)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationTerminal(network.getTwoWindingsTransformer("transformer").getTerminal(ONE))
                .setLowTapPosition(0)
                .setRegulating(false)
                .setTargetDeadband(3)
                .beginStep().setAlpha(1).setRho(2).setR(3).setG(4).setB(5).setX(6)
                .endStep()
                .beginStep().setAlpha(2).setRho(3).setR(4).setG(5).setB(6).setX(7)
                .endStep()
                .beginStep().setAlpha(3).setRho(4).setR(5).setG(6).setB(7).setX(8)
                .endStep()
                .add();

        PhaseTapChanger newPhaseTapChanger = network.getTwoWindingsTransformer("transformer2")
                .newPhaseTapChanger(existingPhaseTapChanger)
                .add();

        assertEquals(existingPhaseTapChanger.getTapPosition(), newPhaseTapChanger.getTapPosition());
        assertEquals(existingPhaseTapChanger.getLowTapPosition(), newPhaseTapChanger.getLowTapPosition());
        assertEquals(existingPhaseTapChanger.getRegulationValue(), newPhaseTapChanger.getRegulationValue());
        assertEquals(existingPhaseTapChanger.getRegulationMode(), newPhaseTapChanger.getRegulationMode());
        assertEquals(existingPhaseTapChanger.isRegulating(), newPhaseTapChanger.isRegulating());
        assertEquals(existingPhaseTapChanger.getTargetDeadband(), newPhaseTapChanger.getTargetDeadband());
        assertEquals(existingPhaseTapChanger.getRegulationTerminal(), newPhaseTapChanger.getRegulationTerminal());

        newPhaseTapChanger.getAllSteps().forEach((tap, newStep) -> {
            PhaseTapChangerStep existingStep = existingPhaseTapChanger.getStep(tap);
            assertEquals(existingStep.getAlpha(), newStep.getAlpha());
            assertEquals(existingStep.getRho(), newStep.getRho());
            assertEquals(existingStep.getR(), newStep.getR());
            assertEquals(existingStep.getG(), newStep.getG());
            assertEquals(existingStep.getB(), newStep.getB());
            assertEquals(existingStep.getX(), newStep.getX());
        });
    }

    @Test
    void shouldReuseCopiedRatioTapChangerProperties() {
        Network network = exampleNetwork();
        RatioTapChanger existingRatioTapChanger = network.getTwoWindingsTransformer("transformer").newRatioTapChanger()
                .setTapPosition(1)
                .setTargetV(400)
                .setRegulationValue(12)
                .setRegulationMode(VOLTAGE)
                .setLowTapPosition(0)
                .setRegulating(false)
                .setLoadTapChangingCapabilities(true)
                .setTargetDeadband(3)
                .beginStep().setRho(2).setR(3).setG(4).setB(5).setX(6)
                .endStep()
                .beginStep().setRho(3).setR(4).setG(5).setB(6).setX(7)
                .endStep()
                .beginStep().setRho(4).setR(5).setG(6).setB(7).setX(8)
                .endStep()
                .add();

        RatioTapChanger newRatioTapChanger = network.getTwoWindingsTransformer("transformer2")
                .newRatioTapChanger(existingRatioTapChanger)
                .add();

        assertEquals(existingRatioTapChanger.getTapPosition(), newRatioTapChanger.getTapPosition());
        assertEquals(existingRatioTapChanger.getLowTapPosition(), newRatioTapChanger.getLowTapPosition());
        assertEquals(existingRatioTapChanger.getRegulationValue(), newRatioTapChanger.getRegulationValue());
        assertEquals(existingRatioTapChanger.getRegulationMode(), newRatioTapChanger.getRegulationMode());
        assertEquals(existingRatioTapChanger.isRegulating(), newRatioTapChanger.isRegulating());
        assertEquals(existingRatioTapChanger.getTargetDeadband(), newRatioTapChanger.getTargetDeadband());
        assertEquals(existingRatioTapChanger.getRegulationTerminal(), newRatioTapChanger.getRegulationTerminal());
        assertEquals(existingRatioTapChanger.getTargetV(), newRatioTapChanger.getTargetV());
        assertEquals(existingRatioTapChanger.hasLoadTapChangingCapabilities(), newRatioTapChanger.hasLoadTapChangingCapabilities());

        newRatioTapChanger.getAllSteps().forEach((tap, newStep) -> {
            RatioTapChangerStep existingStep = existingRatioTapChanger.getStep(tap);
            assertEquals(existingStep.getRho(), newStep.getRho());
            assertEquals(existingStep.getR(), newStep.getR());
            assertEquals(existingStep.getG(), newStep.getG());
            assertEquals(existingStep.getB(), newStep.getB());
            assertEquals(existingStep.getX(), newStep.getX());
        });
    }

    Network exampleNetwork() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation()
                .setId("substation")
                .setCountry(Country.AD)
                .add();
        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("vl1")
                .setTopologyKind(BUS_BREAKER)
                .setName("name")
                .setNominalV(225)
                .setLowVoltageLimit(200)
                .setHighVoltageLimit(250)
                .add();
        vl1.getBusBreakerView().newBus().setId("bus1").add();
        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("vl2")
                .setTopologyKind(BUS_BREAKER)
                .setName("name")
                .setNominalV(90)
                .setLowVoltageLimit(80)
                .setHighVoltageLimit(100)
                .add();
        vl2.getBusBreakerView().newBus().setId("bus2").add();

        substation.newTwoWindingsTransformer()
                .setId("transformer")
                .setR(17)
                .setX(10)
                .setBus1("bus1")
                .setBus2("bus2")
                .add();

        substation.newTwoWindingsTransformer()
                .setId("transformer2")
                .setR(12)
                .setX(15)
                .setBus1("bus1")
                .setBus2("bus2")
                .add();

        return network;
    }
}
