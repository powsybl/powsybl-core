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
            .setNominalV(132.0f)
            .setLowVoltageLimit(118.8f)
            .setHighVoltageLimit(145.2f)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus132 = vl1.getBusBreakerView().newBus()
            .setId("BUS_132")
            .add();
        bus132.setV(133.584f).setAngle(-9.62f);
        vl1.newLoad()
            .setId("LOAD_132")
            .setBus("BUS_132")
            .setP0(7.6f)
            .setQ0(1.6f)
            .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
            .setId("VL_33")
            .setNominalV(33.0f)
            .setLowVoltageLimit(29.7f)
            .setHighVoltageLimit(36.3f)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus33 = vl2.getBusBreakerView().newBus()
            .setId("BUS_33")
            .add();
        bus33.setV(34.881f).setAngle(-15.24f);
        Load load33 = vl2.newLoad()
            .setId("LOAD_33")
            .setBus("BUS_33")
            .setP0(11.2f)
            .setQ0(7.5f)
            .add();
        load33.getTerminal()
            .setP(11.2f)
            .setQ(7.5f);

        VoltageLevel vl3 = substation.newVoltageLevel()
            .setId("VL_11")
            .setNominalV(11.0f)
            .setLowVoltageLimit(9.9f)
            .setHighVoltageLimit(12.1f)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        Bus bus11 = vl3.getBusBreakerView().newBus()
            .setId("BUS_11")
            .add();
        bus11.setV(11.781f).setAngle(-15.24f);
        Load load11 = vl3.newLoad()
            .setId("LOAD_11")
            .setBus("BUS_11")
            .setP0(0.0f)
            .setQ0(-10.6f)
            .add();
        load11.getTerminal()
            .setP(0.0f)
            .setQ(-10.6f);

        ThreeWindingsTransformer twt = substation.newThreeWindingsTransformer()
            .setId("3WT")
            .newLeg1()
                .setR(17.424f)
                .setX(1.7424f)
                .setB(0.000573921028466483f)
                .setG(0.00573921028466483f)
                .setRatedU(132.0f)
                .setVoltageLevel(vl1.getId())
                .setBus(bus132.getId())
            .add()
            .newLeg2()
                .setR(1.089f)
                .setX(0.1089f)
                .setRatedU(33.0f)
                .setVoltageLevel(vl2.getId())
                .setBus(bus33.getId())
            .add()
            .newLeg3()
                .setR(0.121f)
                .setX(0.0121f)
                .setRatedU(11.0f)
                .setVoltageLevel(vl3.getId())
                .setBus(bus11.getId())
            .add()
            .add();

        twt.getLeg2().newRatioTapChanger()
            .beginStep()
                .setRho(0.9f)
                .setR(0.9801f)
                .setX(0.09801f)
                .setG(0.08264462809917356f)
                .setB(0.008264462809917356f)
            .endStep()
            .beginStep()
                .setRho(1.0f)
                .setR(1.089f)
                .setX(0.1089f)
                .setG(0.09182736455463728f)
                .setB(0.009182736455463728f)
            .endStep()
            .beginStep()
                .setRho(1.1f)
                .setR(1.1979f)
                .setX(0.11979f)
                .setG(0.10101010101010101f)
                .setB(0.010101010101010101f)
            .endStep()
            .setTapPosition(2)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setTargetV(33.0f)
            .setRegulationTerminal(load33.getTerminal())
            .add();

        twt.getLeg3().newRatioTapChanger()
            .beginStep()
                .setRho(0.9f)
                .setR(0.1089f)
                .setX(0.01089f)
                .setG(0.8264462809917356f)
                .setB(0.08264462809917356f)
            .endStep()
            .beginStep()
                .setRho(1.0f)
                .setR(0.121f)
                .setX(0.0121f)
                .setG(0.8264462809917356f)
                .setB(0.08264462809917356f)
            .endStep()
            .beginStep()
                .setRho(1.1f)
                .setR(0.1331f)
                .setX(0.01331f)
                .setG(0.9090909090909092f)
                .setB(0.09090909090909092f)
            .endStep()
            .setTapPosition(0)
            .setLoadTapChangingCapabilities(true)
            .setRegulating(true)
            .setTargetV(11.0f)
            .setRegulationTerminal(load11.getTerminal())
            .add();

        return network;
    }

    public static Network createWithCurrentLimits() {
        Network network = create();

        network.getThreeWindingsTransformer("3WT").getLeg1().newCurrentLimits()
            .setPermanentLimit(1000.0f)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(1200.0f)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(1400.0f)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg2().newCurrentLimits()
            .setPermanentLimit(100.0f)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(120.0f)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(140.0f)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        network.getThreeWindingsTransformer("3WT").getLeg3().newCurrentLimits()
            .setPermanentLimit(10.0f)
            .beginTemporaryLimit()
                .setName("20'")
                .setValue(12.0f)
                .setAcceptableDuration(20 * 60)
            .endTemporaryLimit()
            .beginTemporaryLimit()
                .setName("10'")
                .setValue(14.0f)
                .setAcceptableDuration(10 * 60)
            .endTemporaryLimit()
            .add();

        return network;
    }

    private ThreeWindingsTransformerNetworkFactory() {
    }
}
