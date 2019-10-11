/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ContainerType;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VariantManagerConstants;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class MergingNetworkTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    MergingView mergingView;
    Network n1;
    Network n2;
    Network n3;

    @Before
    public void setup() {
        mergingView = MergingView.create();

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
        assertEquals("n1, n2", mergingView.getId());
        assertEquals("n1, n2", mergingView.getName());

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
        final Substation s1 = addSubstation(mergingView, "S1", Country.FR);
        assertSame(mergingView, s1.getNetwork());
        assertEquals(1, mergingView.getCountryCount());
        addSubstation(n2, "S2", Country.ES);
        assertEquals(2, mergingView.getCountryCount());
        assertNull(mergingView.getSubstation("S0"));
        assertSame(s1, mergingView.getSubstation("S1"));
        assertEquals(2, mergingView.getSubstationCount());
        assertTrue(mergingView.getSubstations(Country.FR, "RTE", "A").iterator().hasNext());
        assertFalse(mergingView.getSubstations(Country.BE, "RTE", "A").iterator().hasNext());
        assertFalse(mergingView.getSubstations((String) null, "FAIL").iterator().hasNext());
        assertTrue(mergingView.getSubstations(Country.ES, null).iterator().hasNext());
        assertFalse(mergingView.getSubstations(Country.FR, "RTE", "B").iterator().hasNext());

        // Not implemented yet !
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Not implemented exception");

        TestUtil.notImplemented(mergingView::getVariantManager);
        TestUtil.notImplemented(mergingView::getVoltageLevels);
        TestUtil.notImplemented(mergingView::getVoltageLevelStream);
        TestUtil.notImplemented(mergingView::getVoltageLevelCount);
        TestUtil.notImplemented(() -> mergingView.getVoltageLevel(""));
        TestUtil.notImplemented(mergingView::newLine);
        TestUtil.notImplemented(mergingView::getLines);
        TestUtil.notImplemented(() -> mergingView.getBranch(""));
        TestUtil.notImplemented(mergingView::getBranches);
        TestUtil.notImplemented(mergingView::getBranchStream);
        TestUtil.notImplemented(mergingView::getBranchCount);
        TestUtil.notImplemented(mergingView::getLineStream);
        TestUtil.notImplemented(mergingView::getLineCount);
        TestUtil.notImplemented(() -> mergingView.getLine(""));
        TestUtil.notImplemented(mergingView::newTieLine);
        TestUtil.notImplemented(mergingView::getTwoWindingsTransformers);
        TestUtil.notImplemented(mergingView::getTwoWindingsTransformerStream);
        TestUtil.notImplemented(mergingView::getTwoWindingsTransformerCount);
        TestUtil.notImplemented(() -> mergingView.getTwoWindingsTransformer(""));
        TestUtil.notImplemented(mergingView::getThreeWindingsTransformers);
        TestUtil.notImplemented(mergingView::getThreeWindingsTransformerStream);
        TestUtil.notImplemented(mergingView::getThreeWindingsTransformerCount);
        TestUtil.notImplemented(() -> mergingView.getThreeWindingsTransformer(""));
        TestUtil.notImplemented(mergingView::getGenerators);
        TestUtil.notImplemented(mergingView::getGeneratorStream);
        TestUtil.notImplemented(mergingView::getGeneratorCount);
        TestUtil.notImplemented(() -> mergingView.getGenerator(""));
        TestUtil.notImplemented(mergingView::getBatteries);
        TestUtil.notImplemented(mergingView::getBatteryStream);
        TestUtil.notImplemented(mergingView::getBatteryCount);
        TestUtil.notImplemented(() -> mergingView.getBattery(""));
        TestUtil.notImplemented(mergingView::getLoads);
        TestUtil.notImplemented(mergingView::getLoadStream);
        TestUtil.notImplemented(mergingView::getLoadCount);
        TestUtil.notImplemented(() -> mergingView.getLoad(""));
        TestUtil.notImplemented(mergingView::getShuntCompensators);
        TestUtil.notImplemented(mergingView::getShuntCompensatorStream);
        TestUtil.notImplemented(mergingView::getShuntCompensatorCount);
        TestUtil.notImplemented(() -> mergingView.getShuntCompensator(""));
        TestUtil.notImplemented(mergingView::getDanglingLines);
        TestUtil.notImplemented(mergingView::getDanglingLineStream);
        TestUtil.notImplemented(mergingView::getDanglingLineCount);
        TestUtil.notImplemented(() -> mergingView.getDanglingLine(""));
        TestUtil.notImplemented(mergingView::getStaticVarCompensators);
        TestUtil.notImplemented(mergingView::getStaticVarCompensatorStream);
        TestUtil.notImplemented(mergingView::getStaticVarCompensatorCount);
        TestUtil.notImplemented(() -> mergingView.getStaticVarCompensator(""));
        TestUtil.notImplemented(() -> mergingView.getSwitch(""));
        TestUtil.notImplemented(mergingView::getSwitches);
        TestUtil.notImplemented(mergingView::getSwitchStream);
        TestUtil.notImplemented(mergingView::getSwitchCount);
        TestUtil.notImplemented(() -> mergingView.getBusbarSection(""));
        TestUtil.notImplemented(mergingView::getBusbarSections);
        TestUtil.notImplemented(mergingView::getBusbarSectionStream);
        TestUtil.notImplemented(mergingView::getBusbarSectionCount);
        TestUtil.notImplemented(mergingView::getHvdcConverterStations);
        TestUtil.notImplemented(mergingView::getHvdcConverterStationStream);
        TestUtil.notImplemented(mergingView::getHvdcConverterStationCount);
        TestUtil.notImplemented(() -> mergingView.getHvdcConverterStation(""));
        TestUtil.notImplemented(mergingView::getLccConverterStations);
        TestUtil.notImplemented(mergingView::getLccConverterStationStream);
        TestUtil.notImplemented(mergingView::getLccConverterStationCount);
        TestUtil.notImplemented(() -> mergingView.getLccConverterStation(""));
        TestUtil.notImplemented(mergingView::getVscConverterStations);
        TestUtil.notImplemented(mergingView::getVscConverterStationStream);
        TestUtil.notImplemented(mergingView::getVscConverterStationCount);
        TestUtil.notImplemented(() -> mergingView.getVscConverterStation(""));
        TestUtil.notImplemented(mergingView::getHvdcLines);
        TestUtil.notImplemented(mergingView::getHvdcLineStream);
        TestUtil.notImplemented(mergingView::getHvdcLineCount);
        TestUtil.notImplemented(() -> mergingView.getHvdcLine(""));
        TestUtil.notImplemented(mergingView::newHvdcLine);
        TestUtil.notImplemented(mergingView::getBusBreakerView);
        TestUtil.notImplemented(mergingView::getBusView);
        TestUtil.notImplemented(() -> mergingView.addListener(null));
        TestUtil.notImplemented(() -> mergingView.removeListener(null));
        TestUtil.notImplemented(() -> mergingView.addExtension(null, null));
        TestUtil.notImplemented(() -> mergingView.getExtension(null));
        TestUtil.notImplemented(() -> mergingView.getExtensionByName(null));
        TestUtil.notImplemented(() -> mergingView.removeExtension(null));
        TestUtil.notImplemented(mergingView::getExtensions);
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
        addSubstation(n1, "P1", Country.FR);
        addSubstation(n2, "P1", Country.FR);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The following object(s) exist(s) in both networks: P1");
        mergingView.merge(n1, n2);
    }

    @Test
    public void failAddSameObj() {
        addSubstation(mergingView, "P1", Country.FR);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The network workingNetwork already contains an object 'SubstationImpl' with the id 'P1'");
        addSubstation(mergingView, "P1", Country.FR);
    }

    private Substation addSubstation(final Network network, final String substationId, final Country country) {
        return network.newSubstation()
                .setId(substationId)
                .setName(substationId)
                .setEnsureIdUnicity(false)
                .setCountry(country)
                .setTso("RTE")
                .setGeographicalTags("A")
                .add();
    }
}
