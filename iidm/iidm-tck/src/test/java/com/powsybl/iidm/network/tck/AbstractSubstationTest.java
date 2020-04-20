/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public abstract class AbstractSubstationTest {

    private static final String SUB_NAME = "sub_name";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;

    @Before
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
        thrown.expect(ValidationException.class);
        Substation substation = network.newSubstation()
                                        .setId("sub")
                                        .setName(SUB_NAME)
                                        .setCountry(Country.AD)
                                        .setTso("TSO")
                                        .setEnsureIdUnicity(false)
                                        .setGeographicalTags("geoTag1", "geoTag2")
                                    .add();
        thrown.expect(ValidationException.class);
        thrown.expectMessage("geographical tag is null");
        substation.addGeographicalTag(null);
    }

    @Test
    public void duplicateSubstation() {
        network.newSubstation()
                .setId("duplicate")
                .setName(SUB_NAME)
                .setCountry(Country.AD)
            .add();
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'duplicate'");
        network.newSubstation()
                .setId("duplicate")
                .setName(SUB_NAME)
                .setCountry(Country.AD)
            .add();
    }

}
