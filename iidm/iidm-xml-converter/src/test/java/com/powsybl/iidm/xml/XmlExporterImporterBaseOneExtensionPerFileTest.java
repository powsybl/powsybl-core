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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterImporterBaseOneExtensionPerFileTest extends AbstractConverterTest {

    public void exporteImportOneFilePerExtensionType(Network network, String xiidmBaseRef) throws IOException {
        List<String> extensions = Arrays.asList("loadFoo", "loadBar");
        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, extensions);

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter(platformConfig).export(network, exportProperties, dataSource);

        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(xiidmBaseRef), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadBar.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/multiple-extensions-loadBar.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadFoo.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/multiple-extensions-loadFoo.xiidm"), is);
        }

        Properties importProperties = new Properties();
        importProperties.put(XMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        importProperties.put(XMLImporter.EXTENSIONS_LIST, extensions);
        XMLImporter importer = new XMLImporter(platformConfig);

        Network n = importer.importData(dataSource, importProperties);
        assertNotNull(n);
        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());
        assertEquals(2, n.getLoad("LOAD").getExtensions().size());
        assertEquals(1, n.getLoad("LOAD2").getExtensions().size());
        assertEquals(network.getIdentifiables().size(), n.getIdentifiables().size());
        assertEquals(network.getSubstationCount(), n.getSubstationCount());
        assertEquals(network.getVoltageLevelCount(), n.getVoltageLevelCount());
    }

    @Test
    public void test() throws IOException {
        exporteImportOneFilePerExtensionType(MultipleExtensionsTestNetworkFactory.create(),
                "/multiple-extensions.xiidm");
    }
}
