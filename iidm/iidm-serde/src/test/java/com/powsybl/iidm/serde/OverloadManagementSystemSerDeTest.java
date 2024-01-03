/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OverloadManagementSystemSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        Network network = createNetwork();

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("overloadManagementSystemRoundTripRef.xml");
        allFormatsRoundTripTest(network, "overloadManagementSystemRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    private Network createNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("fictitious", "test");
        network.setCaseDate(ZonedDateTime.parse("2024-01-02T15:00:00.000+01:00"));
        network.setForecastDistance(0);

        // Create a substation "S1", with 3 voltage levels "S1_400", "S1_220" and "S1_90"
        Substation s1 = network.newSubstation().setId("S1").add();
        VoltageLevel s1v400 = createVoltageLevel(s1, 400);
        VoltageLevel s1v225 = createVoltageLevel(s1, 225);
        VoltageLevel s1v90 = createVoltageLevel(s1, 90);

        // Create a substation "S2", with 1 voltage level "S2_400"
        Substation s2 = network.newSubstation().setId("S2").add();
        VoltageLevel s2v400 = createVoltageLevel(s2, 400);

        // Create 2 lines between "S1_400" and "S2_400" ("LINE_1" and "LINE_2")
        createLine(network, s1v400, s2v400, 1);
        createLine(network, s1v400, s2v400, 2);

        // Create a 2-windings transformer between "S1_400" and "S1_225" ("2WT")
        createTwoWindingsTransformer(s1, s1v400, s1v225);

        // Create a 3-windings transformer between "S1_400", "S1_225" and "S1_90" ("3WT")
        createThreeWindingsTransformer(s1, s1v400, s1v225, s1v90);

        // Create an overload management system with trippings on "2WT", "3WT" and "S1_400_LINE_2_BREAKER"
        s1.newOverloadManagementSystem()
                .setId("OMS1")
                .setName("1st OMS")
                .setEnabled(true)
                .setMonitoredElementId("2WT")
                .setMonitoredElementSide(ThreeSides.TWO)
                .newBranchTripping()
                    .setKey("tripping1")
                    .setName("1st tripping name")
                    .setCurrentLimit(1200)
                    .setOpenAction(true)
                    .setBranchToOperateId("2WT")
                    .setSideToOperate(TwoSides.ONE)
                    .add()
                .newThreeWindingsTransformerTripping()
                    .setKey("tripping2")
                    .setName("2nd tripping name")
                    .setCurrentLimit(1000)
                    .setOpenAction(false)
                    .setThreeWindingsTransformerToOperateId("3WT")
                    .setSideToOperate(ThreeSides.ONE)
                    .add()
                .newSwitchTripping()
                    .setKey("tripping3")
                    .setName("3rd tripping name")
                    .setCurrentLimit(1000)
                    .setOpenAction(true)
                    .setSwitchToOperateId("S1_400_LINE_2_BREAKER")
                    .add()
                .add();

        // Create an overload management system monitoring "LINE_1" with a tripping on "LINE_2".
        // Note that this test is very important since the OMS uses identifiers of elements which are not
        // defined in the same substation, and furthermore which will be serialized AFTER the OMS (lines are serialized
        // after the substations). This means that the referenced elements won't be already in the network in creation
        // when the OMS will be read.
        s1.newOverloadManagementSystem()
                .setId("OMS2")
                .setName("2nd OMS")
                .setEnabled(true)
                .setMonitoredElementId("LINE_1")
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("tripping")
                    .setName("tripping name")
                    .setCurrentLimit(1300)
                    .setOpenAction(true)
                    .setBranchToOperateId("LINE_2")
                    .setSideToOperate(TwoSides.ONE)
                    .add()
                .add();
        return network;
    }

    private VoltageLevel createVoltageLevel(Substation substation, int nominalV) {
        String vlId = String.format("%s_%d", substation.getId(), nominalV);
        VoltageLevel vl = substation.newVoltageLevel()
                .setId(vlId)
                .setNominalV(nominalV)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId(vlId + "_BBS")
                .setNode(0)
                .add();
        return vl;
    }

    private void createLine(Network network, VoltageLevel s1v400, VoltageLevel s2v400, int nb) {
        createSwitch(s1v400, "S1_400_LINE_" + nb + "_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, nb);
        createSwitch(s1v400, "S1_400_LINE_" + nb + "_BREAKER", SwitchKind.BREAKER, nb, 10 + nb);
        createSwitch(s2v400, "S2_400_LINE_" + nb + "_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, nb);
        createSwitch(s2v400, "S2_400_LINE_" + nb + "_BREAKER", SwitchKind.BREAKER, nb, 10 + nb);
        network.newLine()
                .setId("LINE_" + nb)
                .setR(0.01)
                .setX(50)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setNode1(10 + nb)
                .setVoltageLevel1("S1_400")
                .setNode2(10 + nb)
                .setVoltageLevel2("S2_400")
                .add();
    }

    private void createTwoWindingsTransformer(Substation s1, VoltageLevel s1v400, VoltageLevel s1v225) {
        createSwitch(s1v400, "S1_400_BBS_2WT_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 13);
        createSwitch(s1v400, "S1_400_2WT_BREAKER", SwitchKind.BREAKER, 13, 23);
        createSwitch(s1v225, "S1_225_BBS_2WT_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 13);
        createSwitch(s1v225, "S1_225_2WT_BREAKER", SwitchKind.BREAKER, 13, 23);
        s1.newTwoWindingsTransformer()
                .setId("2WT")
                .setR(2.0)
                .setX(25)
                .setG(0.0)
                .setB(3.2E-5)
                .setRatedU1(400.0)
                .setRatedU2(225.0)
                .setNode1(23)
                .setVoltageLevel1("S1_400")
                .setNode2(23)
                .setVoltageLevel2("S1_225")
                .add();
    }

    private void createThreeWindingsTransformer(Substation s1, VoltageLevel s1v400, VoltageLevel s1v225, VoltageLevel s1v90) {
        createSwitch(s1v400, "S1_400_BBS_3WT_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 14);
        createSwitch(s1v400, "S1_400_3WT_BREAKER", SwitchKind.BREAKER, 14, 24);
        createSwitch(s1v225, "S1_225_BBS_3WT_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 14);
        createSwitch(s1v225, "S1_225_3WT_BREAKER", SwitchKind.BREAKER, 14, 24);
        createSwitch(s1v90, "S1_90_BBS_3WT_DISCONNECTOR", SwitchKind.DISCONNECTOR, 0, 14);
        createSwitch(s1v90, "S1_90_3WT_BREAKER", SwitchKind.BREAKER, 14, 24);
        s1.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(400)
                .newLeg1()
                    .setR(0.001).setX(0.000001).setB(0).setG(0)
                    .setNode(24)
                    .setRatedU(400)
                    .setVoltageLevel("S1_400")
                    .add()
                .newLeg2()
                    .setR(0.1).setX(0.00001).setB(0).setG(0)
                    .setNode(24)
                    .setRatedU(225)
                    .setVoltageLevel("S1_225")
                    .add()
                .newLeg3()
                    .setR(0.01).setX(0.0001).setB(0).setG(0)
                    .setNode(24)
                    .setRatedU(90)
                    .setVoltageLevel("S1_90")
                    .add()
               .add();
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setKind(kind)
                .setOpen(true)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}
