/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import org.junit.Test;

import com.powsybl.iidm.network.TopologyLevel;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.EnumSet;


/**
 * @author Teofil Calin Banc<teofil-calin.banc at rte-france.com>
 */
public class MaxTopologyLevelTest {

    @Test
    public void testMaxBusBreakerBusBreaker() throws Exception {
        assertEquals(TopologyLevel.BUS_BREAKER, Collections.max(EnumSet.of(TopologyLevel.BUS_BREAKER, TopologyLevel.BUS_BREAKER)));
    }

    @Test
    public void testMaxNodeBreakerBusBreaker() throws Exception {
        assertEquals(TopologyLevel.BUS_BREAKER, Collections.max(EnumSet.of(TopologyLevel.NODE_BREAKER, TopologyLevel.BUS_BREAKER)));
    }

    @Test
    public void testMaxNodeBreakerNodeBreaker() throws Exception {
        assertEquals(TopologyLevel.NODE_BREAKER, Collections.max(EnumSet.of(TopologyLevel.NODE_BREAKER, TopologyLevel.NODE_BREAKER)));
    }

    @Test
    public void testMaxNodeBreakerBusBranch() throws Exception {
        assertEquals(TopologyLevel.BUS_BRANCH, Collections.max(EnumSet.of(TopologyLevel.NODE_BREAKER, TopologyLevel.BUS_BRANCH)));
    }

    @Test
    public void testMaxBusBreakerBusBranch() throws Exception {
        assertEquals(TopologyLevel.BUS_BRANCH, Collections.max(EnumSet.of(TopologyLevel.BUS_BREAKER, TopologyLevel.BUS_BRANCH)));
    }
}
