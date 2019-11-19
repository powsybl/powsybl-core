/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class ThreeWindingsTransformerNetworkFactory {

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("three-windings-transformer", "test");
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
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vl1.getId())
                .setBus(bus132.getId())
            .add()
            .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(33.0)
                .setVoltageLevel(vl2.getId())
                .setBus(bus33.getId())
            .add()
            .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
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
            .setRegulating(false)
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

    public static Network createWithTapChangers() {
        Network network = create();

        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");

        twt.getLeg1().newRatioTapChanger()
        .beginStep()
            .setRho(1.0043151895694895)
            .setR(0.8649000000000129)
            .setX(0.8649000000000129)
            .setG(-0.8574836241348693)
            .setB(-0.8574836241348693)
        .endStep()
        .beginStep()
            .setRho(1.002998629111725)
            .setR(0.6006250000000213)
            .setX(0.6006250000000213)
            .setG(-0.5970390343002507)
            .setB(-0.5970390343002507)
        .endStep()
        .beginStep()
            .setRho(1.0019201564995088)
            .setR(0.38439999999992924)
            .setX(0.38439999999992924)
            .setG(-0.3829280246730904)
            .setB(-0.3829280246730904)
        .endStep()
        .setTapPosition(2)
        .setLoadTapChangingCapabilities(true)
        .setRegulating(false)
        .setTargetV(132.0)
        .setRegulationTerminal(null)
        .setTargetDeadband(0.0)
        .add();

        twt.getLeg1().newPhaseTapChanger()
        .beginStep()
            .setRho(1.0043151895694895)
            .setAlpha(5.313224638415126)
            .setR(0.8649000000000129)
            .setX(0.8649000000000129)
            .setG(-0.8574836241348693)
            .setB(-0.8574836241348693)
        .endStep()
        .beginStep()
            .setRho(1.002998629111725)
            .setAlpha(4.431564716435844)
            .setR(0.6006250000000213)
            .setX(0.6006250000000213)
            .setG(-0.5970390343002507)
            .setB(-0.5970390343002507)
        .endStep()
        .beginStep()
            .setRho(1.0019201564995088)
            .setAlpha(3.547797069667891)
            .setR(0.38439999999992924)
            .setX(0.38439999999992924)
            .setG(-0.3829280246730904)
            .setB(-0.3829280246730904)
        .endStep()
        .setTapPosition(1)
        .setRegulating(false)
        .setRegulationValue(0.0)
        .setRegulationMode(RegulationMode.FIXED_TAP)
        .setRegulationTerminal(null)
        .setTargetDeadband(0.0)
        .add();

        twt.getLeg2().newPhaseTapChanger()
        .beginStep()
            .setRho(0.9931474324087035)
            .setAlpha(0.6919900845542891)
            .setR(-1.365817750000009)
            .setX(-1.365817750000009)
            .setG(1.3847306469659593)
            .setB(-0.8574836241348693)
        .endStep()
        .beginStep()
            .setRho(1.0)
            .setAlpha(0.0)
            .setR(0.0)
            .setX(0.0)
            .setG(0.0)
            .setB(0.0)
        .endStep()
        .beginStep()
            .setRho(1.0069964361903174)
            .setAlpha(-0.6824728416187119)
            .setR(1.4041822500000078)
            .setX(1.4041822500000078)
            .setG(-1.3847380047286029)
            .setB(-1.3847380047286029)
        .endStep()
        .setTapPosition(2)
        .setRegulating(false)
        .setRegulationValue(0.0)
        .setRegulationMode(RegulationMode.FIXED_TAP)
        .setRegulationTerminal(null)
        .setTargetDeadband(0.0)
        .add();

        twt.getLeg3().newPhaseTapChanger()
        .beginStep()
            .setRho(1.0001201177858587)
            .setAlpha(-0.8880134719294318)
            .setR(0.024025000000005292)
            .setX(0.024025000000005292)
            .setG(-0.024019229380145557)
            .setB(-0.024019229380145557)
        .endStep()
        .beginStep()
            .setRho(1.0004803846153107)
            .setAlpha(-1.7756005265572734)
            .setR(0.09610000000002117)
            .setX(0.09610000000002117)
            .setG(-0.09600773656518458)
            .setB(-0.09600773656518458)
        .endStep()
        .beginStep()
            .setRho(1.001080541215341)
            .setAlpha(-2.662335973879195)
            .setR(0.21622500000000322)
            .setX(0.21622500000000322)
            .setG(-0.21575847623476196)
            .setB(-0.21575847623476196)
        .endStep()
        .setTapPosition(2)
        .setRegulating(false)
        .setRegulationValue(0.0)
        .setRegulationMode(RegulationMode.FIXED_TAP)
        .setRegulationTerminal(null)
        .setTargetDeadband(0.0)
        .add();

        return network;
    }

    private ThreeWindingsTransformerNetworkFactory() {
    }
}
