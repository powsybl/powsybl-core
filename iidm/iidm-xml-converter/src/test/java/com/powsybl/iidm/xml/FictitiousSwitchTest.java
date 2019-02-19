/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class FictitiousSwitchTest extends AbstractNetworkXmlTest {

    private static final Network NETWORK = FictitiousSwitchFactory.create();
    private static final String REF = "/fictitiousSwitchRef.xml";

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                REF);
    }

    @Test
    public void testReadImmutable() {
        writeToXmlTest(new ImmutableNetwork(NETWORK), REF);
    }

}
