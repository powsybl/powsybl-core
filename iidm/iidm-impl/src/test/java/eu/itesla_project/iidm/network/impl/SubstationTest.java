/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.NetworkFactory;
import eu.itesla_project.iidm.network.Substation;
import org.junit.Test;

import static org.junit.Assert.*;

public class SubstationTest {
    @Test
    public void testSetterGetter() {
        Network network = NetworkFactory.create("test", "test");
        Substation substation = network.newSubstation()
                                    .setId("sub")
                                    .setName("sub_name")
                                    .setCountry(Country.AD)
                                    .setTso("TSO")
                                    .setEnsureIdUnicity(false).setGeographicalTags("geoTag1", "geoTag2")
                                .add();
        assertEquals("sub", substation.getId());
        assertEquals("sub_name", substation.getName());
        assertEquals(Country.AD, substation.getCountry());
        assertEquals("TSO", substation.getTso());

        substation.setCountry(Country.AF);
        assertEquals(Country.AF, substation.getCountry());
        substation.setTso("new tso");
        assertEquals("new tso", substation.getTso());
    }
}