/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Teofil Calin Banc<teofil-calin.banc at rte-france.com>
 */
class TopologyLevelTest extends AbstractXmlConverterTest {

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
        testConversion(NetworkXml.read(getVersionedNetworkAsStream("fictitiousSwitchRef.xml", IidmXmlVersion.V_1_0)));

        testConversion(FictitiousSwitchFactory.create());
    }

    private void testConversion(Network network) throws IOException {
        writeXmlTest(network, TopologyLevelTest::writeNodeBreaker,
                getVersionedNetworkPath("fictitiousSwitchRef.xml", CURRENT_IIDM_XML_VERSION));

        network.getSwitchStream().forEach(sw -> sw.setRetained(false));
        network.getSwitch("BJ").setRetained(true);

        writeXmlTest(network, TopologyLevelTest::writeBusBreaker,
                getVersionedNetworkPath("fictitiousSwitchRef-bbk.xml", CURRENT_IIDM_XML_VERSION));
        writeXmlTest(network, TopologyLevelTest::writeBusBranch,
                getVersionedNetworkPath("fictitiousSwitchRef-bbr.xml", CURRENT_IIDM_XML_VERSION));
    }

    private static void writeNodeBreaker(Network network, Path path) {
        ExportOptions options = new ExportOptions()
                .setTopologyLevel(TopologyLevel.NODE_BREAKER);

        NetworkXml.write(network, options, path);
    }

    private static void writeBusBreaker(Network network, Path path) {
        ExportOptions options = new ExportOptions()
                .setTopologyLevel(TopologyLevel.BUS_BREAKER);

        NetworkXml.write(network, options, path);
    }

    private static void writeBusBranch(Network network, Path path) {
        ExportOptions options = new ExportOptions()
                .setTopologyLevel(TopologyLevel.BUS_BRANCH);

        NetworkXml.write(network, options, path);
    }

    @Test
    public void testUnconnectableElementSerialization() throws IOException {

        Network network = EurostagTutorialExample1Factory.create();

        //Disconnecting the generator GEN by disconnecting its transformer
        TwoWindingsTransformer stepUpTransfo = network.getTwoWindingsTransformer("NGEN_NHV1");
        stepUpTransfo.getTerminal1().disconnect();
        stepUpTransfo.getTerminal2().disconnect();

        network.getGenerator("GEN")
                .getTerminal().disconnect();

        ExportOptions options = new ExportOptions()
                .setTopologyLevel(TopologyLevel.BUS_BRANCH);
        // TODO: For debug purpose, remove before merge
        NetworkXml.write(network, options, System.out);

        byte[] networkXmlBytes = toBytes(os -> NetworkXml.write(network, options, os));
        try (InputStream is = new ByteArrayInputStream(networkXmlBytes)) {
            // Throws with
            // com.powsybl.iidm.network.ValidationException: Generator 'GEN': connectable bus is not set
            NetworkXml.read(is);
        }
    }

    private static byte[] toBytes(Consumer<OutputStream> generator) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            generator.accept(os);
            os.close();
            return os.toByteArray();
        }
    }
}
