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
class SvInjectionUpdateTest {

    private static final String DIR = "/update/sv-injection/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "svInjection_EQ.xml");
        assertEquals(0, network.getLoadCount());
    }

    @Test
    void importTogetherTest() {
        Network network = readCgmesResources(DIR, "svInjection_EQ.xml", "svInjection_TP.xml", "svInjection_SV.xml");
        assertEquals(1, network.getLoadCount());

        Load load = network.getLoad("ConnectivityNode-TN-SvInjection");
        assertNotNull(load);
        double tol = 0.0000001;
        assertSame(LoadType.FICTITIOUS, load.getLoadType());
        assertTrue(load.isFictitious());
        assertEquals(9.0, load.getP0(), tol);
        assertEquals(1.2, load.getQ0(), tol);
        assertEquals(9.0, load.getTerminal().getP(), tol);
        assertEquals(1.2, load.getTerminal().getQ(), tol);
    }

    @Test
    void importSeparatelyTest() {
        Network network = readCgmesResources(DIR, "svInjection_EQ.xml");
        assertEquals(0, network.getLoadCount());

        readCgmesResources(network, DIR, "svInjection_TP.xml", "svInjection_SV.xml");
        assertEquals(0, network.getLoadCount());
    }
}
