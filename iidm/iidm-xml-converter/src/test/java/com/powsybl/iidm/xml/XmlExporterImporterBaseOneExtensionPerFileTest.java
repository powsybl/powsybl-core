/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import com.powsybl.iidm.network.test.TerminalMockExt;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.IIDM_CURRENT_VERSION_DIR_NAME;
import static com.powsybl.iidm.xml.AbstractXmlConverterTest.IIDM_VERSION_1_0_DIR_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterImporterBaseOneExtensionPerFileTest extends AbstractConverterTest {

    private static MemDataSource exportOneFilePerExtensionType(Network network, List<String> extensions) {
        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, extensions);

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, exportProperties, dataSource);

        return dataSource;
    }

    private static Network importOneFilePerExtensionType(ReadOnlyDataSource dataSource, List<String> extensions) {
        Properties importProperties = new Properties();
        importProperties.put(XMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE));
        importProperties.put(XMLImporter.EXTENSIONS_LIST, extensions);
        return new XMLImporter().importData(dataSource, importProperties);
    }

    private static void testImportMultipleExtensions(Network network, ReadOnlyDataSource dataSource, List<String> extensions) {
        Network n = importOneFilePerExtensionType(dataSource, extensions);

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
    public void testMultipleExtensions() throws IOException {
        Network network = MultipleExtensionsTestNetworkFactory.create();
        List<String> extensions = Arrays.asList("loadFoo", "loadBar");
        MemDataSource dataSource = exportOneFilePerExtensionType(network, extensions);

        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(IIDM_CURRENT_VERSION_DIR_NAME + "multiple-extensions.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadBar.xiidm"))) {
            compareXml(getClass().getResourceAsStream(IIDM_CURRENT_VERSION_DIR_NAME + "multiple-extensions-loadBar.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-loadFoo.xiidm"))) {
            compareXml(getClass().getResourceAsStream(IIDM_CURRENT_VERSION_DIR_NAME + "multiple-extensions-loadFoo.xiidm"), is);
        }

        testImportMultipleExtensions(network, dataSource, extensions);

        //backward compatibility 1.0
        ResourceDataSource dataSourceV1_0 = new ResourceDataSource("multiple-extensions",
                new ResourceSet(IIDM_VERSION_1_0_DIR_NAME,
                        "multiple-extensions.xiidm", "multiple-extensions-loadBar.xiidm", "multiple-extensions-loadFoo.xiidm"));
        testImportMultipleExtensions(network, dataSourceV1_0, extensions);
    }

    @Test
    public void testTerminalMockExtension() {
        MemDataSource dataSource = exportOneFilePerExtensionType(EurostagTutorialExample1Factory.createWithTerminalMockExt(), Collections.singletonList("terminalMock"));
        Network network2 = importOneFilePerExtensionType(dataSource, Collections.singletonList("terminalMock"));

        Load load2 = network2.getLoad("LOAD");
        TerminalMockExt terminalMockExt2 = load2.getExtension(TerminalMockExt.class);
        assertNotNull(terminalMockExt2);
        assertSame(load2.getTerminal(), terminalMockExt2.getTerminal());
    }
}
