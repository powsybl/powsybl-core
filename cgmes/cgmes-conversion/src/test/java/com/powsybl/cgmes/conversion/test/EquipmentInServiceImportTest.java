/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Import of the CGMES 3 attribute {@code Equipment.inService}: out-of-service equipment is taken out of service by
 * disconnecting its terminals.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
class EquipmentInServiceImportTest {

    private static final String LINE_DIR = "/update/line/";
    private static final String LOAD_DIR = "/update/load/";
    private static final String GENERATOR_DIR = "/update/generator/";
    private static final String SWITCH_DIR = "/update/switch/";
    private static final String BUSBAR_SECTION_DIR = "/update/busbar-section/";
    private static final String HVDC_DIR = "/update/hvdc/";

    @Test
    @DisplayName("Out-of-service connectables and machines of an out-of-service generating unit are disconnected")
    void outOfServiceConnectablesTest() {
        // Node-breaker: a fictitious open switch is inserted on every terminal of the out-of-service equipment
        Network network = readCgmesResources(LINE_DIR, "line_EQ.xml", "line_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getLine("ACLineSegment").getTerminal1(), "ACLineSegment-T1");
        assertDisconnectedByFictitiousSwitch(network, network.getLine("ACLineSegment").getTerminal2(), "ACLineSegment-T2");
        assertTrue(network.getLine("EquivalentBranch").getTerminal1().isConnected());
        assertNull(network.getSwitch("EquivalentBranch-T1_SW_fict"));

        network = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getLoad("EnergyConsumer").getTerminal(), "EnergyConsumer_T");
        assertNull(network.getSwitch("EnergySource_T_SW_fict"));

        // The machines of an out-of-service generating unit are disconnected, even when they are in service themselves
        network = readCgmesResources(GENERATOR_DIR, "generator_EQ.xml", "generator_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getGenerator("SynchronousMachine").getTerminal(), "SynchronousMachine_T");
        assertDisconnectedByFictitiousSwitch(network, network.getGenerator("EquivalentInjection").getTerminal(), "EquivalentInjection_T");
        assertNull(network.getSwitch("ExternalNetworkInjection_T_SW_fict"));

        // Bus-breaker: the terminals are disconnected, no fictitious switch is created.
        // In the ENTSO-E MiniGrid, two lines to the boundary and a load are out of service.
        Properties busBreaker = new Properties();
        busBreaker.put(CgmesImport.IMPORT_NODE_BREAKER_AS_BUS_BREAKER, "true");
        network = Network.read(Cgmes3Catalog.miniGrid().dataSource(), busBreaker);
        assertFalse(network.getBoundaryLine("5150a037-e241-421f-98b2-fe60e5c90303").getTerminal().isConnected());
        assertFalse(network.getBoundaryLine("f32baf36-7ea3-4b6a-9452-71e7f18779f8").getTerminal().isConnected());
        assertFalse(network.getLoad("d711629e-7b35-4b01-867a-f8fd41362a88").getTerminal().isConnected());
        assertTrue(network.getLine("d5d1cc4a-6297-4386-b6ce-16dc26f15feb").getTerminal1().isConnected());
        assertNoFictitiousSwitch(network);
    }

    @Test
    @DisplayName("Out-of-service equipment follows the fictitious switches creation mode")
    void fictitiousSwitchesCreationModeTest() {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "NEVER");
        Network network = readCgmesResources(properties, LINE_DIR, "line_EQ.xml", "line_SSH_inService.xml");
        assertNoFictitiousSwitch(network);

        properties.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "ALWAYS_EXCEPT_SWITCHES");
        network = readCgmesResources(properties, LINE_DIR, "line_EQ.xml", "line_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getLine("ACLineSegment").getTerminal1(), "ACLineSegment-T1");
    }

    @Test
    @DisplayName("Out-of-service switches are opened, without fictitious switch")
    void outOfServiceSwitchesTest() {
        // Out-of-service switches are opened, without fictitious switch, whatever their open value
        Network network = readCgmesResources(SWITCH_DIR, "switch_EQ.xml", "switch_SSH_inService.xml");
        assertTrue(network.getSwitch("Breaker").isOpen());
        assertTrue(network.getSwitch("ACLineSegment").isOpen());
        assertFalse(network.getSwitch("SeriesCompensator").isOpen());
        assertNoFictitiousSwitch(network);

        // Back in service: the open value of the SSH applies again
        readCgmesResources(network, SWITCH_DIR, "switch_SSH.xml");
        assertFalse(network.getSwitch("Breaker").isOpen());
    }

