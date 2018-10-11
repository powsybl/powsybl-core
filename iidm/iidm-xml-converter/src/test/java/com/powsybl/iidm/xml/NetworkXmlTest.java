/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.BusbarSectionExt;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlTest extends AbstractConverterTest {

    static Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2013-01-15T18:45:00+01:00"));
        return network;
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(createEurostagTutorialExample1(),
                         NetworkXml::writeAndValidate,
                         NetworkXml::read,
                         "/eurostag-tutorial-example1.xml");
    }

    @Test
    public void testValidationIssueWithProperties() throws Exception {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").getProperties().setProperty("test", "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkXml.writeAndValidate(network, xmlFile);
    }

    @Test
    public void testGzipGunzip() throws IOException {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkXml.write(network, file1);
        Network network2 = NetworkXml.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, file2);
        assertArrayEquals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }


    @AutoService(ExtensionXmlSerializer.class)
    public static class BusbarSectionExtXmlSerializer implements ExtensionXmlSerializer<BusbarSection, BusbarSectionExt> {

        @Override
        public String getExtensionName() {
            return "busbarSectionExt";
        }

        @Override
        public String getCategoryName() {
            return "network";
        }

        @Override
        public Class<? super BusbarSectionExt> getExtensionClass() {
            return BusbarSectionExt.class;
        }

        @Override
        public boolean hasSubElements() {
            return false;
        }

        @Override
        public InputStream getXsdAsStream() {
            return getClass().getResourceAsStream("/xsd/busbarSectionExt.xsd");
        }

        @Override
        public String getNamespaceUri() {
            return "http://www.itesla_project.eu/schema/iidm/ext/busbarSectionExt/1_0";
        }

        @Override
        public String getNamespacePrefix() {
            return "bbse";
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
    public void busBreakerExtensions() throws IOException {
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

}
