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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.iidm.network.test.TieLineNetworkFactory;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalExportTest extends AbstractConverterTest {
    private Network getEurostagLfNetwork() {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource dataSource = new ResourceDataSource("eurostag-tutorial1-lf", new ResourceSet("/", "eurostag-tutorial1-lf.xml"));
        return importer.importData(dataSource, new Properties());
    }

    private void incrementalExport(Network network, String prefix) throws IOException {
        Properties properties;
        properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.INCREMENTAL_CONVERSION, true);
        MemDataSource dataSource = new MemDataSource();

        new XMLExporter().export(network, properties, dataSource);

        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-CONTROL.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-CONTROL.xiidm"), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-STATE.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-STATE.xiidm"), is);
        }
        try (InputStream is = new ByteArrayInputStream(dataSource.getData("-TOPO.xiidm"))) {
            compareXml(getClass().getResourceAsStream("/" + prefix + "-TOPO.xiidm"), is);
        }
    }

    @Test
    public void exportNetworkWithLoadFlow() throws IOException {
        Network network = getEurostagLfNetwork();
        assertNotNull(network);
        incrementalExport(network, "Incremental");
    }

    @Test
    public void incrementalExportLcc() throws IOException {
        Network networkLcc = HvdcTestNetwork.createLcc();
        assertNotNull(networkLcc);
        incrementalExport(networkLcc, "lcc");
    }

    @Test
    public void incrementalExportVsc() throws IOException {
        Network networkVsc = HvdcTestNetwork.createVsc();
        assertNotNull(networkVsc);
        incrementalExport(networkVsc, "vsc");
    }

    @Test
    public void incrementalExportThreeWindingTransformer() throws IOException {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertNotNull(network);
        incrementalExport(network, "twt");
    }

    @Test
    public void incrementalExportTieLine() throws IOException {
        Network network = TieLineNetworkFactory.create();
        assertNotNull(network);
        incrementalExport(network, "tl");
    }

    @Test
    public void incrementalExportBattery() throws IOException {
        Network network = BatteryNetworkFactory.create();
        assertNotNull(network);
        incrementalExport(network, "batterie");
    }
}
