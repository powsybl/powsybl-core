/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.network.VariantManagerConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Yichen Tang {@literal <yichen.tang at rte-france.com>}
 */
public abstract class AbstractManipulationsOnVariantsTest {

    private Network network;
    private VariantManager variantManager;

    @BeforeEach
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        variantManager = network.getVariantManager();
    }

    @Test
    public void errorRemoveInitialVariant() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> variantManager.removeVariant(INITIAL_VARIANT_ID));
        assertEquals("Removing initial variant is forbidden", e.getMessage());
    }

    @Test
    public void errorNotExistingVariant() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> variantManager.removeVariant("not_exists"));
        assertTrue(e.getMessage().contains("not found"));
    }

    @Test
    public void errorCloneToEmptyVariants() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> variantManager.cloneVariant(INITIAL_VARIANT_ID, Collections.emptyList()));
        assertEquals("Empty target variant id list", e.getMessage());
    }

    @Test
    public void errorCloneToExistingVariant() {
        variantManager.cloneVariant(INITIAL_VARIANT_ID, "hello");
        PowsyblException e = assertThrows(PowsyblException.class, () -> variantManager.cloneVariant(INITIAL_VARIANT_ID, "hello"));
        assertTrue(e.getMessage().contains("already exists"));
    }

    @Test
    public void baseTests() {
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onVariantCreated(any(), anyString());
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onVariantOverwritten(any(), any());
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onVariantRemoved(any());

        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(exceptionListener);
        network.addListener(mockedListener);

        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        // extend
        variantManager.cloneVariant(INITIAL_VARIANT_ID, variantsToAdd);
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s2", "s3", "s4"), variantManager.getVariantIds());

        // Check variant creation notification
        verify(mockedListener, times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s1");
        verify(mockedListener, times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s2");
        verify(mockedListener, times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s3");
        verify(mockedListener, times(1)).onVariantCreated(INITIAL_VARIANT_ID, "s4");

        // delete
        variantManager.removeVariant("s2");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s3", "s4"), variantManager.getVariantIds());

        // Check variant removal notification
        verify(mockedListener, times(1)).onVariantRemoved("s2");
        // No notification for Allocation & Reduction
        verifyNoMoreInteractions(mockedListener);

        // allocate
        variantManager.cloneVariant("s4", "s2b");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s1", "s2b", "s3", "s4"), variantManager.getVariantIds());

        // Overwrite
        variantManager.cloneVariant("s4", "s2b", true);
        verify(mockedListener, times(1)).onVariantOverwritten("s4", "s2b");

        // reduce
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        assertEquals(Sets.newHashSet(INITIAL_VARIANT_ID, "s2b", "s1", "s3"), variantManager.getVariantIds());

        try {
            variantManager.getWorkingVariantId();
            fail();
        } catch (Exception ignored) {
            // ignore
        }

        network.removeListener(mockedListener);
    }
}
