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
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalUpdateTest extends AbstractConverterTest {

    private Network getEurostagLfNetwork() {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource dataSource = new ResourceDataSource("eurostag-tutorial1-lf", new ResourceSet("/", "eurostag-tutorial1-lf.xml"));
        Network network = importer.importData(dataSource, new Properties());
        return network;
    }

    @Test
    public void updateNetworkTest() throws IOException {
        //load networks
        Network network = EurostagTutorialExample1Factory.create();
        Network networkLf = getEurostagLfNetwork();
        assertNotEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ());

        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(networkLf, properties, dataSource);

        NetworkXml.update(network, new ImportOptions().setControl(true).setTopo(false), dataSource);
        assertEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ(), 0);
    }
}
