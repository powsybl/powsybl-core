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

import static com.powsybl.iidm.serde.ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.KEEP_ORIGINAL_TOPOLOGY;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static com.powsybl.iidm.serde.TerminalRefSerDe.writeTerminalRefAttribute;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void voltageLevelWithBusAndKeepOriginalTopologyMustExportBusBranch() {
        Network network = Network.create("n1", "test");
        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        vl.getBusBreakerView().newBus()
                .setId("busId")
                .add();

        // Export options : BUS_BRANCH topology level + KEEP_ORIGINAL_TOPOLOGY export option
        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(KEEP_ORIGINAL_TOPOLOGY);

        NetworkSerializerContext context = new NetworkSerializerContext(new SimpleAnonymizer(), mock(TreeDataWriter.class), options, null, CURRENT_IIDM_VERSION, true);

        TopologyLevel exportTopology =
                TopologyLevelUtil.determineTopologyLevel(vl, context);

        assertEquals(TopologyLevel.BUS_BRANCH, exportTopology);
    }

    @Test
    void voltageLevelWithoutBusbarSectionKeepsBusBranch() {
        Network network = Network.create("n1", "test");
        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        // Export options : BUS_BRANCH topology level + KEEP_ORIGINAL_TOPOLOGY export option
        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(KEEP_ORIGINAL_TOPOLOGY);

        NetworkSerializerContext context = new NetworkSerializerContext(new SimpleAnonymizer(), mock(TreeDataWriter.class), options, null, CURRENT_IIDM_VERSION, true);

        TopologyLevel exportTopology =
                TopologyLevelUtil.determineTopologyLevel(vl, context);

        assertEquals(TopologyLevel.BUS_BRANCH, exportTopology);
    }

    @Test
    void voltageLevelWithTerminalRefToBusbarSectionAndThrowExceptionMustFail() {
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
                .setNode(0)
                .add();

        vl.getNodeBreakerView().newSwitch()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .add();

        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(
                ExportOptions.BusBranchVoltageLevelIncompatibilityBehavior.THROW_EXCEPTION);

        NetworkSerializerContext context =
                new NetworkSerializerContext(
                        new SimpleAnonymizer(),
                        mock(TreeDataWriter.class),
                        options,
                        null,
                        CURRENT_IIDM_VERSION,
                        true);

        assertThrows(PowsyblException.class,
                () -> TopologyLevelUtil.determineTopologyLevel(vl, context));
    }

    @Test
    void shouldFailOnMainAndPassWithFixWhenTerminalRefPointsToBusbarSection() {

        // --- Given ---
        Network network = Network.create("n1", "test");

        Substation substation = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel vl = substation.newVoltageLevel()
                .setId("VL1")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        // Busbar section
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();

        // Load
        vl.newLoad()
                .setId("L1")
                .setNode(1)
                .setP0(10)
                .setQ0(5)
                .add();

        // ✅ IMPORTANT : réseau CONNECTÉ
        vl.getNodeBreakerView().newSwitch()
                .setId("SW1")
                .setNode1(0)
                .setNode2(1)
                .setKind(SwitchKind.BREAKER)
                .setOpen(false)
                .add();

        ExportOptions options = new ExportOptions();
        options.setTopologyLevel(TopologyLevel.BUS_BRANCH);
        options.setBusBranchVoltageLevelIncompatibilityBehavior(KEEP_ORIGINAL_TOPOLOGY);

        TreeDataWriter writer = mock(TreeDataWriter.class);

        BusFilter filter = mock(BusFilter.class);
        when(filter.test(any(Connectable.class))).thenReturn(true);

        NetworkSerializerContext context =
                new NetworkSerializerContext(
                        new SimpleAnonymizer(),
                        writer,
                        options,
                        filter,
                        CURRENT_IIDM_VERSION,
                        true);

        // --- When / Then ---

        // ✅ AVANT FIX : exception
        // ✅ APRÈS FIX : pas d’exception

        assertDoesNotThrow(() -> {
            for (Connectable<?> c : vl.getConnectables()) {
                for (Terminal t : c.getTerminals()) {
                    writeTerminalRefAttribute(t, context, writer);
                }
            }
        });
    }
}
