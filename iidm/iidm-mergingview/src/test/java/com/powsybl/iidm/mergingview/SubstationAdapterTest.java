/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class SubstationAdapterTest {

    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("SubstationAdapterTest", "iidm");
    }

    @Test
    public void baseTests() {
        // adder
        final Substation substation = mergingView.newSubstation()
                .setId("sub")
                .setName("sub_name")
                .setCountry(Country.AD)
                .setTso("TSO")
                .setEnsureIdUnicity(false)
                .setGeographicalTags("geoTag1", "geoTag2")
                .add();
        assertTrue(substation instanceof SubstationAdapter);
        assertEquals("sub", substation.getId());
        assertEquals("sub_name", substation.getName());
        assertEquals(Country.AD, substation.getCountry().orElse(null));
        assertEquals("TSO", substation.getTso());
        assertEquals(ContainerType.SUBSTATION, substation.getContainerType());

        // setter and getter
        substation.setCountry(Country.AF);
        assertEquals(Country.AF, substation.getCountry().orElse(null));
        assertEquals(Country.AF, substation.getNullableCountry());
        substation.setTso("new tso");
        assertEquals("new tso", substation.getTso());
        assertEquals(0, substation.getTwoWindingsTransformerCount());
        assertEquals(0, substation.getThreeWindingsTransformerCount());
        assertEquals(substation, substation.addGeographicalTag("geoTag3"));
        assertEquals(3, substation.getGeographicalTags().size());

        // Properties
        final String key = "keyTest";
        final String value = "ValueTest";
        assertFalse(substation.hasProperty());
        substation.setProperty(key, value);
        assertTrue(substation.hasProperty(key));
        assertEquals(value, substation.getProperty(key));
        assertEquals("defaultValue", substation.getProperty("noFound", "defaultValue"));
        assertEquals(1, substation.getPropertyNames().size());

        // Extension
        assertTrue(substation.getExtensions().isEmpty());
        final SubstationExtensionTest extension = new SubstationExtensionTest(null);
        assertNotNull(extension);
        substation.addExtension(SubstationExtensionTest.class, extension);
        assertSame(extension, substation.getExtension(SubstationExtensionTest.class));
        assertSame(extension, substation.getExtensionByName("SubstationExtensionTest"));
        assertNull(substation.getExtension(String.class));
        assertEquals(1, substation.getExtensions().size());
        assertArrayEquals(substation.getExtensions().toArray(new Extension[0]), new Extension[] {extension});
        assertTrue(substation.removeExtension(SubstationExtensionTest.class));

        // VoltageLevel
        final VoltageLevel v1 = substation.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0)
                .setLowVoltageLimit(100.0)
                .setHighVoltageLimit(200.0)
                .add();
        assertTrue(v1 instanceof VoltageLevelAdapter);
        assertTrue(substation.getVoltageLevelStream().findAny().isPresent());
        assertSame(v1, substation.getVoltageLevels().iterator().next());

        // Not implemented yet !
        TestUtil.notImplemented(substation::newTwoWindingsTransformer);
        TestUtil.notImplemented(substation::getTwoWindingsTransformers);
        TestUtil.notImplemented(substation::getTwoWindingsTransformerStream);
        TestUtil.notImplemented(substation::newThreeWindingsTransformer);
        TestUtil.notImplemented(substation::getThreeWindingsTransformers);
        TestUtil.notImplemented(substation::getThreeWindingsTransformerStream);
    }
}
