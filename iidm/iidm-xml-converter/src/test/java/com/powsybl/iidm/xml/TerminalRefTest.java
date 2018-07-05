/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class TerminalRefTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        String filename = "/terminalRef.xiidm";

        Network network = Importers.loadNetwork(filename, getClass().getResourceAsStream(filename));
        assertNotNull(network);
        roundTripXmlTest(network, NetworkXml::writeAndValidate, NetworkXml::read, filename);
    }
}
