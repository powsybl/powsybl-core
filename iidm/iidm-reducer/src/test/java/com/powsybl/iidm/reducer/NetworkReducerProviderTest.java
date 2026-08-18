/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class NetworkReducerProviderTest {

    private static final String NHV1_NHV2_1 = "NHV1_NHV2_1";

    @Test
    void findAllProvidersTest() {
        assertEquals(1, NetworkReducerProvider.findAll().size());
    }

    @Test
    void testDefaultProvider() {
        NetworkReducerProvider provider = NetworkReducer.find();
        assertEquals("Default", provider.getName());

        NetworkReducerProvider namedProvider = NetworkReducer.find("Default");
        assertEquals("Default", namedProvider.getName());
    }

    @Test
    void testCreateWithDefaultParameters() {
        NetworkReducerProvider provider = NetworkReducer.find();
        NetworkReducer reducer = provider.create(IdentifierNetworkPredicate.of("P1"));
        assertNotNull(reducer);
        assertInstanceOf(DefaultNetworkReducer.class, reducer);

        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        reducer.reduce(network);

        assertEquals(1, network.getSubstationCount());
        assertEquals(0, network.getLineCount());
        assertNotNull(network.getLoad(NHV1_NHV2_1));
    }

    @Test
    void testCreateWithReductionOptions() {
        NetworkReducerProvider provider = NetworkReducer.find();
        ReductionOptions options = new ReductionOptions().withBoundaryLines(true);
        NetworkReducer reducer = provider.create(IdentifierNetworkPredicate.of("P2"), options);

        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        reducer.reduce(network);

        assertEquals(2, network.getBoundaryLineCount());
    }
}
