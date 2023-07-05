/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class NodeBreakerPropertiesOnBusTest extends AbstractXmlConverterTest {
    @Test
    void testPropertiesOnBus() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(DateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.getVoltageLevel("C").getBusView().getBus("C_0").setProperty("key_test", "value_test");
        roundTripTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::validateAndRead,
                getVersionedNetworkPath("nodebreaker-busproperties.xml", CURRENT_IIDM_XML_VERSION));
    }
}
