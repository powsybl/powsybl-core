/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.TopologyLevel;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Teofil Calin Banc {@literal <teofil-calin.banc at rte-france.com>}
 */
class TopologyLevelTest extends AbstractIidmSerDeTest {

    @Test
    void testComparison() {
        assertEquals(TopologyLevel.NODE_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.NODE_BREAKER, TopologyLevel.BUS_BRANCH));

        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.NODE_BREAKER));
        assertEquals(TopologyLevel.BUS_BREAKER, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BREAKER));
        assertEquals(TopologyLevel.BUS_BRANCH, TopologyLevel.min(TopologyKind.BUS_BREAKER, TopologyLevel.BUS_BRANCH));
    }

    @Test
    void testConversion() throws IOException {
        testConversion(NetworkSerDe.read(getVersionedNetworkAsStream("fictitiousSwitchRef.xml", IidmVersion.V_1_0)));

        testConversion(FictitiousSwitchFactory.create());
    }

    private void testConversion(Network network) throws IOException {

        ExportOptions options = new ExportOptions();
        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.NODE_BREAKER), "fictitiousSwitchRef.xml", CURRENT_IIDM_VERSION);

        network.getSwitchStream().forEach(sw -> sw.setRetained(false));
        network.getSwitch("BJ").setRetained(true);

        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.BUS_BREAKER), "fictitiousSwitchRef-bbk.xml", CURRENT_IIDM_VERSION);
        testWriteVersionedXml(network, options.setTopologyLevel(TopologyLevel.BUS_BRANCH), "fictitiousSwitchRef-bbr.xml", CURRENT_IIDM_VERSION);
    }
}
