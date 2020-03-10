/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.multi.xml;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class MultiXMLImporterTest extends AbstractXmlConverterTest {

    private MultiXMLImporter importer;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        importer = new MultiXMLImporter();
    }

    @Test
    public void importDataFromTwoFiles() {
        Properties parameters = new Properties();
        parameters.put(MultiXMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

        ReadOnlyDataSource dataSource = new ResourceDataSource("multiple-extensions", new ResourceSet(getVersionDir(CURRENT_IIDM_XML_VERSION), "multiple-extensions.xiidm", "multiple-extensions-ext.xiidm"));
        Network network = importer.importData(dataSource, parameters);
        assertNotNull(network);
        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());
    }

    @Test
    public void importDataFromMultipleFilesTest1() {
        List<String> extensionsList = Arrays.asList("loadFoo", "loadBar");

        Properties parameters = new Properties();

        parameters.put(MultiXMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        parameters.put(MultiXMLImporter.EXTENSIONS_LIST, extensionsList);

        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet(getVersionDir(CURRENT_IIDM_XML_VERSION), "multiple-extensions.xiidm", "multiple-extensions-loadFoo.xiidm", "multiple-extensions-loadBar.xiidm"));
        Network network = importer.importData(dataSourceBase, parameters);
        assertNotNull(network);
        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());
    }

    @Test
    public void importDataFromMultipleFilesTest2() {
        List<String> extensionsList = Collections.singletonList("loadFoo");

        Properties parameters = new Properties();
        parameters.put(MultiXMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        parameters.put(MultiXMLImporter.EXTENSIONS_LIST, extensionsList);

        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet(getVersionDir(CURRENT_IIDM_XML_VERSION), "multiple-extensions.xiidm", "multiple-extensions-loadFoo.xiidm"));
        Network network = importer.importData(dataSourceBase, parameters);
        assertNotNull(network);
        assertEquals(1, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());
    }

    @Test
    public void importDataFromMultipleFilesTest3() {
        Properties parameters = new Properties();
        parameters.put(MultiXMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));

        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet(getVersionDir(CURRENT_IIDM_XML_VERSION), "multiple-extensions.xiidm", "multiple-extensions-loadFoo.xiidm"));
        Network network = importer.importData(dataSourceBase, parameters);
        assertNotNull(network);
        assertEquals(1, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());
    }

    @Test
    public void importFromSingleFileTest() {
        List<String> extensionsList = Collections.emptyList();
        Network network = importFromSingleFile(extensionsList);
        assertEquals(0, network.getLoad("LOAD").getExtensions().size());
        assertEquals(0, network.getLoad("LOAD2").getExtensions().size());

        List<String> extensionsList1 = Collections.singletonList("loadBar");
        Network network1 = importFromSingleFile(extensionsList1);
        assertEquals(1, network1.getLoad("LOAD").getExtensions().size());
        assertEquals(0, network1.getLoad("LOAD2").getExtensions().size());

        List<String> extensionsList2 = Collections.singletonList("loadFoo");
        Network network2 = importFromSingleFile(extensionsList2);
        assertEquals(1, network2.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network2.getLoad("LOAD2").getExtensions().size());
    }

    private Network importFromSingleFile(List<String> extensionsList) {
        Properties parameters = new Properties();
        parameters.put(MultiXMLImporter.EXTENSIONS_LIST, extensionsList);
        parameters.put(MultiXMLImporter.IMPORT_MODE, IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE.toString());

        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet(getVersionDir(CURRENT_IIDM_XML_VERSION), "multiple-extensions.xml"));
        Network network = importer.importData(dataSourceBase, parameters);
        assertNotNull(network);
        return network;
    }
}
