/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.TopologyLevelUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.network.TopologyLevel.BUS_BRANCH;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class BusBranchExportTest extends AbstractSerDeTest {

    @Test
    void roundTripTestFail() {
        // Read a NodeBreaker XIIDM with all switch open="true"
        String filename = "/busBranchExportError.xiidm";
        Network networkNodeBreaker = Network.read(filename, getClass().getResourceAsStream(filename));
        assertNotNull(networkNodeBreaker);
        // WRITE this network with BUS_BRANCH Topology level
        Properties propertiesExport = new Properties();
        propertiesExport.put(XMLExporter.TOPOLOGY_LEVEL, BUS_BRANCH.name());
        Path transformFile = tmpDir.resolve("exportBusBranch.xiidm");
        PowsyblException e = assertThrows(PowsyblException.class, () -> networkNodeBreaker.write("XIIDM", propertiesExport, transformFile));
        assertEquals("Cannot export voltage level \"VL1\" in BUS_BRANCH topology: this would lead to an invalid IIDM.", e.getMessage());
    }

    @Test
    void roundTripTestOK() {
        // Read a NodeBreaker XIIDM with all switch open="true"
        String filename = "/busBranchExportError.xiidm";
        Network networkNodeBreaker = Network.read(filename, getClass().getResourceAsStream(filename));
        assertNotNull(networkNodeBreaker);
        // WRITE this network with BUS_BRANCH Topology level
        Properties propertiesExport = new Properties();
        propertiesExport.put(XMLExporter.TOPOLOGY_LEVEL, BUS_BRANCH.name());
        propertiesExport.put("iidm.export.xml.bus-branch.voltage-level.incompatibility-behavior", "KEEP_ORIGINAL_TOPOLOGY");
        Path transformFile = tmpDir.resolve("exportBusBranch.xiidm");
        networkNodeBreaker.write("XIIDM", propertiesExport, transformFile);
        // Expected : the new file with BusBranch topoLevel readable
        Network networkBusBranch = Network.read(transformFile);
        assertNotNull(networkBusBranch);
    }

    @Test
    void shouldKeepOriginalTopologyWhenDetectingReferenceToBusbarTerminal() {
        Network network = Network.create("n1", "test");
        Substation substation = network.newSubstation().setId("S1").add();
        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl.getNodeBreakerView().newBusbarSection().setId("BBS1").setNode(0).add();
        BusbarSection bbs2 = vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();

        vl.newGenerator()
                .setId("GEN1")
                .setNode(2)
                .setRegulatingTerminal(bbs2.getTerminal())
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
                .add();

        vl.getNodeBreakerView().newSwitch()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setKind(SwitchKind.BREAKER)
                .setOpen(true)
                .add();

        vl.getNodeBreakerView().newSwitch()
                .setId("SW2")
                .setNode1(0)
                .setNode2(2)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .add();

        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.KEEP_ORIGINAL_TOPOLOGY);
        Path file = tmpDir.resolve("data");

        NetworkSerDe.write(network, options, file);
        assertDoesNotThrow(() -> NetworkSerDe.read(file));
    }

    @Test
    void shouldReturnFalseWhenTopologyIsBusBreaker() {
        Network network = Network.create("n1", "test");

        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        assertFalse(TopologyLevelUtil.hasReferencedBusbarSections(vl));
    }
}
