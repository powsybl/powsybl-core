/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class ThreeWindingsTransformerNetworkFactory {

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("three-windings-transformer", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-03-05T13:30:30.486+01:00"));
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
        vl1.newGenerator()
                .setId("GEN_132")
                .setBus("BUS_132")
                .setMinP(0.0)
                .setMaxP(140)
                .setTargetP(7.2)
                .setTargetV(135)
                .setVoltageRegulatorOn(true)
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
                .setRatedU0(132.0)
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(33.0)
                .setTargetDeadband(0)
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(11.0)
                .setRegulationTerminal(load11.getTerminal())
                .add();

        return network;
    }

    public static Network createWithUnsortedEnds(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("three-windings-transformer-with-unsorted-ends", "test");
        network.setCaseDate(ZonedDateTime.parse("2018-03-05T13:30:30.486+01:00"));
        Substation substation = network.newSubstation()
                .setId("SUBSTATION")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = substation.newVoltageLevel()
                .setId("VL_132")
                .setNominalV(132.0)
                .setLowVoltageLimit(118.8)
                .setHighVoltageLimit(145.2)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("busbarSection_132")
                .setName("busbarSection_132")
                .setNode(0)
                .add();

        vl1.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(2)
                .add();

        vl1.newGenerator()
                .setId("GEN_132")
                .setNode(1)
                .setMinP(0.0)
                .setMaxP(140)
                .setTargetP(7.2)
                .setTargetV(135)
                .setVoltageRegulatorOn(true)
                .add();

        VoltageLevel vl2 = substation.newVoltageLevel()
                .setId("VL_33")
                .setNominalV(33.0)
                .setLowVoltageLimit(29.7)
                .setHighVoltageLimit(36.3)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("busbarSection_33")
                .setName("busbarSection_33")
                .setNode(0)
                .add();

        vl2.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        vl2.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(2)
                .add();

        Load load33 = vl2.newLoad()
                .setId("LOAD_33")
                .setNode(1)
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
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl3.getNodeBreakerView().newBusbarSection()
                .setId("busbarSection_11")
                .setName("busbarSection_11")
                .setNode(0)
                .add();

        vl3.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        vl3.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(2)
                .add();

        Load load11 = vl3.newLoad()
                .setId("LOAD_11")
                .setNode(1)
                .setP0(0.0)
                .setQ0(-10.6)
                .add();
        load11.getTerminal()
                .setP(0.0)
                .setQ(-10.6);

        ThreeWindingsTransformer twt = substation.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(11.0)
                .setVoltageLevel(vl3.getId())
                .setNode(2)
                .add()
                .newLeg2()
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vl1.getId())
                .setNode(2)
                .add()
                .newLeg3()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(33.0)
                .setVoltageLevel(vl2.getId())
                .setNode(2)
                .add()
                .add();

        twt.getLeg3().newRatioTapChanger()
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(33.0)
                .setTargetDeadband(0)
                .setRegulationTerminal(load33.getTerminal())
                .add();

        twt.getLeg1().newRatioTapChanger()
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(11.0)
                .setRegulationTerminal(load11.getTerminal())
                .add();

        return network;
    }

    public static Network createWithCurrentLimits() {
        Network network = create();

        network.getThreeWindingsTransformer("3WT").getLeg1().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg2().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg3().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

    public static Network createWithUnsortedEndsAndCurrentLimits() {
        Network network = createWithUnsortedEnds(NetworkFactory.findDefault());

        network.getThreeWindingsTransformer("3WT").getLeg1().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg2().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg3().getOrCreateSelectedOperationalLimitsGroup().newCurrentLimits()
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

    public static Network createWithApparentPowerLimits() {
        Network network = create();

        network.getThreeWindingsTransformer("3WT").getLeg1().getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg2().getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg3().getOrCreateSelectedOperationalLimitsGroup().newApparentPowerLimits()
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

    public static Network createWithActivePowerLimits() {
        Network network = create();

        network.getThreeWindingsTransformer("3WT").getLeg1().getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg2().getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits()
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

        network.getThreeWindingsTransformer("3WT").getLeg3().getOrCreateSelectedOperationalLimitsGroup().newActivePowerLimits()
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

    public static Network createWithCurrentLimitsAndTerminalsPAndQ() {
        Network network = createWithCurrentLimits();
        network.getThreeWindingsTransformer("3WT").getLeg1().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg2().getTerminal().setP(1400.0).setQ(400.0);
        network.getThreeWindingsTransformer("3WT").getLeg3().getTerminal().setP(1400.0).setQ(400.0);
        return network;
    }

    private ThreeWindingsTransformerNetworkFactory() {
    }
}
