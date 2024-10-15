/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class SwitchConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/switches/";

    @Test
    void lineWithZeroImpedanceTest() {
        // CGMES network:
        //   An ACLineSegment ACL_12 with zero impedance between two nodes of the same voltage level.
        // IIDM network:
        //   A branch with 0 impedance inside a VoltageLevel is converted to a Switch
        Network network = readCgmesResources(DIR, "line_with_0_impedance.xml");
        assertNotNull(network);

        // The line has been imported as a fictitious switch
        assertNull(network.getLine("ACL_12"));
        assertNotNull(network.getSwitch("ACL_12"));
        assertTrue(network.getSwitch("ACL_12").isFictitious());
    }
}
