/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class NodeBreakerPropertiesOnBusTest extends AbstractIidmSerDeTest {
    @Test
    void testPropertiesOnBus() throws IOException {
        Network network = FictitiousSwitchFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.getVoltageLevel("C").getBusView().getBus("C_0").setProperty("key_test", "value_test");

        // can export and reload a network in current and older XIIDM versions
        for (IidmVersion version : IidmVersion.values()) {
            allFormatsRoundTripTest(network, "nodebreaker-busproperties.xml", version);
        }
    }
}
