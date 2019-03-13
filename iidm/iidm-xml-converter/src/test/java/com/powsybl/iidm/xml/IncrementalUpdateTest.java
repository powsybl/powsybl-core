/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalUpdateTest extends AbstractConverterTest {

    @Test
    public void updateNetworkTest() throws IOException {
        //load networks
        Network network = Importers.loadNetwork("/home/benhamedcha/eurostag.xiidm");
        Network networkLf = Importers.loadNetwork("/home/benhamedcha/eurostag-lf.xiidm");
        assertNotEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ());

        ReadOnlyDataSource dataSource = new ResourceDataSource("sim1-STATE", new ResourceSet("/", "sim1-STATE.xiidm"));
        NetworkXml.update(network, new ImportOptions().setControl(false).setTopo(false), dataSource);
        assertEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ(), 0);

    }
}
