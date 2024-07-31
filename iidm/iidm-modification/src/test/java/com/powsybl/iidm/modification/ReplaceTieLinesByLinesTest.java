/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.AbstractModificationTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class ReplaceTieLinesByLinesTest extends AbstractModificationTest {

    @Test
    void roundTripBusBreaker() throws IOException {
        Network network = createBusBreakerNetworkWithAliasesPropertiesExtensions();
        new ReplaceTieLinesByLines().apply(network);
        writeXmlTest(network, "/eurostag-replace-tl.xml");
    }

    @Test
    void roundTripNodeBreaker() throws IOException {
        Network network = createDummyNodeBreakerNetwork();
        new ReplaceTieLinesByLines().apply(network);
        writeXmlTest(network, "/replace-tl-nb.xml");
    }

    @Test
    void postProcessor() throws IOException {
        NetworkSerDe.write(createBusBreakerNetworkWithAliasesPropertiesExtensions(), tmpDir.resolve("tl-test.xml"));
        Network read = Network.read(tmpDir.resolve("tl-test.xml"), LocalComputationManager.getDefault(),
                new ImportConfig("replaceTieLinesByLines"), new Properties());
        writeXmlTest(read, "/eurostag-replace-tl.xml");
    }

    private static Network createBusBreakerNetworkWithAliasesPropertiesExtensions() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();

        DanglingLine dl1 = network.getDanglingLine("NHV1_XNODE1");
        dl1.addAlias("test1");
        dl1.addAlias("test2", "same");
        dl1.setProperty("key1", "value1");
        dl1.setProperty("key2", "value2");
        dl1.setProperty("key3", "value4");

        DanglingLine dl2 = network.getDanglingLine("XNODE1_NHV2");
        dl2.addAlias("test3");
        dl2.addAlias("test4", "same");
        dl2.setProperty("key1", "value1");
        dl2.setProperty("key2", "value3");

        dl1.addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());
        dl2.addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());
        network.getTieLine("NHV1_NHV2_1").addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());

        return network;
    }

    private static Network createDummyNodeBreakerNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2023-04-29T14:52:01.427+02:00"));
        Substation s1 = network.newSubstation().setId("S1").add();
        Substation s2 = network.newSubstation().setId("S2").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        DanglingLine dl1 = vl1.newDanglingLine().setId("DL1").setNode(0).setP0(0.0).setQ0(0.0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        DanglingLine dl2 = vl2.newDanglingLine().setId("DL2").setNode(0).setP0(0.0).setQ0(0.0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        network.newTieLine().setId("TL").setDanglingLine1(dl1.getId()).setDanglingLine2(dl2.getId()).add();
        return network;
    }

    private static final class DummyIdentifiableExtension<T extends Identifiable<T>> extends AbstractExtension<T> {
        @Override
        public String getName() {
            return "foo";
        }
    }

    @Test
    void testDryRun() {
        Network network = createDummyNodeBreakerNetwork();

        // ReplaceTieLinesByLines - Passing dryRun
        ReplaceTieLinesByLines networkModification = new ReplaceTieLinesByLines();
        assertTrue(networkModification.apply(network, true));

        // Useful methods for dry run
        assertTrue(networkModification.hasImpactOnNetwork());
        assertTrue(networkModification.isLocalDryRunPossible());
    }
}
