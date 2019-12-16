/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionDir;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterBaseExtensionsTest extends AbstractConverterTest {

    private void exporterTestBaseExtensions(Network network) throws IOException {
        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.ANONYMISED, "false");
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, exportProperties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream(getVersionDir(CURRENT_IIDM_XML_VERSION) + "multiple-extensions.xiidm"), is);
        }
        // check the exported extensions file and compare it to xiidmExtRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-ext", "xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream(getVersionDir(CURRENT_IIDM_XML_VERSION) + "multiple-extensions-ext.xiidm"), is);
        }
    }

    @Test
    public void exportBaseExtensions() throws IOException {
        exporterTestBaseExtensions(NetworkXml.read(getClass().getResourceAsStream(getVersionDir(IidmXmlVersion.V_1_0) + "multiple-extensions.xml")));
        exporterTestBaseExtensions(MultipleExtensionsTestNetworkFactory.create());
    }

    @Test
    public void validationTest() throws IOException {
        Path path = tmpDir.resolve("base.xiidm");
        IidmImportExportMode mode = IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE;
        Network network = MultipleExtensionsTestNetworkFactory.create();
        NetworkXml.writeAndValidate(network, new ExportOptions().setMode(mode), path);
        Network network1 = NetworkXml.validateAndRead(path, new ImportOptions().setMode(mode));
        assertEquals(network.getExtensions().size(), network1.getExtensions().size());
        assertEquals(network.getIdentifiables().size(), network1.getIdentifiables().size());
    }
}
