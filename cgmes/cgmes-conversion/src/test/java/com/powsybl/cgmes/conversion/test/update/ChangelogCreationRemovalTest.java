/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.powsybl.cgmes.conversion.update.Changelog;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.conversion.update.IidmChangeCreation;
import com.powsybl.cgmes.conversion.update.IidmChangeRemoval;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public final class ChangelogCreationRemovalTest {

    @Before
    public void setUp() {
        network = ChangelogUpdateTest.create();
        changelog = new Changelog(network);
        expected = new HashMap<>();
        actual = new HashMap<>();
        variant = network.getVariantManager().getWorkingVariantId();
    }

    @Test
    public void testChangelogCreate() {
        // initial changelog is empty
        assertTrue(changelog.getChangesForVariant(variant).isEmpty());

        makeCreateChanges(network);
        List<IidmChange> currentChanges = changelog.getChangesForVariant(variant);
        // Check there is one Creation event
        assertTrue(currentChanges.size() == 1 && currentChanges.get(0) instanceof IidmChangeCreation);
        // Expected to be TwoWindingsTransformer, with right name and id
        assertTrue(currentChanges.get(0).getIdentifiable() instanceof TwoWindingsTransformer);
        assertTrue(expected("create").equals(actual(currentChanges)));
    }

    @Test
    public void testChangelogRemove() {
        // initial changelog is empty
        assertTrue(changelog.getChangesForVariant(variant).isEmpty());

        makeRemoveChanges(network);
        List<IidmChange> currentChanges = changelog.getChangesForVariant(variant);
        // Check there is one Removal event, we ignore Update events for this check.
        assertTrue(
            sizeIgnoreUpdateOnRemoval(currentChanges) == 1 && currentChanges.get(0) instanceof IidmChangeRemoval);
        // Expected to be Line, with right name and id
        assertTrue(currentChanges.get(0).getIdentifiable() instanceof Line);
        assertTrue(expected("remove").equals(actual(currentChanges)));
    }

    private Map<String, String> expected(String event) {
        if (event.equals("create")) {
            expected.put("id", "twt");
            expected.put("name", "twt");
        } else if (event.equals("remove")) {
            expected.put("id", "line");
            expected.put("name", "line");
        }
        return expected;
    }

    private Map<String, String> actual(List<IidmChange> changes) {
        actual.put("id", changes.get(0).getIdentifiable().getId());
        actual.put("name", changes.get(0).getIdentifiable().getName());
        return actual;
    }

    private int sizeIgnoreUpdateOnRemoval(List<IidmChange> changes) {
        int count = 0;
        for (IidmChange i : changes) {
            if (!(i instanceof IidmChangeRemoval)) {
                continue;
            }
            count++;
        }
        return count;
    }

    private void makeCreateChanges(Network network) {
        Substation substation = network.getSubstation("substation");
        substation.newTwoWindingsTransformer()
            .setId("twt")
            .setName("twt")
            .setVoltageLevel1("voltageLevel1")
            .setVoltageLevel2("voltageLevel2")
            .setConnectableBus1("bus1")
            .setConnectableBus2("bus2")
            .setR(2.0)
            .setX(14.745)
            .setG(4.0)
            .setB(3.2E-5)
            .setRatedU1(111.0)
            .setRatedU2(222.0)
            .add();
    }

    private void makeRemoveChanges(Network network) {
        network.getLine("line").remove();
    }

    private Network network;
    private Changelog changelog;
    Map<String, String> expected;
    Map<String, String> actual;
    String variant;

}
