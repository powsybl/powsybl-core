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
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.network.TopologyLevel.BUS_BRANCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals("Cannot export voltage level 'VL1' with all its BREAKER switches open in BUS_BRANCH topology", e.getMessage());
    }

    @Test
    void roundTripTestOK() {
        // Read a NodeBreaker XIIDM with all BREAKERS switch open="true"
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
}
