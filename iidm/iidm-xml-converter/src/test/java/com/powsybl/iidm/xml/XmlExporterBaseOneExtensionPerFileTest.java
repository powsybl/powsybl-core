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

import static org.junit.Assert.*;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterBaseOneExtensionPerFileTest extends AbstractConverterTest {

    public void exporterOneFilePerExtensionType(Network network, String xiidmBaseRef, List<String> extensionsList) throws IOException {

        Properties properties = new Properties();
        properties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        properties.put(XMLExporter.EXTENSIONS_LIST, extensionsList);

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter(platformConfig).export(network, properties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream(xiidmBaseRef), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadBar.xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream("/multiple-extensions-loadBar.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadFoo.xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream("/multiple-extensions-loadFoo.xiidm"), is);
        }
    }

    @Test(expected = NullPointerException.class)
    public void exportOneExtensionTypeTest() throws IOException {
        List<String> extensionsList = Arrays.asList("loadBar");
        exporterOneFilePerExtensionType(MultipleExtensionsTestNetworkFactory.create(), "/multiple-extensions.xiidm", extensionsList);
    }

    @Test
    public void getIdentifiablesPerExtensionTypeTest() {
        Network n = MultipleExtensionsTestNetworkFactory.create();
        Map<String, Set<String>> m = NetworkXml.getIdentifiablesPerExtensionType(n);
        assertEquals(2, m.size());
        assertEquals("[loadFoo=[LOAD, LOAD2], loadBar=[LOAD]]", m.entrySet().toString());
    }

    @Test
    public void exportAllExtensionsTest() throws IOException {
        List<String> extensionsList = Arrays.asList("loadFoo", "loadBar");
        exporterOneFilePerExtensionType(MultipleExtensionsTestNetworkFactory.create(), "/multiple-extensions.xiidm", extensionsList);
    }
}
