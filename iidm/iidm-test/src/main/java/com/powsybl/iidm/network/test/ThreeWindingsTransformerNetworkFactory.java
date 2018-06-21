/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import org.joda.time.DateTime;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class ThreeWindingsTransformerNetworkFactory {

    public static Network create() {
        Network network = NetworkFactory.create("three-windings-transformer", "test");
        network.setCaseDate(DateTime.parse("2018-03-05T13:30:30.486+01:00"));
        Substation substation = network.newSubstation()
            .setId("SUBSTATION")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = substation.newVoltageLevel()
            .setId("VL_132")
            .setNominalV(132.0)
            .setLowVoltageLimit(118.8)
            .setHighVoltageLimit(145.2)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus132 = vl1.getBusBreakerView().newBus()
            .setId("BUS_132")
            .add();
        bus132.setV(133.584).setAngle(-9.62);
        vl1.newLoad()
            .setId("LOAD_132")
            .setBus("BUS_132")
            .setP0(7.6)
            .setQ0(1.6)
            .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
            .setId("VL_33")
            .setNominalV(33.0)
            .setLowVoltageLimit(29.7)
            .setHighVoltageLimit(36.3)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus33 = vl2.getBusBreakerView().newBus()
            .setId("BUS_33")
            .add();
        bus33.setV(34.881).setAngle(-15.24);
        Load load33 = vl2.newLoad()
            .setId("LOAD_33")
            .setBus("BUS_33")
            .setP0(11.2)
            .setQ0(7.5)
            .add();
        load33.getTerminal()
            .setP(11.2)
            .setQ(7.5);

        VoltageLevel vl3 = substation.newVoltageLevel()
            .setId("VL_11")
            .setNominalV(11.0)
            .setLowVoltageLimit(9.9)
            .setHighVoltageLimit(12.1)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus11 = vl3.getBusBreakerView().newBus()
            .setId("BUS_11")
            .add();
        bus11.setV(11.781).setAngle(-15.24);
        Load load11 = vl3.newLoad()
            .setId("LOAD_11")
            .setBus("BUS_11")
            .setP0(0.0)
            .setQ0(-10.6)
            .add();
        load11.getTerminal()
            .setP(0.0)
            .setQ(-10.6);

        ThreeWindingsTransformer twt = substation.newThreeWindingsTransformer()
            .setId("3WT")
            .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setB(0.000573921028466483)
                .setG(0.00573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vl1.getId())
                .setBus(bus132.getId())
            .add()
            .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setRatedU(33.0)
                .setVoltageLevel(vl2.getId())
                .setBus(bus33.getId())
            .add()
            .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setRatedU(11.0)
                .setVoltageLevel(vl3.getId())
                .setBus(bus11.getId())
            .add()
            .add();

        twt.getLeg2().newRatioTapChanger()
            .beginStep()
                .setRho(0.9)
                .setR(0.9801)
                .setX(0.09801)
                .setG(0.08264462809917356)
                .setB(0.008264462809917356)
            .endStep()
            .beginStep()
                .setRho(1.0)
                .setR(1.089)
                .setX(0.1089)
                .setG(0.09182736455463728)
                .setB(0.009182736455463728)
            .endStep()
            .beginStep()
                .setRho(1.1)
                .setR(1.1979)
                .setX(0.11979)
                .setG(0.10101010101010101)
                .setB(0.010101010101010101)
            .endStep()
            .setTapPosition(2)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setTargetV(33.0)
            .setRegulationTerminal(load33.getTerminal())
            .add();

        twt.getLeg3().newRatioTapChanger()
            .beginStep()
                .setRho(0.9)
                .setR(0.1089)
                .setX(0.01089)
                .setG(0.8264462809917356)
                .setB(0.08264462809917356)
            .endStep()
            .beginStep()
                .setRho(1.0)
                .setR(0.121)
                .setX(0.0121)
                .setG(0.8264462809917356)
                .setB(0.08264462809917356)
            .endStep()
            .beginStep()
                .setRho(1.1)
                .setR(0.1331)
                .setX(0.01331)
                .setG(0.9090909090909092)
                .setB(0.09090909090909092)
            .endStep()
            .setTapPosition(0)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setTargetV(11.0)
            .setRegulationTerminal(load11.getTerminal())
            .add();

        return network;
    }

    public static Network createWithCurrentLimits() {
        Network network = create();

        network.getThreeWindingsTransformer("3WT").getLeg1().newCurrentLimits()
            .setPermanentLimit(1000.0)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(1200.0)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(1400.0)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg2().newCurrentLimits()
            .setPermanentLimit(100.0)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(120.0)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(140.0)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg3().newCurrentLimits()
            .setPermanentLimit(10.0)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(12.0)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(14.0)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        return network;
    }

    private ThreeWindingsTransformerNetworkFactory() {
    }
}
