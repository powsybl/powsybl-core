/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.OperationalLimitsGroup;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class OperationalLimitsGroupTest extends AbstractSerDeTest {

    @Test
    void multipleLimitsGroupsOnLineTest() {
        Network network = Network.read("OperationalLimits.xml", getClass().getResourceAsStream("/OperationalLimits.xml"));
        Line line = network.getLine("Line");
        Collection<OperationalLimitsGroup> limitsGroups = line.getOperationalLimitsGroups1();
        assertEquals(3, limitsGroups.size());
    }

}
