/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusbarSectionUpdateTest {

    private static final String DIR = "/update/busbar-section/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml");
        assertEquals(1, network.getBusbarSectionCount());

        assertEq(network.getBusbarSection("BusbarSection"));
    }

    private static void assertEq(BusbarSection busbarSection) {
        assertNotNull(busbarSection);
        assertNotNull(busbarSection.getTerminal());
        assertTrue(busbarSection.getTerminal().isConnected());
    }
}
