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
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.io.PowerFlowRawData33;
import com.powsybl.psse.model.pf.io.PowerFlowRawData35;
import com.powsybl.psse.model.pf.io.PowerFlowRawxData35;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.powsybl.psse.model.PsseVersion.fromRevision;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawDataTest extends AbstractConverterTest {

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

    private ReadOnlyDataSource ieee14WhitespaceAsDelimiterRaw() {
        return new ResourceDataSource("IEEE_14_bus_whitespaceAsDelimiter", new ResourceSet("/", "IEEE_14_bus_whitespaceAsDelimiter.raw"));
    }

    private ReadOnlyDataSource ieee24Raw() {
        return new ResourceDataSource("IEEE_24_bus", new ResourceSet("/", "IEEE_24_bus.raw"));
    }

    private ReadOnlyDataSource ieee24Raw35() {
        return new ResourceDataSource("IEEE_24_bus_rev35", new ResourceSet("/", "IEEE_24_bus_rev35.raw"));
    }

    private ReadOnlyDataSource ieee24Rawx35() {
        return new ResourceDataSource("IEEE_24_bus_rev35", new ResourceSet("/", "IEEE_24_bus_rev35.rawx"));
    }

    private ReadOnlyDataSource ieee14CompletedRaw() {
        return new ResourceDataSource("IEEE_14_bus_completed", new ResourceSet("/", "IEEE_14_bus_completed.raw"));
    }

    private ReadOnlyDataSource ieee14CompletedRaw35() {
        return new ResourceDataSource("IEEE_14_bus_completed_rev35", new ResourceSet("/", "IEEE_14_bus_completed_rev35.raw"));
    }

    private ReadOnlyDataSource ieee14CompletedRawx35() {
        return new ResourceDataSource("IEEE_14_bus_completed_rev35", new ResourceSet("/", "IEEE_14_bus_completed_rev35.rawx"));
    }

    private ReadOnlyDataSource ieee14InvalidRaw() {
        return new ResourceDataSource("IEEE_14_bus_invalid", new ResourceSet("/", "IEEE_14_bus_invalid.raw"));
    }

    private static String toJson(PssePowerFlowModel rawData) throws JsonProcessingException {
        PsseVersion version = fromRevision(rawData.getCaseIdentification().getRev());
        SimpleBeanPropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(PropertyWriter writer) {
                Revision rev = writer.getAnnotation(Revision.class);
                return rev == null || PsseVersioned.isValidVersion(version, rev);
            }
        };
        FilterProvider filters = new SimpleFilterProvider().addFilter("PsseVersionFilter", filter);
        return new ObjectMapper().writerWithDefaultPrettyPrinter().with(filters).writeValueAsString(rawData);
    }

    @Test
    public void ieee14BusTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void testAccessToFieldNotPresentInVersion() throws IOException {
        PssePowerFlowModel raw33 = new PowerFlowRawData33().read(ieee14Raw(), "raw", new Context());
        assertNotNull(raw33);

        PsseGenerator g = raw33.getGenerators().get(0);
        // Trying to get a field only available since version 35 gives an error
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(g::getNreg)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'nreg' is valid since version 35");

        PsseRates br = raw33.getNonTransformerBranches().get(0).getRates();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(br::getRate1)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'rate1' is valid since version 35");

        PsseTransformer twt = raw33.getTransformers().get(0);
        PsseRates winding1Rates = twt.getWinding1Rates();

        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(twt::getZcod)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'zcod' is valid since version 35");

        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(winding1Rates::getRate1)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'rate1' is valid since version 35");

        PssePowerFlowModel raw35 = new PowerFlowRawData35().read(ieee14Raw35(), "raw", new Context());
        assertNotNull(raw35);
        PsseRates br35 = raw35.getNonTransformerBranches().get(0).getRates();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(br35::getRatea)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'ratea' is valid since version 33 until 33");

        PsseTransformer twt35 = raw35.getTransformers().get(0);
        PsseRates winding135Rates = twt35.getWinding1Rates();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(winding135Rates::getRatea)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'ratea' is valid since version 33 until 33");
    }

    @Test
    public void testAccessToFieldNotPresentInVersionCompleted() throws IOException {
        PssePowerFlowModel raw33 = new PowerFlowRawData33().read(ieee14CompletedRaw(), "raw", new Context());
        assertNotNull(raw33);

        PsseFacts f33 = raw33.getFacts().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(f33::getNreg)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'nreg' is valid since version 35");

        PsseTwoTerminalDcConverter c33 = raw33.getTwoTerminalDcTransmissionLines().get(0).getRectifier();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(c33::getNd)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'nd' is valid since version 35");

        PssePowerFlowModel raw35 = new PowerFlowRawData35().read(ieee14CompletedRaw35(), "raw", new Context());
        assertNotNull(raw35);
        PsseFacts f35 = raw35.getFacts().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(f35::getRemot)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'remot' is valid since version 33 until 33");
    }

    @Test
    public void ieee14BusReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14Raw(), "raw", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(CASE_IDENTIFICATION);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(BUS);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(LOAD);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"i", "id", "status", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(FIXED_BUS_SHUNT);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(GENERATOR);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"i", "j", "ckt", "r", "x", "b",
            "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj", "st", "met", "len", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(NON_TRANSFORMER_BRANCH);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"i", "j", "k", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r12", "x12", "sbase12",
            "windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
            "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(TRANSFORMER_2);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"i", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AREA_INTERCHANGE);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"i", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(ZONE);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"i", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(OWNER);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    @Test
    public void ieee14BusWhitespaceAsDelimiterTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14WhitespaceAsDelimiterRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void minimalExampleRawxTest() throws IOException {
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(minimalRawx(), "rawx", new Context());

        double tol = 0.000001;
        assertEquals(35, rawData.getCaseIdentification().getRev(), 0);
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
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(minimalRawx(), "rawx", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"rev", "title1"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(CASE_IDENTIFICATION);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "ide"};
        String[] actualBusDataReadFields = context.getFieldNames(BUS);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "pl", "ql"};
        String[] actualLoadDataReadFields = context.getFieldNames(LOAD);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(GENERATOR);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "xpu"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(NON_TRANSFORMER_BRANCH);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);
    }

    @Test
    public void ieee14BusRev35RawxTest() throws IOException {
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14Rawx35(), "rawx", new Context());
        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    public void ieee14BusRev35RawxReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14Rawx35(), "rawx", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(CASE_IDENTIFICATION);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(BUS);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(LOAD);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"ibus", "shntid", "stat", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(FIXED_BUS_SHUNT);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(GENERATOR);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "rpu", "xpu", "bpu",
            "rate1", "rate2", "rate3", "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(NON_TRANSFORMER_BRANCH);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
            "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
            "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(TRANSFORMER_2);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"iarea", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AREA_INTERCHANGE);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"izone", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(ZONE);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"iowner", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(OWNER);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    @Test
    public void ieee14BusRev35Test() throws IOException {
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14Raw35(), "raw", new Context());
        assertNotNull(rawData);
        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    public void ieee14BusRev35ReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14Raw35(), "raw", context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[]{"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getFieldNames(CASE_IDENTIFICATION);
        assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

        String[] expectedBusDataReadFields = new String[]{"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getFieldNames(BUS);
        assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

        String[] expectedLoadDataReadFields = new String[]{"ibus", "loadid", "stat", "area", "zone", "pl", "ql",
            "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getFieldNames(LOAD);
        assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

        String[] expectedFixedBusShuntDataReadFields = new String[]{"ibus", "shntid", "stat", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFieldNames(FIXED_BUS_SHUNT);
        assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

        String[] expectedGeneratorDataReadFields = new String[]{"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getFieldNames(GENERATOR);
        assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

        String[] expectedNonTransformerBranchDataReadFields = new String[]{"ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name",
            "rate1", "rate2", "rate3", "rate4", "rate5", "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12",
            "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getFieldNames(NON_TRANSFORMER_BRANCH);
        assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

        String[] expected2wTransformerDataReadFields = new String[]{"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
            "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7",
            "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1",
            "vmi1", "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.getFieldNames(TRANSFORMER_2);
        assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

        String[] expectedAreaInterchangeDataReadFields = new String[]{"iarea", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getFieldNames(AREA_INTERCHANGE);
        assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

        String[] expectedZoneDataReadFields = new String[]{"izone", "zoname"};
        String[] actualZoneDataReadFields = context.getFieldNames(ZONE);
        assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

        String[] expectedOwnerDataReadFields = new String[]{"iowner", "owname"};
        String[] actualOwnerDataReadFields = context.getFieldNames(OWNER);
        assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
    }

    @Test
    public void ieee14BusWriteTest() throws IOException {
        Context context = new Context();

        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14Raw(), "raw", context);
        assertNotNull(rawData);
        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_exported.raw"), is);
        }
    }

    @Test
    public void ieee14BusRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee14Raw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.raw"), is);
        }
    }

    @Test
    public void minimalExampleRawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawXData35.read(minimalRawx(), "rawx", context);
        assertNotNull(rawData);

        rawXData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "MinimalExample_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "MinimalExample_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "MinimalExample_exported.rawx"), is);
        }
    }

    @Test
    public void ieee14BusRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawXData35.read(ieee14Rawx35(), "rawx", context);
        assertNotNull(rawData);

        rawXData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.rawx"), is);
        }
    }

    @Test
    public void ieee14WhitespaceAsDelimiterWriteTest() throws IOException {
        Context context = new Context();

        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14WhitespaceAsDelimiterRaw(), "raw", context);
        assertNotNull(rawData);
        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_whitespaceAsDelimiter_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_whitespaceAsDelimiter_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_whitespaceAsDelimiter_exported.raw"), is);
        }
    }

    @Test
    public void ieee24BusTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_24_bus.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee24Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee24BusSwitchedShuntDataReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee24Raw(), "raw", context);
        assertNotNull(rawData);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    public void ieee24BusRev35Test() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_24_bus_rev35.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee24Raw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee24BusRev35SwitchedShuntDataReadReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee24Raw35(), "raw", context);
        assertNotNull(rawData);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit", "s1", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    public void ieee24BusRev35RawxTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_24_bus_rev35.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee24Rawx35(), "rawx", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee24BusRev35RawxSwitchedShuntDataReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawXData35 = new PowerFlowRawxData35().read(ieee24Rawx35(), "rawx", context);
        assertNotNull(rawXData35);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit", "s1", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    public void ieee24BusWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee24Raw(), "raw", context);
        assertNotNull(rawData);

        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_24_bus_exported.raw"), is);
        }
    }

    @Test
    public void ieee24BusRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee24Raw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_rev35_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_24_bus_rev35_exported.raw"), is);
        }
    }

    @Test
    public void ieee24BusRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawxData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawxData35.read(ieee24Rawx35(), "rawx", context);
        assertNotNull(rawData);

        rawxData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_rev35_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_24_bus_rev35_exported.rawx"), is);
        }
    }

    @Test
    public void ieee14BusCompletedTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_completed.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14CompletedRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee14BusCompletedRev35Test() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_completed_rev35.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14CompletedRaw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee14BusCompletedRev35RawxTest() throws IOException {
        String expectedJson = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_completed_rev35.json")), StandardCharsets.UTF_8);
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14CompletedRawx35(), "rawx", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    public void ieee14BusCompletedWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14CompletedRaw(), "raw", context);
        assertNotNull(rawData);

        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_exported.raw"), is);
        }
    }

    @Test
    public void ieee14BusCompletedRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee14CompletedRaw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_rev35_exported.raw"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_rev35_exported.raw"), is);
        }
    }

    @Test
    public void ieee14BusCompletedRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawxData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawxData35.read(ieee14CompletedRawx35(), "rawx", context);
        assertNotNull(rawData);

        rawxData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_rev35_exported.rawx"))) {
            compareTxt(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_rev35_exported.rawx"), is);
        }
    }

    @Test
    public void invalidIeee14BusTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14InvalidRaw(), "raw", context);
        assertNotNull(rawData);

        PsseValidation psseValidation = new PsseValidation(rawData, context.getVersion());
        List<String> warnings = psseValidation.getWarnings();
        StringBuilder sb = new StringBuilder();
        warnings.forEach(warning -> {
            String s = String.format("%s%n", warning);
            sb.append(s);
        });
        String warningsRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_invalid.txt")), StandardCharsets.UTF_8);
        assertEquals(warningsRef, sb.toString());
        assertFalse(psseValidation.isValidCase());
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
