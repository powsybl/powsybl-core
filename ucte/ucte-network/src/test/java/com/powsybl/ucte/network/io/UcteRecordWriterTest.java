/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UcteRecordWriterTest {

    private static Stream<Arguments> provideDoubleArguments() {
        return Stream.of(
            Arguments.of(3.14, "3.1400%n"),
            Arguments.of(3.1415927, "3.1415%n"),
            Arguments.of(-3.1, "-3.100%n"),
            Arguments.of(0.0001, "0.0001%n"),
            Arguments.of(999999.9, "999999%n"),
            Arguments.of(-99999.9, "-99999%n"),
            Arguments.of(-Double.NaN, "%n")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoubleArguments")
    void shouldWriteDoubleTests(double value, String expected) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        recordWriter.writeDouble(value, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format(expected), writer.toString());
    }

    private static Stream<Arguments> provideDoubleForFailingWriting() {
        return Stream.of(
            Arguments.of(12345678),
            Arguments.of(-12345678),
            Arguments.of(1000000.1),
            Arguments.of(-100000.1)
        );
    }

    @ParameterizedTest
    @MethodSource("provideDoubleForFailingWriting")
    void shouldFailWriteDoubleTests(double value) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeDouble(value, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    private static Stream<Arguments> provideIntArguments() {
        return Stream.of(
            Arguments.of(999999, "999999%n"),
            Arguments.of(-99999, "-99999%n")
        );
    }

    @ParameterizedTest
    @MethodSource("provideIntArguments")
    void shouldWriteIntTests(int value, String expected) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        recordWriter.writeInteger(value, 0, 6);
        recordWriter.newLine();
        bufferedWriter.close();

        assertEquals(String.format(expected), writer.toString());
    }

    private static Stream<Arguments> provideIntForFailingWriting() {
        return Stream.of(
            Arguments.of(12345678),
            Arguments.of(-12345678),
            Arguments.of(1000000),
            Arguments.of(-100000)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIntForFailingWriting")
    void shouldFailWriteIntTests(int value) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        assertThrows(IllegalArgumentException.class, () -> recordWriter.writeInteger(value, 0, 6));
        recordWriter.newLine();
        bufferedWriter.close();
    }

    private static String writeStringField(String value, int beginIndex, int endIndex) throws IOException {
        StringWriter writer = new StringWriter();
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        UcteRecordWriter recordWriter = new UcteRecordWriter(bufferedWriter);

        recordWriter.writeString(value, beginIndex, endIndex);
        recordWriter.newLine();
        bufferedWriter.close();
        return writer.toString();
    }

    @Test
    void shouldNeutralizeLineBreaksInString() throws IOException {
        // a control character in a string field would otherwise split the fixed-width record
        // and let the value forge extra records (e.g. via an imported network property)
        assertEquals(String.format("AB CD%n"), writeStringField("AB\nCD", 0, 12));
        assertEquals(String.format("AB  CD%n"), writeStringField("AB\r\nCD", 0, 12));
    }

    @Test
    void shouldTruncateStringToFieldWidth() throws IOException {
        assertEquals(String.format("ABCDE%n"), writeStringField("ABCDEFGHIJKL", 0, 5));
    }

    @Test
    void shouldWriteStringUnchangedWhenItFits() throws IOException {
        assertEquals(String.format("ABC%n"), writeStringField("ABC", 0, 5));
    }
}
