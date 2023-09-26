/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultNamingStrategyTest {
    @Test
    void testName() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        assertEquals("Default", namingStrategy.getName());
    }

    @Test
    void testDisconnector() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
        assertEquals("test", namingStrategy.getDisconnectorIdPrefix("test"));

        // Suffix
        assertEquals("5", namingStrategy.getDisconnectorIdSuffix(5));
        assertEquals("5_6", namingStrategy.getDisconnectorIdSuffix(5, 6));

        // Full Id
        assertEquals("test_DISCONNECTOR", namingStrategy.getDisconnectorId("test"));
        assertEquals("test_DISCONNECTOR_5", namingStrategy.getDisconnectorId("test", 5));
        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId("test", 5, 6));
    }

    @Test
    void testDisconnectorWithBbs() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        Network network = createNbNetwork();

        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId(network.getBusbarSection("D"), "test", 5, 6));
    }

    @Test
    void testBreaker() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
        assertEquals("test", namingStrategy.getBreakerIdPrefix("test"));

        // Suffix
        assertEquals("5", namingStrategy.getBreakerIdSuffix(5));
        assertEquals("5_6", namingStrategy.getBreakerIdSuffix(5, 6));

        // Full Id
        assertEquals("test_BREAKER", namingStrategy.getBreakerId("test"));
        assertEquals("test_BREAKER_5", namingStrategy.getBreakerId("test", 5));
        assertEquals("test_BREAKER_5_6", namingStrategy.getBreakerId("test", 5, 6));
    }

    @Test
    void testSwitch() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
        assertEquals("test", namingStrategy.getSwitchIdPrefix("test"));

        // Suffix
        assertEquals("5", namingStrategy.getSwitchIdSuffix(5));
        assertEquals("5_6", namingStrategy.getSwitchIdSuffix(5, 6));

        // Full Id
        assertEquals("test_SW", namingStrategy.getSwitchId("test"));
        assertEquals("test_SW_5", namingStrategy.getSwitchId("test", 5));
        assertEquals("test_SW_5_6", namingStrategy.getSwitchId("test", 5, 6));
    }

    @Test
    void testBusBar() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
        assertEquals("test", namingStrategy.getBusbarIdPrefix("test"));

        // Suffix
        assertEquals("5", namingStrategy.getBusbarIdSuffix(5));
        assertEquals("5_6", namingStrategy.getBusbarIdSuffix(5, 6));

        // Full Id
        assertEquals("test", namingStrategy.getBusbarId("test"));
        assertEquals("test_5", namingStrategy.getBusbarId("test", 5));
        assertEquals("test_5_6", namingStrategy.getBusbarId("test", 5, 6));
    }

    @Test
    void testFeeder() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Full Id
        assertEquals("prefix_voltageLevelId", namingStrategy.getFeederId("prefix", "voltageLevelId"));
    }

    @Test
    void testSwitchBaseId() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        Network network = createNbNetwork();

        // On Connectable
        assertEquals("CB", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 0));
        assertEquals("CB1", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 1));

        // On VoltageLevel abs BusbarSections
        assertEquals("C", namingStrategy.getSwitchBaseId(network.getVoltageLevel("C"), network.getBusbarSection("D"), network.getBusbarSection("O")));
    }

    @Test
    void testVoltageLevel() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Full Id
        assertEquals("prefix_suffix", namingStrategy.getVoltageLevelId(null, "prefix", "suffix"));
    }

    @Test
    void testBbs() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Full Id
        assertEquals("1", namingStrategy.getBbsId(1));
        assertEquals("1A", namingStrategy.getBbsId(1, "A"));
        assertEquals("12", namingStrategy.getBbsId(1, 2));
        assertEquals("1A2", namingStrategy.getBbsId(1, "A", 2));
    }

    @Test
    void testGenerator() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        // Full Id
        assertEquals("substHG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.HYDRO, "prefix", "suffix", 7));
        assertEquals("substNG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.NUCLEAR, "prefix", "suffix", 7));
        assertEquals("substVG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.SOLAR, "prefix", "suffix", 7));
        assertEquals("substTG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.THERMAL, "prefix", "suffix", 7));
        assertEquals("substEG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.WIND, "prefix", "suffix", 7));
        assertEquals("substXG7", namingStrategy.getGeneratorId(network.getSubstation("subst"), EnergySource.OTHER, "prefix", "suffix", 7));
    }
}
