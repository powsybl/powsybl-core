/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MergingNetworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;
    private Network n1;
    private Network n2;
    private Network n3;

    @Before
    public void setup() {
        mergingView = MergingView.create("MergingNetworkTest", "iidm");

        n1 = Network.create("n1", "iidm");
        n2 = Network.create("n2", "iidm");
        n3 = Network.create("n3", "ucte");
    }

    @Test
    public void testSetterGetter() {
        mergingView.merge(n1, n2);
        assertSame(mergingView, mergingView.getNetwork());
        assertSame(mergingView, mergingView.getIdentifiable("n1").getNetwork());
        assertSame(mergingView, mergingView.getIdentifiable("n2").getNetwork());
        assertEquals("MergingNetworkTest", mergingView.getId());
        assertEquals("MergingNetworkTest", mergingView.getName());

        final DateTime caseDate = new DateTime();
        mergingView.setCaseDate(caseDate);
        assertEquals(caseDate, mergingView.getCaseDate());
        mergingView.setForecastDistance(3);
        assertEquals(3, mergingView.getForecastDistance());
        assertEquals(ContainerType.NETWORK, mergingView.getContainerType());

        assertEquals("iidm", mergingView.getSourceFormat());
        mergingView.merge(n3);
        assertEquals("hybrid", mergingView.getSourceFormat());

        // Properties
        final String key = "keyTest";
        final String value = "ValueTest";
        assertFalse(mergingView.hasProperty());
        mergingView.setProperty(key, value);
        assertTrue(mergingView.hasProperty(key));
        assertEquals(value, mergingView.getProperty(key));
        assertEquals("defaultValue", mergingView.getProperty("noFound", "defaultValue"));
        assertEquals(1, mergingView.getPropertyNames().size());

        // Identifiables
        assertFalse("MergingView cannot be empty", mergingView.getIdentifiables().isEmpty());

        // Substations
        assertTrue(mergingView.getCountries().isEmpty());
        final Substation s1 = addSubstation(mergingView, "S1", Country.FR, false);
        assertSame(mergingView, s1.getNetwork());
        assertEquals(1, mergingView.getCountryCount());
        addSubstation(n2, "S2", Country.ES, false);
        assertEquals(2, mergingView.getCountryCount());
        assertTrue(mergingView.getSubstations().iterator().hasNext());
        assertNull(mergingView.getSubstation("S0"));
        assertSame(s1, mergingView.getSubstation("S1"));
        assertEquals(2, mergingView.getSubstationCount());
        assertTrue(mergingView.getSubstations(Country.FR, "RTE", "A").iterator().hasNext());
        assertFalse(mergingView.getSubstations(Country.BE, "RTE", "A").iterator().hasNext());
        assertFalse(mergingView.getSubstations((String) null, "FAIL").iterator().hasNext());
        assertTrue(mergingView.getSubstations(Country.ES, null).iterator().hasNext());
        assertFalse(mergingView.getSubstations(Country.FR, "RTE", "B").iterator().hasNext());

        // VoltageLevel
        mergingView.merge(NoEquipmentNetworkFactory.create());
        assertTrue(mergingView.getVoltageLevel("vl1") instanceof VoltageLevelAdapter);
        assertTrue(mergingView.getVoltageLevels().iterator().hasNext());
        assertEquals(2, mergingView.getVoltageLevelStream().count());
        assertEquals(2, mergingView.getVoltageLevelCount());

        // Batteries
        assertFalse(mergingView.getBatteries().iterator().hasNext());
        assertEquals(0, mergingView.getBatteryStream().count());
        assertEquals(0, mergingView.getBatteryCount());

        // VscConverterStations
        assertFalse(mergingView.getVscConverterStations().iterator().hasNext());
        assertEquals(0, mergingView.getVscConverterStationStream().count());
        assertEquals(0, mergingView.getVscConverterStationCount());

        // TwoWindingsTransformers
        assertFalse(mergingView.getTwoWindingsTransformers().iterator().hasNext());
        assertEquals(0, mergingView.getTwoWindingsTransformerStream().count());
        assertEquals(0, mergingView.getTwoWindingsTransformerCount());

        // Switch
        assertFalse(mergingView.getSwitches().iterator().hasNext());
        assertEquals(0, mergingView.getSwitchStream().count());
        assertEquals(0, mergingView.getSwitchCount());

        // StaticVarCompensators
        assertFalse(mergingView.getStaticVarCompensators().iterator().hasNext());
        assertEquals(0, mergingView.getStaticVarCompensatorStream().count());
        assertEquals(0, mergingView.getStaticVarCompensatorCount());

        // ShuntCompensators
        assertFalse(mergingView.getShuntCompensators().iterator().hasNext());
        assertEquals(0, mergingView.getShuntCompensatorStream().count());
        assertEquals(0, mergingView.getShuntCompensatorCount());

        // Loads
        assertFalse(mergingView.getLoads().iterator().hasNext());
        assertEquals(0, mergingView.getLoadStream().count());
        assertEquals(0, mergingView.getLoadCount());

        // Generator
        assertFalse(mergingView.getGenerators().iterator().hasNext());
        assertEquals(0, mergingView.getGeneratorStream().count());
        assertEquals(0, mergingView.getGeneratorCount());

        // BusbarSection
        assertFalse(mergingView.getBusbarSections().iterator().hasNext());
        assertEquals(0, mergingView.getBusbarSectionStream().count());
        assertEquals(0, mergingView.getBusbarSectionCount());

        // LccConverterStations
        assertFalse(mergingView.getLccConverterStations().iterator().hasNext());
        assertEquals(0, mergingView.getLccConverterStationStream().count());
        assertEquals(0, mergingView.getLccConverterStationCount());

        // HvdcConverterStations
        assertFalse(mergingView.getHvdcConverterStations().iterator().hasNext());
        assertEquals(0, mergingView.getHvdcConverterStationStream().count());
        assertEquals(0, mergingView.getHvdcConverterStationCount());

        // Branches
        assertFalse(mergingView.getBranches().iterator().hasNext());
        assertEquals(0, mergingView.getBranchStream().count());
        assertEquals(0, mergingView.getBranchCount());

        // ThreeWindingsTransformers
        assertFalse(mergingView.getThreeWindingsTransformers().iterator().hasNext());
        assertEquals(0, mergingView.getThreeWindingsTransformerStream().count());
        assertEquals(0, mergingView.getThreeWindingsTransformerCount());

        // Lines
        assertFalse(mergingView.getLines().iterator().hasNext());
        assertEquals(0, mergingView.getLineStream().count());
        assertEquals(0, mergingView.getLineCount());

        // DanglingLines
        assertFalse(mergingView.getDanglingLines().iterator().hasNext());
        assertEquals(0, mergingView.getDanglingLineStream().count());
        assertEquals(0, mergingView.getDanglingLineCount());

        // HvdcLines
        assertFalse(mergingView.getHvdcLines().iterator().hasNext());
        assertEquals(0, mergingView.getHvdcLineStream().count());
        assertEquals(0, mergingView.getHvdcLineCount());

        // Others
        assertNotNull(mergingView.getBusBreakerView());
        assertNotNull(mergingView.getBusView());

        // Extensions
        AbstractExtension extension = Mockito.mock(AbstractExtension.class);
        Mockito.when(extension.getName()).thenReturn("NetworkExtension");

        mergingView.addExtension(AbstractExtension.class, extension);
        assertNotNull(mergingView.getExtension(AbstractExtension.class));
        assertNotNull(mergingView.getExtensionByName("NetworkExtension"));
        mergingView.removeExtension(AbstractExtension.class);
        assertTrue(mergingView.getExtensions().isEmpty());

        // Not implemented yet !
        TestUtil.notImplemented(mergingView::getVariantManager);
        // Lines
        TestUtil.notImplemented(mergingView::newLine);
        TestUtil.notImplemented(mergingView::newTieLine);
        // Listeners
        TestUtil.notImplemented(() -> mergingView.addListener(null));
        TestUtil.notImplemented(() -> mergingView.removeListener(null));
    }

    @Test
    public void failMergeIfMultiVariants() {
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Failed");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Merging of multi-variants network is not supported");
        mergingView.merge(n1);
    }

    @Test
    public void failMergeWithSameObj() {
        addSubstation(n1, "P1", Country.FR, false);
        addSubstation(n2, "P1", Country.FR, false);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The object 'P1' already exists into merging view");
        mergingView.merge(n1, n2);
    }

    @Test
    public void failAddSameObj() {
        addSubstation(mergingView, "P1", Country.FR, false);
        Substation autoIdSub = addSubstation(mergingView, "P1", Country.FR, true);
        assertSame(autoIdSub, mergingView.getSubstation("P1#0"));
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The network already contains an object 'SubstationAdderImpl' with the id 'P1'");
        addSubstation(mergingView, "P1", Country.FR, false);
    }

    private static Substation addSubstation(final Network network, final String substationId, final Country country, boolean ensureIdUnicity) {
        return network.newSubstation()
                .setId(substationId)
                .setName(substationId)
                .setEnsureIdUnicity(ensureIdUnicity)
                .setCountry(country)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
    }
}
