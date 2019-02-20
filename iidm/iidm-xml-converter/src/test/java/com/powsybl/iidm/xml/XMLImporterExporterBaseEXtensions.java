/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */
public class XMLImporterExporterBaseEXtensions extends AbstractConverterTest {
    public void importExport(String xiidmBaseRef, String xiidmExtRef) throws IOException {
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.SEPARATE_BASE_EXTENSIONS, "true");

        XMLImporter importer;
        PlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        importer = new XMLImporter(platformConfig);

        List<String> extensionsList = Arrays.asList("multiple-extensions-ext");
        Properties options = new Properties();

        ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions-base", new ResourceSet("/", xiidmBaseRef.substring(1, xiidmBaseRef.length()), xiidmExtRef.substring(1, xiidmExtRef.length())));
        Network network = importer.importData(dataSourceBase, options, extensionsList);

        MemDataSource dataSource = new MemDataSource();
        new XMLExporter().export(network, properties, dataSource);
        // check the base exported file and compare it to iidmBaseRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(xiidmBaseRef), is);
        }
        // check the exported extensions file and compare it to xiidmExtRef reference file
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("ext", "xiidm"))) {
            compareXml(getClass().getResourceAsStream(xiidmExtRef), is);
        }
    }

    @Test
    public void test() throws IOException {
        importExport("/multiple-extensions-base.xiidm",
                "/multiple-extensions-ext.xiidm");
    }
}
