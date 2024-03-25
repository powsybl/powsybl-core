/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.io;

import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UcteRecordWriterTest {

    @Test
    void shouldWriteZeroPaddedPositiveDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 3.14;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("3.1400%n"), writer.toString());
    }

    @Test
    void shouldWriteShrinkedPositiveDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 3.1415927;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("3.1415%n"), writer.toString());
    }

    @Test
    void shouldWriteZeroPaddedNegativeDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -3.1;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("-3.100%n"), writer.toString());
    }

    @Test
    void shouldNotUseScientificNotationForSmallDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 0.0001;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("0.0001%n"), writer.toString());
    }

    @Test
    void shouldReturnEmptyValueOnNanInput() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = Double.NaN;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("%n"), writer.toString());
    }

    @Test
    void shouldFailOnBigPositiveDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 12345678;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeDouble(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldFailOnBigNegativeDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -12345678;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeDouble(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldSucceedOnLimitBigPositiveDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 999999.9;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("999999%n"), writer.toString());
    }

    @Test
    void shouldFailOnLimitBigPositiveDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 1000000.1;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeDouble(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldSucceedOnLimitBigNegativeDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -99999.9;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("-99999%n"), writer.toString());
    }

    @Test
    void shouldFailOnLimitBigNegativeDouble() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -100000.1;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeDouble(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldFailOnBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 12345678;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeInteger(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldFailOnBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -12345678;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeInteger(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldSucceedOnLimitBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 999999;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("999999%n"), writer.toString());
    }

    @Test
    void shouldFailOnLimitBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 1000000;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeInteger(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    void shouldSucceedOnLimitBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -99999;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("-99999%n"), writer.toString());
    }

    @Test
    void shouldFailOnLimitBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -100000;
        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeInteger(input, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }
}
