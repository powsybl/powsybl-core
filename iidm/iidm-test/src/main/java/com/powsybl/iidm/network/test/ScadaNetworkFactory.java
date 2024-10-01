/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import java.time.ZonedDateTime;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class ScadaNetworkFactory {

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);
        Network network = Network.create("scada", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        Substation sub = network.newSubstation()
                .setId("sub")
                .add();
        VoltageLevel vl = sub.newVoltageLevel()
                .setNominalV(440.0)
                .setId("vl")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        String busId = "bus";
        vl.getBusBreakerView()
                .newBus()
                .setId(busId)
                .add();
        vl.newBattery()
                .setId("battery")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setBus(busId)
                .add();
        DanglingLine dl = vl.newDanglingLine()
                .setId("dl")
                .setBus(busId)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setHasShuntAdmittanceLineEquivalentModel(true)
                .newGeneration()
                .setMinP(100.0)
                .setMaxP(200.0)
                .add()
                .add();
        dl.getGeneration().newMinMaxReactiveLimits()
                .setMinQ(100)
                .setMaxQ(200)
                .add();
        Generator generator = vl.newGenerator()
                .setVoltageRegulatorOn(true)
                .setId("generator")
                .setBus(busId)
                .setMinP(0.0)
                .setMaxP(100.0)
                .add();
        generator.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0.0)
                .setMinQ(0.0)
                .setMaxQ(0.0)
                .endPoint()
                .beginPoint()
                .setP(10.0)
                .setMinQ(5.0)
                .setMaxQ(15.0)
                .endPoint()
                .add();
        LccConverterStation lcs = vl.newLccConverterStation()
                .setId("lcs")
                .setLossFactor(80f)
                .setPowerFactor(0.2f)
                .setBus(busId)
                .add();
        VoltageLevel vl2 = sub.newVoltageLevel()
                .setId("vl2")
                .setNominalV(220.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        String bus2Id = "bus2";
        vl2.getBusBreakerView()
                .newBus()
                .setId(bus2Id)
                .add();
        network.newLine()
                .setId("line")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setBus1(busId)
                .setBus2(bus2Id)
                .setVoltageLevel1(vl.getId())
                .setVoltageLevel2(vl2.getId())
                .add();
        vl2.newLoad()
                .setId("load")
                .setBus(bus2Id)
                .add();
        vl2.newShuntCompensator()
                .setId("shunt")
                .setBus(bus2Id)
                .setVoltageRegulatorOn(true)
                .newLinearModel()
                .setBPerSection(10.0)
                .setMaximumSectionCount(1)
                .add()
                .add();
        vl2.newStaticVarCompensator()
                .setId("svc")
                .setBmin(1.1)
                .setBmax(3.2)
                .setBus(bus2Id)
                .add();
        VoltageLevel vl3 = sub.newVoltageLevel()
                .setId("vl3")
                .setNominalV(420.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl3.getNodeBreakerView().newSwitch()
                .setId("switch")
                .setNode1(0)
                .setNode2(1)
                .setKind(SwitchKind.BREAKER)
                .add();
        ThreeWindingsTransformer t3wt = sub.newThreeWindingsTransformer()
                .setId("t3wt")
                .newLeg1()
                .setRatedU(400)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setBus(busId)
                .setVoltageLevel(vl.getId())
                .add()
                .newLeg2()
                .setRatedU(220)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setBus(bus2Id)
                .setVoltageLevel(vl2.getId())
                .add()
                .newLeg3()
                .setRatedU(190)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setNode(0)
                .setVoltageLevel(vl3.getId())
                .add().add();
        t3wt.getLeg1().newRatioTapChanger()
                .setRegulating(true)
                .beginStep()
                .setRho(1.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        t3wt.getLeg2().newPhaseTapChanger()
                .setRegulationMode(null)
                .beginStep()
                .setAlpha(1.0)
                .setRho(1.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        TwoWindingsTransformer t2wt = sub.newTwoWindingsTransformer()
                .setId("tw2t")
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU1(440.0)
                .setRatedU2(220.0)
                .setBus1(busId)
                .setVoltageLevel1(vl.getId())
                .setBus2(bus2Id)
                .setVoltageLevel2(vl2.getId())
                .add();
        t2wt.newRatioTapChanger()
                .setRegulating(true)
                .beginStep()
                .setRho(1.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        t2wt.newPhaseTapChanger()
                .setRegulationMode(null)
                .beginStep()
                .setAlpha(1.0)
                .setRho(1.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        VscConverterStation vcs = vl2.newVscConverterStation()
                .setId("vcs")
                .setBus(bus2Id)
                .setLossFactor(80f)
                .setVoltageRegulatorOn(true)
                .add();
        network.newHvdcLine()
                .setNominalV(400.0)
                .setR(1.0)
                .setMaxP(20.0)
                .setConverterStationId1(lcs.getId())
                .setConverterStationId2(vcs.getId())
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setId("hvdcline")
                .add();
        return network;
    }

    private ScadaNetworkFactory() {
    }
}
