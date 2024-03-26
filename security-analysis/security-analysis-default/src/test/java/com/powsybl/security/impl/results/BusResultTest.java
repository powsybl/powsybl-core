/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl.results;

import com.google.common.testing.EqualsTester;
import com.powsybl.security.results.BusResult;
import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class BusResultTest {
    @Test
    void test() {
        BusResult busResult = new BusResult("vl_id", "bus_id", 400, 0.0003);
        assertEquals("vl_id", busResult.getVoltageLevelId());
        assertEquals("bus_id", busResult.getBusId());
        assertEquals(400.0, busResult.getV());
        assertEquals(0.0003, busResult.getAngle());

        new EqualsTester()
            .addEqualityGroup(new BusResult("vl_id2", "bus_id2", 250, 0.0003),
                new BusResult("vl_id2", "bus_id2", 250, 0.0003))
            .addEqualityGroup(busResult, busResult)
            .testEquals();
    }
}
