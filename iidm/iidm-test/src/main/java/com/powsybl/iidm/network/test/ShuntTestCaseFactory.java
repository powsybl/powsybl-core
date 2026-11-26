/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class ShuntTestCaseFactory {

    private static final String SHUNT = "SHUNT";
    public static final double LOCAL_TARGET_V = 210;
    public static final double REMOTE_TARGET_VALUE = 200;
    public static final double TARGET_DEADBAND = 5.0;

    private ShuntTestCaseFactory() {
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(double bPerSection) {
        return create(NetworkFactory.findDefault(), bPerSection);
    }

    public static Network create(NetworkFactory networkFactory) {
        return create(networkFactory, 1e-5);
    }

    public static Network create(NetworkFactory networkFactory, double bPerSection) {
        Network network = createBase(networkFactory);

        network.getVoltageLevel("VL1")
                .newShuntCompensator()
                .setId(SHUNT)
                .setBus("B1")
                .setConnectableBus("B1")
                .setSectionCount(1)
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .withTargetValue(REMOTE_TARGET_VALUE)
                    .withTargetDeadband(TARGET_DEADBAND)
                    .withTerminal(getRemoteTerminal(network))
                    .add()
                .newLinearModel()
                    .setMaximumSectionCount(1)
                    .setBPerSection(bPerSection)
                    .add()
                .add()
                .addAlias("Alias");

        return network;
    }

    public static Network createWithActivePower(NetworkFactory networkFactory) {
        Network network = create(networkFactory);
        ShuntCompensator s = network.getShuntCompensator(SHUNT);
        s.getTerminal().setP(1.0);
        return network;
    }

    public static Network createWithActivePower() {
        return createWithActivePower(NetworkFactory.findDefault());
    }

    public static Network createNonLinear() {
        return createNonLinear(NetworkFactory.findDefault());
    }

    public static Network createNonLinear(NetworkFactory networkFactory) {
        Network network = createBase(networkFactory);

        network.getVoltageLevel("VL1")
                .newShuntCompensator()
                    .setId(SHUNT)
                    .setBus("B1")
                    .setConnectableBus("B1")
                    .setSectionCount(1)
                    .newVoltageRegulation()
                        .withTargetValue(REMOTE_TARGET_VALUE)
                        .withMode(RegulationMode.VOLTAGE)
                        .withTerminal(getRemoteTerminal(network))
                        .withTargetDeadband(TARGET_DEADBAND)
                        .add()
                    .newNonLinearModel()
                        .beginSection()
                            .setB(1e-5)
                            .setG(0.0)
                        .endSection()
                        .beginSection()
                            .setB(2e-2)
                            .setG(3e-1)
                        .endSection()
                    .add()
                .add();

        return network;
    }

    private static Network createBase(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("shuntTestCase", "test")
                .setCaseDate(ZonedDateTime.parse("2019-09-30T16:29:18.263+02:00"));

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

        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(220)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();

        vl2.newLoad()
                .setId("LOAD")
                .setConnectableBus("B2")
                .setBus("B2")
                .setP0(100.0)
                .setQ0(50.0)
                .add();
        return network;
    }

    public static Network createLocalLinear() {
        return createLocalShunt(create());
    }

    public static Network createLocalShunt(Network network) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(SHUNT);
        shuntCompensator.setLocalTargetV(LOCAL_TARGET_V);
        shuntCompensator.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(TARGET_DEADBAND)
            .withRegulating(true)
            .build();
        return network;
    }

    public static Network createDisabledRemoteLinear() {
        return createDisabledRemote(create());
    }

    public static Network createDisabledLocalLinear() {
        return createDisabledLocal(createLocalLinear());
    }

    public static Network createDisabledRemoteNonLinear() {
        return createDisabledRemote(createNonLinear());
    }

    public static Network createDisabledLocalNonLinear() {
        return createDisabledLocal(createLocalNonLinear());
    }

    public static Network createDisabledRemote(Network network) {
        network.getShuntCompensator(SHUNT).newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(REMOTE_TARGET_VALUE)
            .withTargetDeadband(TARGET_DEADBAND)
            .withRegulating(false)
            .build();
        return network;
    }

    public static Network createDisabledLocal(Network network) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(SHUNT);
        shuntCompensator.setLocalTargetV(LOCAL_TARGET_V);
        shuntCompensator.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(TARGET_DEADBAND)
            .withRegulating(false)
            .build();
        return network;
    }

    private static Network createLocalOffWithNoTarget(Network network) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(SHUNT);
        shuntCompensator.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(false)
            .build();
        shuntCompensator.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createDisabledRemoteLinearNoTarget() {
        return createDisabledRemoteNoTarget(create());
    }

    public static Network createDisabledRemoteNoTarget(Network network) {
        ShuntCompensator shuntCompensator = network.getShuntCompensator(SHUNT);
        shuntCompensator.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(getRemoteTerminal(network))
            .withTargetValue(Double.NaN)
            .withTargetDeadband(TARGET_DEADBAND)
            .withRegulating(false)
            .build();
        shuntCompensator.setLocalTargetV(Double.NaN);
        return network;
    }

    public static Network createRemoteNonLinearNoTarget() {
        return createDisabledRemoteNoTarget(createNonLinear());
    }

    public static Network createDisabledLocalLinearNoTarget() {
        return createLocalOffWithNoTarget(createLocalLinear());
    }

    public static Network createLocalNonLinearNoTarget() {
        return createLocalOffWithNoTarget(createLocalNonLinear());
    }

    public static Network createLocalNonLinear() {
        return createLocalShunt(createNonLinear());
    }

    private static Terminal getRemoteTerminal(Network network) {
        return network.getLoad("LOAD").getTerminal();
    }

}
