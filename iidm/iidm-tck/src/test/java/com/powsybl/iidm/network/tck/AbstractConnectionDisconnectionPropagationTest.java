/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractConnectionDisconnectionPropagationTest {

    private Network createBaseNetwork() {
        Network network = Network.create("test", "test");
        // Substations
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        Substation s2 = network.newSubstation()
            .setId("S2")
            .setCountry(Country.FR)
            .add();
        Substation s3 = network.newSubstation()
            .setId("S3")
            .setCountry(Country.FR)
            .add();

        // Voltage levels
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
            .setId("VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl3 = s3.newVoltageLevel()
            .setId("VL3")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs1 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        bbs1.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs2 = vl2.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS2")
            .setNode(0)
            .add();
        bbs2.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs3 = vl3.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS3")
            .setNode(0)
            .add();
        bbs3.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // VL1 - Breakers and disconnectors
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L1_VL1")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setFictitious(false)
            .add();

        // VL2 - Breakers and disconnectors
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS2")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl2.getNodeBreakerView().newBreaker()
            .setId("B_L1_VL2")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setFictitious(false)
            .add();

        return network;
    }

    private Network createNetworkWithTeePoint() {
        // Base network
        Network network = createBaseNetwork();

        // Fictitious voltage level
        VoltageLevel fictitiousVl = network.newVoltageLevel()
            .setId("L1_VL")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .setFictitious(true)
            .add();

        // Fictitious topology
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(0)
            .setNode2(1)
            .add();
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(1)
            .setNode2(2)
            .add();
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(1)
            .setNode2(3)
            .add();

        // VL3 - Breakers and disconnectors
        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_BREAKER")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(false)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_DISCONNECTOR_2_0")
            .setNode1(2)
            .setNode2(0)
            .setOpen(false)
            .add();

        // Lines
        network.newLine()
            .setId("L1_1")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("L1_VL")
            .setNode1(2)
            .setNode2(0)
            .add();
        network.newLine()
            .setId("L1_2")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("L1_VL")
            .setVoltageLevel2("VL2")
            .setNode1(2)
            .setNode2(2)
            .add();
        network.newLine()
            .setId("testLine")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("L1_VL")
            .setVoltageLevel2("VL3")
            .setNode1(3)
            .setNode2(1)
            .add();

        return network;
    }

    private Network createNetworkWithFictitiousVoltageLevel() {
        Network network = createBaseNetwork();

        // VL3 - Breakers and disconnectors
        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_1_BREAKER")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(false)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_1_DISCONNECTOR_2_0")
            .setNode1(2)
            .setNode2(0)
            .setOpen(false)
            .add();
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_2_BREAKER")
            .setNode1(4)
            .setNode2(3)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(false)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_2_DISCONNECTOR_3_0")
            .setNode1(3)
            .setNode2(0)
            .setOpen(false)
            .add();

        // Lines
        network.newLine()
            .setId("L1_1")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL3")
            .setNode1(2)
            .setNode2(1)
            .add();
        network.newLine()
            .setId("L1_2")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL3")
            .setVoltageLevel2("VL2")
            .setNode1(4)
            .setNode2(2)
            .add();

        return network;
    }

    @Test
    void propagationOnTeePointTest() {
        Network network = createNetworkWithTeePoint();
    }

    @Test
    void propagationThroughVoltageLevelTest() {
        Network network = createNetworkWithFictitiousVoltageLevel();
    }
}
