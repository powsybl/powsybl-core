/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.BusbarSectionExt;
import com.powsybl.iidm.network.test.ScadaNetworkFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkXmlTest extends AbstractXmlConverterTest {

    static Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00+01:00"));
        return network;
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripXmlTest(createEurostagTutorialExample1(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("eurostag-tutorial-example1.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1.xml");
    }

    @Test
    void testValidationIssueWithProperties() {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").setProperty("test", "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkXml.writeAndValidate(network, xmlFile);
        Network readNetwork = NetworkXml.read(xmlFile);
        assertEquals("foo", readNetwork.getGenerator("GEN").getProperty("test"));
    }

    @Test
    void testGzipGunzip() throws IOException {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkXml.write(network, file1);
        Network network2 = NetworkXml.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, file2);
        assertArrayEquals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }

    @AutoService(ExtensionXmlSerializer.class)
    public static class BusbarSectionExtXmlSerializer extends AbstractExtensionXmlSerializer<BusbarSection, BusbarSectionExt> {

        public BusbarSectionExtXmlSerializer() {
            super("busbarSectionExt", "network", BusbarSectionExt.class, false, "busbarSectionExt.xsd",
                    "http://www.itesla_project.eu/schema/iidm/ext/busbarSectionExt/1_0", "bbse");
        }

        @Override
        public void write(BusbarSectionExt busbarSectionExt, XmlWriterContext context) throws XMLStreamException {
        }

        @Override
        public BusbarSectionExt read(BusbarSection busbarSection, XmlReaderContext context) {
            return new BusbarSectionExt(busbarSection);
        }
    }

    private static Network writeAndRead(Network network, ExportOptions options) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkXml.write(network, options, os);

            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                return NetworkXml.read(is);
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
    void testOptionalSubstation() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2021-08-20T12:02:48.504+02:00"));
        String vlId = "ADDITIONAL_VL";
        VoltageLevel vl = network.newVoltageLevel()
                .setId(vlId)
                .setNominalV(200)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setHighVoltageLimit(280)
                .setLowVoltageLimit(160)
                .add();
        String bus1Id = "ADDITIONAL_BUS1";
        String bus2Id = "ADDITIONAL_BUS2";
        String bus3Id = "ADDITIONAL_BUS3";
        vl.getBusBreakerView().newBus().setId(bus1Id).add();
        vl.getBusBreakerView().newBus().setId(bus2Id).add();
        vl.getBusBreakerView().newBus().setId(bus3Id).add();
        network.newTwoWindingsTransformer()
                .setId("ADDITIONAL_T2WT")
                .setVoltageLevel1(vlId)
                .setBus1(bus1Id)
                .setRatedU1(220.0)
                .setVoltageLevel2(vlId)
                .setBus2(bus2Id)
                .setRatedU2(158.0)
                .setR(0.21)
                .setX(Math.sqrt(18 * 18 - 0.21 * 0.21))
                .setG(0.0)
                .setB(0.0)
                .add();
        network.newThreeWindingsTransformer()
                .setId("ADDITIONAL_T3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .setVoltageLevel(vlId)
                .setBus(bus1Id)
                .add()
                .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(33.0)
                .setVoltageLevel(vlId)
                .setBus(bus2Id)
                .add()
                .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(11.0)
                .setVoltageLevel(vlId)
                .setBus(bus3Id)
                .add()
                .add();

        roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("eurostag-tutorial-example1-opt-sub.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("eurostag-tutorial-example1-opt-sub.xml", IidmXmlVersion.V_1_6);
    }

    @Test
    void testScada() throws IOException {
        Network network = ScadaNetworkFactory.create();
        assertEquals(ValidationLevel.EQUIPMENT, network.runValidationChecks(false));
        roundTripTest(network,
                NetworkXml::write,
                NetworkXml::read,
                getVersionedNetworkPath("scadaNetwork.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("scadaNetwork.xml", IidmXmlVersion.V_1_7);
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
}
