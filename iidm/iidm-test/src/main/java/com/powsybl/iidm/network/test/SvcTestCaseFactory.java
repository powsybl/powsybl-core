/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
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
                .setVoltageRegulatorOn(true)
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
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
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
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(390)
                .setReactivePowerSetpoint(350)
                .add();

        return network;
    }

    public static Network createWithRemoteRegulatingTerminal() {
        return createWithRemoteRegulatingTerminal(NetworkFactory.findDefault());
    }

    public static Network createWithRemoteRegulatingTerminal(NetworkFactory networkFactory) {
        Network network = create(networkFactory);

        network.getStaticVarCompensator("SVC2")
                .setRegulatingTerminal(network.getLoad("L2").getTerminal());

        return network;
    }

    private static Network addReactiveTarget(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setReactivePowerSetpoint(350);
        return network;
    }

    private static Network addVoltageTarget(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setVoltageSetpoint(390);
        return network;
    }

    private static Network addBothTarget(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setReactivePowerSetpoint(350)
                .setVoltageSetpoint(390);
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
        network.getStaticVarCompensator("SVC2")
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE);
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
        network.getStaticVarCompensator("SVC2")
                .setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
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
        return addReactiveTarget(addOffNoTarget(network));
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
        return addVoltageTarget(addOffNoTarget(network));
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
        return addBothTarget(addOffNoTarget(network));
    }

    public static Network createLocalOffNoTarget() {
        return addOffNoTarget(create());
    }

    public static Network createRemoteOffNoTarget() {
        return addOffNoTarget(createWithRemoteRegulatingTerminal());
    }

    private static Network addOffNoTarget(Network network) {
        network.getStaticVarCompensator("SVC2")
                .setRegulationMode(StaticVarCompensator.RegulationMode.OFF)
                .setVoltageSetpoint(Double.NaN)
                .setReactivePowerSetpoint(Double.NaN);
        return network;
    }
}
