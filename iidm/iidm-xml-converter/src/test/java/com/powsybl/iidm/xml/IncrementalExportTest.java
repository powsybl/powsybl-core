/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalExportTest extends AbstractConverterTest {

    private void exporterTest(Network network) throws IOException {
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));

        MemDataSource dataSource = new MemDataSource();
        //FileDataSource dataSource = new FileDataSource(Paths.get("/home/benhamedcha"), "test");
        new XMLExporter().export(network, properties, dataSource);

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-CONTROL.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/Incremental-CONTROL.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-STATE.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/Incremental-STATE.xiidm"), is);
        }

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-TOPO.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/Incremental-TOPO.xiidm"), is);
        }
    }

    private Network getEurostagLfNetwork() {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource dataSource = new ResourceDataSource("eurostag-tutorial1-lf", new ResourceSet("/", "eurostag-tutorial1-lf.xml"));
        Network network = importer.importData(dataSource, new Properties());
        return network;
    }

    @Test
    public void exportNetworkWithLoadFlow() throws IOException {
        exporterTest(getEurostagLfNetwork());
    }
}
