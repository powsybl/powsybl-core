/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.psse.model.data.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawDataTest {

    private ReadOnlyDataSource ieee14Raw() {
        return new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"));
    }

    private ReadOnlyDataSource ieee14Raw35() {
        return new ResourceDataSource("IEEE_14_bus_rev35", new ResourceSet("/", "IEEE_14_bus_rev35.raw"));
    }

    private ReadOnlyDataSource ieee14Rawx35() {
        return new ResourceDataSource("IEEE_14_bus_rev35", new ResourceSet("/", "IEEE_14_bus_rev35.rawx"));
    }

    private ReadOnlyDataSource minimalRawx() {
        return new ResourceDataSource("MinimalExample", new ResourceSet("/", "MinimalExample.rawx"));
    }

    private static String toJson(PsseRawModel rawData) throws JsonProcessingException {
        int version = rawData.getCaseIdentification().getRev();
        SimpleBeanPropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(PropertyWriter writer) {
                PsseRev rev = writer.getAnnotation(PsseRev.class);
                return rev == null || (rev.since() <= version && version <= rev.until());
            }
        };
        FilterProvider filters = new SimpleFilterProvider().addFilter("PsseVersionFilter", filter);
        return new ObjectMapper().writerWithDefaultPrettyPrinter().with(filters).writeValueAsString(rawData);
    }

    @Test
    public void ieee14BusTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus.json")), StandardCharsets.UTF_8);
        PsseRawModel rawData = new RawData33().read(ieee14Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void testAccessToFieldNotPresentInVersion() throws IOException {
        PsseRawModel raw33 = new RawData33().read(ieee14Raw(), "raw", new Context());
        assertNotNull(raw33);

        PsseGenerator g = raw33.getGenerators().get(0);
        // Trying to get a field only available since version 35 gives an error
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(g::getNreg)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'nreg' is valid since version 35");

        PsseNonTransformerBranch b = raw33.getNonTransformerBranches().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(b::getRate1)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'rate1' is valid since version 35");

        PsseTransformer twt = raw33.getTransformers().get(0);
        PsseTransformerWinding winding1 = twt.getWinding1();

        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(twt::getZcod)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'zcod' is valid since version 35");

        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(winding1::getRate1)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'rate1' is valid since version 35");

        PsseRawModel raw35 = new RawData35().read(ieee14Raw35(), "raw", new Context());
        assertNotNull(raw35);
        PsseNonTransformerBranch b35 = raw35.getNonTransformerBranches().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(b35::getRatea)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'ratea' is valid since version 33 until 33");

        PsseTransformer twt35 = raw35.getTransformers().get(0);
        PsseTransformerWinding winding135 = twt35.getWinding1();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(winding135::getRata)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'rata' is valid since version 33 until 33");
    }

    @Test
    public void ieee14BusReadFieldsTest() throws IOException {
        Context context = new Context();
        PsseRawModel rawData = new RawData33().read(ieee14Raw(), "raw", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.CASE_IDENTIFICATION_DATA);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.BUS_DATA);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.LOAD_DATA);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"i", "id", "status", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.FIXED_BUS_SHUNT_DATA);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.GENERATOR_DATA);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"i", "j", "ckt", "r", "x", "b",
            "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj", "st", "met", "len", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.NON_TRANSFORMER_BRANCH_DATA);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"i", "j", "k", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r12", "x12", "sbase12",
            "windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
            "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.TRANSFORMER_2_DATA);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"i", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.AREA_INTERCHANGE_DATA);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"i", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.ZONE_DATA);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"i", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.OWNER_DATA);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    @Test
    public void minimalExampleRawxTest() throws IOException {
        PsseRawModel rawData = new RawXData35().read(minimalRawx(), "rawx", new Context());

        double tol = 0.000001;
        assertEquals("PSS(R)E MINIMUM RAWX CASE", rawData.getCaseIdentification().getTitle1());

        assertEquals(2, rawData.getBuses().size());
        assertEquals(101, rawData.getBuses().get(0).getI());
        assertEquals("Source", rawData.getBuses().get(0).getName());
        assertEquals(3, rawData.getBuses().get(0).getIde());
        assertEquals(102, rawData.getBuses().get(1).getI());
        assertEquals("Sink", rawData.getBuses().get(1).getName());
        assertEquals(1, rawData.getBuses().get(1).getIde());

        assertEquals(1, rawData.getLoads().size());
        assertEquals(102, rawData.getLoads().get(0).getI());
        assertEquals("1", rawData.getLoads().get(0).getId());
        assertEquals(500.0, rawData.getLoads().get(0).getPl(), tol);
        assertEquals(200.0, rawData.getLoads().get(0).getQl(), tol);

        assertEquals(1, rawData.getGenerators().size());
        assertEquals(101, rawData.getGenerators().get(0).getI());
        assertEquals("1", rawData.getGenerators().get(0).getId());

        assertEquals(1, rawData.getNonTransformerBranches().size());
        assertEquals(101, rawData.getNonTransformerBranches().get(0).getI());
        assertEquals(102, rawData.getNonTransformerBranches().get(0).getJ());
        assertEquals("1", rawData.getNonTransformerBranches().get(0).getCkt());
        assertEquals(0.01, rawData.getNonTransformerBranches().get(0).getX(), tol);
    }

    @Test
    public void minimalExampleRawxReadFieldsTest() throws IOException {
        Context context = new Context();
        PsseRawModel rawData = new RawXData35().read(minimalRawx(), "rawx", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"title1"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.CASE_IDENTIFICATION_DATA);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "ide"};
        String[] actualBusDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.BUS_DATA);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "pl", "ql"};
        String[] actualLoadDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.LOAD_DATA);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.GENERATOR_DATA);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "xpu"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.NON_TRANSFORMER_BRANCH_DATA);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);
    }

    @Test
    public void ieee14BusRev35RawxTest() throws IOException {
        PsseRawModel rawData = new RawXData35().read(ieee14Rawx35(), "rawx", new Context());
        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    public void ieee14BusRev35RawxReadFieldsTest() throws IOException {
        Context context = new Context();
        PsseRawModel rawData = new RawXData35().read(ieee14Rawx35(), "rawx", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.CASE_IDENTIFICATION_DATA);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.BUS_DATA);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.LOAD_DATA);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"ibus", "shntid", "stat", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.FIXED_BUS_SHUNT_DATA);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.GENERATOR_DATA);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "rpu", "xpu", "bpu",
            "rate1", "rate2", "rate3", "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.NON_TRANSFORMER_BRANCH_DATA);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
            "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
            "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.TRANSFORMER_2_DATA);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"iarea", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.AREA_INTERCHANGE_DATA);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"izone", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.ZONE_DATA);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"iowner", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.OWNER_DATA);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    @Test
    public void ieee14BusRev35Test() throws IOException {
        PsseRawModel rawData = new RawData35().read(ieee14Raw35(), "raw", new Context());
        assertNotNull(rawData);
        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    public void ieee14BusRev35ReadFieldsTest() throws IOException {
        Context context = new Context();
        PsseRawModel rawData = new RawData35().read(ieee14Raw35(), "raw", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.CASE_IDENTIFICATION_DATA);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.BUS_DATA);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "stat", "area", "zone", "pl", "ql",
            "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.LOAD_DATA);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"ibus", "shntid", "stat", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.FIXED_BUS_SHUNT_DATA);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.GENERATOR_DATA);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name",
            "rate1", "rate2", "rate3", "rate4", "rate5", "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12",
            "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.NON_TRANSFORMER_BRANCH_DATA);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
            "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7",
            "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1",
            "vmi1", "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.TRANSFORMER_2_DATA);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"iarea", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.AREA_INTERCHANGE_DATA);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"izone", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.ZONE_DATA);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"iowner", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(AbstractRecordGroup.PsseRecordGroup.OWNER_DATA);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    private void assertArrayEquals(String[] expected, String[] actual) {
        if (!Arrays.equals(expected, actual)) {
            String message = "Arrays are different:" + System.lineSeparator()
                + "Expected: " + Arrays.toString(expected) + System.lineSeparator()
                + "Actual  : " + Arrays.toString(actual);
            throw new AssertionError(message);
        }
    }
}
