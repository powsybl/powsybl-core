/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.TwoVoltageLevelNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GroundSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        // Initialise the network
        Network network = TwoVoltageLevelNetworkFactory.createWithGrounds();

        // Test for current version
        allFormatsRoundTripTest(network, "ground.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("ground.xml", IidmVersion.V_1_11);
    }
}
