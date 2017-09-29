/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * A very small network to test phase shifters.
 *
 *     G1                   LD2
 *     |          L1        |
 *     |  ----------------- |
 *     B1                   B2
 *        --------B3-------
 *           PS1       L2
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class PhaseShifterTestCaseFactory {

    private PhaseShifterTestCaseFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("phaseShifterTestCase", "code");
        network.setCaseDate(DateTime.parse("2016-10-18T10:06:00.000+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        b1.setV(400f).setAngle(0f);
        Generator g1 = vl1.newGenerator()
                .setId("G1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setVoltageRegulatorOn(true)
                .setTargetP(100f)
                .setTargetV(400f)
                .setMinP(50)
                .setMaxP(150)
                .add();
        g1.getTerminal().setP(-100.16797f).setQ(-58.402832f);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        b2.setV(385.6934f).setAngle(-3.6792064f);
        Load ld2 = vl2.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(100f)
                .setQ0(50f)
                .add();
        ld2.getTerminal().setP(100f).setQ(50f);
        Line l1 = network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(4f)
                .setX(200f)
                .setG1(0f)
                .setB1(0f)
                .setG2(0f)
                .setB2(0f)
                .add();
        l1.getTerminal1().setP(50.084026f).setQ(29.201416f);
        l1.getTerminal2().setP(-50f).setQ(-25f);
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b3 = vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        b3.setV(392.6443f).setAngle(-1.8060945f);
        TwoWindingsTransformer ps1 = s1.newTwoWindingsTransformer()
                .setId("PS1")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL3")
                .setConnectableBus2("B3")
                .setBus2("B3")
                .setRatedU1(380f)
                .setRatedU2(380f)
                .setR(2f)
                .setX(100f)
                .setG(0f)
                .setB(0f)
                .add();
        ps1.getTerminal1().setP(50.08403f).setQ(29.201416f);
        ps1.getTerminal2().setP(-50.042015f).setQ(-27.100708f);
        ps1.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(ps1.getTerminal2())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulationValue(200)
                .beginStep()
                    .setAlpha(-20f)
                    .setRho(1f)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .beginStep()
                    .setAlpha(0f)
                    .setRho(1f)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .beginStep()
                    .setAlpha(20f)
                    .setRho(1f)
                    .setR(0f)
                    .setX(0f)
                    .setG(0f)
                    .setB(0f)
                .endStep()
                .add();
        Line l2 = network.newLine()
                .setId("L2")
                .setVoltageLevel1("VL3")
                .setConnectableBus1("B3")
                .setBus1("B3")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(2f)
                .setX(100f)
                .setG1(0f)
                .setB1(0f)
                .setG2(0f)
                .setB2(0f)
                .add();
        l2.getTerminal1().setP(50.042015f).setQ(27.100708f);
        l2.getTerminal2().setP(-50f).setQ(-25f);
        return network;
    }
}
