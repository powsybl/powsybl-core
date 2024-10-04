/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.modification.util.ControlledRegulatingTerminals;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.modification.TransformersTestUtils.addPhaseTapChanger;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ControlledRegulatingTerminalsTest {

    @Test
    void twoWindingsTransformerRtcTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("TWT");

        assertNotEquals(t2w.getRatioTapChanger().getRegulationTerminal(), t2w.getTerminal2());
        assertNotEquals(t2w.getPhaseTapChanger().getRegulationTerminal(), t2w.getTerminal2());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t2w.getRatioTapChanger().getRegulationTerminal(), t2w.getTerminal2());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t2w.getPhaseTapChanger().getRegulationTerminal(), t2w.getTerminal2());
        assertEquals(t2w.getRatioTapChanger().getRegulationTerminal(), t2w.getTerminal2());
        assertEquals(t2w.getPhaseTapChanger().getRegulationTerminal(), t2w.getTerminal2());
    }

    @Test
    void threeWindingsTransformerRtcTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT");

        assertNotEquals(t3w.getLeg2().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        assertNotEquals(t3w.getLeg3().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t3w.getLeg2().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t3w.getLeg3().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        assertEquals(t3w.getLeg2().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        assertEquals(t3w.getLeg3().getRatioTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
    }

    @Test
    void threeWindingsTransformerPtcLeg1Test() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(t3w.getLeg1());
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        assertNotEquals(t3w.getLeg1().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg2().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t3w.getLeg1().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg2().getTerminal());
        assertEquals(t3w.getLeg1().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg2().getTerminal());
    }

    @Test
    void threeWindingsTransformerPtcLeg2Test() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(t3w.getLeg2());
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        assertNotEquals(t3w.getLeg2().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t3w.getLeg2().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        assertEquals(t3w.getLeg2().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
    }

    @Test
    void threeWindingsTransformerPtcLeg3Test() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT");
        addPhaseTapChanger(t3w.getLeg3());
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        assertNotEquals(t3w.getLeg3().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(t3w.getLeg3().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
        assertEquals(t3w.getLeg3().getPhaseTapChanger().getRegulationTerminal(), t3w.getLeg1().getTerminal());
    }

    @Test
    void generatorTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        Generator generator = network.getGenerator("GEN_132");
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("3WT");

        assertNotEquals(generator.getRegulatingTerminal(), t3w.getLeg1().getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(generator.getRegulatingTerminal(), t3w.getLeg1().getTerminal());
        assertEquals(generator.getRegulatingTerminal(), t3w.getLeg1().getTerminal());
    }

    @Test
    void shuntCompensatorTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        ShuntCompensator shuntCompensator = network.getShuntCompensator("SHUNT");
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("TWT");

        assertNotEquals(shuntCompensator.getRegulatingTerminal(), t2w.getTerminal1());
        controlledRegulatingTerminals.replaceRegulatingTerminal(shuntCompensator.getRegulatingTerminal(), t2w.getTerminal1());
        assertEquals(shuntCompensator.getRegulatingTerminal(), t2w.getTerminal1());
    }

    @Test
    void staticVarCompensatorTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator("SVC");
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("TWT");

        assertNotEquals(staticVarCompensator.getRegulatingTerminal(), t2w.getTerminal1());
        controlledRegulatingTerminals.replaceRegulatingTerminal(staticVarCompensator.getRegulatingTerminal(), t2w.getTerminal1());
        assertEquals(staticVarCompensator.getRegulatingTerminal(), t2w.getTerminal1());
    }

    @Test
    void voltageSourceConverterTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);

        HvdcConverterStation<?> hvdcConverterStation = network.getHvdcConverterStation("VSC1");
        VscConverterStation vscConverterStation = (VscConverterStation) hvdcConverterStation;
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("TWT");

        assertNotEquals(vscConverterStation.getRegulatingTerminal(), t2w.getTerminal1());
        controlledRegulatingTerminals.replaceRegulatingTerminal(vscConverterStation.getRegulatingTerminal(), t2w.getTerminal1());
        assertEquals(vscConverterStation.getRegulatingTerminal(), t2w.getTerminal1());
    }

    @Test
    void batteryTest() {
        Network network = BatteryNetworkFactory.create();

        Battery battery = network.getBattery("BAT2");
        VoltageRegulation voltageRegulation = battery.newExtension(VoltageRegulationAdder.class)
                .withRegulatingTerminal(battery.getTerminal())
                .withVoltageRegulatorOn(true)
                .withTargetV(50.0)
                .add();

        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);
        Generator generator = network.getGenerator("GEN");

        assertNotEquals(voltageRegulation.getRegulatingTerminal(), generator.getTerminal());
        controlledRegulatingTerminals.replaceRegulatingTerminal(voltageRegulation.getRegulatingTerminal(), generator.getTerminal());
        assertEquals(voltageRegulation.getRegulatingTerminal(), generator.getTerminal());
    }

    @Test
    void generatorRemoteReactiveControlTest() {
        Network network = BatteryNetworkFactory.create();

        Generator generator = network.getGenerator("GEN");
        RemoteReactivePowerControl remoteReactivePowerControl = generator.newExtension(RemoteReactivePowerControlAdder.class)
                .withTargetQ(100.0)
                .withRegulatingTerminal(generator.getTerminal())
                .withEnabled(true)
                .add();

        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);
        Line line = network.getLine("NHV1_NHV2_1");

        assertNotEquals(remoteReactivePowerControl.getRegulatingTerminal(), line.getTerminal1());
        controlledRegulatingTerminals.replaceRegulatingTerminal(remoteReactivePowerControl.getRegulatingTerminal(), line.getTerminal1());
        assertEquals(remoteReactivePowerControl.getRegulatingTerminal(), line.getTerminal1());
    }

    @Test
    void slackTerminalTest() {
        Network network = BatteryNetworkFactory.create();

        Generator generator = network.getGenerator("GEN");
        VoltageLevel voltageLevel = network.getVoltageLevel("VLGEN");
        SlackTerminal slackTerminal = voltageLevel.newExtension(SlackTerminalAdder.class)
                .withTerminal(generator.getTerminal())
                .add();

        ControlledRegulatingTerminals controlledRegulatingTerminals = new ControlledRegulatingTerminals(network);
        Line line = network.getLine("NHV1_NHV2_1");

        assertNotEquals(slackTerminal.getTerminal(), line.getTerminal1());
        controlledRegulatingTerminals.replaceRegulatingTerminal(slackTerminal.getTerminal(), line.getTerminal1());
        assertEquals(slackTerminal.getTerminal(), line.getTerminal1());
    }
}
