/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class HvdcXmlTest extends AbstractXmlConverterTest {

    @Test
    void roundTripLccTest() throws IOException {
        roundTripXmlTest(HvdcTestNetwork.createLcc(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("LccRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("LccRoundTripRef.xml");
    }

    @Test
    void roundTripVscTest() throws IOException {
        roundTripXmlTest(HvdcTestNetwork.createVsc(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("VscRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("VscRoundTripRef.xml");
    }
}
