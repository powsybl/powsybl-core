/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawReaderTest {

    @Test
    public void ieee14BusTest() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus.raw")))) {
            PsseRawModel rawData = new PsseRawReader().read(reader);
            assertNotNull(rawData);
            String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus.json")), StandardCharsets.UTF_8);
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
            assertEquals(jsonRef, json);
        }
    }

    @Test
    public void ieee14BusReadFieldsTest() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus.raw")))) {
            PsseContext context = new PsseContext();
            PsseRawModel rawData = new PsseRawReader().read(reader, context);
            assertNotNull(rawData);

            String[] expectedCaseIdentificationDataReadFields = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
            String[] actualCaseIdentificationDataReadFields = context.getCaseIdentificationDataReadFields();
            assertTrue(compareReadFields(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields));

            String[] expectedBusDataReadFields = new String[] {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
            String[] actualBusDataReadFields = context.getBusDataReadFields();
            assertTrue(compareReadFields(expectedBusDataReadFields, actualBusDataReadFields));

            String[] expectedLoadDataReadFields = new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
            String[] actualLoadDataReadFields = context.getLoadDataReadFields();
            assertTrue(compareReadFields(expectedLoadDataReadFields, actualLoadDataReadFields));

            String[] expectedFixedBusShuntDataReadFields = new String[] {"i", "id", "status", "gl", "bl"};
            String[] actualFixedBusShuntDataReadFields = context.getFixedBusShuntDataReadFields();
            assertTrue(compareReadFields(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields));

            String[] expectedGeneratorDataReadFields = new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg",
                "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
                "f3", "o4", "f4", "wmod", "wpf"};
            String[] actualGeneratorDataReadFields = context.getGeneratorDataReadFields();
            assertTrue(compareReadFields(expectedGeneratorDataReadFields, actualGeneratorDataReadFields));

            String[] expectedNonTransformerBranchDataReadFields = new String[] {"i", "j", "ckt", "r", "x", "b",
                "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj", "st", "met", "len", "o1", "f1", "o2", "f2", "o3",
                "f3", "o4", "f4"};
            String[] actualNonTransformerBranchDataReadFields = context.getNonTransformerBranchDataReadFields();
            assertTrue(compareReadFields(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields));

            String[] expected2wTransformerDataReadFields = new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm",
                "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r12", "x12", "sbase12",
                "windv1", "nomv1", "ang1", "rata1", "ratb1", "ratc1", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
                "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
            String[] actual2wTransformerDataReadFields = context.get2wTransformerDataReadFields();
            assertTrue(compareReadFields(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields));

            String[] expectedAreaInterchangeDataReadFields = new String[] {"i", "isw", "pdes", "ptol", "arname"};
            String[] actualAreaInterchangeDataReadFields = context.getAreaInterchangeDataReadFields();
            assertTrue(compareReadFields(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields));

            String[] expectedZoneDataReadFields = new String[] {"i", "zoname"};
            String[] actualZoneDataReadFields = context.getZoneDataReadFields();
            assertTrue(compareReadFields(expectedZoneDataReadFields, actualZoneDataReadFields));

            String[] expectedOwnerDataReadFields = new String[] {"i", "owname"};
            String[] actualOwnerDataReadFields = context.getOwnerDataReadFields();
            assertTrue(compareReadFields(expectedOwnerDataReadFields, actualOwnerDataReadFields));
        }
    }

    @Test
    public void minimalExampleRawxTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/MinimalExample.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile);

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
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/MinimalExample.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseContext context = new PsseContext();
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile, context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[] {"title1"};
        String[] actualCaseIdentificationDataReadFields = context.getCaseIdentificationDataReadFields();
        assertTrue(compareReadFields(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields));

        String[] expectedBusDataReadFields = new String[] {"ibus", "name", "ide"};
        String[] actualBusDataReadFields = context.getBusDataReadFields();
        assertTrue(compareReadFields(expectedBusDataReadFields, actualBusDataReadFields));

        String[] expectedLoadDataReadFields = new String[] {"ibus", "loadid", "pl", "ql"};
        String[] actualLoadDataReadFields = context.getLoadDataReadFields();
        assertTrue(compareReadFields(expectedLoadDataReadFields, actualLoadDataReadFields));

        String[] expectedGeneratorDataReadFields = new String[] {"ibus", "machid"};
        String[] actualGeneratorDataReadFields = context.getGeneratorDataReadFields();
        assertTrue(compareReadFields(expectedGeneratorDataReadFields, actualGeneratorDataReadFields));

        String[] expectedNonTransformerBranchDataReadFields = new String[] {"ibus", "jbus", "ckt", "xpu"};
        String[] actualNonTransformerBranchDataReadFields = context.getNonTransformerBranchDataReadFields();
        assertTrue(compareReadFields(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields));
    }

    @Test
    public void ieee14BusRev35RawxTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile);

        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
        assertEquals(jsonRef, json);
    }

    @Test
    public void ieee14BusRev35RawxReadFieldsTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseContext context = new PsseContext();
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile, context);
        assertNotNull(rawData);

        String[] expectedCaseIdentificationDataReadFields = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
        String[] actualCaseIdentificationDataReadFields = context.getCaseIdentificationDataReadFields();
        assertTrue(compareReadFields(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields));

        String[] expectedBusDataReadFields = new String[] {"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
        String[] actualBusDataReadFields = context.getBusDataReadFields();
        assertTrue(compareReadFields(expectedBusDataReadFields, actualBusDataReadFields));

        String[] expectedLoadDataReadFields = new String[] {"ibus", "loadid", "stat", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
        String[] actualLoadDataReadFields = context.getLoadDataReadFields();
        assertTrue(compareReadFields(expectedLoadDataReadFields, actualLoadDataReadFields));

        String[] expectedFixedBusShuntDataReadFields = new String[] {"ibus", "shntid", "stat", "gl", "bl"};
        String[] actualFixedBusShuntDataReadFields = context.getFixedBusShuntDataReadFields();
        assertTrue(compareReadFields(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields));

        String[] expectedGeneratorDataReadFields = new String[] {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg",
            "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4", "wmod", "wpf"};
        String[] actualGeneratorDataReadFields = context.getGeneratorDataReadFields();
        assertTrue(compareReadFields(expectedGeneratorDataReadFields, actualGeneratorDataReadFields));

        String[] expectedNonTransformerBranchDataReadFields = new String[] {"ibus", "jbus", "ckt", "rpu", "xpu", "bpu",
            "rate1", "rate2", "rate3", "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3",
            "f3", "o4", "f4"};
        String[] actualNonTransformerBranchDataReadFields = context.getNonTransformerBranchDataReadFields();
        assertTrue(compareReadFields(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields));

        String[] expected2wTransformerDataReadFields = new String[] {"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
            "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
            "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "cod1", "cont1", "rma1", "rmi1", "vma1", "vmi1",
            "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
        String[] actual2wTransformerDataReadFields = context.get2wTransformerDataReadFields();
        assertTrue(compareReadFields(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields));

        String[] expectedAreaInterchangeDataReadFields = new String[] {"iarea", "isw", "pdes", "ptol", "arname"};
        String[] actualAreaInterchangeDataReadFields = context.getAreaInterchangeDataReadFields();
        assertTrue(compareReadFields(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields));

        String[] expectedZoneDataReadFields = new String[] {"izone", "zoname"};
        String[] actualZoneDataReadFields = context.getZoneDataReadFields();
        assertTrue(compareReadFields(expectedZoneDataReadFields, actualZoneDataReadFields));

        String[] expectedOwnerDataReadFields = new String[] {"iowner", "owname"};
        String[] actualOwnerDataReadFields = context.getOwnerDataReadFields();
        assertTrue(compareReadFields(expectedOwnerDataReadFields, actualOwnerDataReadFields));
    }

    @Test
    public void ieee14BusRev35Test() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus_rev35.raw")))) {
            PsseRawModel rawData = new PsseRawReader().read(reader);
            assertNotNull(rawData);
            String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
            assertEquals(jsonRef, json);
        }
    }

    @Test
    public void ieee14BusRev35ReadFieldsTest() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus_rev35.raw")))) {
            assertNotNull(reader);
            PsseContext context = new PsseContext();
            PsseRawModel rawData = new PsseRawReader().read(reader, context);
            assertNotNull(rawData);

            String[] expectedCaseIdentificationDataReadFields = new String[] {"ic", "sbase", "rev", "xfrrat", "nxfrat", "basfrq", "title1", "title2"};
            String[] actualCaseIdentificationDataReadFields = context.getCaseIdentificationDataReadFields();
            assertTrue(
                compareReadFields(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields));

            String[] expectedBusDataReadFields = new String[] {"ibus", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
            String[] actualBusDataReadFields = context.getBusDataReadFields();
            assertTrue(compareReadFields(expectedBusDataReadFields, actualBusDataReadFields));

            String[] expectedLoadDataReadFields = new String[] {"ibus", "loadid", "stat", "area", "zone", "pl", "ql",
                "ip", "iq", "yp", "yq", "owner", "scale"};
            String[] actualLoadDataReadFields = context.getLoadDataReadFields();
            assertTrue(compareReadFields(expectedLoadDataReadFields, actualLoadDataReadFields));

            String[] expectedFixedBusShuntDataReadFields = new String[] {"ibus", "shntid", "stat", "gl", "bl"};
            String[] actualFixedBusShuntDataReadFields = context.getFixedBusShuntDataReadFields();
            assertTrue(compareReadFields(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields));

            String[] expectedGeneratorDataReadFields = new String[] {"ibus", "machid", "pg", "qg", "qt", "qb", "vs", "ireg", "nreg",
                "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "baslod", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "wmod", "wpf"};
            String[] actualGeneratorDataReadFields = context.getGeneratorDataReadFields();
            assertTrue(compareReadFields(expectedGeneratorDataReadFields, actualGeneratorDataReadFields));

            String[] expectedNonTransformerBranchDataReadFields = new String[] {"ibus", "jbus", "ckt", "rpu", "xpu", "bpu", "name",
                "rate1", "rate2", "rate3", "rate4", "rate5", "rate6", "rate7", "rate8", "rate9", "rate10", "rate11", "rate12",
                "gi", "bi", "gj", "bj", "stat", "met", "len", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4"};
            String[] actualNonTransformerBranchDataReadFields = context.getNonTransformerBranchDataReadFields();
            assertTrue(compareReadFields(expectedNonTransformerBranchDataReadFields,
                actualNonTransformerBranchDataReadFields));

            String[] expected2wTransformerDataReadFields = new String[] {"ibus", "jbus", "kbus", "ckt", "cw", "cz", "cm",
                "mag1", "mag2", "nmet", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r1_2", "x1_2", "sbase1_2",
                "windv1", "nomv1", "ang1", "wdg1rate1", "wdg1rate2", "wdg1rate3", "wdg1rate4", "wdg1rate5", "wdg1rate6", "wdg1rate7",
                "wdg1rate8", "wdg1rate9", "wdg1rate10", "wdg1rate11", "wdg1rate12", "cod1", "cont1", "node1", "rma1", "rmi1", "vma1",
                "vmi1", "ntp1", "tab1", "cr1", "cx1", "windv2", "nomv2"};
            String[] actual2wTransformerDataReadFields = context.get2wTransformerDataReadFields();
            assertTrue(compareReadFields(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields));

            String[] expectedAreaInterchangeDataReadFields = new String[] {"iarea", "isw", "pdes", "ptol", "arname"};
            String[] actualAreaInterchangeDataReadFields = context.getAreaInterchangeDataReadFields();
            assertTrue(compareReadFields(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields));

            String[] expectedZoneDataReadFields = new String[] {"izone", "zoname"};
            String[] actualZoneDataReadFields = context.getZoneDataReadFields();
            assertTrue(compareReadFields(expectedZoneDataReadFields, actualZoneDataReadFields));

            String[] expectedOwnerDataReadFields = new String[] {"iowner", "owname"};
            String[] actualOwnerDataReadFields = context.getOwnerDataReadFields();
            assertTrue(compareReadFields(expectedOwnerDataReadFields, actualOwnerDataReadFields));
        }
    }

    private boolean compareReadFields(String[] expected, String[] actual) {
        if (actual != null && expected != null && actual.length == expected.length) {
            for (int i = 0; i < expected.length; i++) {
                if (!expected[i].equals(actual[i])) {
                    logReadFields(expected, actual);
                    return false;
                }
            }
            return true;
        }
        logReadFields(expected, actual);
        return false;
    }

    private void logReadFields(String[] expected, String[] actual) {
        LOGGER.info("Expected: {}", Arrays.toString(expected));
        LOGGER.info("Actual:   {}", Arrays.toString(actual));
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseRawReaderTest.class);
}
