/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.getElement;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.getElementCount;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class CgmesTopologyKindTest extends AbstractSerDeTest {

    @ParameterizedTest
    @EnumSource(value = CgmesTopologyKind.class, names = {"NODE_BREAKER", "BUS_BRANCH"})
    void cgmesTopologyKindTest(CgmesTopologyKind topologyKind) throws IOException {
        Network network = mixedTopologyNetwork();

        // Assert the CIM 16 and CIM 100 exports with given topology kind are valid
        assertValidExport(network, topologyKind, false);
        assertValidExport(network, topologyKind, true);
    }

    private void assertValidExport(Network network, CgmesTopologyKind topologyKind, boolean cim100Export) throws IOException {
        // Build the export parameters
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.TOPOLOGY_KIND, topologyKind.name());
        if (cim100Export) {
            exportParams.put(CgmesExport.CIM_VERSION, "100");
        }

        // Export to CGMES
        String eqFile = ConversionUtil.writeCgmesProfile(network, "EQ", tmpDir, exportParams);
        String sshFile = ConversionUtil.writeCgmesProfile(network, "SSH", tmpDir, exportParams);
        String svFile = ConversionUtil.writeCgmesProfile(network, "SV", tmpDir, exportParams);
        String tpFile = ConversionUtil.writeCgmesProfile(network, "TP", tmpDir, exportParams);

        // Assert the exports are valid
        assertValidProfileInHeader(eqFile, topologyKind, cim100Export);
        assertValidCim16EquipmentOperationElements(eqFile, sshFile, svFile, topologyKind, cim100Export);
        assertValidConnectivityElements(eqFile, topologyKind, cim100Export);
        assertValidTopologyElements(tpFile, topologyKind, cim100Export);
    }

    private void assertValidProfileInHeader(String eqFile, CgmesTopologyKind topologyKind, boolean cim100Export) {
        if (topologyKind == CgmesTopologyKind.NODE_BREAKER && !cim100Export) {
            assertTrue(eqFile.contains(CgmesNamespace.CIM_16_EQ_OPERATION_PROFILE));
        } else {
            assertFalse(eqFile.contains(CgmesNamespace.CIM_16_EQ_OPERATION_PROFILE));
            assertFalse(eqFile.contains(CgmesNamespace.CIM_100_EQ_OPERATION_PROFILE));
        }
    }

    private void assertValidCim16EquipmentOperationElements(String eqFile, String sshFile, String svFile, CgmesTopologyKind topologyKind, boolean cim100Export) {
        if (topologyKind == CgmesTopologyKind.NODE_BREAKER || cim100Export) {
            assertEquals(1, getElementCount(eqFile, "CurrentLimit"));
            assertEquals(1, getElementCount(eqFile, "ActivePowerLimit"));
            assertEquals(2, getElementCount(eqFile, "ApparentPowerLimit"));
            assertEquals(1, getElementCount(eqFile, "StationSupply"));
            assertEquals(1, getElementCount(eqFile, "GroundDisconnector"));
            assertEquals(1, getElementCount(eqFile, "LoadArea"));
            assertEquals(1, getElementCount(eqFile, "SubLoadArea"));
            assertTrue(getElement(eqFile, "ConformLoadGroup", "ConformLoad_LG").contains("cim:LoadGroup.SubLoadArea"));
            assertTrue(getElement(eqFile, "NonConformLoadGroup", "NonConformLoad_LG").contains("cim:LoadGroup.SubLoadArea"));
            assertTrue(getElement(eqFile, "ControlArea", "Interchange").contains("cim:ControlArea.EnergyArea"));
            assertEquals(1, getElementCount(sshFile, "StationSupply"));
            assertEquals(1, getElementCount(sshFile, "GroundDisconnector"));
            assertEquals(7, getElementCount(svFile, "SvStatus"));
        } else {
            assertEquals(1, getElementCount(eqFile, "CurrentLimit")); // CurrentLimit are NOT part of CIM16 EQ_OP
            assertEquals(0, getElementCount(eqFile, "ActivePowerLimit"));
            assertEquals(0, getElementCount(eqFile, "ApparentPowerLimit"));
            assertEquals(0, getElementCount(eqFile, "StationSupply"));
            assertEquals(0, getElementCount(eqFile, "GroundDisconnector"));
            assertEquals(0, getElementCount(eqFile, "LoadArea"));
            assertEquals(0, getElementCount(eqFile, "SubLoadArea"));
            assertFalse(getElement(eqFile, "ConformLoadGroup", "ConformLoad_LG").contains("cim:LoadGroup.SubLoadArea"));
            assertFalse(getElement(eqFile, "NonConformLoadGroup", "NonConformLoad_LG").contains("cim:LoadGroup.SubLoadArea"));
            assertFalse(getElement(eqFile, "ControlArea", "Interchange").contains("cim:ControlArea.EnergyArea"));
            assertEquals(0, getElementCount(sshFile, "StationSupply"));
            assertEquals(0, getElementCount(sshFile, "GroundDisconnector"));
            assertEquals(0, getElementCount(svFile, "SvStatus"));
        }
    }

    private void assertValidConnectivityElements(String eqFile, CgmesTopologyKind topologyKind, boolean cim100Export) {
        if (topologyKind == CgmesTopologyKind.NODE_BREAKER) {
            assertEquals(5, getElementCount(eqFile, "ConnectivityNode"));
            assertEquals(3, getElementCount(eqFile, "Breaker"));
        } else {
            // In case of a CIM16 bus-branch export, the buses from the BusBreakerView aren't exported as ConnectivityNode
            int connectivityNodesCount = cim100Export ? 4 : 0;
            assertEquals(connectivityNodesCount, getElementCount(eqFile, "ConnectivityNode"));
            assertEquals(1, getElementCount(eqFile, "Breaker")); // because one is non-retained
        }
        assertValidTerminalCount(eqFile, topologyKind, cim100Export);
    }

    private void assertValidTopologyElements(String tpFile, CgmesTopologyKind topologyKind, boolean cim100Export) {
        // The number of topological nodes is independent from the topology kind or cim version
        assertEquals(4, getElementCount(tpFile, "TopologicalNode"));
        assertValidTerminalCount(tpFile, topologyKind, cim100Export);
    }

    private void assertValidTerminalCount(String eqOrTpFile, CgmesTopologyKind topologyKind, boolean cim100Export) {
        // BusbarSection (2), Generator (1), ACLineSegment (2), ConformLoad (1), NonConformLoad (1), retained Breaker (2)
        // terminals are always exported
        int terminalCount = 9;
        if (topologyKind == CgmesTopologyKind.NODE_BREAKER || cim100Export) {
            // StationSupply (1), GroundDisconnector (2) terminals are exported if not a CIM16 bus-branch export
            terminalCount += 3;
        }
        if (topologyKind == CgmesTopologyKind.NODE_BREAKER) {
            // non-retained Breaker (4) terminals are exported if not a bus-branch export
            terminalCount += 4;
        }

        assertEquals(terminalCount, getElementCount(eqOrTpFile, "Terminal"));
    }

    private Network mixedTopologyNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");

        //    VL_1: Bus-Breaker                            VL_2: Node-Breaker
        //
        //                           ________LN________
        //                           |                |    BBS_2A    __BK2__     BBS_2B
        // ____(GEN-BUS)____BK1____(BUS)_           _(1)____(0)______|     |______(3)________GRDIS__
        //    |         |                                    |       |_BK3_|       |           |
        //    |         |                                   (2)                   (4)         (5)
        //   GEN       AUX                                  LD_C                 LD_NC

        // Create Substation 1 with a Generator and a station supply Load
        Substation substation1 = network.newSubstation()
                .setId("ST_1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VL_1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("BUS")
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("GEN-BUS")
                .add();
        voltageLevel1.getBusBreakerView().newSwitch()
                .setId("BK1")
                .setBus1("BUS")
                .setBus2("GEN-BUS")
                .setOpen(false)
                .add();
        voltageLevel1.newGenerator()
                .setId("GEN")
                .setBus("GEN-BUS")
                .setTargetP(1.0)
                .setTargetQ(1.0)
                .setMinP(0.0)
                .setMaxP(2.0)
                .setVoltageRegulatorOn(false)
                .add();
        voltageLevel1.newLoad()
                .setId("AUX")
                .setBus("GEN-BUS")
                .setP0(0.0)
                .setQ0(0.0)
                .setLoadType(LoadType.AUXILIARY)
                .add()
                .setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.STATION_SUPPLY);

        // Create Substation 2 with a BusbarSection, a Load and a GroundDisconnector
        Substation substation2 = network.newSubstation()
                .setId("ST_2")
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VL_2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel2.getNodeBreakerView().newBusbarSection()
                .setId("BBS_2A")
                .setNode(0)
                .add();
        voltageLevel2.getNodeBreakerView().newBusbarSection()
                .setId("BBS_2B")
                .setNode(3)
                .add();
        voltageLevel2.getNodeBreakerView().newSwitch()
                .setId("BK2")
                .setNode1(0)
                .setNode2(3)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .setRetained(false)
                .add();
        voltageLevel2.getNodeBreakerView().newSwitch()
                .setId("BK3")
                .setNode1(0)
                .setNode2(3)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .setRetained(true) // will be considered non-retained by the export
                                   // because it has the same topological nodes on each side
                .add();
        voltageLevel2.newLoad()
                .setId("LD_C")
                .setNode(2)
                .setP0(1.0)
                .setQ0(0.0)
                .add()
                .setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.CONFORM_LOAD);
        voltageLevel2.newLoad()
                .setId("LD_NC")
                .setNode(4)
                .setP0(0.0)
                .setQ0(1.0)
                .add()
                .setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, CgmesNames.NONCONFORM_LOAD);
        voltageLevel2.getNodeBreakerView().newSwitch()
                .setId("GRDIS")
                .setNode1(3)
                .setNode2(5)
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(false)
                .setRetained(true)
                .add()
                .setProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, "GroundDisconnector");

        // Create a Line between substations 1 and 2
        Line line = network.newLine()
                .setId("LN")
                .setR(0.1)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .setVoltageLevel1("VL_1")
                .setVoltageLevel2("VL_2")
                .setBus1("BUS")
                .setNode2(1)
                .add();

        // Create ControlArea
        network.newArea()
                .setId("Interchange")
                .setAreaType("ControlAreaTypeKind.Interchange")
                .add();

        // Create connectivity
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(1).add();
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(2).add();
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(3).setNode2(4).add();

        // Add limits
        line.newCurrentLimits1().setPermanentLimit(100).add();
        line.newApparentPowerLimits1().setPermanentLimit(100).add();
        line.newActivePowerLimits2().setPermanentLimit(100).add();
        line.newApparentPowerLimits2().setPermanentLimit(100).add();

        return network;
    }
}
