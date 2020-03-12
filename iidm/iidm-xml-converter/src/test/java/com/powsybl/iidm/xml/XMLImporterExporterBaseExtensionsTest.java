/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */
public class XMLImporterExporterBaseExtensionsTest extends AbstractXmlConverterTest {

    private void importExport(String directory) throws IOException {
        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));
        List<String> exportExtensionsList = Arrays.asList("loadFoo", "loadBar");
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, exportExtensionsList);

        XMLImporter importer = new XMLImporter();

        Properties importProperties = new Properties();
        importProperties.put(XMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));
        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet(directory, "multiple-extensions.xiidm", "multiple-extensions-ext.xiidm"));
        Network network = importer.importData(dataSourceBase, importProperties);

        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, exportProperties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("multiple-extensions.xiidm", CURRENT_IIDM_XML_VERSION), is);
        }
        // check the exported extensions file and compare it to "multiple-extensions-ext.xiidm" reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-ext", "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("multiple-extensions-ext.xiidm", CURRENT_IIDM_XML_VERSION), is);
        }
    }

    @Test
    public void importExportBaseExtensions() throws IOException {
        importExport(getVersionDir(IidmXmlVersion.V_1_0));
        importExport(getVersionDir(CURRENT_IIDM_XML_VERSION));
    }
}
