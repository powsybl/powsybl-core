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
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TieLineReductionTest {

    @Test
    void test() {
        boolean[] tieLineRemoved = new boolean[1];
        var observer = new DefaultNetworkReducerObserver() {
            @Override
            public void tieLineRemoved(TieLine tieLine) {
                tieLineRemoved[0] = true;
            }
        };
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        var reducer = NetworkReducer.builder()
                .withNetworkPredicate(new IdentifierNetworkPredicate(List.of("VLGEN", "VLHV1", "VLLOAD")))
                .withObservers(observer)
                .build();
        assertFalse(tieLineRemoved[0]);
        reducer.reduce(network);
        assertTrue(tieLineRemoved[0]);
        assertEquals(List.of("P1", "P2", "VLGEN", "LOAD", "NGEN", "NGEN_NHV1", "NHV2_NLOAD", "sim1", "VLLOAD", "NHV1", "GEN", "VLHV1", "NHV1_XNODE1", "NLOAD", "NVH1_XNODE2"),
                network.getIdentifiables().stream().map(Identifiable::getId).toList());
    }
}
