/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.immutable.ImmutableNetwork;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterXmlTest extends AbstractNetworkXmlTest {

    private static final String REF = "/phaseShifterRoundTripRef.xml";
    private static final Network NETWORK = PhaseShifterTestCaseFactory.create();

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(NETWORK,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                REF);
    }

    @Test
    public void testWriteToXml() {
        writeToXmlTest(ImmutableNetwork.of(NETWORK), REF);
    }
}
