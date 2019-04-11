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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;


/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */
public class XMLImporterLimitsTest extends AbstractConverterTest {
    @Test
    public void importExport() throws IOException {

        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));

        //test with no extensions so the base-ext.ext will not be created
        List<String> exportExtensionsList = new ArrayList<>();
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, exportExtensionsList);

        Network network = MultipleExtensionsTestNetworkFactory.create();
        assertEquals(2, network.getLoad("LOAD").getExtensions().size());
        assertEquals(1, network.getLoad("LOAD2").getExtensions().size());

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter(platformConfig).export(network, exportProperties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getClass().getResourceAsStream("/multiple-extensions.xiidm"), is);
        }

        XMLImporter importer;
        importer = new XMLImporter(platformConfig);

        Properties importProperties = new Properties();
        importProperties.put(XMLImporter.IMPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));
        Network network1 = importer.importData(dataSource, importProperties);

        assertEquals(0, network1.getLoad("LOAD").getExtensions().size());
        assertEquals(0, network1.getLoad("LOAD2").getExtensions().size());
    }
}
