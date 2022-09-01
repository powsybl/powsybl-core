/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.network.Exporters;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XMLExporterTest extends AbstractXmlConverterTest {
    private FileSystem fileSystem;

    public void exporterTest(Network network, IidmXmlVersion version, String xmlFileName, Properties properties) throws IOException {
        properties.put(XMLExporter.ANONYMISED, "false");
        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);
        // check the exported file and compare it to iidm reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData(null, "xiidm"))) {
            compareXml(getVersionedNetworkAsStream(xmlFileName, version), is);
        }
    }

    @Test
    public void exportTest() throws IOException {
        exporterTest(MultipleExtensionsTestNetworkFactory.create(), CURRENT_IIDM_XML_VERSION, "multiple-extensions.xml", new Properties());
    }

    @Test
    public void paramsTest() {
        var xmlExporter = new XMLExporter();
        assertEquals(10, xmlExporter.getParameters().size());
        assertEquals("IIDM XML v" + CURRENT_IIDM_XML_VERSION.toString(".") + " exporter", xmlExporter.getComment());
    }

    @Test
    public void exportOldVersionWithoutNewExtensions() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path workingDir = fileSystem.getPath("/working-dir");
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));
        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);
        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);
        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();

        Properties params = new Properties();
        params.setProperty(XMLExporter.VERSION, IidmXmlVersion.V_1_2.toString("."));
        params.setProperty(XMLExporter.THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND, "true");
        assertThrows(PowsyblException.class, () -> Exporters.export("XIIDM", network, params,
                workingDir));
        try {
            Exporters.export("XIIDM", network, params, workingDir);
            fail();
        } catch (PowsyblException exception) {
            assertEquals("Version V_1_2 does not support slackTerminal extension", exception.getMessage());
        }
    }

    @Test
    public void testWriteXml() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2019-05-27T12:17:02.504+02:00"));
        String voltageLevelId = "VLHV2";
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        assertNotNull(vl);
        String busId = "NHV2";
        Bus bus = vl.getBusBreakerView().getBus(busId);
        assertNotNull(bus);
        Terminal t = bus.getConnectedTerminals().iterator().next();
        assertNotNull(t);
        vl.newExtension(SlackTerminalAdder.class).withTerminal(t).add();
        Properties params = new Properties();
        params.setProperty(XMLExporter.VERSION, IidmXmlVersion.V_1_2.toString("."));
        exporterTest(network, IidmXmlVersion.V_1_2, "extensionTooRecentExportTest.xml", params);
    }
}
