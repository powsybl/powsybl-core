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
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        b1.setV(400).setAngle(0);
        Generator g1 = vl1.newGenerator()
                .setId("G1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setVoltageRegulatorOn(true)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        g1.getTerminal().setP(-100.16797).setQ(-58.402832);
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        b2.setV(385.6934).setAngle(-3.6792064);
        Load ld2 = vl2.newLoad()
                .setId("LD2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(100.0)
                .setQ0(50.0)
                .add();
        ld2.getTerminal().setP(100.0).setQ(50.0);
        Line l1 = network.newLine()
                .setId("L1")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(4.0)
                .setX(200.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        l1.getTerminal1().setP(50.084026).setQ(29.201416);
        l1.getTerminal2().setP(-50.0).setQ(-25.0);
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b3 = vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        b3.setV(392.6443).setAngle(-1.8060945);
        TwoWindingsTransformer ps1 = s1.newTwoWindingsTransformer()
                .setId("PS1")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL3")
                .setConnectableBus2("B3")
                .setBus2("B3")
                .setRatedU1(380.0)
                .setRatedU2(380.0)
                .setR(2.0)
                .setX(100.0)
                .setG(0.0)
                .setB(0.0)
                .add();
        ps1.getTerminal1().setP(50.08403).setQ(29.201416);
        ps1.getTerminal2().setP(-50.042015).setQ(-27.100708);
        ps1.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(ps1.getTerminal2())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulationValue(200)
                .beginStep()
                    .setAlpha(-20.0)
                    .setRho(1.0)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setAlpha(0.0)
                    .setRho(1.0)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
                .endStep()
                .beginStep()
                    .setAlpha(20.0)
                    .setRho(1.0)
                    .setR(0.0)
                    .setX(0.0)
                    .setG(0.0)
                    .setB(0.0)
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
                .setR(2.0)
                .setX(100.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .add();
        l2.getTerminal1().setP(50.042015).setQ(27.100708);
        l2.getTerminal2().setP(-50.0).setQ(-25.0);
        return network;
    }
}
