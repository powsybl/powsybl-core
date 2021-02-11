/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public abstract class AbstractAliasesTest {

    @Test
    public void canAddAliases() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        assertTrue(load.getAliases().isEmpty());
        load.addAlias("Load alias");

        assertEquals(1, load.getAliases().size());
        assertTrue(load.getAliases().contains("Load alias"));
        assertNotNull(network.getLoad("Load alias"));
        assertEquals(network.getLoad("Load alias"), load);
        assertFalse(network.getLoad("load1").getAliasType("Load alias").isPresent());

        String alias = "alias2";
        load.addAlias(alias, null);
        assertTrue(load.getAliases().contains(alias));
        assertFalse(load.getAliasType(alias).isPresent());

        alias = "alias3";
        load.addAlias(alias, "");
        assertTrue(load.getAliases().contains(alias));
        assertFalse(load.getAliasType(alias).isPresent());
    }

    @Test
    public void canAddAliasesWithTypes() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        assertTrue(load.getAliases().isEmpty());
        assertFalse(load.getAliasFromType("alias type").isPresent());
        load.addAlias("Load alias", "alias type");

        assertEquals(1, load.getAliases().size());
        assertTrue(load.getAliasFromType("alias type").isPresent());
        assertTrue(load.getAliases().contains("Load alias"));
        assertEquals("Load alias", load.getAliasFromType("alias type").orElse(null));
        assertNotNull(network.getLoad("Load alias"));
        assertEquals(network.getLoad("Load alias"), load);
        assertEquals("alias type", network.getLoad("load1").getAliasType("Load alias").orElse(null));
    }

    @Test
    public void canRemoveAliases() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        assertTrue(load.getAliases().isEmpty());
        load.addAlias("Load alias");

        assertEquals(1, load.getAliases().size());
        assertTrue(load.getAliases().contains("Load alias"));

        load.removeAlias("Load alias");
        assertTrue(load.getAliases().isEmpty());
    }

    @Test
    public void silentlyIgnoreAffectingObjectsIdAsAlias() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        assertTrue(load.getAliases().isEmpty());
        load.addAlias("load1");
        assertTrue(load.getAliases().isEmpty());
        assertEquals(load, network.getLoad("load1"));
    }

    @Test
    public void silentlyIgnoreAffectingTwiceSameIdToAnObject() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        assertTrue(load.getAliases().isEmpty());
        load.addAlias("Load alias");
        load.addAlias("Load alias");

        assertEquals(1, load.getAliases().size());
        assertTrue(load.getAliases().contains("Load alias"));
    }

    @Test
    public void ensureAliasUnicityFromId() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        load.addAlias("generator1", true);
        assertFalse(load.getAliases().contains("generator1"));
        assertTrue(load.getAliases().contains("generator1#0"));
    }

    @Test
    public void ensureAliasUnicityFromAlias() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        Generator generator = network.getGenerator("generator1");

        load.addAlias("alias");
        generator.addAlias("alias", true);

        assertTrue(load.getAliases().contains("alias"));
        assertFalse(generator.getAliases().contains("alias"));
        assertTrue(generator.getAliases().contains("alias#0"));
    }

    @Test(expected = PowsyblException.class)
    public void failWhenDuplicatedAlias() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        Generator generator = network.getGenerator("generator1");
        load.addAlias("Alias");
        generator.addAlias("Alias");
    }

    @Test(expected = PowsyblException.class)
    public void failWhenAliasEqualToAnId() {
        Network network = NetworkTest1Factory.create();
        Generator generator = network.getGenerator("generator1");
        generator.addAlias("load1");
    }

    @Test(expected = PowsyblException.class)
    public void failWhenRemovingNonExistingAlias() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        load.removeAlias("Load alias");
    }

    @Test(expected = PowsyblException.class)
    public void failWhenAliasTypeIsNull() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        load.getAliasFromType(null);
    }

    @Test(expected = PowsyblException.class)
    public void failWhenAliasTypeIsEmpty() {
        Network network = NetworkTest1Factory.create();
        Load load = network.getLoad("load1");
        load.getAliasFromType("");
    }

    @Test(expected = PowsyblException.class)
    public void mergeFailWhenAliasEqualsToAnIdOfOtherNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Network otherNetwork = FourSubstationsNodeBreakerFactory.create();
        otherNetwork.getGenerator("GH1").addAlias("NHV2_NLOAD");
        network.merge(otherNetwork);
    }

    @Test(expected = PowsyblException.class)
    public void mergeFailWhenAliasEqualsToAnAliasOfOtherNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Network otherNetwork = FourSubstationsNodeBreakerFactory.create();
        network.getTwoWindingsTransformer("NHV2_NLOAD").addAlias("Alias");
        otherNetwork.getGenerator("GH1").addAlias("Alias");
        network.merge(otherNetwork);
    }
}
