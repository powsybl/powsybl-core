/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class ReplaceTieLinesByLinesTest extends AbstractConverterTest {

    @Test
    void roundTripBusBreaker() throws IOException {
        Network network = createBusBreakerNetworkWithAliasesPropertiesExtensions();
        new ReplaceTieLinesByLines().apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tl.xml");
    }

    @Test
    void roundTripNodeBreaker() throws IOException {
        Network network = createDummyNodeBreakerNetwork();
        new ReplaceTieLinesByLines().apply(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/replace-tl-nb.xml");
    }

    @Test
    void postProcessor() throws IOException {
        NetworkXml.write(createBusBreakerNetworkWithAliasesPropertiesExtensions(), tmpDir.resolve("tl-test.xml"));
        Network read = Network.read(tmpDir.resolve("tl-test.xml"), LocalComputationManager.getDefault(),
                new ImportConfig("replaceTieLinesByLines"), new Properties());
        roundTripXmlTest(read, NetworkXml::writeAndValidate, NetworkXml::validateAndRead,
                "/eurostag-replace-tl.xml");
    }

    private static Network createBusBreakerNetworkWithAliasesPropertiesExtensions() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();

        BoundaryLine bl1 = network.getBoundaryLine("NHV1_XNODE1");
        bl1.addAlias("test1");
        bl1.addAlias("test2", "same");
        bl1.setProperty("key1", "value1");
        bl1.setProperty("key2", "value2");
        bl1.setProperty("key3", "value4");

        BoundaryLine bl2 = network.getBoundaryLine("XNODE1_NHV2");
        bl2.addAlias("test3");
        bl2.addAlias("test4", "same");
        bl2.setProperty("key1", "value1");
        bl2.setProperty("key2", "value3");

        bl1.addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());
        bl2.addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());
        network.getTieLine("NHV1_NHV2_1").addExtension(DummyIdentifiableExtension.class, new DummyIdentifiableExtension<>());

        return network;
    }

    private static Network createDummyNodeBreakerNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(DateTime.parse("2023-04-29T14:52:01.427+02:00"));
        Substation s1 = network.newSubstation().setId("S1").add();
        Substation s2 = network.newSubstation().setId("S2").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(1f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        BoundaryLine bl1 = vl1.newBoundaryLine().setId("DL1").setNode(0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        BoundaryLine bl2 = vl2.newBoundaryLine().setId("DL2").setNode(0).setR(1.5).setX(13.0).setG(0.0).setB(1e-6).add();
        network.newTieLine().setId("TL").setBoundaryLine1(bl1.getId()).setBoundaryLine2(bl2.getId()).add();
        return network;
    }

    private static class DummyIdentifiableExtension<T extends Identifiable<T>> extends AbstractExtension<T> {
        @Override
        public String getName() {
            return "foo";
        }
    }
}
