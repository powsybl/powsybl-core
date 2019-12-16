/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReactiveLimitsXmlTest extends AbstractXmlConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripVersionnedXmlTest("reactiveLimitsRoundTripRef.xml", IidmXmlVersion.V_1_0);

        roundTripXmlTest(ReactiveLimitsTestNetworkFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "reactiveLimitsRoundTripRef.xml");
    }
}
