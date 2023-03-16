/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class MergingNetworkTest {

    private MergingView mergingView;
    private Network n1;
    private Network n2;
    private Network n3;

    @BeforeEach
    void setup() {
        mergingView = MergingView.create("MergingNetworkTest", "iidm");

        n1 = Network.create("n1", "iidm");
        n2 = Network.create("n2", "iidm");
        n3 = Network.create("n3", "ucte");
    }

    @Test
    void testSetterGetter() {
        mergingView.merge(n1, n2);
        assertSame(mergingView, mergingView.getNetwork());
        assertSame(mergingView, mergingView.getIdentifiable("n1").getNetwork());
        assertSame(mergingView, mergingView.getIdentifiable("n2").getNetwork());
        assertEquals("MergingNetworkTest", mergingView.getId());
        assertEquals("MergingNetworkTest", mergingView.getOptionalName().orElse(null));
        assertEquals("MergingNetworkTest", mergingView.getNameOrId());

        final DateTime caseDate = new DateTime();
        mergingView.setCaseDate(caseDate);
        assertEquals(caseDate, mergingView.getCaseDate());
        mergingView.setForecastDistance(3);
        assertEquals(3, mergingView.getForecastDistance());
        assertEquals(ContainerType.NETWORK, mergingView.getContainerType());

        assertEquals("iidm", mergingView.getSourceFormat());
        mergingView.merge(n3);
        assertEquals("hybrid", mergingView.getSourceFormat());

        mergingView.setFictitious(true);
        assertTrue(mergingView.isFictitious());

        // Properties
        final String key = "keyTest";
        final String value = "ValueTest";
        assertFalse(mergingView.hasProperty());
        mergingView.setProperty(key, value);
        assertTrue(mergingView.hasProperty(key));
        assertEquals(value, mergingView.getProperty(key));
        assertEquals("defaultValue", mergingView.getProperty("noFound", "defaultValue"));
        assertEquals(1, mergingView.getPropertyNames().size());
        assertFalse(mergingView.removeProperty("noFound"));
        assertTrue(mergingView.removeProperty("keyTest"));
        assertFalse(mergingView.hasProperty("keyTest"));
        assertFalse(mergingView.hasProperty());

        // Identifiables
        assertFalse(mergingView.getIdentifiables().isEmpty(), "MergingView cannot be empty");

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
        assertTrue(mergingView.getVoltageLevel("vl1") instanceof BusBreakerVoltageLevelAdapter);
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
        assertArrayEquals(Iterables.toArray(mergingView.getStaticVarCompensators(), StaticVarCompensator.class),
                Iterables.toArray(mergingView.getConnectables(StaticVarCompensator.class), StaticVarCompensator.class));
        assertFalse(mergingView.getConnectables(StaticVarCompensator.class).iterator().hasNext());
        assertEquals(0, mergingView.getConnectableStream(StaticVarCompensator.class).count());
        assertEquals(0, mergingView.getConnectableCount(StaticVarCompensator.class));

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
        assertArrayEquals(Iterables.toArray(mergingView.getLines(), Line.class), Iterables.toArray(mergingView.getConnectables(Line.class), Line.class));
        assertEquals(0, mergingView.getConnectableStream(Line.class).count());
        assertEquals(0, mergingView.getConnectableCount(Line.class));

        // DanglingLines
        assertFalse(mergingView.getDanglingLines().iterator().hasNext());
        assertEquals(0, mergingView.getDanglingLineStream().count());
        assertEquals(0, mergingView.getDanglingLineCount());
        assertArrayEquals(Iterables.toArray(mergingView.getDanglingLines(), DanglingLine.class), Iterables.toArray(mergingView.getConnectables(DanglingLine.class), DanglingLine.class));
        assertEquals(0, mergingView.getConnectableStream(DanglingLine.class).count());
        assertEquals(0, mergingView.getConnectableCount(DanglingLine.class));

        // Connectables
        assertFalse(mergingView.getConnectables().iterator().hasNext());
        assertEquals(0, mergingView.getConnectableStream().count());
        assertEquals(0, mergingView.getConnectableCount());

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
        // Lines
        TestUtil.notImplemented(mergingView::newTieLine);
        // Listeners
        TestUtil.notImplemented(() -> mergingView.addListener(null));
        TestUtil.notImplemented(() -> mergingView.removeListener(null));
    }

    @Test
    void testNonEmptyConnectableGetters() {
        // Init networks
        n1 = EurostagTutorialExample1Factory.create();
        n1.getLine("NHV1_NHV2_1").remove();
        n1.getVoltageLevel("VLHV1").newDanglingLine()
                .setId("DL1")
                .setConnectableBus("NHV1")
                .setBus("NHV1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setUcteXnodeCode("code")
                .add();
        n2 = DanglingLineNetworkFactory.create();
        n2.getDanglingLine("DL").remove();
        VoltageLevel vl = n2.getVoltageLevel("VL");
        vl.newDanglingLine()
                .setId("DL")
                .setBus("BUS")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .setUcteXnodeCode("code2")
                .add();
        vl.getBusBreakerView().newBus().setId("BUS1").add();
        vl.newDanglingLine()
                .setId("DL2")
                .setConnectableBus("BUS1")
                .setBus("BUS1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setUcteXnodeCode("code")
                .add();
        mergingView.merge(n1, n2);

        // test non empty connectable getters

        // TwoWindingsTransformers
        assertEquals(Arrays.asList("NGEN_NHV1", "NHV2_NLOAD"), mergingView.getConnectableStream(TwoWindingsTransformer.class).map(TwoWindingsTransformer::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(TwoWindingsTransformer.class), TwoWindingsTransformer.class),
                mergingView.getConnectableStream(TwoWindingsTransformer.class).toArray());
        assertEquals(2, mergingView.getConnectableCount(TwoWindingsTransformer.class));

        // Loads
        assertEquals(Collections.singletonList("LOAD"), mergingView.getConnectableStream(Load.class).map(Load::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(Load.class), Load.class), mergingView.getConnectableStream(Load.class).toArray());
        assertEquals(1, mergingView.getConnectableCount(Load.class));

        // Generator
        assertEquals(Arrays.asList("GEN", "G"), mergingView.getConnectableStream(Generator.class).map(Generator::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(Generator.class), Generator.class),
                mergingView.getConnectableStream(Generator.class).toArray());
        assertEquals(2, mergingView.getConnectableCount(Generator.class));

        // Lines
        assertEquals(List.of("NHV1_NHV2_2"), mergingView.getConnectableStream(Line.class).map(Line::getId).collect(Collectors.toList()));
        assertEquals(List.of("DL1 + DL2"), mergingView.getIdentifiableStream(IdentifiableType.TIE_LINE).map(Identifiable::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(Line.class), Line.class),
                mergingView.getConnectableStream(Line.class).toArray());
        assertEquals(1, mergingView.getConnectableCount(Line.class));
        assertEquals(1, mergingView.getTieLineCount());

        // DanglingLines
        assertEquals(Collections.singletonList("DL"), mergingView.getConnectableStream(DanglingLine.class).filter(dl -> !dl.isMerged()).map(DanglingLine::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(DanglingLine.class), DanglingLine.class),
                mergingView.getConnectableStream(DanglingLine.class).toArray());
        assertEquals(3, mergingView.getConnectableCount(DanglingLine.class));

        // Connectables
        assertEquals(Arrays.asList("LOAD", "NHV1_NHV2_2", "NGEN_NHV1", "DL1", "NHV2_NLOAD", "GEN", "G", "DL", "DL2"),
                mergingView.getConnectableStream().map(Connectable::getId).collect(Collectors.toList()));
        assertArrayEquals(Iterables.toArray(mergingView.getConnectables(), Connectable.class),
                mergingView.getConnectableStream().toArray());
        assertEquals(9, mergingView.getConnectableCount());
    }

    @Test
    void failMergeIfMultiVariants() {
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Failed");
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.merge(n1));
        assertTrue(e.getMessage().contains("Merging of multi-variants network is not supported"));
    }

    @Test
    void failMergeWithSameObj() {
        addSubstation(n1, "P1", Country.FR, false);
        addSubstation(n2, "P1", Country.FR, false);
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.merge(n1, n2));
        assertTrue(e.getMessage().contains("The object 'P1' already exists into merging view"));
    }

    @Test
    void failAddSameObj() {
        addSubstation(mergingView, "P1", Country.FR, false);
        Substation autoIdSub = addSubstation(mergingView, "P1", Country.FR, true);
        assertSame(autoIdSub, mergingView.getSubstation("P1#0"));
        PowsyblException e = assertThrows(PowsyblException.class, () -> addSubstation(mergingView, "P1", Country.FR, false));
        assertTrue(e.getMessage().contains("The network already contains an object 'SubstationAdapter' with the id 'P1'"));
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
