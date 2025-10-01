/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.network.TopologyLevel.BUS_BRANCH;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
class BusBranchExportTest extends AbstractSerDeTest {

    @Test
    void roundTripTest() {
        // Read a NodeBreaker XIIDM with all switch open="true"
        String filename = "busBranchExportError.xiidm";
        Network networkNodeBreaker = Network.read("busBranchExportError.xiidm", getClass().getResourceAsStream("/V1_4/busBranchExportError.xiidm"));
        assertNotNull(networkNodeBreaker);
        // WRITE this network with BUS_BRANCH Topology level
        Properties propertiesExport = new Properties();
        propertiesExport.put(XMLExporter.EXTENSIONS_LIST, "activePowerControl,hvdcAngleDroopActivePowerControl,hvdcOperatorActivePowerRange,standbyAutomaton");
        propertiesExport.put(XMLExporter.VERSION, "1.14");
        propertiesExport.put(XMLExporter.TOPOLOGY_LEVEL, BUS_BRANCH.name());
        Path transformFile = tmpDir.resolve("exportBusBranch.xiidm");
        networkNodeBreaker.write("XIIDM", propertiesExport, transformFile);
        // Expected : the new file with BusBranch topoLevel readable
        Network networkBusBranch = Network.read(transformFile);
        assertNotNull(networkBusBranch);
    }

}
