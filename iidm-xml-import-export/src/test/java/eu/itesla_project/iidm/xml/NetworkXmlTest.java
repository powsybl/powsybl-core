/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.xml;

import com.google.common.io.ByteStreams;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import java.io.IOException;
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
    
    private void assertXMLEquals(String expectedXML, String actualXML) throws Exception {
    	
    	Source control = Input.fromString(expectedXML).build();
    	Source test = Input.fromString(actualXML).build();
    	Diff myDiff=DiffBuilder.compare(control).withTest(test).ignoreWhitespace().build();
    	assertFalse( myDiff.hasDifferences());
    }

    private Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        DateTime date = DateTime.parse("2013-01-15T18:45:00+01:00");
        network.setCaseDate(date);
        network.setForecastDistance(0);
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
        assertXMLEquals(new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8),
                    new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/eurostag-tutorial-example1.xml")), StandardCharsets.UTF_8));
        NetworkXml.validate(xmlFile);
    }
    
   

    @Test
    public void testValidationIssueWithProperties() throws Exception {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").getProperties().setProperty("test", "foo");
        Path xmlFile = writeNetwork(network);
        NetworkXml.validate(xmlFile);
    }

    @Test
    public void testEurostagTutorialExample1Read() throws Exception {
        Network network = createEurostagTutorialExample1();
        Path xmlFile = writeNetwork(network);
        Network network2 = NetworkXml.read(xmlFile);
        Path xmlFile2 = tmpDir.resolve("n2.xml");
        NetworkXml.write(network2, xmlFile2);
        assertEquals(new String(Files.readAllBytes(xmlFile), StandardCharsets.UTF_8),
                     new String(Files.readAllBytes(xmlFile2), StandardCharsets.UTF_8));
    }

}