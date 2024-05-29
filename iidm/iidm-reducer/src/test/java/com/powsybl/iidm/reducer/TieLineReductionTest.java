/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TieLineReductionTest {

    private Network network;

    private NetworkReducerObserverImpl observer;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithTieLine();
        observer = new NetworkReducerObserverImpl();
    }

    @Test
    void testOneSideTieLineReduction() {
        var reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(List.of("VLGEN", "VLHV1", "VLLOAD")))
                .withObservers(observer)
                .build();
        assertEquals(0, observer.getTieLineRemovedCount());
        reducer.reduce(network);
        assertEquals(2, observer.getTieLineRemovedCount());
        assertEquals(List.of("P1", "P2", "VLGEN", "LOAD", "NGEN", "NGEN_NHV1", "NHV2_NLOAD", "sim1", "VLLOAD", "NHV1", "GEN", "VLHV1", "NHV1_XNODE1", "NLOAD", "NVH1_XNODE2"),
                network.getIdentifiables().stream().map(Identifiable::getId).toList());
    }

    @Test
    void testTwoSidesTieLineReduction() {
        var reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(List.of("VLGEN", "VLLOAD")))
                .withObservers(observer)
                .build();
        assertEquals(0, observer.getTieLineRemovedCount());
        reducer.reduce(network);
        assertEquals(2, observer.getTieLineRemovedCount());
        assertEquals(List.of("P1", "P2", "VLGEN", "LOAD", "NGEN", "NGEN_NHV1", "NHV2_NLOAD", "sim1", "VLLOAD", "GEN", "NLOAD"),
                network.getIdentifiables().stream().map(Identifiable::getId).toList());
    }

    @Test
    void testNoTieLineReduction() {
        var reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(List.of("VLHV1", "VLHV2", "VLLOAD")))
                .withObservers(observer)
                .build();
        assertEquals(0, observer.getTieLineRemovedCount());
        reducer.reduce(network);
        assertEquals(0, observer.getTieLineRemovedCount());
    }
}
