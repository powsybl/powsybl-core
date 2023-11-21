/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerializer;
import com.powsybl.commons.extensions.ExtensionSerializer;
import com.powsybl.commons.io.ReaderContext;
import com.powsybl.commons.io.WriterContext;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.serializer.extensions.util.NetworkSourceExtension;
import com.powsybl.iidm.serializer.extensions.util.NetworkSourceExtensionImpl;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkSerializerTest extends AbstractIidmSerializerTest {

    static Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00+01:00"));
        return network;
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripXmlTest(createEurostagTutorialExample1(),
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("eurostag-tutorial-example1.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1.xml");
    }

    @Test
    void testValidationIssueWithProperties() {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").setProperty("test", "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkSerializer.writeAndValidate(network, xmlFile);
        Network readNetwork = NetworkSerializer.read(xmlFile);
        assertEquals("foo", readNetwork.getGenerator("GEN").getProperty("test"));
    }

    @Test
    void testGzipGunzip() throws IOException {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkSerializer.write(network, file1);
        Network network2 = NetworkSerializer.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkSerializer.write(network2, file2);
        assertArrayEquals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }

    @AutoService(ExtensionSerializer.class)
    public static class BusbarSectionExtSerializer extends AbstractExtensionSerializer<BusbarSection, BusbarSectionExt> {

        public BusbarSectionExtSerializer() {
            super("busbarSectionExt", "network", BusbarSectionExt.class, "busbarSectionExt.xsd",
                    "http://www.itesla_project.eu/schema/iidm/ext/busbarSectionExt/1_0", "bbse");
        }

        @Override
        public void write(BusbarSectionExt busbarSectionExt, WriterContext context) {
        }

        @Override
        public BusbarSectionExt read(BusbarSection busbarSection, ReaderContext context) {
            context.getReader().readEndNode();
            return new BusbarSectionExt(busbarSection);
        }
    }

    private static Network writeAndRead(Network network, ExportOptions options) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkSerializer.write(network, options, os);

            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                return NetworkSerializer.read(is);
            }
        }
    }

    @Test
    void busBreakerExtensions() throws IOException {
        Network network = NetworkTest1Factory.create();
        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.addExtension(BusbarSectionExt.class, new BusbarSectionExt(bb));

        //Re-import in node breaker
        Network nodeBreakerNetwork = writeAndRead(network, new ExportOptions());

        assertNotSame(network, nodeBreakerNetwork);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals(1, bb2.getExtensions().size());
        assertNotNull(bb2.getExtension(BusbarSectionExt.class));

        //Re-import in bus breaker
        //Check that network is correctly imported, and busbar and its extension are not here any more
        Network busBreakerNetwork = writeAndRead(network, new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER));
        assertNull(busBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1"));
    }

    @Test
    void testScada() throws IOException {
        Network network = ScadaNetworkFactory.create();
        assertEquals(ValidationLevel.EQUIPMENT, network.runValidationChecks(false));
        roundTripXmlTest(network,
                NetworkSerializer::write,
                NetworkSerializer::read,
                getVersionedNetworkPath("scadaNetwork.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("scadaNetwork.xml", IidmVersion.V_1_7);
    }

    @Test
    void checkWithSpecificEncoding() throws IOException {
        Network network = NetworkTest1Factory.create();
        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.addExtension(BusbarSectionExt.class, new BusbarSectionExt(bb));
        ExportOptions export = new ExportOptions();
        export.setCharset(StandardCharsets.ISO_8859_1);
        //Re-import in node breaker
        Network nodeBreakerNetwork = writeAndRead(network, export);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals(1, bb2.getExtensions().size());
        assertNotNull(bb2.getExtension(BusbarSectionExt.class));
    }

    @Test
    void failImportWithSeveralSubnetworkLevels() throws URISyntaxException {
        Path path = Path.of(getClass().getResource(getVersionedNetworkPath("multiple-subnetwork-levels.xml",
                CURRENT_IIDM_XML_VERSION)).toURI());
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerializer.validateAndRead(path));
        assertTrue(e.getMessage().contains("Only one level of subnetworks is currently supported."));
    }

    @Test
    void roundTripWithSubnetworksTest() throws IOException {
        Network n1 = createNetwork(1);
        Network n2 = createNetwork(2);
        n1.setCaseDate(DateTime.parse("2013-01-15T18:41:00+01:00"));
        n2.setCaseDate(DateTime.parse("2013-01-15T18:42:00+01:00"));

        Network merged = Network.merge("Merged", n1, n2);
        merged.setCaseDate(DateTime.parse("2013-01-15T18:40:00+01:00"));
        // add an extension at root network level
        NetworkSourceExtension source = new NetworkSourceExtensionImpl("Source_0");
        merged.addExtension(NetworkSourceExtension.class, source);

        roundTripXmlTest(merged,
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("subnetworks.xml", IidmSerializerConstants.CURRENT_IIDM_XML_VERSION));
        roundTripTest(merged,
                (n, jsonFile) -> NetworkSerializer.write(n, new ExportOptions().setFormat(TreeDataFormat.JSON), jsonFile),
                jsonFile -> NetworkSerializer.read(jsonFile, new ImportOptions().setFormat(TreeDataFormat.JSON)),
                getVersionedNetworkPath("subnetworks.json", IidmSerializerConstants.CURRENT_IIDM_XML_VERSION));

        roundTripVersionedXmlFromMinToCurrentVersionTest("subnetworks.xml", IidmVersion.V_1_5);
        roundTripVersionedJsonFromMinToCurrentVersionTest("subnetworks.json", IidmVersion.V_1_11);
    }

    private Network createNetwork(int num) {
        String dlId = "dl" + num;
        String voltageLevelId = "vl" + num;
        String busId = "b" + num;

        Network network = Network.create("Network-" + num, "format");
        Substation s1 = network.newSubstation()
                .setId("s" + num)
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId(voltageLevelId)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId(busId)
                .add();
        network.getVoltageLevel(voltageLevelId).newDanglingLine()
                .setId(dlId)
                .setName(dlId + "_name")
                .setConnectableBus(busId)
                .setBus(busId)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setPairingKey("code")
                .add();

        // Add an extension on the network and on an inner element
        NetworkSourceExtension source = new NetworkSourceExtensionImpl("Source_" + num);
        network.addExtension(NetworkSourceExtension.class, source);

        if (num == 1) {
            Generator generator = vl1.newGenerator()
                    .setId("GEN")
                    .setBus(busId)
                    .setConnectableBus(busId)
                    .setMinP(-9999.99)
                    .setMaxP(9999.99)
                    .setVoltageRegulatorOn(true)
                    .setTargetV(24.5)
                    .setTargetP(607.0)
                    .setTargetQ(301.0)
                    .add();
            generator.newMinMaxReactiveLimits()
                    .setMinQ(-9999.99)
                    .setMaxQ(9999.99)
                    .add();
        } else if (num == 2) {
            vl1.newLoad()
                    .setId("LOAD")
                    .setBus(busId)
                    .setConnectableBus(busId)
                    .setP0(600.0)
                    .setQ0(200.0)
                    .add();

            // Add an extension on an inner element
            Load load = network.getLoad("LOAD");
            TerminalMockExt terminalMockExt = new TerminalMockExt(load);
            load.addExtension(TerminalMockExt.class, terminalMockExt);
        }
        return network;
    }
}
