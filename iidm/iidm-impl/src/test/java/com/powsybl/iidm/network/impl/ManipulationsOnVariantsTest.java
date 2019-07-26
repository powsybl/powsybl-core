/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class ManipulationsOnVariantsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VariantManager variantManager;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        variantManager = network.getVariantManager();
    }

    @Test
    public void errorRemoveInitialVariant() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Removing initial variant is forbidden");
        variantManager.removeVariant(INITIAL_VARIANT_ID);
    }

    @Test
    public void errorNotExistingVariant() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("not found");
        variantManager.removeVariant("not_exists");
    }

    @Test
    public void errorCloneToEmptyVariants() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Empty target variant id list");
        variantManager.cloneVariant(INITIAL_VARIANT_ID, Collections.emptyList());
    }

    @Test
    public void errorCloneToExistingVariant() {
        variantManager.cloneVariant(INITIAL_VARIANT_ID, "hello");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("already exists");
        variantManager.cloneVariant(INITIAL_VARIANT_ID, "hello");
    }

    @Test
    public void baseTests() {
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);

        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        // extend
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variantsToAdd);
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s2", "s3", "s4"), variantManager.getVariantIds());

        // Check variant creation notification
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s1");
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s2");
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s3");
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s4");

        // delete
        variantManager.removeVariant("s2");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s3", "s4"), variantManager.getVariantIds());

        // Check variant removal notification
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantRemoved("s2");
        // No notification for Allocation & Reduction
        Mockito.verifyNoMoreInteractions(mockedListener);

        // allocate
        variantManager.cloneVariant("s4", "s2b");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s2b", "s3", "s4"), variantManager.getVariantIds());

        // Overwrite
        variantManager.cloneVariant("s4", "s2b", true);
        Mockito.verify(mockedListener, Mockito.times(1)).onVariantOverwritten("s4", "s2b");

        // reduce
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s2b", "s1", "s3"), variantManager.getVariantIds());

        try {
            variantManager.getWorkingVariantId();
            fail();
        } catch (Exception ignored) {
        }

        network.removeListener(mockedListener);
    }
}
