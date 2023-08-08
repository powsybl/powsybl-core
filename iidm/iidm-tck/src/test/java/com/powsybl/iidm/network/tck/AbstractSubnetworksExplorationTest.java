/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.powsybl.iidm.network.test.NetworkTest1Factory.id;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public abstract class AbstractSubnetworksExplorationTest {

    private static Network merged;
    private static Network subnetwork1;
    private static Network subnetwork2;

    @BeforeAll
    static void setUpClass() {
        Network n1 = NetworkTest1Factory.create("1");
        n1.newSubstation()
                .setId(id("substation2", "1"))
                .setCountry(Country.ES)
                .setTso(id("TSO2", "1"))
                .setGeographicalTags(id("region2", "1"))
                .add();
        n1.getVoltageLevel(id("voltageLevel1", "1")).newBattery()
                .setId(id("battery1", "1"))
                .setMaxP(20.0)
                .setMinP(10.0)
                .setTargetP(15.0)
                .setTargetQ(10.0)
                .setNode(4)
                .add();

        Network n2 = NetworkTest1Factory.create("2");
        n2.newSubstation()
                .setId(id("substation2", "2"))
                .setCountry(Country.BE)
                .setTso(id("TSO2", "2"))
                .setGeographicalTags(id("region2", "2"))
                .add();
        n2.getVoltageLevel(id("voltageLevel1", "2")).newBattery()
                .setId(id("battery1", "2"))
                .setMaxP(20.0)
                .setMinP(10.0)
                .setTargetP(15.0)
                .setTargetQ(10.0)
                .setNode(4)
                .add();

        merged = Network.create("merged", n1, n2);
        subnetwork1 = merged.getSubnetwork(id("network", "1"));
        subnetwork2 = merged.getSubnetwork(id("network", "2"));
    }

    @Test
    public void testExploreSubnetworks() {
        assertEquals(2, merged.getSubnetworks().size());
        assertEquals(0, subnetwork1.getSubnetworks().size());
        assertEquals(0, subnetwork2.getSubnetworks().size());
        assertNull(subnetwork1.getSubnetwork("merged"));
        assertNull(subnetwork1.getSubnetwork(id("network", "1")));
        assertNull(subnetwork1.getSubnetwork(id("network", "2")));
        assertNull(subnetwork2.getSubnetwork("merged"));
        assertNull(subnetwork2.getSubnetwork(id("network", "1")));
        assertNull(subnetwork2.getSubnetwork(id("network", "2")));
    }

    @Test
    public void testExploreNetwork() {
        assertEquals(merged, merged.getNetwork());
        assertEquals(merged, subnetwork1.getNetwork());
        assertEquals(merged, subnetwork2.getNetwork());
    }

    @Test
    public void testExploreVariantManager() {
        assertEquals(merged.getVariantManager(), subnetwork1.getVariantManager());
        assertEquals(merged.getVariantManager(), subnetwork2.getVariantManager());
    }

    @Test
    public void testExploreCountries() {
        assertEquals(3, merged.getCountryCount());
        assertEquals(2, subnetwork1.getCountryCount());
        assertEquals(2, subnetwork2.getCountryCount());
        assertCollection(List.of(Country.FR, Country.ES, Country.BE), merged.getCountries());
        assertCollection(List.of(Country.FR, Country.ES), subnetwork1.getCountries());
        assertCollection(List.of(Country.FR, Country.BE), subnetwork2.getCountries());
    }

    @Test
    public void testExploreSubstations() {
        String n1Substation1 = id("substation1", "1");
        String n1Substation2 = id("substation2", "1");
        String n2Substation1 = id("substation1", "2");
        String n2Substation2 = id("substation2", "2");
        var expectedIdsForMerged = List.of(n1Substation1, n1Substation2, n2Substation1, n2Substation2);
        var expectedIdsForSubnetwork1 = List.of(n1Substation1, n1Substation2);
        var expectedIdsForSubnetwork2 = List.of(n2Substation1, n2Substation2);
        assertIds(expectedIdsForMerged, merged.getSubstations());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getSubstations());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getSubstations());
        assertIds(expectedIdsForMerged, merged.getSubstationStream());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getSubstationStream());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getSubstationStream());
        assertEquals(4, merged.getSubstationCount());
        assertEquals(2, subnetwork1.getSubstationCount());
        assertEquals(2, subnetwork2.getSubstationCount());

        assertIds(List.of(n1Substation1, n2Substation1), merged.getSubstations(Country.FR, null));
        assertIds(List.of(n1Substation1), subnetwork1.getSubstations(Country.FR, null));
        assertIds(List.of(n2Substation1), subnetwork2.getSubstations(Country.FR, null));
        var countryName = Country.ES.getName();
        assertIds(List.of(n1Substation2), merged.getSubstations(countryName, null));
        assertIds(List.of(n1Substation2), subnetwork1.getSubstations(countryName, null));
        assertFalse(subnetwork2.getSubstations(countryName, null).iterator().hasNext());
        countryName = Country.BE.getName();
        assertIds(List.of(n2Substation2), merged.getSubstations(countryName, null));
        assertFalse(subnetwork1.getSubstations(countryName, null).iterator().hasNext());
        assertIds(List.of(n2Substation2), subnetwork2.getSubstations(countryName, null));

        assertNotNull(merged.getSubstation(n1Substation1));
        assertNotNull(merged.getSubstation(n2Substation1));
        assertNotNull(subnetwork1.getSubstation(n1Substation1));
        assertNull(subnetwork1.getSubstation(n2Substation1));
        assertNull(subnetwork2.getSubstation(n1Substation1));
        assertNotNull(subnetwork2.getSubstation(n2Substation1));
    }

    @Test
    public void testExploreVoltageLevels() {
        String n1VoltageLevel1 = id("voltageLevel1", "1");
        String n2VoltageLevel1 = id("voltageLevel1", "2");
        var expectedIdsForMerged = List.of(n1VoltageLevel1, n2VoltageLevel1);
        var expectedIdsForSubnetwork1 = List.of(n1VoltageLevel1);
        var expectedIdsForSubnetwork2 = List.of(n2VoltageLevel1);
        assertIds(expectedIdsForMerged, merged.getVoltageLevels());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getVoltageLevels());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getVoltageLevels());
        assertIds(expectedIdsForMerged, merged.getVoltageLevelStream());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getVoltageLevelStream());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getVoltageLevelStream());
        assertEquals(2, merged.getVoltageLevelCount());
        assertEquals(1, subnetwork1.getVoltageLevelCount());
        assertEquals(1, subnetwork2.getVoltageLevelCount());

        assertNotNull(merged.getVoltageLevel(n1VoltageLevel1));
        assertNotNull(merged.getVoltageLevel(n2VoltageLevel1));
        assertNotNull(subnetwork1.getVoltageLevel(n1VoltageLevel1));
        assertNull(subnetwork1.getVoltageLevel(n2VoltageLevel1));
        assertNull(subnetwork2.getVoltageLevel(n1VoltageLevel1));
        assertNotNull(subnetwork2.getVoltageLevel(n2VoltageLevel1));
    }

    @Test
    public void testExploreGenerators() {
        String n1Generator1 = id("generator1", "1");
        String n2Generator1 = id("generator1", "2");
        var expectedIdsForMerged = List.of(n1Generator1, n2Generator1);
        var expectedIdsForSubnetwork1 = List.of(n1Generator1);
        var expectedIdsForSubnetwork2 = List.of(n2Generator1);
        assertIds(expectedIdsForMerged, merged.getGenerators());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getGenerators());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getGenerators());
        assertIds(expectedIdsForMerged, merged.getGeneratorStream());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getGeneratorStream());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getGeneratorStream());
        assertEquals(2, merged.getGeneratorCount());
        assertEquals(1, subnetwork1.getGeneratorCount());
        assertEquals(1, subnetwork2.getGeneratorCount());

        assertNotNull(merged.getGenerator(n1Generator1));
        assertNotNull(merged.getGenerator(n2Generator1));
        assertNotNull(subnetwork1.getGenerator(n1Generator1));
        assertNull(subnetwork1.getGenerator(n2Generator1));
        assertNull(subnetwork2.getGenerator(n1Generator1));
        assertNotNull(subnetwork2.getGenerator(n2Generator1));
    }

    @Test
    public void testExploreLoads() {
        String n1Load1 = id("load1", "1");
        String n2Load1 = id("load1", "2");
        var expectedIdsForMerged = List.of(n1Load1, n2Load1);
        var expectedIdsForSubnetwork1 = List.of(n1Load1);
        var expectedIdsForSubnetwork2 = List.of(n2Load1);
        assertIds(expectedIdsForMerged, merged.getLoads());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getLoads());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getLoads());
        assertIds(expectedIdsForMerged, merged.getLoadStream());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getLoadStream());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getLoadStream());
        assertEquals(2, merged.getLoadCount());
        assertEquals(1, subnetwork1.getLoadCount());
        assertEquals(1, subnetwork2.getLoadCount());

        assertNotNull(merged.getLoad(n1Load1));
        assertNotNull(merged.getLoad(n2Load1));
        assertNotNull(subnetwork1.getLoad(n1Load1));
        assertNull(subnetwork1.getLoad(n2Load1));
        assertNull(subnetwork2.getLoad(n1Load1));
        assertNotNull(subnetwork2.getLoad(n2Load1));
    }

    @Test
    public void testExploreBatteries() {
        String n1Battery1 = id("battery1", "1");
        String n2Battery1 = id("battery1", "2");
        var expectedIdsForMerged = List.of(n1Battery1, n2Battery1);
        var expectedIdsForSubnetwork1 = List.of(n1Battery1);
        var expectedIdsForSubnetwork2 = List.of(n2Battery1);
        assertIds(expectedIdsForMerged, merged.getBatteries());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getBatteries());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getBatteries());
        assertIds(expectedIdsForMerged, merged.getBatteryStream());
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getBatteryStream());
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getBatteryStream());
        assertEquals(2, merged.getBatteryCount());
        assertEquals(1, subnetwork1.getBatteryCount());
        assertEquals(1, subnetwork2.getBatteryCount());

        assertNotNull(merged.getBattery(n1Battery1));
        assertNotNull(merged.getBattery(n2Battery1));
        assertNotNull(subnetwork1.getBattery(n1Battery1));
        assertNull(subnetwork1.getBattery(n2Battery1));
        assertNull(subnetwork2.getBattery(n1Battery1));
        assertNotNull(subnetwork2.getBattery(n2Battery1));
    }

    private <T> void assertCollection(Collection<T> expected, Collection<T> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    private void assertIds(Collection<String> expectedIds, Iterable<? extends Identifiable<?>> iterable) {
        assertIds(expectedIds, StreamSupport.stream(iterable.spliterator(), false));
    }

    private void assertIds(Collection<String> expectedIds, Stream<? extends Identifiable<?>> stream) {
        Set<String> ids = stream.map(Identifiable::getId).collect(Collectors.toSet());
        assertCollection(expectedIds, ids);
    }
}
