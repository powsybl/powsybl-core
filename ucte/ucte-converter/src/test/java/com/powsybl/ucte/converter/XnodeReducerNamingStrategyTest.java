/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.reducer.CountriesNetworkPredicate;
import com.powsybl.iidm.reducer.NetworkPredicate;
import com.powsybl.iidm.reducer.NetworkReducer;
import com.powsybl.iidm.reducer.ReducerNamingStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class XnodeReducerNamingStrategyTest {
    @Test
    public void testReducerWithXnodeReducerNamingStrategy() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("uxTestGridForMerging", new ResourceSet("/", "uxTestGridForMerging.uct"));
        Network network = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        emulateLoadflow(network);
        NetworkPredicate predicate = CountriesNetworkPredicate.of(Country.FR);
        ReducerNamingStrategy reducerNamingStrategy = new XnodeReducerNamingStrategy();

        assertEquals(2, network.getCountries().size());
        assertNotNull(network.getLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1"));
        assertNotNull(network.getLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1"));
        NetworkReducer reducer = NetworkReducer.builder()
                .withDanglingLines(true)
                .withNetworkPredicate(predicate)
                .withNamingStrategy(reducerNamingStrategy)
                .build();
        reducer.reduce(network);
        assertEquals(1, network.getCountries().size());
        assertNotNull(network.getDanglingLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1"));
        assertNotNull(network.getDanglingLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1"));
        assertNotNull(network.getDanglingLine("XXXXXX11"));
        assertNotNull(network.getDanglingLine("XXXXXX12"));
        assertEquals("XXXXXX11", network.getDanglingLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1").getId());
        assertEquals("XXXXXX12", network.getDanglingLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1").getId());
    }

    private void emulateLoadflow(Network network) {
        network.getLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1").getTerminal1().setP(10);
        network.getLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1").getTerminal2().setP(-10);
        network.getLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1").getTerminal1().setQ(10);
        network.getLine("BBBBBB11 XXXXXX11 1 + FFFFFF11 XXXXXX11 1").getTerminal2().setQ(-10);
        network.getLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1").getTerminal1().setP(10);
        network.getLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1").getTerminal2().setP(-10);
        network.getLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1").getTerminal1().setQ(10);
        network.getLine("BBBBBB11 XXXXXX12 1 + FFFFFF11 XXXXXX12 1").getTerminal2().setQ(-10);
    }
}