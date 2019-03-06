/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalExportTest extends AbstractConverterTest {

    public void exporterTest(Network network) throws IOException {
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        FileDataSource dataSource = new FileDataSource(Paths.get("/home/benhamedcha/"), "base");
        new XMLExporter().export(network, properties, dataSource);
    }

    @Test
    public void exportTest() throws IOException {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        //we want a network without extensions
        List<String> extensionsList = Arrays.asList();
        Properties parameters = new Properties();
        parameters.put(XMLImporter.EXTENSIONS_LIST, extensionsList);
        //ReadOnlyDataSource dataSourceBase = new ResourceDataSource("multiple-extensions", new ResourceSet("/", "multiple-extensions.xiidm", "test-loadFoo.xiidm"));
        ReadOnlyDataSource dataSource = new ResourceDataSource("lilleNodeBreaker", new ResourceSet("/", "lilleNodeBreaker.xiidm"));
        Network network = importer.importData(dataSource, parameters);
        exporterTest(network);
    }
}
