/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.EnumSet;

import com.powsybl.iidm.network.TopologyKind;

/**
 * @author Teofil Calin Banc<teofil-calin.banc at rte-france.com>
 */
public class MaxTopologyKindTest {

    @Test
    public void testMaxBusBreakerBusBreaker() throws Exception {
        assertEquals(TopologyKind.BUS_BREAKER, Collections.max(EnumSet.of(TopologyKind.BUS_BREAKER, TopologyKind.BUS_BREAKER)));
    }

    @Test
    public void testMaxNodeBreakerBusBreaker() throws Exception {
        assertEquals(TopologyKind.BUS_BREAKER, Collections.max(EnumSet.of(TopologyKind.NODE_BREAKER, TopologyKind.BUS_BREAKER)));
    }

    @Test
    public void testMaxNodeBreakerNodeBreaker() throws Exception {
        assertEquals(TopologyKind.NODE_BREAKER, Collections.max(EnumSet.of(TopologyKind.NODE_BREAKER, TopologyKind.NODE_BREAKER)));
    }
}
