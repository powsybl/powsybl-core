/**
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tck.AbstractBusBreakerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BusTest extends AbstractBusBreakerTest {

    @Test
    void testFictitiousP0AndFictitiousQ0ForInvalidatedBusInBusBreaker() {
        Network network = createTestNetwork();
        Bus bus = network.getVoltageLevel("VL1").getBusView().getBus("VL1_0");
        network.getSwitch("BR1").setOpen(true);
        assertThrows(PowsyblException.class, bus::getFictitiousP0, "Bus has been invalidated");
        assertThrows(PowsyblException.class, bus::getFictitiousQ0, "Bus has been invalidated");
    }

}
