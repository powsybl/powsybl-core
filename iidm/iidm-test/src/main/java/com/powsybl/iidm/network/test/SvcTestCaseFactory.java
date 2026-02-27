/**
 * Copyright (c) 2016-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;

import java.time.ZonedDateTime;

import java.util.Objects;

/**
 * A very small network to test SVC modeling. 2 buses B1 and B2. A generator G1 regulating voltage is connected to B1.
 * B1 and B2 are connected by a line with a high reactance to cause an important voltage drop.
 * A SVC is connected to B2 to compensate the voltage drop.
 *
 *     G1                L2
 *     |                 |
 *     B1 ---------------B2
 *                       |
 *                       SVC2
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class SvcTestCaseFactory {

    private SvcTestCaseFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("svcTestCase", "code");
        network.setCaseDate(ZonedDateTime.parse("2016-06-29T14:54:03.427+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl1.newGenerator()
                .setId("G1")
                .setConnectableBus("B1")
                .setBus("B1")
                .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).withTargetValue(400).add()
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl2.newLoad()
                .setId("L2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(100.0)
                .setQ0(50.0)
                .add();
        vl2.newStaticVarCompensator()
                .setId("SVC2")
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(390)
                    .add()
                .add();
        network.newLine()
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
        return network;
    }

    public static Network createWithMoreSVCs() {
        return createWithMoreSVCs(NetworkFactory.findDefault());
    }

    public static Network createWithMoreSVCs(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.getVoltageLevel("VL2").newStaticVarCompensator()
                .setId("SVC3")
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(390)
                    .add()
                .setTargetQ(350)
                .add();

        return network;
    }

    public static Network createWithRemoteRegulatingTerminal() {
        return createWithRemoteRegulatingTerminal(NetworkFactory.findDefault());
    }

    public static Network createWithRemoteRegulatingTerminal(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.getStaticVarCompensator("SVC2").getVoltageRegulation()
                // use a real remote terminal
                .setTerminal(network.getGenerator("G1").getTerminal());

        return network;
    }

    private static Network addReactiveTarget(Network network) {
        VoltageRegulation voltageRegulation = network.getStaticVarCompensator("SVC2").getVoltageRegulation();
        voltageRegulation.setTargetValue(350);
        voltageRegulation.setMode(RegulationMode.VOLTAGE);
        return network;
    }

    private static Network addVoltageTarget(Network network) {
        VoltageRegulation voltageRegulation = network.getStaticVarCompensator("SVC2").getVoltageRegulation();
        voltageRegulation.setTargetValue(390);
        voltageRegulation.setMode(RegulationMode.VOLTAGE);
        return network;
    }

    private static Network addBothTarget(Network network) {
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setTargetV(350);
        svc2.setTargetQ(390);
        return network;
    }

    private static Network createLocalVoltageControl(Network network) {
        return addVoltageControl(network);
    }

    public static Network createLocalVoltageControl() {
        return createLocalVoltageControl(create());
    }

    public static Network createRemoteVoltageControl() {
        return createLocalVoltageControl(createWithRemoteRegulatingTerminal());
    }

    private static Network addVoltageControl(Network network) {
        addVoltageTarget(network);
        VoltageRegulation voltageRegulation = network.getStaticVarCompensator("SVC2").getVoltageRegulation();
        voltageRegulation.setMode(RegulationMode.VOLTAGE);
        voltageRegulation.setRegulating(true);
        return network;
    }

    private static Network createReactiveControl(Network network) {
        return addReactiveControl(network);
    }

    public static Network createLocalReactiveControl() {
        return createReactiveControl(create());
    }

    public static Network createRemoteReactiveControl() {
        return createReactiveControl(createWithRemoteRegulatingTerminal());
    }

    private static Network addReactiveControl(Network network) {
        addReactiveTarget(network);
        VoltageRegulation voltageRegulation = network.getStaticVarCompensator("SVC2").getVoltageRegulation();
        voltageRegulation.setMode(RegulationMode.REACTIVE_POWER);
        voltageRegulation.setRegulating(true);
        return network;
    }

    public static Network createLocalOffReactiveTarget() {
        return createOffReactiveTarget(create());
    }

    public static Network createRemoteOffReactiveTarget() {
        return createOffReactiveTarget(createWithRemoteRegulatingTerminal());
    }

    private static Network createOffReactiveTarget(Network network) {
        return addOffReactiveTarget(network);
    }

    private static Network addOffReactiveTarget(Network network) {
        addReactiveTarget(addNotRegulating(network));
        VoltageRegulation voltageRegulation = network.getStaticVarCompensator("SVC2").getVoltageRegulation();
        voltageRegulation.setMode(RegulationMode.REACTIVE_POWER);
        voltageRegulation.setRegulating(true);
        return network;
    }

    public static Network createLocalOffVoltageTarget() {
        return createOffVoltageTarget(create());
    }

    public static Network createRemoteOffVoltageTarget() {
        return createOffVoltageTarget(createWithRemoteRegulatingTerminal());
    }

    private static Network createOffVoltageTarget(Network network) {
        return addOffVoltageTarget(network);
    }

    private static Network addOffVoltageTarget(Network network) {
        return addVoltageTarget(addNotRegulating(network));
    }

    public static Network createLocalOffBothTarget() {
        return createOffBothTarget(create());
    }

    public static Network createRemoteOffBothTarget() {
        return createOffBothTarget(createWithRemoteRegulatingTerminal());
    }

    private static Network createOffBothTarget(Network network) {
        return addOffBothTarget(network);
    }

    private static Network addOffBothTarget(Network network) {
        return addBothTarget(addNotRegulating(network));
    }

    public static Network createLocalOffNoTarget() {
        return addNotRegulating(create());
    }

    public static Network createRemoteOffNoTarget() {
        return addNotRegulating(createWithRemoteRegulatingTerminal());
    }

    private static Network addNotRegulating(Network network) {
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        VoltageRegulation voltageRegulation = svc2.getVoltageRegulation();
        voltageRegulation.setRegulating(false);
        voltageRegulation.setMode(RegulationMode.VOLTAGE);
        voltageRegulation.setTargetValue(Double.NaN);
        svc2.setTargetV(Double.NaN);
        svc2.setTargetQ(Double.NaN);
        return network;
    }
}
