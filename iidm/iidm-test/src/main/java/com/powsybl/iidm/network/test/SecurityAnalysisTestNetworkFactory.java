/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public final class SecurityAnalysisTestNetworkFactory {

    private static final String S1VL1 = "S1VL1";
    private static final String S1VL2 = "S1VL2";
    private static final String S2VL1 = "S2VL1";
    private static final String S2VL2 = "S2VL2";
    private static final String LINE_S1S2V1_1 = "LINE_S1S2V1_1";
    private static final String LINE_S1S2V1_2 = "LINE_S1S2V1_2";
    private static final String LINE_S1S2V2 = "LINE_S1S2V2";
    private static final String TWT = "TWT";
    private static final String TWT2 = "TWT2";

    private SecurityAnalysisTestNetworkFactory() {

    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("fictitious", "test");
        network.setCaseDate(ZonedDateTime.parse("2022-12-18T16:00:00.000+01:00"));
        network.setForecastDistance(0);

        // first substation
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId(S1VL1)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("S1VL1_BBS")
                .setName("S1VL1_BBS")
                .setNode(0)
                .add();

        VoltageLevel s1vl2 = s1.newVoltageLevel()
                .setId(S1VL2)
                .setNominalV(225)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        s1vl2.getNodeBreakerView().newBusbarSection()
                .setId("S1VL2_BBS1")
                .setName("S1VL2_BBS1")
                .setNode(0)
                .add();

        // second substation
        Substation s2 = network.newSubstation()
                .setId("S2")
                .add();

        VoltageLevel s2vl1 = s2.newVoltageLevel()
                .setId(S2VL1)
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        s2vl1.getNodeBreakerView().newBusbarSection()
                .setId("S2VL1_BBS1")
                .setName("S2VL1_BBS1")
                .setNode(0)
                .add();
        VoltageLevel s2vl2 = s2.newVoltageLevel()
                .setId(S2VL2)
                .setNominalV(225)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        s2vl2.getNodeBreakerView().newBusbarSection()
                .setId("S2VL2_BBS1")
                .setName("S2VL2_BBS1")
                .setNode(0)
                .add();

        // generator
        createSwitch(s1vl1, "S1VL1_BBS1_GEN_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl1, "S1VL1_BBS1_GEN_BREAKER", SwitchKind.BREAKER, false, 1, 2);
        s1vl1.newGenerator()
                .setId("GEN")
                .setEnergySource(EnergySource.OTHER)
                .setMinP(0)
                .setMaxP(150)
                .setVoltageRegulatorOn(true)
                .setTargetV(400)
                .setTargetP(100.0)
                .setNode(2)
                .add();

        // Loads
        createSwitch(s1vl2, "S1VL2_BBS1_LD1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s1vl2, "S1VL2_BBS1_LD1_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        createSwitch(s2vl2, "S2VL2_BBS1_LD2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 3);
        createSwitch(s2vl2, "S2VL2_BBS1_LD2_BREAKER", SwitchKind.BREAKER, false, 3, 4);
        s1vl2.newLoad()
                .setId("LD1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(50)
                .setQ0(4)
                .setNode(4)
                .add();
        s2vl2.newLoad()
                .setId("LD2")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(50)
                .setQ0(4)
                .setNode(4)
                .add();

        // lines
        createSwitch(s1vl1, "S1VL2_BBS_LINES1S2V1_1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 16);
        createSwitch(s1vl1, "S1VL2_LINES1S2V1_1_BREAKER", SwitchKind.BREAKER, false, 16, 17);
        createSwitch(s2vl1, "S2VL2_BBS_LINES1S2V1_1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 16);
        createSwitch(s2vl1, "S2VL2_LINES1S2V1_1_BREAKER", SwitchKind.BREAKER, false, 16, 17);
        network.newLine()
                .setId(LINE_S1S2V1_1)
                .setR(0.01)
                .setX(50)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(17)
                .setVoltageLevel1(S1VL1)
                .setNode2(17)
                .setVoltageLevel2(S2VL1)
                .add();
        createSwitch(s1vl1, "S1VL2_BBS_LINES1S2V1_2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 14);
        createSwitch(s1vl1, "S1VL2_LINES1S2V1_2_BREAKER", SwitchKind.BREAKER, false, 14, 15);
        createSwitch(s2vl1, "S2VL2_BBS_LINES1S2V1_2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 14);
        createSwitch(s2vl1, "S2VL2_LINES1S2V1_2_BREAKER", SwitchKind.BREAKER, false, 14, 15);
        network.newLine()
                .setId(LINE_S1S2V1_2)
                .setR(0.01)
                .setX(50)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(15)
                .setVoltageLevel1(S1VL1)
                .setNode2(15)
                .setVoltageLevel2(S2VL1)
                .add();
        createSwitch(s1vl2, "S1VL2_BBS_LINES1S2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 12);
        createSwitch(s1vl2, "S1VL2_LINES1S2_BREAKER", SwitchKind.BREAKER, false, 12, 13);
        createSwitch(s2vl2, "S2VL2_BBS_LINES1S2_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 12);
        createSwitch(s2vl2, "S2VL2_LINES1S2_BREAKER", SwitchKind.BREAKER, false, 12, 13);
        network.newLine()
                .setId("LINE_S1S2V2")
                .setR(0.01)
                .setX(50)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(13)
                .setVoltageLevel1(S1VL2)
                .setNode2(13)
                .setVoltageLevel2(S2VL2)
                .add();

        // transformers
        createSwitch(s1vl1, "S1VL1_BBS_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(s1vl1, "S1VL1_TWT_BREAKER", SwitchKind.BREAKER, false, 10, 11);
        createSwitch(s1vl2, "S1VL2_BBS_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(s1vl2, "S1VL2_TWT_BREAKER", SwitchKind.BREAKER, false, 10, 11);
        s1.newTwoWindingsTransformer()
                .setId(TWT)
                .setR(2.0)
                .setX(25)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(400.0)
                .setRatedU2(225.0)
                .setNode1(11)
                .setVoltageLevel1(S1VL1)
                .setNode2(11)
                .setVoltageLevel2(S1VL2)
                .add();
        createSwitch(s2vl1, "S2VL1_BBS_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(s2vl1, "S2VL1_TWT_BREAKER", SwitchKind.BREAKER, false, 10, 11);
        createSwitch(s2vl2, "S2VL2_BBS_TWT_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 10);
        createSwitch(s2vl2, "S2VL2_TWT_BREAKER", SwitchKind.BREAKER, false, 10, 11);
        s2.newTwoWindingsTransformer()
                .setId(TWT2)
                .setR(2.0)
                .setX(50)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(400.0)
                .setRatedU2(225.0)
                .setNode1(11)
                .setVoltageLevel1(S2VL1)
                .setNode2(11)
                .setVoltageLevel2(S2VL2)
                .add();
        return network;
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    public static Network createWithFixedCurrentLimits() {
        return createWithFixedCurrentLimits(NetworkFactory.findDefault());
    }

    public static Network createWithFixedCurrentLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);
        network.getLine(LINE_S1S2V1_1).getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits()
                .setPermanentLimit(75)
                .add();
        network.getLine(LINE_S1S2V1_1).getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(75)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(80)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(85)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("Undefined")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        network.getLine(LINE_S1S2V1_2).getOrCreateSelectedOperationalLimitsGroup2().newCurrentLimits().setPermanentLimit(75).add();
        network.getLine(LINE_S1S2V1_2).getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(75)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(80)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(85)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("Undefined")
                .setAcceptableDuration(0)
                .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        network.getLine(LINE_S1S2V2).getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits()
                .setPermanentLimit(60)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(80)
                .endTemporaryLimit()
                .add();
        network.getTwoWindingsTransformer(TWT2).getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits().setPermanentLimit(90)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(100)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(110)
                .endTemporaryLimit()
                .add();
        network.getTwoWindingsTransformer(TWT).getOrCreateSelectedOperationalLimitsGroup1().newCurrentLimits().setPermanentLimit(92)
                .beginTemporaryLimit()
                .setName("10'")
                .setAcceptableDuration(10 * 60)
                .setValue(100)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(110)
                .endTemporaryLimit()
                .add();
        return network;
    }

    public static Network createWithFixedPowerLimits() {
        return createWithFixedPowerLimits(NetworkFactory.findDefault());
    }

    public static Network createWithFixedPowerLimits(NetworkFactory networkFactory) {
        Network network = create(networkFactory);
        network.getTwoWindingsTransformer(TWT).newActivePowerLimits1().setPermanentLimit(71).add();
        network.getTwoWindingsTransformer(TWT2).newActivePowerLimits1().setPermanentLimit(55).add();
        network.getLine(LINE_S1S2V1_1).newActivePowerLimits1().setPermanentLimit(55).add();
        network.getLine(LINE_S1S2V1_2).newActivePowerLimits1().setPermanentLimit(55).add();
        network.getLine(LINE_S1S2V2).newActivePowerLimits1().setPermanentLimit(30).add();
        return network;
    }
}
