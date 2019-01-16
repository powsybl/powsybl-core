/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import com.powsybl.iidm.network.util.ImmutableNetwork;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class LineXmlTest extends AbstractNetworkXmlTest {

    private static final String TIELINE_REF = "/tieLineRef.xml";
    private static final Network TIELINE_NETWORK = NoEquipmentNetworkFactory.createWithTieLine();

    private static final String DANGLINGLINE_REF = "/danglingLineRef.xml";
    private static final Network DANGLINELINE_NETWORK = NoEquipmentNetworkFactory.createWithDanglingLine();

    @Test
    public void tieLine() throws IOException {
        roundTripXmlTest(TIELINE_NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read, TIELINE_REF);
    }

    @Test
    public void readImmutableTieline() {
        writeToXmlTest(ImmutableNetwork.of(TIELINE_NETWORK), TIELINE_REF);
    }

    @Test
    public void danglingLine() throws IOException {
        roundTripXmlTest(DANGLINELINE_NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read, DANGLINGLINE_REF);
    }

    @Test
    public void readImmutableDanglineLine() {
        writeToXmlTest(ImmutableNetwork.of(DANGLINELINE_NETWORK), DANGLINGLINE_REF);
    }
}
