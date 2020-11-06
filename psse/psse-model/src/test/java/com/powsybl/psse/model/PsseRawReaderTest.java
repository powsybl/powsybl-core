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
            assertArrayEquals(expectedCaseIdentificationDataReadFields, actualCaseIdentificationDataReadFields);

            String[] expectedBusDataReadFields = new String[] {"i", "name", "baskv", "ide", "area", "zone", "owner", "vm", "va"};
            String[] actualBusDataReadFields = context.getBusDataReadFields();
            assertArrayEquals(expectedBusDataReadFields, actualBusDataReadFields);

            String[] expectedLoadDataReadFields = new String[] {"i", "id", "status", "area", "zone", "pl", "ql", "ip", "iq", "yp", "yq", "owner", "scale"};
            String[] actualLoadDataReadFields = context.getLoadDataReadFields();
            assertArrayEquals(expectedLoadDataReadFields, actualLoadDataReadFields);

            String[] expectedFixedBusShuntDataReadFields = new String[] {"i", "id", "status", "gl", "bl"};
            String[] actualFixedBusShuntDataReadFields = context.getFixedBusShuntDataReadFields();
            assertArrayEquals(expectedFixedBusShuntDataReadFields, actualFixedBusShuntDataReadFields);

            String[] expectedGeneratorDataReadFields = new String[] {"i", "id", "pg", "qg", "qt", "qb", "vs", "ireg",
                "mbase", "zr", "zx", "rt", "xt", "gtap", "stat", "rmpct", "pt", "pb", "o1", "f1", "o2", "f2", "o3",
                "f3", "o4", "f4", "wmod", "wpf"};
            String[] actualGeneratorDataReadFields = context.getGeneratorDataReadFields();
            assertArrayEquals(expectedGeneratorDataReadFields, actualGeneratorDataReadFields);

            String[] expectedNonTransformerBranchDataReadFields = new String[] {"i", "j", "ckt", "r", "x", "b",
                "ratea", "rateb", "ratec", "gi", "bi", "gj", "bj", "st", "met", "len", "o1", "f1", "o2", "f2", "o3",
                "f3", "o4", "f4"};
            String[] actualNonTransformerBranchDataReadFields = context.getNonTransformerBranchDataReadFields();
            assertArrayEquals(expectedNonTransformerBranchDataReadFields, actualNonTransformerBranchDataReadFields);

            String[] expected2wTransformerDataReadFields = new String[] {"i", "j", "k", "ckt", "cw", "cz", "cm",
                "mag1", "mag2", "nmetr", "name", "stat", "o1", "f1", "o2", "f2", "o3", "f3", "o4", "f4", "r12", "x12", "sbase12"};
            String[] actual2wTransformerDataReadFields = context.get2wTransformerDataReadFields();
            assertArrayEquals(expected2wTransformerDataReadFields, actual2wTransformerDataReadFields);

            String[] expected2wTransformerW1DataReadFields = new String[] {"windv", "nomv", "ang", "rata", "ratb",
                "ratc", "cod", "cont", "rma", "rmi", "vma", "vmi", "ntp", "tab", "cr", "cx"};
            String[] actual2wTransformerW1DataReadFields = context.get2wTransformerDataWinding1ReadFields();
            assertArrayEquals(expected2wTransformerW1DataReadFields, actual2wTransformerW1DataReadFields);

            String[] expected2wTransformerW2DataReadFields = new String[] {"windv", "nomv"};
            String[] actual2wTransformerW2DataReadFields = context.get2wTransformerDataWinding2ReadFields();
            assertArrayEquals(expected2wTransformerW2DataReadFields, actual2wTransformerW2DataReadFields);

            String[] expectedAreaInterchangeDataReadFields = new String[] {"i", "isw", "pdes", "ptol", "arname"};
            String[] actualAreaInterchangeDataReadFields = context.getAreaInterchangeDataReadFields();
            assertArrayEquals(expectedAreaInterchangeDataReadFields, actualAreaInterchangeDataReadFields);

            String[] expectedZoneDataReadFields = new String[] {"i", "zoname"};
            String[] actualZoneDataReadFields = context.getZoneDataReadFields();
            assertArrayEquals(expectedZoneDataReadFields, actualZoneDataReadFields);

            String[] expectedOwnerDataReadFields = new String[] {"i", "owname"};
            String[] actualOwnerDataReadFields = context.getOwnerDataReadFields();
            assertArrayEquals(expectedOwnerDataReadFields, actualOwnerDataReadFields);
        }
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
