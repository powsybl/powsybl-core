/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.impl.results;

import com.google.common.testing.EqualsTester;
import com.powsybl.security.results.BusResults;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class BusResultsTest {
    @Test
    public void test() {
        BusResults busResults = new BusResults("vl_id", "bus_id", 400, 0.0003);
        assertEquals("vl_id", busResults.getVoltageLevelId());
        assertEquals("bus_id", busResults.getBusId());
        assertEquals(400.0, busResults.getV());
        assertEquals(0.0003, busResults.getAngle());

        new EqualsTester()
            .addEqualityGroup(new BusResults("vl_id2", "bus_id2", 250, 0.0003),
                new BusResults("vl_id2", "bus_id2", 250, 0.0003))
            .addEqualityGroup(busResults, busResults)
            .testEquals();
    }
}
