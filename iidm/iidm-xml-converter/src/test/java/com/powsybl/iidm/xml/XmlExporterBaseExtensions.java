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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;


/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class XmlExporterBaseExtensions  extends AbstractConverterTest {

    public void exporterTestBaseExtensions(Network network, String xiidmBaseRef, String xiidmExtRef) throws IOException {
        Properties exportProperties = new Properties();
        exportProperties.put(XMLExporter.ANONYMISED, "false");
        exportProperties.put(XMLExporter.EXPORT_MODE, String.valueOf(IidmImportExportMode.EXTENSIONS_IN_ONE_SEPARATED_FILE));
        List<String> exportExtensionsList = Arrays.asList("ALL");
        exportProperties.put(XMLExporter.EXTENSIONS_LIST, exportExtensionsList);

        MemDataSource dataSource = new MemDataSource();

        new XMLExporter().export(network, exportProperties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream(xiidmBaseRef), is);
        }
        // check the exported extensions file and compare it to xiidmExtRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-ext", "xiidm"))) {
            assertNotNull(is);
            compareXml(getClass().getResourceAsStream(xiidmExtRef), is);
        }
    }

    @Test
    public void exportBaseExtensions() throws IOException {
        exporterTestBaseExtensions(MultipleExtensionsTestNetworkFactory.create(),
                "/multiple-extensions.xiidm",
                "/multiple-extensions-ext.xiidm");
    }
}
