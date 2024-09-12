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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class OperationalLimitsGroupTest extends AbstractSerDeTest {

    @Test
    void multipleLimitsGroupsOnLineTest() {
        // Retrieve line
        Network network = Network.read("OperationalLimits.xml", getClass().getResourceAsStream("/OperationalLimits.xml"));
        Line line = network.getLine("Line");

        // There are 4 CGMES OperationalLimitSets on side 1 merged into 3 IIDM OperationalLimitsGroup
        FlowsLimitsHolder holder1 = line.getOperationalLimitsHolder1();
        FlowsLimitsHolder holder2 = line.getOperationalLimitsHolder2();
        assertEquals(3, holder1.getOperationalLimitsGroups().size());
        assertEquals(0, holder2.getOperationalLimitsGroups().size());

        // The CGMES winter current and active power limits have been merged into the same limits group
        // since their OperationalLimitSet name are equals
        Optional<OperationalLimitsGroup> winterLimits = holder1.getOperationalLimitsGroup("WINTER");
        assertTrue(winterLimits.isPresent());
        assertTrue(winterLimits.get().getCurrentLimits().isPresent());
        assertTrue(winterLimits.get().getActivePowerLimits().isPresent());

        // The CGMES spring current limits and summer active power limits have different limits group
        // since their OperationalLimitSet name are distinct
        Optional<OperationalLimitsGroup> springLimits = holder1.getOperationalLimitsGroup("SPRING");
        assertTrue(springLimits.isPresent());
        assertTrue(springLimits.get().getCurrentLimits().isPresent());
        assertTrue(springLimits.get().getActivePowerLimits().isEmpty());

        Optional<OperationalLimitsGroup> summerLimits = holder1.getOperationalLimitsGroup("SUMMER");
        assertTrue(summerLimits.isPresent());
        assertTrue(summerLimits.get().getCurrentLimits().isEmpty());
        assertTrue(summerLimits.get().getActivePowerLimits().isPresent());
    }

}
