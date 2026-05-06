/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.serde.anonymizer.SimpleAnonymizer;
import com.powsybl.iidm.serde.util.TopologyLevelUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * @author Teofil Calin Banc {@literal <teofil-calin.banc at rte-france.com>}
 */
class TopologyLevelTest extends AbstractIidmSerDeTest {

    @Test
    void testComparison() {
        assertEquals(TopologyLevel.NODE_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BRANCH));

        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BRANCH));
    }

    @Test
    void testConversion() throws IOException {
        testConversion(NetworkSerDe.read(getVersionedNetworkAsStream("fictitiousSwitchRef.xml", IidmVersion.V_1_0)));

        testConversion(FictitiousSwitchFactory.create());
    }

    private void testConversion(Network network) throws IOException {

        ExportOptions options = new ExportOptions();
        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.NODE_BREAKER), "fictitiousSwitchRef.xml", CURRENT_IIDM_VERSION);

        network.getSwitchStream().forEach(sw -> sw.setRetained(false));
        network.getSwitch("BJ").setRetained(true);

        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.BUS_BREAKER), "fictitiousSwitchRef-bbk.xml", CURRENT_IIDM_VERSION);
        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.BUS_BRANCH), "fictitiousSwitchRef-bbr.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void voltageLevelWithBusbarSectionAndKeepOriginalTopologyMustExportNodeBreaker() {
        Network network = Network.create("n1", "test");
        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setName("B")
                .setNode(0)
                .add();

        // Export options : BUS_BRANCH topology level + keep original topology
        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.KEEP_ORIGINAL_TOPOLOGY);

        NetworkSerializerContext context = new NetworkSerializerContext(new SimpleAnonymizer(), mock(TreeDataWriter.class), options, null, CURRENT_IIDM_VERSION, true);

        TopologyLevel exportTopology =
                TopologyLevelUtil.determineTopologyLevel(vl, context);

        assertEquals(TopologyLevel.NODE_BREAKER, exportTopology,
                "VoltageLevel with BusbarSection must be exported in NODE_BREAKER " +
                        "when keepOriginalTopology is enabled");
    }

    @Test
    void voltageLevelWithBusbarSectionWithoutKeepOriginalTopologyMustThrowException() {
        Network network = Network.create("n2", "test");

        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setName("B")
                .setNode(0)
                .add();

        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.THROW_EXCEPTION);

        NetworkSerializerContext context = new NetworkSerializerContext(new SimpleAnonymizer(), mock(TreeDataWriter.class), options, null, CURRENT_IIDM_VERSION, true);

        assertThrows(PowsyblException.class, () -> TopologyLevelUtil.determineTopologyLevel(vl, context));
    }
}
