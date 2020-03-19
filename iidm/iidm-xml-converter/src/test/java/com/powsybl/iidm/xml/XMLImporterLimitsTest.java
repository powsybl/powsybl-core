/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.MultipleExtensionsTestNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */
public class XMLImporterLimitsTest extends AbstractXmlConverterTest {

    private void importExport(Network network) throws IOException {

        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

        //test with no extensions so the base-ext.ext will not be created
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, Collections.emptyList());

        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, exportProperties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getVersionedNetworkAsStream("multiple-extensions.xiidm", CURRENT_IIDM_XML_VERSION), is);
        }

        XMLImporter importer;
        importer = new XMLImporter();

        Properties importProperties = new Properties();
        importProperties.put(XMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));
        Network network1 = importer.importData(dataSource, importProperties);

        assertEquals(0, network1.getLoad("LOAD").getExtensions().size());
        assertEquals(0, network1.getLoad("LOAD2").getExtensions().size());
    }

    @Test
    public void test() throws IOException {
        importExport(MultipleExtensionsTestNetworkFactory.create());

        //backward compatibility 1.0
        importExport(NetworkXml.read(getVersionedNetworkAsStream("multiple-extensions.xml", IidmXmlVersion.V_1_0)));
    }
}
