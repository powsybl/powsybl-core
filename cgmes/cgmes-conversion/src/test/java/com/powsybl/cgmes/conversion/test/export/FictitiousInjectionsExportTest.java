/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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

        String loadId = "VL_VL_FICT_NCL_0";
        String terminalId = "VL_VL_FICT_T_0";

        // In node/breaker, a connectivity node is exported for the load in EQ
        validateCgmesExportOfFictitiousInjections(network, params, loadId, terminalId, true, 1.1, 2.2);
    }

    @Test
    void busBranchExport() throws IOException {
        Network network = createBbNetworkWithFictitiousOnBus();
        Properties params = new Properties();

        String loadId = "BUS_TN_FICT_NCL";
        String terminalId = "BUS_TN_FICT_T";

        validateCgmesExportOfFictitiousInjections(network, params, loadId, terminalId, false, 3.3, 4.4);
    }

    @Test
    void busBranchExportCim100() throws IOException {
        Network network = createBbNetworkWithFictitiousOnBus();
        Properties params = new Properties();
        params.put(CgmesExport.CIM_VERSION, "100");

        String loadId = "BUS_TN_FICT_NCL";
        String terminalId = "BUS_TN_FICT_T";

        validateCgmesExportOfFictitiousInjections(network, params, loadId, terminalId, true, 3.3, 4.4);
    }

    @Test
    void busBranchExportFromNodeBreakerNetwork() throws IOException {
        Network network = NetworkFactory.findDefault().createNetwork("TestNetworkNodeBreaker", "test");
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(225.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        vl.getNodeBreakerView().newBusbarSection().setId("BBS").setNode(0).add();
        vl.getNodeBreakerView().newBusbarSection().setId("BBS2").setNode(1).add();
        vl.getNodeBreakerView().newSwitch().setId("SW").setKind(SwitchKind.BREAKER).setNode1(0).setNode2(1).setRetained(false).setOpen(false).add();
        vl.getNodeBreakerView().setFictitiousP0(0, 1.0).setFictitiousQ0(0, 2.0);
        vl.getNodeBreakerView().setFictitiousP0(1, 1.0).setFictitiousQ0(1, 2.0);

        Properties params = new Properties();
        params.put(CgmesExport.TOPOLOGY_KIND, "BUS_BRANCH");

        String loadId = "VL_0_TN_FICT_NCL";
        String terminalId = "VL_0_TN_FICT_T";
        validateCgmesExportOfFictitiousInjections(network, params, loadId, terminalId, false, 2.0, 4.0);
    }

    private void validateCgmesExportOfFictitiousInjections(Network network, Properties params, String loadId, String terminalId, boolean connectivityNodeWritten, double expectedFictitiousP, double expectedFictitiousQ) throws IOException {
        validateEqExport(network, params, loadId, terminalId, connectivityNodeWritten);
        validateTpExport(network, params, terminalId);
        validateSshExport(network, params, loadId, terminalId, expectedFictitiousP, expectedFictitiousQ);
        valdiateSvExport(network, params, terminalId);
    }

    private void validateEqExport(Network network, Properties params, String loadId, String terminalId, boolean connectivityNodeWritten) throws IOException {
        String eqXml = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir, params);

        // In EQ, a non-conform load is exported with its terminal. If the network is a node breaker or the export is CIM100, a connectivity node is exported for the load
        String terminalEq = getElement(eqXml, "Terminal", terminalId);
        assertNotNull(terminalEq);
        checkWrittenConnectivityNode(connectivityNodeWritten, terminalEq);
        assertEquals(loadId, getResource(terminalEq, "Terminal.ConductingEquipment"));
    }

    private static void checkWrittenConnectivityNode(boolean connectivityNodeWritten, String terminalEq) {
        if (connectivityNodeWritten) {
            assertTrue(terminalEq.contains("<cim:Terminal.ConnectivityNode"));
        } else {
            assertFalse(terminalEq.contains("<cim:Terminal.ConnectivityNode"));
        }
    }

    private void validateTpExport(Network network, Properties params, String terminalId) throws IOException {
        String tpXml = ConversionUtil.writeCgmesProfile(network, "TP", tmpDir, params);

        // In TP, the terminal of the fictitious injection references a topologicalNode
        String terminalTp = getElement(tpXml, "Terminal", terminalId);
        assertNotNull(terminalTp);
        assertTrue(terminalTp.contains("<cim:Terminal.TopologicalNode"));
    }

    private void validateSshExport(Network network, Properties params, String loadId, String terminalId, double expectedFictitiousP, double expectedFictitiousQ) throws IOException {
        String sshXml = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir, params);

        // In SSH, the p/q of the fictitious injection are exported and the terminal status should be connected
        String sshLoad = getElement(sshXml, "NonConformLoad", loadId);
        assertNotNull(sshLoad);
        assertEquals(expectedFictitiousP, Double.parseDouble(getAttribute(sshLoad, "EnergyConsumer.p")));
        assertEquals(expectedFictitiousQ, Double.parseDouble(getAttribute(sshLoad, "EnergyConsumer.q")));
        String sshTerminal = getElement(sshXml, "Terminal", terminalId);
        assertNotNull(sshTerminal);
        assertEquals("true", getAttribute(sshTerminal, "ACDCTerminal.connected"));
    }

    private void valdiateSvExport(Network network, Properties params, String terminalId) throws IOException {
        String svXml = ConversionUtil.writeCgmesProfile(network, "SV", tmpDir, params);

        // In SV, an SvPowerFlow is exported for the terminal of the fictitious load
        assertTrue(svXml.contains(terminalId));
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
