/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

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
        Substation substation = mergingView.newSubstation()
                .setId("subAdapted")
                .setName("subAdapted_name")
                .setCountry(Country.AD)
                .setTso("TSO")
                .setEnsureIdUnicity(false)
                .setGeographicalTags("geoTag1", "geoTag2")
                .add();
        assertNotNull(substation);
        assertEquals("subAdapted", substation.getId());
        assertEquals("subAdapted_name", substation.getOptionalName().orElse(null));
        assertEquals("subAdapted_name", substation.getNameOrId());
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
        final AbstractExtension extension = Mockito.mock(AbstractExtension.class);
        Mockito.when(extension.getName()).thenReturn("SubstationExtension");
        assertNotNull(extension);
        substation.addExtension(AbstractExtension.class, extension);
        assertSame(extension, substation.getExtension(AbstractExtension.class));
        assertSame(extension, substation.getExtensionByName("SubstationExtension"));
        assertNull(substation.getExtension(String.class));
        assertEquals(1, substation.getExtensions().size());
        assertArrayEquals(substation.getExtensions().toArray(new Extension[0]), new Extension[] {extension});
        assertTrue(substation.removeExtension(AbstractExtension.class));

        // VoltageLevel
        final VoltageLevel v1 = substation.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0)
                .setLowVoltageLimit(100.0)
                .setHighVoltageLimit(200.0)
                .add();
        assertNotNull(v1);
        assertTrue(substation.getVoltageLevelStream().findAny().isPresent());
        assertSame(v1, substation.getVoltageLevels().iterator().next());

        // TwoWindingsTransformer
        mergingView.merge(NoEquipmentNetworkFactory.create());
        substation = mergingView.getSubstation("sub");
        final TwoWindingsTransformer t2wt = substation.newTwoWindingsTransformer()
                .setId("2wt")
                .setName("twt_name")
                .setEnsureIdUnicity(false)
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .setBus1("busA")
                .setConnectableBus1("busA")
                .setBus2("busB")
                .setConnectableBus2("busB")
                .add();
        assertSame(t2wt, substation.getTwoWindingsTransformers().iterator().next());
        assertEquals(substation.getThreeWindingsTransformerStream().count(), substation.getThreeWindingsTransformerCount());

        // ThreeWindingsTransformer
        final ThreeWindingsTransformer t3wt = substation.newThreeWindingsTransformer()
                    .setId("3wt")
                    .setName("twtName")
                    .setEnsureIdUnicity(false)
                    .setRatedU0(132.0d)
                    .newLeg1()
                        .setR(1.3)
                        .setX(1.4)
                        .setG(1.6)
                        .setB(1.7)
                        .setRatedU(1.1)
                        .setVoltageLevel("vl1")
                        .setConnectableBus("busA")
                        .setBus("busA")
                        .setRatedU(1.0d)
                    .add()
                    .newLeg2()
                        .setR(2.03)
                        .setX(2.04)
                        .setRatedU(2.05)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                        .setRatedU(2.0d)
                    .add()
                    .newLeg3()
                        .setR(3.3)
                        .setX(3.4)
                        .setRatedU(3.5)
                        .setVoltageLevel("vl2")
                        .setBus("busB")
                        .setConnectableBus("busB")
                        .setRatedU(3.0d)
                    .add()
                .add();
        assertSame(t3wt, substation.getThreeWindingsTransformers().iterator().next());
        assertEquals(substation.getTwoWindingsTransformerStream().count(), substation.getTwoWindingsTransformerCount());
    }
}
