/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcDetailedNetworkTest {

    @Test
    void test() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(1, network.getDcLineCount());
        assertEquals(2, network.getDcLineCommutatedConverterCount());
    }
}
