/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.getAttribute;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.getElement;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.getResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class FictitiousInjectionsExportTest extends AbstractSerDeTest {

    @Test
    void nodeBreakerExport() throws IOException {
        Network network = NetworkFactory.findDefault().createNetwork("TestNetworkNodeBreaker", "test");
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl.getNodeBreakerView().newBusbarSection().setId("BBS").setNode(0).add();

        // Set fictitious injection on node 0
        vl.getNodeBreakerView().setFictitiousP0(0, 1.1).setFictitiousQ0(0, 2.2);

        Properties params = new Properties();

        String eqXml = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir, params);
        String sshXml = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir, params);
        String tpXml = ConversionUtil.writeCgmesProfile(network, "TP", tmpDir, params);
        String svXml = ConversionUtil.writeCgmesProfile(network, "SV", tmpDir, params);

        String loadId = "VL_VL_FICT_NCL_0";
        String terminalId = "VL_VL_FICT_T_0";

        // EQ: NonConformLoad exists and Terminal references ConnectivityNode
        assertNotNull(getElement(eqXml, "NonConformLoad", loadId));
        String terminalEq = getElement(eqXml, "Terminal", terminalId);
        assertNotNull(terminalEq);
        assertTrue(terminalEq.contains("<cim:Terminal.ConnectivityNode "));
        assertEquals(loadId, getResource(terminalEq, "Terminal.ConductingEquipment"));

        // TP: terminal of the fictitious load references a TopologicalNode
        String tpTerminal = getElement(tpXml, "Terminal", terminalId);
        assertTrue(tpTerminal.contains("<cim:Terminal.TopologicalNode"));

        // SSH: values and terminal connected
        String sshLoad = getElement(sshXml, "NonConformLoad", loadId);
        assertNotNull(sshLoad);
        assertEquals("1.1", getAttribute(sshLoad, "EnergyConsumer.p"));
        assertEquals("2.2", getAttribute(sshLoad, "EnergyConsumer.q"));
        String sshTerminal = getElement(sshXml, "Terminal", terminalId);
        assertNotNull(sshTerminal);
        assertEquals("true", getAttribute(sshTerminal, "ACDCTerminal.connected"));

        // SV: SvPowerFlow is exported for the terminal of the fictitious load
        assertTrue(svXml.contains(terminalId));
    }

    @Test
    void busBranchExport() throws IOException {
        Network network = createBbNetworkWithFictitiousOnBus();
        Properties params = new Properties();

        String eqXml = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir, params);
        String tpXml = ConversionUtil.writeCgmesProfile(network, "TP", tmpDir, params);
        String sshXml = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir, params);
        String svXml = ConversionUtil.writeCgmesProfile(network, "SV", tmpDir, params);

        String loadId = "BUS_TN_FICT_NCL";
        String terminalId = "BUS_TN_FICT_T";

        // EQ: terminal must NOT have a ConnectivityNode
        String terminalEq = getElement(eqXml, "Terminal", terminalId);
        assertNotNull(terminalEq);
        assertFalse(terminalEq.contains("<cim:Terminal.ConnectivityNode"));
        assertEquals(loadId, getResource(terminalEq, "Terminal.ConductingEquipment"));

        // TP: terminal of the fictitious load references a TopologicalNode
        String terminalTp = getElement(tpXml, "Terminal", terminalId);
        assertTrue(terminalTp.contains("<cim:Terminal.TopologicalNode"));

        // SSH: values and terminal connected
        String sshLoad = getElement(sshXml, "NonConformLoad", loadId);
        assertNotNull(sshLoad);
        assertEquals("3.3", getAttribute(sshLoad, "EnergyConsumer.p"));
        assertEquals("4.4", getAttribute(sshLoad, "EnergyConsumer.q"));
        String sshTerminal = getElement(sshXml, "Terminal", terminalId);
        assertNotNull(sshTerminal);
        assertEquals("true", getAttribute(sshTerminal, "ACDCTerminal.connected"));

        // SV: SvPowerFlow is exported for the terminal of the fictitious load
        assertTrue(svXml.contains(terminalId));
    }

    @Test
    void busBranchExportCim100() throws IOException {
        Network n = createBbNetworkWithFictitiousOnBus();
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");

        String eqXml = ConversionUtil.writeCgmesProfile(n, "EQ", tmpDir, params);
        String tpXml = ConversionUtil.writeCgmesProfile(n, "TP", tmpDir, params);
        String sshXml = ConversionUtil.writeCgmesProfile(n, "SSH", tmpDir, params);

        String loadId = "BUS_TN_FICT_NCL";
        String termId = "BUS_TN_FICT_T";

        // EQ: terminal must have a ConnectivityNode
        String terminalEq = getElement(eqXml, "Terminal", termId);
        assertNotNull(terminalEq);
        assertTrue(terminalEq.contains("<cim:Terminal.ConnectivityNode"));
        assertEquals(loadId, getResource(terminalEq, "Terminal.ConductingEquipment"));

        // TP: terminal of the fictitious load references a TopologicalNode
        String terminalTp = getElement(tpXml, "Terminal", termId);
        assertNotNull(terminalTp);
        assertTrue(terminalTp.contains("<cim:Terminal.TopologicalNode"));

        // SSH: values and terminal connected
        String sshLoad = getElement(sshXml, "NonConformLoad", loadId);
        assertNotNull(sshLoad);
        assertEquals("3.3", getAttribute(sshLoad, "EnergyConsumer.p"));
        assertEquals("4.4", getAttribute(sshLoad, "EnergyConsumer.q"));
        String sshTerminal = getElement(sshXml, "Terminal", termId);
        assertNotNull(sshTerminal);
        assertEquals("true", getAttribute(sshTerminal, "ACDCTerminal.connected"));
    }

    private static Network createBbNetworkWithFictitiousOnBus() {
        Network network = NetworkFactory.findDefault().createNetwork("TestNetworkBusBreaker", "test");
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b = vl.getBusBreakerView().newBus().setId("BUS").add();
        // Set fictitious P0/Q0 on the bus
        b.setFictitiousP0(3.3).setFictitiousQ0(4.4);
        return network;
    }
}
