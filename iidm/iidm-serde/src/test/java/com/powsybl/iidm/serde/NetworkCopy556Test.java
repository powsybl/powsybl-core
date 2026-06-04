/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkCopy556Test {

    @Test
    void roundTripTest() throws IOException {
        Network network = NetworkSerDe.read(Objects.requireNonNull(getClass().getResourceAsStream("/nodeBreaker.xml")));
        BusbarSection bbs1 = network.getBusbarSection("E1");
        BusbarSection bbs2 = network.getBusbarSection("E2");
        //When: action 1 - make an injections in bus BBS0 and in bus BBS1 (v with different value)
        bbs1.getTerminal().getBusBreakerView().getBus().setV(400.0);
        bbs2.getTerminal().getBusBreakerView().getBus().setV(401.0);
        //Then
        assertEquals(400.0, bbs1.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        assertEquals(401.0, bbs2.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        //When: action 2 - copy the network
        Network copy = NetworkSerDe.copy(network);
        //Then
        bbs1 = copy.getBusbarSection("E1");
        bbs2 = copy.getBusbarSection("E2");
        assertEquals(400.0, bbs1.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
        assertEquals(401.0, bbs2.getTerminal().getBusBreakerView().getBus().getV(), 0.0);
    }
    /**
     * Note
     * BBS0   ----breaker-----    BBS1
     * Action 1: make an injections in bus BBS0 and in bus BBS1 (v with different value)
     * Action 2: copy the network
     * =>
     * The value in the bus from BBS0 and in the value in bus from BBS1 have to be the same in many cases !
     * breaker (retained="true/false" open="true")  => no changes the values are different
     * breaker (retained="false" open="false")  => v have to be the same in the original network (not the copy)
     * breaker (retained="true" open="false")  => v have to be the same in the copied network (not the original one)
     */
}
