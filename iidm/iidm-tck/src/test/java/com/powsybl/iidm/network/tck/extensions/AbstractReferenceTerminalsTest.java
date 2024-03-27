/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.powsybl.iidm.network.VariantManagerConstants.INITIAL_VARIANT_ID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractReferenceTerminalsTest {

    Network network;
    VariantManager variantManager;
    Generator gh1;
    Generator gh2;
    Generator gh3;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        variantManager = network.getVariantManager();
        gh1 = network.getGenerator("GH1");
        gh2 = network.getGenerator("GH2");
        gh3 = network.getGenerator("GH3");
    }

    @Test
    public void test() {
        assertNull(network.getExtension(ReferenceTerminals.class));
        network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Set.of(gh1.getTerminal()))
                .add();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        assertNotNull(ext);
        assertEquals(1, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().contains(gh1.getTerminal()));

        // add another one, using static utility method
        ReferenceTerminals.addTerminal(gh2.getTerminal());
        assertEquals(2, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh2.getTerminal())));

        // add already existing
        ext.addReferenceTerminal(gh1.getTerminal())
            .addReferenceTerminal(gh2.getTerminal())
            .addReferenceTerminal(gh3.getTerminal());
        assertEquals(3, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh2.getTerminal(), gh3.getTerminal())));

        // clear
        ReferenceTerminals.reset(network);
        assertEquals(0, ext.getReferenceTerminals().size());
    }

    @Test
    public void testResetAddGet() {
        assertNull(network.getExtension(ReferenceTerminals.class));
        assertTrue(ReferenceTerminals.getTerminals(network).isEmpty());
        ReferenceTerminals.reset(network);
        assertNotNull(network.getExtension(ReferenceTerminals.class));
        network.removeExtension(ReferenceTerminals.class);
        ReferenceTerminals.addTerminal(gh1.getTerminal());
        assertEquals(1, ReferenceTerminals.getTerminals(network).size());
        assertTrue(ReferenceTerminals.getTerminals(network).contains(gh1.getTerminal()));
    }

    @Test
    void testVariants() {
        network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Set.of(gh1.getTerminal()))
                .add();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);

        // create variants
        String variant1 = "variant1";
        String variant2 = "variant2";
        List<String> targetVariantIds = Arrays.asList(variant1, variant2);
        variantManager.cloneVariant(INITIAL_VARIANT_ID, targetVariantIds);

        // add gh2 to variant1
        variantManager.setWorkingVariant(variant1);
        ext.addReferenceTerminal(gh2.getTerminal());
        // add gh3 to variant2
        variantManager.setWorkingVariant(variant2);
        ext.addReferenceTerminal(gh3.getTerminal());

        // initial variant unmodified
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertEquals(1, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().contains(gh1.getTerminal()));

        // check variant 1 as expected
        variantManager.setWorkingVariant(variant1);
        assertEquals(2, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh2.getTerminal())));

        // check variant 2 as expected
        variantManager.setWorkingVariant(variant2);
        assertEquals(2, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh3.getTerminal())));

        // clear variant 1
        variantManager.setWorkingVariant(variant1);
        ext.reset();

        // check variant 1 empty
        assertEquals(0, ext.getReferenceTerminals().size());

        // check other variants unchanged
        variantManager.setWorkingVariant(INITIAL_VARIANT_ID);
        assertTrue(ext.getReferenceTerminals().contains(gh1.getTerminal()));
        variantManager.setWorkingVariant(variant2);
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh3.getTerminal())));

        // test variant recycling in array
        String variant3 = "variant3";
        variantManager.removeVariant(variant1);
        variantManager.cloneVariant(variant2, variant3);
        variantManager.setWorkingVariant(variant3);
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh3.getTerminal())));

        // test array resize for coverage completeness
        variantManager.removeVariant(variant2);
    }

    @Test
    void testWrongNetwork() {
        Network other = EurostagTutorialExample1Factory.create();
        Terminal terminal = other.getBusBreakerView().getBus("NHV1").getConnectedTerminals().iterator().next();
        PowsyblException ex1 = assertThrows(PowsyblException.class, () -> network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Set.of(terminal))
                .add());
        assertEquals("Terminal given is not in the right Network (sim1 instead of fourSubstations)", ex1.getMessage());
        network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Set.of())
                .add();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        PowsyblException ex2 = assertThrows(PowsyblException.class, () -> ext.addReferenceTerminal(terminal));
        assertEquals("Terminal given is not in the right Network (sim1 instead of fourSubstations)", ex2.getMessage());
    }

    @Test
    void testRemoveEquipment() {
        network.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Set.of(gh1.getTerminal(), gh2.getTerminal()))
                .add();
        ReferenceTerminals ext = network.getExtension(ReferenceTerminals.class);
        assertEquals(2, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().containsAll(Set.of(gh1.getTerminal(), gh2.getTerminal())));
        // remove equipment
        gh1.remove();
        // check terminal removed from extension
        assertEquals(1, ext.getReferenceTerminals().size());
        assertTrue(ext.getReferenceTerminals().contains(gh2.getTerminal()));
    }

    @Test
    void testCleanup() {
        Network net = Mockito.spy(EurostagTutorialExample1Factory.create());

        net.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Collections.emptySet())
                .add();
        // check listener added
        Mockito.verify(net, Mockito.times(1)).addListener(Mockito.any());
        Mockito.verify(net, Mockito.times(0)).removeListener(Mockito.any());

        // overwrite existing extension
        net.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(Collections.emptySet())
                .add();
        // check old listener removed and new listener added
        Mockito.verify(net, Mockito.times(2)).addListener(Mockito.any());
        Mockito.verify(net, Mockito.times(1)).removeListener(Mockito.any());

        // remove extension
        net.removeExtension(ReferenceTerminals.class);
        // check all clean
        Mockito.verify(net, Mockito.times(2)).addListener(Mockito.any());
        Mockito.verify(net, Mockito.times(2)).removeListener(Mockito.any());
    }

}
