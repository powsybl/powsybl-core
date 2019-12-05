/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.IidmXmlVersion;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IdentifiableExtensionXmlSerializerTest extends AbstractXmlConverterTest {

    @Test
    public void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        LoadZipModel zipModel = new LoadZipModel(load, 1, 2, 3, 4, 5, 6, 380);
        load.addExtension(LoadZipModel.class, zipModel);
        byte[] buffer;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkXml.write(network, new ExportOptions(), os);
            buffer = os.toByteArray();
        }
        // try to validate the schema with extensions
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            NetworkXml.validateWithExtensions(is);
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            Network network2 = NetworkXml.read(is);
            LoadZipModel zipModel2 = network2.getLoad("LOAD").getExtension(LoadZipModel.class);
            assertNotNull(zipModel2);
            assertTrue(zipModel.getA1() == zipModel2.getA1()
                    && zipModel.getA2() == zipModel2.getA2()
                    && zipModel.getA3() == zipModel2.getA3()
                    && zipModel.getA4() == zipModel2.getA4()
                    && zipModel.getA5() == zipModel2.getA5()
                    && zipModel.getA6() == zipModel2.getA6()
                    && zipModel.getV0() == zipModel2.getV0()
            );
        }
    }

    @Test
    public void testMultipleExtensions() throws IOException {
        roundTripXmlTest(MultipleExtensionsTestNetworkFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "multiple-extensions.xml");

        // backward compatibility 1.0
        roundTripVersionnedXmlTest("multiple-extensions.xml", IidmXmlVersion.V_1_0);
    }

    // Define a network extension without XML serializer
    static class NetworkDummyExtension extends AbstractExtension<Network> {
        @Override
        public String getName() {
            return "networkDummy";
        }
    }

    @Test
    public void testExtensionWithoutSerializerThrowsException() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkDummyExtension source = new NetworkDummyExtension();
        network.addExtension(NetworkDummyExtension.class, source);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                NetworkXml.write(network, new ExportOptions(), os);
            } catch (PowsyblException x) {
                assertTrue(x.getMessage().contains("Provider not found for extension"));
            }
        }
    }

    @Test
    public void testExtensionWithoutSerializerDoNotThrowException() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkDummyExtension source = new NetworkDummyExtension();
        network.addExtension(NetworkDummyExtension.class, source);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            try {
                NetworkXml.write(network, new ExportOptions().setThrowExceptionIfExtensionNotFound(false), os);
            } catch (PowsyblException x) {
                assertTrue(x.getMessage().contains("Provider not found for extension"));
            }
        }
    }

    // Define a network extension with XML serializer
    static class NetworkSourceExtension extends AbstractExtension<Network> {

        NetworkSourceExtension(String sourceData) {
            this.sourceData = sourceData;
        }

        @Override
        public String getName() {
            return "networkSource";
        }

        String getSourceData() {
            return sourceData;
        }

        private final String sourceData;
    }

    @AutoService(ExtensionXmlSerializer.class)
    public static class NetworkSourceExtensionXmlSerializer implements ExtensionXmlSerializer<Network, NetworkSourceExtension> {

        @Override
        public String getExtensionName() {
            return "networkSource";
        }

        @Override
        public String getCategoryName() {
            return "network";
        }

        @Override
        public Class<? super NetworkSourceExtension> getExtensionClass() {
            return NetworkSourceExtension.class;
        }

        @Override
        public boolean hasSubElements() {
            return false;
        }

        @Override
        public InputStream getXsdAsStream() {
            return getClass().getResourceAsStream("/xsd/networkSource.xsd");
        }

        @Override
        public String getNamespaceUri(IidmXmlVersion version) {
            return "http://www.itesla_project.eu/schema/iidm/ext/networksource/1_0";
        }

        @Override
        public String getNamespacePrefix() {
            return "extNetworkSource";
        }

        @Override
        public void write(NetworkSourceExtension networkSource, XmlWriterContext context) throws XMLStreamException {
            context.getWriter().writeAttribute("sourceData", networkSource.getSourceData());
        }

        @Override
        public NetworkSourceExtension read(Network network, XmlReaderContext context) {
            String sourceData = context.getReader().getAttributeValue(null, "sourceData");
            return new NetworkSourceExtension(sourceData);
        }
    }

    @Test
    public void testNetworkSourceExtension() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        String sourceData = "eurostag-tutorial-example1-created-from-IIDM-API";
        NetworkSourceExtension source = new NetworkSourceExtension(sourceData);
        network.addExtension(NetworkSourceExtension.class, source);
        byte[] buffer;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkXml.write(network, new ExportOptions(), os);
            buffer = os.toByteArray();
        }
        // try to validate the schema with extensions
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            NetworkXml.validateWithExtensions(is);
        }
        try (ByteArrayInputStream is = new ByteArrayInputStream(buffer)) {
            Network network2 = NetworkXml.read(is);
            NetworkSourceExtension source2 = network2.getExtension(NetworkSourceExtension.class);
            assertNotNull(source2);
            assertEquals(sourceData, source2.getSourceData());
        }
    }

    @Test
    public void testTerminalExtension() throws IOException {
        Network network2 = roundTripXmlTest(EurostagTutorialExample1Factory.createWithTerminalMockExt(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "eurostag-tutorial-example1-with-terminalMock-ext.xml");
        Load loadXml = network2.getLoad("LOAD");
        TerminalMockExt terminalMockExtXml = loadXml.getExtension(TerminalMockExt.class);
        assertNotNull(terminalMockExtXml);
        assertSame(loadXml.getTerminal(), terminalMockExtXml.getTerminal());

        // backward compatibility 1.0
        roundTripVersionnedXmlTest("eurostag-tutorial-example1-with-terminalMock-ext.xml", IidmXmlVersion.V_1_0);
    }
}
