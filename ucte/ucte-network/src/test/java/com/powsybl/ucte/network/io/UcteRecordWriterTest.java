/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network.io;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class UcteRecordWriterTest {

    @Test
    public void shouldWriteZeroPaddedPositiveFloat() throws IOException {
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
    public void shouldWriteShrinkedPositiveFloat() throws IOException {
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
    public void shouldWriteZeroPaddedNegativeFloat() throws IOException {
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
    public void shouldNotUseScientificNotationForSmallFloat() throws IOException {
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
    public void shouldReturnEmptyValueOnNanInput() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = Double.NaN;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("%n"), writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBigPositiveFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 12345678;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBigNegativeFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -12345678;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    public void shouldSucceedOnLimitBigPositiveFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 999999.9;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("999999%n"), writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnLimitBigPositiveFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = 1000000.1;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    public void shouldSucceedOnLimitBigNegativeFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -99999.9;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("-99999%n"), writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnLimitBigNegativeFloat() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        double input = -100000.1;
        recordWriter.writeDouble(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 12345678;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -12345678;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    public void shouldSucceedOnLimitBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 999999;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("999999%n"), writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnLimitBigPositiveInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = 1000000;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }

    @Test
    public void shouldSucceedOnLimitBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -99999;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format("-99999%n"), writer.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnLimitBigNegativeInteger() throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        int input = -100000;
        recordWriter.writeInteger(input, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();
    }
}
