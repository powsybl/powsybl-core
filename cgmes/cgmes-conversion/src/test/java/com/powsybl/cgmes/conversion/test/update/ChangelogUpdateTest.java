/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.powsybl.cgmes.conversion.update.Changelog;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.conversion.update.IidmChangeRemoval;
import com.powsybl.cgmes.conversion.update.IidmChangeUpdate;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public final class ChangelogUpdateTest {

    @Before
    public void setUp() {
        network = create();
        changelog = new Changelog(network);
    }

    @Test
    public void testCloneVariantBaseChanges() {
        // Make changes in initial variant
        String variant1 = network.getVariantManager().getWorkingVariantId();
        makeNetworkChangesOnBase(network);
        // Keep a copy of the changes for this variant in changelog
        List<IidmChange> changesVariant1 = new ArrayList<>(changelog.getChangesForVariant(variant1));

        // Clone initial variant and check changelog of cloned variant
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "2");
        network.getVariantManager().setWorkingVariant("2");
        String variant2 = network.getVariantManager().getWorkingVariantId();
        List<IidmChange> changesVariant2 = new ArrayList<>(changelog.getChangesForVariant(variant2));
        assertEquals(changesVariant1, changesVariant2);

        // Perform changes on base when new variant is current
        makeNetworkChangesOnBase(network);
        List<IidmChange> changesVariant1b = changelog.getChangesForVariant(variant1);
        List<IidmChange> changesVariant2b = changelog.getChangesForVariant(variant2);

        // We have made changes on data that is not variant-dependent
        // So changes in variant1 have also increased after second call to network changes
        assertTrue(changesVariant1b.size() > changesVariant1.size());
        assertTrue(changesVariant2b.size() > changesVariant2.size());

        // As changes are not variant-dependent, both variant1 and variant2 changes are still equal
        assertEquals(changesVariant1b, changesVariant2b);
    }

    @Test
    public void testCloneVariantVariantSpecificChanges() {
        // Make changes in initial variant
        String variant1 = network.getVariantManager().getWorkingVariantId();
        makeNetworkChangesVariantSpecific(network);
        // Keep a copy of the changes for this variant in changelog
        List<IidmChange> changesVariant1 =  new ArrayList<>(changelog.getChangesForVariant(variant1));

        // Clone initial variant and check changelog of cloned variant
        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "2");
        network.getVariantManager().setWorkingVariant("2");
        String variant2 = network.getVariantManager().getWorkingVariantId();
        List<IidmChange> changesVariant2 = new ArrayList<>(changelog.getChangesForVariant(variant2));
        assertEquals(changesVariant1, changesVariant2);

        // Perform changes on new variant
        makeNetworkChangesVariantSpecific(network);
        // Get changes objects again, after update
        List<IidmChange> changesVariant1b = changelog.getChangesForVariant(variant1);
        List<IidmChange> changesVariant2b = changelog.getChangesForVariant(variant2);
        // We have made changes on data that is variant-dependent
        // So, new changes are only visible in the new variant
        List<IidmChange> changesOnlyOnVariant2 = changesVariant2b.subList(changesVariant1b.size(), changesVariant2b.size());
        assertFalse(containsAny(changesVariant1b, changesOnlyOnVariant2));
        // Check variant1 is not affected by changes
        assertEquals(changesVariant1, changesVariant1b);
        // Check changes made in initial variant are still present in the new variant
        assertEquals(changesVariant1, changesVariant2b.subList(0, changesVariant1.size()));
        // Additional changes have been recorded in the new variant
        assertTrue(changesVariant2b.size() > changesVariant2.size());
        assertEquals(changesVariant1.size(), changesVariant1b.size());
    }

    @Test
    public void testChangesLogRightOrder() {
        String variant1 = network.getVariantManager().getWorkingVariantId();
        // Make changes, on Variant and base.
        makeNetworkChangesVariantSpecific(network);
        makeRemoveChanges(network);
        List<IidmChange> changes =  new ArrayList<>(changelog.getChangesForVariant(variant1));
        // To test the right ordering, the list of changes should have multiple items
        assertTrue(changes.size() > 1);
        // Removal is base change, without sorting by index it would be first change in the list.
        // Check the changes order is correct, and removal is last, as it was made last
        assertTrue(isSortedByIndex(changes));
        assertTrue(changes.get(0) instanceof IidmChangeUpdate);
        assertTrue(Iterables.getLast(changes) instanceof IidmChangeRemoval);
    }

    private static boolean isSortedByIndex(List<IidmChange> changes) {
        int last = -1;
        for (IidmChange c : changes) {
            if (c.getIndex() < last) {
                return false;
            }
            last = c.getIndex();
        }
        return true;
    }

    private static void makeNetworkChangesOnBase(Network network) {
        Line line = network.getLine("line");
        double newR = line.getR() * 1.1;
        double newX = line.getX() * 1.1;
        line.setR(newR).setX(newX);
    }

    private static void makeNetworkChangesVariantSpecific(Network network) {
        Generator g = network.getGenerator("generator");
        double newP = g.getTargetP() * 1.1;
        double newQ = g.getTargetQ() * 1.1;
        g.setTargetP(newP).setTargetQ(newQ).getTerminal().setP(-newP).setQ(-newQ);

        Load load = network.getLoad("load");
        newP = load.getP0() * 1.1;
        newQ = load.getQ0() * 1.1;
        load.setP0(newP).setQ0(newQ).getTerminal().setP(newP).setQ(newQ);
    }

    private static void makeRemoveChanges(Network network) {
        network.getLoad("load").remove();
    }

    private static boolean containsAny(List<IidmChange> changes, List<IidmChange> other) {
        for (IidmChange c : other) {
            if (changes.contains(c)) {
                return true;
            }
        }
        return false;
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Objects.requireNonNull(networkFactory);

        Network network = networkFactory.createNetwork("network", "test");
        Substation substation = network.newSubstation()
            .setId("substation")
            .setCountry(Country.FR)
            .setTso("TSO")
            .setGeographicalTags("region")
            .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
            .setId("voltageLevel1")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
            .setId("voltageLevel2")
            .setNominalV(230)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        voltageLevel1.getBusBreakerView()
            .newBus()
            .setName("bus1Name")
            .setId("bus1")
            .add();
        voltageLevel2.getBusBreakerView()
            .newBus()
            .setName("bus2Name")
            .setId("bus2")
            .add();
        voltageLevel1.newLoad()
            .setId("load")
            .setBus("bus1")
            .setP0(10)
            .setQ0(3)
            .add();
        Generator generator = voltageLevel1.newGenerator()
            .setId("generator")
            .setEnergySource(EnergySource.NUCLEAR)
            .setMinP(200.0)
            .setMaxP(900.0)
            .setRegulationMode(RegulationMode.VOLTAGE)
            .setTargetV(24.5)
            .setTargetP(607.0)
            .setTargetQ(301.0)
            .setBus("bus1")
            .add();
        generator.newReactiveCapabilityCurve()
            .beginPoint().setP(200.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
            .beginPoint().setP(900.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
            .add();
        network.newLine()
            .setId("line")
            .setName("line")
            .setVoltageLevel1("voltageLevel1")
            .setVoltageLevel2("voltageLevel2")
            .setBus1("bus1")
            .setBus2("bus2")
            .setR(1.935)
            .setX(34.2)
            .setB1(2.120575e-5)
            .setG1(3.375e-5)
            .setB2(2.120575e-5)
            .setG2(3.375e-5)
            .add();
        return network;
    }

    private Network network;
    private Changelog changelog;
}
