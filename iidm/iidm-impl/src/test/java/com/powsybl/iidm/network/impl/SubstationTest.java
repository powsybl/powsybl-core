/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class SubstationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;

    @Before
    public void initNetwork() {
        network = NetworkFactory.create("test", "test");
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
        assertEquals(Country.AD, substation.getCountry());
        assertEquals("TSO", substation.getTso());
        assertEquals(ContainerType.SUBSTATION, substation.getContainerType());

        // setter and getter
        substation.setCountry(Country.AF);
        assertEquals(Country.AF, substation.getCountry());
        substation.setTso("new tso");
        assertEquals("new tso", substation.getTso());
    }

    @Test
    public void invalidCountry() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("country is invalid");
        network.newSubstation()
                .setId("no_country")
                .setName("sub_name")
            .add();
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
