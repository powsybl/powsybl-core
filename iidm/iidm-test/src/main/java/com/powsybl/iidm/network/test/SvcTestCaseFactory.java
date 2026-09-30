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

    public static final double REMOTE_TARGET_VALUE = 400;
    public static final double LOCAL_TARGET_V = 390;
    public static final double LOCAL_TARGET_Q = 350;

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
                .newVoltageRegulation().withMode(RegulationMode.VOLTAGE).add()
                .setTargetP(100.0)
                .setLocalTargetV(400.0)
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
                    .add()
                .setLocalTargetV(LOCAL_TARGET_V)
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
                    .add()
                .setLocalTargetV(LOCAL_TARGET_V)
                .setLocalTargetQ(LOCAL_TARGET_Q)
                .add();

        return network;
    }

    public static Network createWithRemoteRegulatingTerminal() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .build();
        svc2.setLocalTargetV(Double.NaN);
        svc2.setLocalTargetQ(Double.NaN);
        return network;
    }

    public static Network createLocalVoltageControl() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setLocalTargetV(LOCAL_TARGET_V);
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .build();
        svc2.setLocalTargetQ(Double.NaN);
        return network;
    }

    public static Network createRemoteVoltageControl() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(LOCAL_TARGET_V)
            .build();
        svc2.setLocalTargetQ(Double.NaN);
        svc2.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createLocalReactiveControl() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(true)
            .build();
        svc2.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createRemoteReactiveControl() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(true)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .build();
        svc2.setLocalTargetQ(Double.NaN);
        svc2.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createLocalOffReactiveTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(false)
            .build();
        svc2.setLocalTargetV(Double.NaN);
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        return network;
    }

    public static Network createRemoteOffReactiveTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .withRegulating(false)
            .build();
        svc2.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createLocalOffVoltageTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withRegulating(false)
            .withMode(RegulationMode.VOLTAGE)
            .build();
        svc2.setLocalTargetV(LOCAL_TARGET_V);
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        return network;
    }

    public static Network createRemoteOffVoltageTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withRegulating(false)
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .build();
        return network;
    }

    public static Network createLocalOffBothTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setLocalTargetV(LOCAL_TARGET_V);
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();
        return network;
    }

    public static Network createRemoteOffBothTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.setLocalTargetV(LOCAL_TARGET_V);
        svc2.setLocalTargetQ(LOCAL_TARGET_Q);
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .withRegulating(false)
            .build();
        return network;
    }

    public static Network createLocalOffNoTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();
        svc2.setLocalTargetQ(Double.NaN);
        svc2.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createRemoteOffNoTarget() {
        Network network = create();
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC2");
        svc2.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(getRemoteTerminal(network))
            .withRegulating(false)
            .build();
        return network;
    }

    private static Terminal getRemoteTerminal(Network network) {
        return network.getLoad("L2").getTerminal();
    }

}
