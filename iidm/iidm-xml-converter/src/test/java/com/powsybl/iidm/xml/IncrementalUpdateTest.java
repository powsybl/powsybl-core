/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import org.junit.Test;

import java.io.IOException;


/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalUpdateTest extends AbstractConverterTest {

    @Test
    public void updateNetworkTest() throws IOException {
        //load networks
        /*Network network = Importers.loadNetwork("/home/benhamedcha/eurostag.xiidm");
        Network networkLf = Importers.loadNetwork("/home/benhamedcha/eurostag-lf.xiidm");
        assertNotEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ());

        //ReadOnlyDataSource dataSource = new ResourceDataSource("Incremental-STATE", new ResourceSet("/", "Incremental-STATE.xiidm"));
        FileDataSource dataSource = new FileDataSource(Paths.get("/home/benhamedcha"), "test");

        NetworkXml.update(network, new ImportOptions().setControl(true).setTopo(false), dataSource);
        new XMLExporter().export(network, new Properties(), dataSource);

        assertEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ(), 0);
        */
    }
}
