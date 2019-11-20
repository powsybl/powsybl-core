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
        roundTripXmlTest(BatteryNetworkFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                IIDM_CURRENT_VERSION_DIR_NAME + "batteryRoundTripRef.xml");
        
        //backward compatibility 1.0
        roundTripVersionnedXmlTest("batteryRoundTripRef.xml", IIDM_VERSION_1_0_DIR_NAME);
    }
}
