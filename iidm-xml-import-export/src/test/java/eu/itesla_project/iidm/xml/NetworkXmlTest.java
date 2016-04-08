/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.common.io.ByteStreams;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlTest {

    private FileSystem fileSystem;
    private Path tmpDir;

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        tmpDir = fileSystem.getPath("tmp");
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private void validateXsd(Path xmlFile) throws SAXException, IOException {
        try (InputStream is = Files.newInputStream(xmlFile)) {
            validateXsd(is);
        }
    }

    private void validateXsd(InputStream is) throws SAXException, IOException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream("/xsd/iidm.xsd")));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(is));
    }

    private Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        DateTime date = DateTime.parse("2013-01-15T18:45:00+01:00");
        network.setDate(date);
        for (VoltageLevel vl : network.getVoltageLevels()) {
            vl.setDate(date);
        }
        return network;
    }

    private Path writeNetwork(Network network) throws XMLStreamException, IOException {
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkXml.write(network, xmlFile);
        return xmlFile;
    }

    @Test
    public void testEurostagTutorialExample1Write() throws Exception {
        Network network = createEurostagTutorialExample1();
        Path xmlFile = writeNetwork(network);
        assertTrue(new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8)
                .equals(new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/eurostag-tutorial-example1.xml")), StandardCharsets.UTF_8)));
        validateXsd(xmlFile);
    }

    @Test
    public void testValidationIssueWithProperties() throws Exception {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").getProperties().setProperty("test", "foo");
        Path xmlFile = writeNetwork(network);
        validateXsd(xmlFile);
    }

    @Test
    public void testEurostagTutorialExample1Read() throws Exception {
        Network network = createEurostagTutorialExample1();
        Path xmlFile = writeNetwork(network);
        Network network2 = NetworkXml.read(xmlFile);
        Path xmlFile2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, xmlFile2);
        assertTrue(new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8).equals(new String(Files.readAllBytes(xmlFile2), StandardCharsets.UTF_8)));
    }

}