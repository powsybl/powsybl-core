/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcXmlTest extends AbstractNetworkXmlTest {

    private static final Network LCC_NETWORK = HvdcTestNetwork.createLcc();
    private static final String LCC_REF = "/LccRoundTripRef.xml";

    private static final Network VSC_NETWORK = HvdcTestNetwork.createVsc();
    private static final String VSC_REF = "/VscRoundTripRef.xml";

    @Test
    public void roundTripLccTest() throws IOException {
        roundTripXmlTest(LCC_NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                LCC_REF);
    }

    @Test
    public void readImmutableLcc() {
        writeToXmlTest(ImmutableNetwork.of(LCC_NETWORK), LCC_REF);
    }

    @Test
    public void roundTripVscTest() throws IOException {
        roundTripXmlTest(VSC_NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                VSC_REF);
    }

    @Test
    public void readImmutableVsc() {
        writeToXmlTest(ImmutableNetwork.of(VSC_NETWORK), VSC_REF);
    }
}
