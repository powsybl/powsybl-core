/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class NodeMappingTest extends AbstractSerDeTest {

    @Test
    void nodeMappingTest() throws IOException {
        // Export test network to CGMES EQ
        Network network = networkWithMultipleConnectionKinds();
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        // IIDM nodes 0, 1, 2 are mapped to CGMES node VL_VL_0_CN
        assertNotNull(getElement(eqFile, "ConnectivityNode", "VL_VL_0_CN"));
        String nodeAssociation = "<cim:Terminal.ConnectivityNode rdf:resource=\"#_VL_VL_0_CN\"/>";
        assertTrue(getElement(eqFile, "Terminal", "BBS_BS_T_1").contains(nodeAssociation));
        assertTrue(getElement(eqFile, "Terminal", "LD1_EC_T_1").contains(nodeAssociation));
        assertTrue(getElement(eqFile, "Terminal", "LD2_EC_T_1").contains(nodeAssociation));
        assertTrue(getElement(eqFile, "Terminal", "DIS_SW_T_1").contains(nodeAssociation));

        // IIDM node 3 is mapped to CGMES node VL_VL_3_CN
        assertNotNull(getElement(eqFile, "ConnectivityNode", "VL_VL_3_CN"));
        nodeAssociation = "<cim:Terminal.ConnectivityNode rdf:resource=\"#_VL_VL_3_CN\"/>";
        assertTrue(getElement(eqFile, "Terminal", "LD3_EC_T_1").contains(nodeAssociation));
        assertTrue(getElement(eqFile, "Terminal", "DIS_SW_T_2").contains(nodeAssociation));

        // IIDM node 4 is mapped to CGMES node VL_VL_4_CN
        assertNotNull(getElement(eqFile, "ConnectivityNode", "VL_VL_4_CN"));
        nodeAssociation = "<cim:Terminal.ConnectivityNode rdf:resource=\"#_VL_VL_4_CN\"/>";
        assertTrue(getElement(eqFile, "Terminal", "GEN_SM_T_1").contains(nodeAssociation));
    }

    private Network networkWithMultipleConnectionKinds() {
        // This test network contains 4 nodes connection kinds:
        // - Connection through internal connection: 0-1
        // - Connection through not-exported fictitious switch: 0-2
        // - Connection through regular switch: 0-3
        // - No connection (isolated node): 4

        //             BBS
        //   --------- (0) ---------
        //     |        |        |
        //    (1)      FICT     DIS      (4)
        //    LD1       |        |       GEN
        //             (2)      (3)
        //             LD2      LD3

        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation = network.newSubstation()
                .setId("ST")
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VL")
                .setNominalV(100.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        voltageLevel.getNodeBreakerView().newInternalConnection()
                .setNode1(0)
                .setNode2(1)
                .add();
        voltageLevel.newLoad()
                .setId("LD1")
                .setNode(1)
                .setP0(1.0)
                .setQ0(0.0)
                .add();
        voltageLevel.getNodeBreakerView().newSwitch()
                .setId("FICT")
                .setNode1(0)
                .setNode2(2)
                .setKind(SwitchKind.BREAKER)
                .setOpen(true)
                .setRetained(false)
                .setFictitious(true)
                .add()
                .setProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL, "true");
        voltageLevel.newLoad()
                .setId("LD2")
                .setNode(2)
                .setP0(1.0)
                .setQ0(0.0)
                .add();
        voltageLevel.getNodeBreakerView().newSwitch()
                .setId("DIS")
                .setNode1(0)
                .setNode2(3)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(false)
                .setRetained(false)
                .add();
        voltageLevel.newLoad()
                .setId("LD3")
                .setNode(3)
                .setP0(1.0)
                .setQ0(0.0)
                .add();
        // Isolated generator
        voltageLevel.newGenerator()
                .setId("GEN")
                .setNode(4)
                .setTargetP(3.0)
                .setTargetQ(0.0)
                .setMinP(0.0)
                .setMaxP(3.0)
                .setVoltageRegulatorOn(false)
                .add();

        return network;
    }
}
