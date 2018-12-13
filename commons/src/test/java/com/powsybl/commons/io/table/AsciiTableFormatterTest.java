/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public class AsciiTableFormatterTest {

    TableFormatterConfig config = new TableFormatterConfig(Locale.US, "inv");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAsciiTableFormatter1() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer myWriter = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(myWriter, "myFormatter", config,
                new Column("column1")
                        .setColspan(5),
                new Column("colomun2"))) {
            formatter.writeWithColspan("content1", 3);
            formatter.writeWithColspan("content1", 3);
            formatter.writeWithColspan("content2", 1);
            formatter.writeWithColspan("content3", 5);
            formatter.writeWithColspan("content2", 1);
            formatter.writeWithColspan("content3", 5);
            formatter.writeWithColspan("content3", 1);
            formatter.writeWithColspan("content3", 1);
            formatter.writeWithColspan("content3", 1);
            formatter.writeWithColspan("content3", 1);
            formatter.writeWithColspan("content3", 1);
            formatter.writeWithColspan("content3", 1);
        }
        assertEquals("myFormatter:" + System.lineSeparator() +
                        "+------------------------------------------------------+----------+\n" +
                        "| column1                                              | colomun2 |\n" +
                        "+-----------------------------------------------------------------+\n" +
                        "| content1                       | content1                       |\n" +
                        "| content2 | content3                                             |\n" +
                        "| content2 | content3                                             |\n" +
                        "| content3 | content3 | content3 | content3 | content3 | content3 |\n" +
                        "+----------+----------+----------+----------+----------+----------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
        myWriter.close();
    }


    @Test
    public void testAsciiTableFormatter2() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer myWriter = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(myWriter, "myFormatter", config,
                new Column("column1"),
                new Column("column2"),
                new Column("column3"))) {
            formatter.writeWithColspan("Test1", 3);
            formatter.writeWithColspan("Test1", 2);
            formatter.writeWithColspan("Test1", 1);
        }
        assertEquals("myFormatter:" + System.lineSeparator() +
                        "+---------+---------+---------+\n" +
                        "| column1 | column2 | column3 |\n" +
                        "+-----------------------------+\n" +
                        "| Test1                       |\n" +
                        "| Test1             | Test1   |\n" +
                        "+-------------------+---------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
        myWriter.close();
    }

    @Test
    public void testAsciiTableFormatter3() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer myWriter = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter3 = new AsciiTableFormatter(myWriter, "myFormatter", config,
                new Column("column1").setColspan(2),
                new Column("column2"))) {
            formatter3.writeWithColspan("Test3", 3);
            formatter3.writeWithColspan("Test3", 1);
            formatter3.writeWithColspan("Test3", 1);
            formatter3.writeWithColspan("Test3", 1);
        }
        assertEquals("myFormatter:" + System.lineSeparator() +
                        "+---------------+---------+\n" +
                        "| column1       | column2 |\n" +
                        "+-------------------------+\n" +
                        "| Test3                   |\n" +
                        "| Test3 | Test3 | Test3   |\n" +
                        "+-------+-------+---------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
        myWriter.close();
    }
}
