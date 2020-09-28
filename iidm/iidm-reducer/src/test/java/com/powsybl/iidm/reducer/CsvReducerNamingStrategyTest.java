/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourCountriesNetworkFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class CsvReducerNamingStrategyTest {
    @Test
    public void checkCsvReducerNamingStrategyImport() throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/csvReducerNamingStrategy.csv"))) {
            Network network = FourCountriesNetworkFactory.create();
            emulateLoadflow(network);
            NetworkPredicate predicate = CountriesNetworkPredicate.of(Country.FR, Country.DE);
            ReducerNamingStrategy reducerNamingStrategy = new CsvReducerNamingStrategy(reader);

            assertEquals(4, network.getCountries().size());
            assertNotNull(network.getLine("DE-NL"));
            assertNotNull(network.getLine("FR-BE"));
            NetworkReducer reducer = NetworkReducer.builder()
                    .withDanglingLines(true)
                    .withNetworkPredicate(predicate)
                    .withNamingStrategy(reducerNamingStrategy)
                    .build();
            reducer.reduce(network);
            assertEquals(2, network.getCountries().size());
            assertNull(network.getLine("DE-NL"));
            assertNull(network.getLine("FR-BE"));
            assertNotNull(network.getDanglingLine("DE-NL"));
            assertNotNull(network.getDanglingLine("FR-BE"));
            assertNotNull(network.getDanglingLine("Germany-Netherlands"));
            assertEquals(network.getDanglingLine("DE-NL"), network.getDanglingLine("Germany-Netherlands"));
            assertEquals("Germany-Netherlands", network.getDanglingLine("DE-NL").getId());
            assertNotNull(network.getDanglingLine("France-Belgium"));
            assertEquals(network.getDanglingLine("FR-BE"), network.getDanglingLine("France-Belgium"));
            assertEquals("France-Belgium", network.getDanglingLine("FR-BE").getId());

        }
    }

    private void emulateLoadflow(Network network) {
        network.getLine("DE-NL").getTerminal1().setP(10);
        network.getLine("DE-NL").getTerminal2().setP(-10);
        network.getLine("DE-NL").getTerminal1().setQ(10);
        network.getLine("DE-NL").getTerminal2().setQ(-10);
        network.getLine("FR-BE").getTerminal1().setP(10);
        network.getLine("FR-BE").getTerminal2().setP(-10);
        network.getLine("FR-BE").getTerminal1().setQ(10);
        network.getLine("FR-BE").getTerminal2().setQ(-10);
    }
}