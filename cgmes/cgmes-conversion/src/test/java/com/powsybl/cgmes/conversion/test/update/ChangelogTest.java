/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.cgmes.conversion.update.Changelog;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.iidm.network.*;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public final class ChangelogTest {

    @Before
    public void setUp() {
        network = create();
        changelog = new Changelog(network);
    }

    @Test
    public void testCloneVariant() {
        // apply variant specific and generic changes
        String variantInitialState = network.getVariantManager().getWorkingVariantId();
        NetworkChanges.scaleLoadGenerator(network, 1000);
        NetworkChanges.scaleLine(network, 1000);

        List<IidmChange> changesInitialVariant = changelog.getChangesForVariant(variantInitialState);

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "1");
        network.getVariantManager().setWorkingVariant("1");

        String clonedVariant = network.getVariantManager().getWorkingVariantId();
        NetworkChanges.scaleLoadGenerator(network, 1000);
        NetworkChanges.scaleLine(network, 1000);

        List<IidmChange> changesClonedVariant = changelog.getChangesForVariant(clonedVariant);

        assertTrue(changesClonedVariant.size() > changesInitialVariant.size());
        assertTrue(initialChangesVisibleInClonedVariant(changesInitialVariant, changesClonedVariant));

        int numNewChanges = changesClonedVariant.size() - changesInitialVariant.size();
        assertTrue(clonedNotVisibleInInitial(changesInitialVariant, changesClonedVariant, numNewChanges));
    }

    private boolean initialChangesVisibleInClonedVariant(List<IidmChange> variant1,
        List<IidmChange> valriant2) {
        for (IidmChange i : variant1) {
            if (!valriant2.contains(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean clonedNotVisibleInInitial(List<IidmChange> variant1, List<IidmChange> valriant2, int num) {
        int count = 0;
        for (IidmChange i : valriant2) {
            if (!variant1.contains(i)) {
                count++;
            } else {
                continue;
            }
        }
        boolean diff = count == num ? true : false;
        return diff;
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
        Bus bus = voltageLevel1.getBusBreakerView()
            .newBus()
            .setName("bus1Name")
            .setId("bus1")
            .add();
        Bus bus2 = voltageLevel2.getBusBreakerView()
            .newBus()
            .setName("bus2Name")
            .setId("bus2")
            .add();
        Load load = voltageLevel1.newLoad()
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
            .setVoltageRegulatorOn(true)
            .setTargetV(24.5)
            .setTargetP(607.0)
            .setTargetQ(301.0)
            .setBus("bus1")
            .add();
        generator.newReactiveCapabilityCurve()
            .beginPoint().setP(200.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
            .beginPoint().setP(900.0).setMinQ(300.0).setMaxQ(500.0).endPoint()
            .add();
        Line line = network.newLine()
            .setId("line")
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
