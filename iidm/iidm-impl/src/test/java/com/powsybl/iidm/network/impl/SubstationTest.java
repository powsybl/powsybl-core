/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;
import com.powsybl.iidm.network.Substation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubstationTest {

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
                                    .setName("sub_name")
                                    .setCountry(Country.AD)
                                    .setTso("TSO")
                                    .setEnsureIdUnicity(false)
                                    .setGeographicalTags("geoTag1", "geoTag2")
                                .add();
        assertEquals("sub", substation.getId());
        assertEquals("sub_name", substation.getName());
        assertEquals(Country.AD, substation.getCountry().orElse(null));
        assertEquals("TSO", substation.getTso());
        assertEquals(ContainerType.SUBSTATION, substation.getContainerType());

        // setter and getter
        substation.setCountry(Country.AF);
        assertEquals(Country.AF, substation.getCountry().orElse(null));
        substation.setTso("new tso");
        assertEquals("new tso", substation.getTso());

        // Create mocked network listeners
        NetworkListener exceptionListener = Mockito.mock(DefaultNetworkListener.class);
        Mockito.doThrow(new UnsupportedOperationException()).when(exceptionListener).onElementAdded(Mockito.any(), Mockito.anyString(), Mockito.any());
        NetworkListener mockedListener = Mockito.mock(DefaultNetworkListener.class);
        // Test without listeners registered
        substation.addGeographicalTag("no listeners");
        Mockito.verifyNoMoreInteractions(mockedListener);
        Mockito.verifyNoMoreInteractions(exceptionListener);
        // Add observer changes to current network
        network.addListener(exceptionListener);
        network.addListener(mockedListener);
        // Change in order to raise update notification
        substation.addGeographicalTag("test");
        // Check notification done
        Mockito.verify(mockedListener, Mockito.times(1))
               .onElementAdded(Mockito.any(Substation.class), Mockito.anyString(), Mockito.anyString());
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
                                        .setName("sub_name")
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
                .setName("sub_name")
                .setCountry(Country.AD)
            .add();
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'duplicate'");
        network.newSubstation()
                .setId("duplicate")
                .setName("sub_name")
                .setCountry(Country.AD)
            .add();
    }

}
