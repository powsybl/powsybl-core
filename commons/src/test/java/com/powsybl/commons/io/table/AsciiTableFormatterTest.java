/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public class AsciiTableFormatterTest {

    private TableFormatterConfig config = new TableFormatterConfig(Locale.US, "inv");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAsciiTableFormatter1() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer,  null, config,
                new Column("column1").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.write("Line:1 Cell:1", 2);
            formatter.write("Line:1 Cell:2", 1);
            formatter.write("Line:2 Cell:1", 1);
            formatter.write("Line:2 Cell:2", 1);
            formatter.write("Line:2 Cell:3", 1);
        }
        assertEquals("+-------------------------------+---------------+\n" +
                        "|            column1            |    column2    |\n" +
                        "+-------------------------------+---------------+\n" +
                        "|         Line:1 Cell:1         | Line:1 Cell:2 |\n" +
                        "| Line:2 Cell:1 | Line:2 Cell:2 | Line:2 Cell:3 |\n" +
                        "+---------------+---------------+---------------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
        writer.close();
    }

    @Test
    public void testAsciiTableFormatter2() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer,  null, config,
                new Column("column1").setColspan(4).setHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.write("Line:1 Cell:1", 1);
            formatter.write("Line:1 Cell:2", 1);
            formatter.write("Line:1 Cell:3", 1);
            formatter.write("Line:1 Cell:4", 1);
            formatter.write("Line:1 Cell:5", 1);
            formatter.write("Line:1 Cell:6", 1);
        }
        assertEquals("+---------------------------------------------------------------+-------------------------------+\n" +
                        "|                            column1                            |            column2            |\n" +
                        "+---------------------------------------------------------------+-------------------------------+\n" +
                        "| Line:1 Cell:1 | Line:1 Cell:2 | Line:1 Cell:3 | Line:1 Cell:4 | Line:1 Cell:5 | Line:1 Cell:6 |\n" +
                        "+---------------+---------------+---------------+---------------+---------------+---------------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
        writer.close();
    }

    @Test
    public void testUnauthorizedColspan() throws IOException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("You have exceded the authorized colspan");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer,  null, config,
                new Column("column1").setColspan(4).setHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.write("Line:1 Cell:1", 1);
            formatter.write("Line:1 Cell:2", 1);
            formatter.write("Line:1 Cell:3", 1);
            formatter.write("Line:1 Cell:4", 2);
            formatter.write("Line:1 Cell:5", 1);
        }
    }

    @Test
    public void getColumnIndexTest() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer,  null, config,
                new Column("column1").setColspan(4).setHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.write("Line:1 Cell:1", 1);
            formatter.write("Line:1 Cell:2", 1);
            formatter.write("Line:1 Cell:3", 1);
            formatter.write("Line:1 Cell:4", 1);
            formatter.write("Line:1 Cell:5", 2);
            Method method = AsciiTableFormatter.class.getDeclaredMethod("getColumnIndex", int.class);
            method.setAccessible(true);
            int output = (int) method.invoke(formatter, 0);
            assertEquals(0, output);
            output = (int) method.invoke(formatter, 3);
            assertEquals(0, output);
            output = (int) method.invoke(formatter, 5);
            assertEquals(1, output);
        }

    }
}