    @Test
    @DisplayName("An out-of-service busbar section is detached from its node, the node is unchanged")
    void outOfServiceBusbarSectionTest() {
        // The busbar section is detached from its node by a fictitious open switch, the rest of the node is unchanged
        Network network = readCgmesResources(BUSBAR_SECTION_DIR, "busbarSection_EQ.xml");
        int loadNode = network.getLoad("EnergyConsumer").getTerminal().getNodeBreakerView().getNode();

        readCgmesResources(network, BUSBAR_SECTION_DIR, "busbarSection_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getBusbarSection("BusbarSection").getTerminal(), "BusbarSection-T");
        assertEquals(loadNode, network.getLoad("EnergyConsumer").getTerminal().getNodeBreakerView().getNode());
    }

    @Test
    @DisplayName("The inService value of DC equipment is ignored, in both DC models")
    void outOfServiceDcEquipmentTest() {
        // The inService value of DC equipment, converters included, is ignored in both DC models.
        // Simplified DC model: no fictitious switch disconnects the out-of-service VSC converter
        Properties properties = new Properties();
        properties.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        Network network = readCgmesResources(HVDC_DIR, "hvdc_EQ.xml", "hvdc_SSH.xml");
        readCgmesResources(network, properties, HVDC_DIR, "hvdc_SSH_inService.xml");
        assertNoFictitiousSwitch(network);

        // Detailed DC model: the DC terminals of the out-of-service DC line and VSC converter stay connected,
        // and no fictitious switch disconnects the converter on its AC side
        properties.put(CgmesImport.USE_DETAILED_DC_MODEL, "true");
        network = readCgmesResources(properties, HVDC_DIR, "hvdc_EQ.xml", "hvdc_SSH.xml");
        readCgmesResources(network, properties, HVDC_DIR, "hvdc_SSH_inService.xml");
        assertTrue(network.getDcLine("DCLineSegment-Lcc").getDcTerminal1().isConnected());
        assertTrue(network.getDcLine("DCLineSegment-Lcc").getDcTerminal2().isConnected());
        assertTrue(network.getVoltageSourceConverter("DCLineSegment-Vsc-VscConverter-1").getDcTerminal1().isConnected());
        assertNoFictitiousSwitch(network);
    }

    @Test
    @DisplayName("Out-of-service boundary lines and tie line halves are disconnected")
    void outOfServiceBoundaryAndTieLinesTest() {
        Network network = readCgmesResources("/update/boundary-line/", "boundaryLine_EQ.xml", "boundaryLine_EQ_BD.xml", "boundaryLine_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, network.getBoundaryLine("ACLineSegment").getTerminal(), "ACLineSegment-T1");
        assertTrue(network.getBoundaryLine("EquivalentBranch").getTerminal().isConnected());

        // Only the out-of-service half of the tie line is disconnected
        network = readCgmesResources("/update/tie-line/", "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH_inService.xml");
        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertDisconnectedByFictitiousSwitch(network, tieLine.getBoundaryLine1().getTerminal(), "ACLineSegment-1-T1");
        assertTrue(tieLine.getBoundaryLine2().getTerminal().isConnected());
    }

    @Test
    @DisplayName("An SSH update puts equipment back in service, and out of service again")
    void sshUpdateTest() {
        Network network = readCgmesResources(LOAD_DIR, "load_EQ.xml", "load_SSH_inService.xml");
        Terminal terminal = network.getLoad("EnergyConsumer").getTerminal();
        assertDisconnectedByFictitiousSwitch(network, terminal, "EnergyConsumer_T");

        // Back in service: the fictitious switch is closed
        readCgmesResources(network, LOAD_DIR, "load_SSH.xml");
        assertFalse(network.getSwitch("EnergyConsumer_T_SW_fict").isOpen());

        // Out of service again: the same fictitious switch is reopened
        readCgmesResources(network, LOAD_DIR, "load_SSH_inService.xml");
        assertDisconnectedByFictitiousSwitch(network, terminal, "EnergyConsumer_T");
        assertEquals(1, network.getSwitchStream().filter(EquipmentInServiceImportTest::isCreatedForDisconnectedTerminal).count());
    }

    @Test
    @DisplayName("Equipment.inService is not read from CGMES 2.4.15 models")
    void cim16Test() {
        // CGMES 2.4.15 has no Equipment.inService: the attribute is not read
        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        Properties properties = new Properties();
        properties.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        readCgmesResources(network, properties, LINE_DIR, "cim16_microGridBE_SSH_inService.xml");
        assertTrue(network.getLine("ffbabc27-1ccd-4fdc-b037-e341706c8d29").getTerminal1().isConnected());
        assertTrue(network.getLine("ffbabc27-1ccd-4fdc-b037-e341706c8d29").getTerminal2().isConnected());
    }

    /**
     * Assert that a node-breaker terminal is disconnected by the open fictitious switch created for its CGMES terminal,
     * and that this switch is connected to the node of the terminal.
     *
     * @param network the network
     * @param terminal the terminal
     * @param cgmesTerminalId the CGMES id of the terminal
     */
    private static void assertDisconnectedByFictitiousSwitch(Network network, Terminal terminal, String cgmesTerminalId) {
        Switch fictitiousSwitch = network.getSwitch(cgmesTerminalId + "_SW_fict");
        assertNotNull(fictitiousSwitch);
        assertTrue(fictitiousSwitch.isOpen());
        VoltageLevel.NodeBreakerView topology = terminal.getVoltageLevel().getNodeBreakerView();
        int node = terminal.getNodeBreakerView().getNode();
        assertTrue(topology.getNode1(fictitiousSwitch.getId()) == node || topology.getNode2(fictitiousSwitch.getId()) == node);
        assertFalse(terminal.isConnected());
    }

    /**
     * Assert that no fictitious switch was created for a disconnected terminal.
     *
     * @param network the network
     */
    private static void assertNoFictitiousSwitch(Network network) {
        assertTrue(network.getSwitchStream().noneMatch(EquipmentInServiceImportTest::isCreatedForDisconnectedTerminal));
    }

    /**
     * Tell whether a switch is a fictitious switch created for a disconnected terminal. {@link Switch#isFictitious()}
     * cannot be used: the lines, equivalent branches and series compensators imported as switches are fictitious too.
     *
     * @param sw the switch
     * @return {@code true} if the switch was created for a disconnected terminal
     */
    private static boolean isCreatedForDisconnectedTerminal(Switch sw) {
        return sw.isFictitious() && "true".equals(sw.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));
    }
}
