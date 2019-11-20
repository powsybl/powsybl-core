/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class BatteryXmlTest extends AbstractXmlConverterTest {

    @Test
    public void batteryRoundTripTest() throws IOException {
        roundTripVersionnedXmlTest("batteryRoundTripRef.xml", "V1_0");

        roundTripXmlTest(BatteryNetworkFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/V1_1/batteryRoundTripRef.xml");
    }
}
