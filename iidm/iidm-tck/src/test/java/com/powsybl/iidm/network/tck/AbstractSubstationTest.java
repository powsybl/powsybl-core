/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractSubstationTest {

    private static final String SUB_NAME = "sub_name";

    private Network network;

    @BeforeEach
    public void initNetwork() {
        network = Network.create("test", "test");
    }

    @Test
    public void baseTests() {
        // adder
        Substation substation = network.newSubstation()
                                    .setId("sub")
                                    .setName(SUB_NAME)
                                    .setCountry(Country.AD)
                                    .setTso("TSO")
                                    .setEnsureIdUnicity(false)
                                    .setGeographicalTags("geoTag1", "geoTag2")
                                .add();
        assertEquals("sub", substation.getId());
        assertEquals(SUB_NAME, substation.getOptionalName().orElse(null));
        assertEquals(SUB_NAME, substation.getNameOrId());
        assertEquals(Country.AD, substation.getCountry().orElse(null));
        assertEquals("TSO", substation.getTso());
        assertEquals(ContainerType.SUBSTATION, substation.getContainerType());

        // setter and getter
        substation.setCountry(Country.AF);
        assertEquals(Country.AF, substation.getCountry().orElse(null));
        substation.setTso("new tso");
        assertEquals("new tso", substation.getTso());

        // Create mocked network listeners
        NetworkListener exceptionListener = mock(DefaultNetworkListener.class);
        doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementAdded(any(), anyString(), any());
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Test without listeners registered
        substation.addGeographicalTag("no listeners");
        verifyNoMoreInteractions(mockedListener);
        verifyNoMoreInteractions(exceptionListener);
        // Add observer changes to current network
        network.addListener(exceptionListener);
        network.addListener(mockedListener);
        // Change in order to raise update notification
        substation.addGeographicalTag("test");
        // Check notification done
        verify(mockedListener, times(1))
               .onElementAdded(any(Substation.class), anyString(), anyString());
        // Remove observer
        network.removeListener(mockedListener);

        // Remove
        substation.remove();
        assertNotNull(substation);
        try {
            substation.getNetwork();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access network of removed substation sub", e.getMessage());
        }
    }

    @Test
    public void emptyCountry() {
        Substation s = network.newSubstation()
                .setId("undefined_country")
                .add();
        assertFalse(s.getCountry().isPresent());
    }

    @Test
    public void addNullTag() {
        Substation substation = network.newSubstation()
                                        .setId("sub")
                                        .setName(SUB_NAME)
                                        .setCountry(Country.AD)
                                        .setTso("TSO")
                                        .setEnsureIdUnicity(false)
                                        .setGeographicalTags("geoTag1", "geoTag2")
                                    .add();
        ValidationException e = assertThrows(ValidationException.class, () -> substation.addGeographicalTag(null));
        assertTrue(e.getMessage().contains("geographical tag is null"));
    }

    @Test
    public void duplicateSubstation() {
        network.newSubstation()
                .setId("duplicate")
                .setName(SUB_NAME)
                .setCountry(Country.AD)
            .add();
        PowsyblException e = assertThrows(PowsyblException.class, () ->
                network.newSubstation()
                        .setId("duplicate")
                        .setName(SUB_NAME)
                        .setCountry(Country.AD)
                        .add());
        assertTrue(e.getMessage().contains("with the id 'duplicate'"));
    }

}
