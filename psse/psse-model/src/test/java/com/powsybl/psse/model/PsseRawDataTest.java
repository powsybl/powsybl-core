/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.io.PowerFlowRawData32;
import com.powsybl.psse.model.pf.io.PowerFlowRawData33;
import com.powsybl.psse.model.pf.io.PowerFlowRawData35;
import com.powsybl.psse.model.pf.io.PowerFlowRawxData35;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static com.powsybl.psse.model.PsseVersion.fromRevision;
import static com.powsybl.psse.model.pf.io.PowerFlowRecordGroup.*;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PsseRawDataTest extends AbstractSerDeTest {

    private ReadOnlyDataSource ieee14Raw() {
        return new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"));
    }

    private ReadOnlyDataSource ieee14nonInductionMachineDataRaw() {
        return new ResourceDataSource("IEEE_14_bus_non_induction_machine_data", new ResourceSet("/", "IEEE_14_bus_non_induction_machine_data.raw"));
    }

    private ReadOnlyDataSource ieee14Raw35() {
        return new ResourceDataSource("IEEE_14_bus_rev35", new ResourceSet("/", "IEEE_14_bus_rev35.raw"));
    }

    private ReadOnlyDataSource ieee14nonInductionMachineDataRaw35() {
        return new ResourceDataSource("IEEE_14_bus_non_induction_machine_data_rev35", new ResourceSet("/", "IEEE_14_bus_non_induction_machine_data_rev35.raw"));
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

    private ReadOnlyDataSource ieee14NodeBreakerRaw35() {
        return new ResourceDataSource("IEEE_14_bus_nodeBreaker_rev35", new ResourceSet("/", "IEEE_14_bus_nodeBreaker_rev35.raw"));
    }

    private ReadOnlyDataSource ieee14NodeBreakerRawx35() {
        return new ResourceDataSource("IEEE_14_bus_nodeBreaker_rev35", new ResourceSet("/", "IEEE_14_bus_nodeBreaker_rev35.rawx"));
    }

    private ReadOnlyDataSource ieee14InvalidRaw() {
        return new ResourceDataSource("IEEE_14_bus_invalid", new ResourceSet("/", "IEEE_14_bus_invalid.raw"));
    }

    private ReadOnlyDataSource exampleVersion32() {
        return new ResourceDataSource("ExampleVersion32", new ResourceSet("/", "ExampleVersion32.raw"));
    }

    private ReadOnlyDataSource ieee14IsolatedBusesRaw() {
        return new ResourceDataSource("IEEE_14_isolated_buses", new ResourceSet("/", "IEEE_14_isolated_buses.raw"));
    }

    private ReadOnlyDataSource ieee14QrecordRev35Raw() {
        return new ResourceDataSource("IEEE_14_bus_Q_record_rev35", new ResourceSet("/", "IEEE_14_bus_Q_record_rev35.raw"));
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
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().with(filters).writeValueAsString(rawData);
        return TestUtil.normalizeLineSeparator(json);
    }

    private static String loadReference(String path) {
        try {
            InputStream is = Objects.requireNonNull(PsseRawDataTest.class.getResourceAsStream(path));
            return TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void ieee14BusTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void testAccessToFieldNotPresentInVersion32() throws IOException {
        PssePowerFlowModel raw32 = new PowerFlowRawData32().read(exampleVersion32(), "raw", new Context());
        assertNotNull(raw32);

        PsseBus b = raw32.getBuses().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(b::getNvhi)
            .withMessage("Wrong version of PSSE RAW model (32). Field 'nvhi' is valid since version 33");

        PsseLoad l = raw32.getLoads().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(l::getIntrpt)
            .withMessage("Wrong version of PSSE RAW model (32). Field 'intrpt' is valid since version 33");

        PsseTransformer t = raw32.getTransformers().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(t::getVecgrp)
            .withMessage("Wrong version of PSSE RAW model (32). Field 'vecgrp' is valid since version 33");
    }

    @Test
    void testAccessToFieldNotPresentInVersion33() throws IOException {
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
            .withMessage("Wrong version of PSSE RAW model (35). Field 'ratea' is valid since version 32 until 33");

        PsseTransformer twt35 = raw35.getTransformers().get(0);
        PsseRates winding135Rates = twt35.getWinding1Rates();
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(winding135Rates::getRatea)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'ratea' is valid since version 32 until 33");
    }

    @Test
    void testAccessToFieldNotPresentInVersionCompleted() throws IOException {
        PssePowerFlowModel raw33 = new PowerFlowRawData33().read(ieee14CompletedRaw(), "raw", new Context());
        assertNotNull(raw33);

        PsseFacts f33 = raw33.getFacts().get(0);
        // Trying to get a field only available since version 35 gives an error
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(f33::getNreg)
            .withMessage("Wrong version of PSSE RAW model (33). Field 'nreg' is valid since version 35");

        PssePowerFlowModel raw35 = new PowerFlowRawData35().read(ieee14CompletedRaw35(), "raw", new Context());
        assertNotNull(raw35);
        PsseFacts f35 = raw35.getFacts().get(0);
        assertThatExceptionOfType(PsseException.class)
            .isThrownBy(f35::getRemot)
            .withMessage("Wrong version of PSSE RAW model (35). Field 'remot' is valid since version 32 until 33");
    }

    @Test
    void ieee14BusReadFieldsTest() throws IOException {
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

        String[] expected2wTransformerDataFirstRecordReadFields = new String[]{"i", "j", "k", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        String[] actual2wTransformerDataFirstRecordReadFields = context.getFieldNames(TRANSFORMER);
        assertArrayEquals(expected2wTransformerDataFirstRecordReadFields, actual2wTransformerDataFirstRecordReadFields);

        String[] expected2wTransformerDataSecondRecordReadFields = new String[]{"r12", "x12", "sbase12"};
        String[] actual2wTransformerDataSecondRecordReadFields = context.getFieldNames(INTERNAL_TRANSFORMER_IMPEDANCES);
        assertArrayEquals(expected2wTransformerDataSecondRecordReadFields, actual2wTransformerDataSecondRecordReadFields);

        String[] expected2wTransformerDataWindingRecordReadFields = new String[] {"windv", "nomv", "ang",
            "rata", "ratb", "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx"};
        String[] actual2wTransformerDataWindingRecordReadFields = context.getFieldNames(INTERNAL_TRANSFORMER_WINDING);
        assertArrayEquals(expected2wTransformerDataWindingRecordReadFields, actual2wTransformerDataWindingRecordReadFields);

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
    void ieee14BusWhitespaceAsDelimiterTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14WhitespaceAsDelimiterRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14IsolatedBusesTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_isolated_buses.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14IsolatedBusesRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void minimalExampleRawxTest() throws IOException {
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
    void minimalExampleRawxReadFieldsTest() throws IOException {
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
    void ieee14BusRev35RawxTest() throws IOException {
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14Rawx35(), "rawx", new Context());
        String jsonRef = loadReference("/IEEE_14_bus_rev35.json");
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    void ieee14BusRev35RawxReadFieldsTest() throws IOException {
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
        String[] actual2wTransformerDataReadFields = context.getFieldNames(TRANSFORMER);
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
    void ieee14BusRev35Test() throws IOException {
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14Raw35(), "raw", new Context());
        assertNotNull(rawData);
        String jsonRef = loadReference("/IEEE_14_bus_rev35.json");
        assertEquals(jsonRef, toJson(rawData));
    }

    @Test
    void ieee14BusRev35ReadFieldsTest() throws IOException {
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

        String[] expected2wTransformerDataFirstRecordReadFields = new String[]{"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
        String[] actual2wTransformerDataFirstRecrodReadFields = context.getFieldNames(TRANSFORMER);
        assertArrayEquals(expected2wTransformerDataFirstRecordReadFields, actual2wTransformerDataFirstRecrodReadFields);

        String[] expected2wTransformerDataSecondRecordReadFields = new String[]{"r12", "x12", "sbase12"};
        String[] actual2wTransformerDataSecondRecrodReadFields = context.getFieldNames(INTERNAL_TRANSFORMER_IMPEDANCES);
        assertArrayEquals(expected2wTransformerDataSecondRecordReadFields, actual2wTransformerDataSecondRecrodReadFields);

        String[] expected2wTransformerDataWindingRecordReadFields = new String[]{"windv", "nomv", "ang",
            "wdgrate1", "wdgrate2", "wdgrate3", "wdgrate4", "wdgrate5", "wdgrate6", "wdgrate7",
            "wdgrate8", "wdgrate9", "wdgrate10", "wdgrate11", "wdgrate12", "cod", "cont", "node", "rma", "rmi", "vma",
            "vmi", "ntp", "tab", "cr", "cx"};
        String[] actual2wTransformerDataWindingRecrodReadFields = context.getFieldNames(INTERNAL_TRANSFORMER_WINDING);
        assertArrayEquals(expected2wTransformerDataWindingRecordReadFields, actual2wTransformerDataWindingRecrodReadFields);

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
    void ieee14BusWriteTest() throws IOException {
        Context context = new Context();

        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14Raw(), "raw", context);
        assertNotNull(rawData);
        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_exported.raw"), is);
        }
    }

    @Test
    void ieee14BusRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee14Raw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.raw"), is);
        }
    }

    @Test
    void minimalExampleRawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawXData35.read(minimalRawx(), "rawx", context);
        assertNotNull(rawData);

        rawXData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "MinimalExample_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "MinimalExample_exported.rawx"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "MinimalExample_exported.rawx"), is);
        }
    }

    @Test
    void ieee14BusRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawXData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawXData35.read(ieee14Rawx35(), "rawx", context);
        assertNotNull(rawData);

        rawXData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_rev35_exported.rawx"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_rev35_exported.rawx"), is);
        }
    }

    @Test
    void ieee14WhitespaceAsDelimiterWriteTest() throws IOException {
        Context context = new Context();

        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14WhitespaceAsDelimiterRaw(), "raw", context);
        assertNotNull(rawData);
        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_whitespaceAsDelimiter_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_whitespaceAsDelimiter_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_whitespaceAsDelimiter_exported.raw"), is);
        }
    }

    @Test
    void ieee14IsolatedBusesWriteTest() throws IOException {
        Context context = new Context();

        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14IsolatedBusesRaw(), "raw", context);
        assertNotNull(rawData);
        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_isolated_buses_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_isolated_buses_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_isolated_buses_exported.raw"), is);
        }
    }

    @Test
    void ieee24BusTest() throws IOException {
        String expectedJson = loadReference("/IEEE_24_bus.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee24Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee24BusSwitchedShuntDataReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee24Raw(), "raw", context);
        assertNotNull(rawData);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"i", "modsw", "adjm", "stat", "vswhi", "vswlo", "swrem", "rmpct", "rmidnt", "binit", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    void ieee24BusRev35Test() throws IOException {
        String expectedJson = loadReference("/IEEE_24_bus_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee24Raw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee24BusRev35SwitchedShuntDataReadReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee24Raw35(), "raw", context);
        assertNotNull(rawData);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"i", "id", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit", "s1", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    void ieee24BusRev35RawxTest() throws IOException {
        String expectedJson = loadReference("/IEEE_24_bus_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee24Rawx35(), "rawx", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee24BusRev35RawxSwitchedShuntDataReadFieldsTest() throws IOException {
        Context context = new Context();
        PssePowerFlowModel rawXData35 = new PowerFlowRawxData35().read(ieee24Rawx35(), "rawx", context);
        assertNotNull(rawXData35);

        String[] expectedSwitchedShuntDataReadFields = new String[] {"ibus", "shntid", "modsw", "adjm", "stat", "vswhi", "vswlo", "swreg", "nreg", "rmpct", "rmidnt", "binit", "s1", "n1", "b1"};
        String[] actualSwitchedShuntDataReadFields = context.getFieldNames(SWITCHED_SHUNT);
        assertArrayEquals(expectedSwitchedShuntDataReadFields, actualSwitchedShuntDataReadFields);
    }

    @Test
    void ieee24BusWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee24Raw(), "raw", context);
        assertNotNull(rawData);

        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_24_bus_exported.raw"), is);
        }
    }

    @Test
    void ieee24BusRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee24Raw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_rev35_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_24_bus_rev35_exported.raw"), is);
        }
    }

    @Test
    void ieee24BusRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawxData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawxData35.read(ieee24Rawx35(), "rawx", context);
        assertNotNull(rawData);

        rawxData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_24_bus_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_24_bus_rev35_exported.rawx"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_24_bus_rev35_exported.rawx"), is);
        }
    }

    @Test
    void ieee14BusCompletedTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_completed.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14CompletedRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusCompletedRev35Test() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_completed_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14CompletedRaw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusCompletedRev35RawxTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_completed_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14CompletedRawx35(), "rawx", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusNodeBreakerRev35Test() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_nodeBreaker_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14NodeBreakerRaw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusNodeBreakerRev35CopyTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_nodeBreaker_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14NodeBreakerRaw35(), "raw", new Context());
        assertNotNull(rawData);
        PssePowerFlowModel copiedRawData = rawData.referenceAndCopyPssePowerFlowModel();
        assertEquals(expectedJson, toJson(copiedRawData));
    }

    @Test
    void ieee14BusNodeBreakerRev35RawxTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_nodeBreaker_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawxData35().read(ieee14NodeBreakerRawx35(), "rawx", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusNonInductionMachineDataTest() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus.json");
        PssePowerFlowModel rawData = new PowerFlowRawData33().read(ieee14nonInductionMachineDataRaw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusNonInductionMachineDataRev35Test() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14nonInductionMachineDataRaw35(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusQrecordRev35Test() throws IOException {
        String expectedJson = loadReference("/IEEE_14_bus_Q_record_rev35.json");
        PssePowerFlowModel rawData = new PowerFlowRawData35().read(ieee14QrecordRev35Raw(), "raw", new Context());
        assertNotNull(rawData);
        assertEquals(expectedJson, toJson(rawData));
    }

    @Test
    void ieee14BusCompletedWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData33 rawData33 = new PowerFlowRawData33();
        PssePowerFlowModel rawData = rawData33.read(ieee14CompletedRaw(), "raw", context);
        assertNotNull(rawData);

        rawData33.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_exported.raw"), is);
        }
    }

    @Test
    void ieee14BusCompletedRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee14CompletedRaw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_rev35_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_rev35_exported.raw"), is);
        }
    }

    @Test
    void ieee14BusCompletedRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawxData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawxData35.read(ieee14CompletedRawx35(), "rawx", context);
        assertNotNull(rawData);

        rawxData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_completed_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_completed_rev35_exported.rawx"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_completed_rev35_exported.rawx"), is);
        }
    }

    @Test
    void ieee14BusNodeBreakerRev35WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData35 rawData35 = new PowerFlowRawData35();
        PssePowerFlowModel rawData = rawData35.read(ieee14NodeBreakerRaw35(), "raw", context);
        assertNotNull(rawData);

        rawData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_nodeBreaker_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_nodeBreaker_rev35_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_nodeBreaker_rev35_exported.raw"), is);
        }
    }

    @Test
    void ieee14BusNodeBreakerRev35RawxWriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawxData35 rawxData35 = new PowerFlowRawxData35();
        PssePowerFlowModel rawData = rawxData35.read(ieee14NodeBreakerRawx35(), "rawx", context);
        assertNotNull(rawData);

        rawxData35.write(rawData, context, new FileDataSource(fileSystem.getPath("/work/"), "IEEE_14_bus_nodeBreaker_rev35_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "IEEE_14_bus_nodeBreaker_rev35_exported.rawx"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "IEEE_14_bus_nodeBreaker_rev35_exported.rawx"), is);
        }
    }

    @Test
    void invalidIeee14BusTest() throws IOException {
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
        String warningsRef = loadReference("/IEEE_14_bus_invalid.txt");
        assertEquals(warningsRef, TestUtil.normalizeLineSeparator(sb.toString()));
        assertFalse(psseValidation.isValidCase());
    }

    @Test
    void exampleVersion32WriteTest() throws IOException {
        Context context = new Context();
        PowerFlowRawData32 rawData32 = new PowerFlowRawData32();
        PssePowerFlowModel rawData = rawData32.read(exampleVersion32(), "raw", context);
        assertNotNull(rawData);

        rawData32.write(rawData, context,
            new FileDataSource(fileSystem.getPath("/work/"), "ExampleVersion32_exported"));
        try (InputStream is = Files.newInputStream(fileSystem.getPath("/work/", "ExampleVersion32_exported.raw"))) {
            assertTxtEquals(getClass().getResourceAsStream("/" + "ExampleVersion32_exported.raw"), is);
        }
    }

    private void assertArrayEquals(String[] expected, String[] actual) {
        if (!Arrays.equals(expected, actual)) {
            String message = "Arrays are different:" + System.lineSeparator()
                + "Expected: " + Arrays.toString(expected) + System.lineSeparator()
                + "Actual  : " + Arrays.toString(actual);
            throw new IllegalStateException(message);
        }
    }
}
