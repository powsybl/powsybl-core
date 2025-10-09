/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io.table;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
class AsciiTableFormatterTest {

    private TableFormatterConfig config = new TableFormatterConfig(Locale.US, "inv");

    @Test
    void testAsciiTableFormatter1() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer, null, config,
                new Column("column1").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER).setTitleHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setHorizontalAlignment(HorizontalAlignment.CENTER).setTitleHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.writeCell("Line:1 Cell:1", 2)
                    .writeCell("Line:1 Cell:2", 1)
                    .writeCell("Line:2 Cell:1", 1)
                    .writeCell("Line:2 Cell:2", 1)
                    .writeCell("Line:2 Cell:3", 1);
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
    void testAsciiTableFormatter2() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        try (AsciiTableFormatter formatter = new AsciiTableFormatter(writer, null, config,
                new Column("column1").setColspan(4).setHorizontalAlignment(HorizontalAlignment.CENTER).setTitleHorizontalAlignment(HorizontalAlignment.CENTER),
                new Column("column2").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER).setTitleHorizontalAlignment(HorizontalAlignment.CENTER))) {
            formatter.writeCell("Line:1 Cell:1", 1)
                    .writeCell("Line:1 Cell:2", 1)
                    .writeCell("Line:1 Cell:3", 1)
                    .writeCell("Line:1 Cell:4", 1)
                    .writeCell("Line:1 Cell:5", 1)
                    .writeCell("Line:1 Cell:6", 1);
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
    void testUnauthorizedColspan() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             AsciiTableFormatter formatter = new AsciiTableFormatter(writer, null, config,
                 new Column("column1").setColspan(4).setHorizontalAlignment(HorizontalAlignment.CENTER),
                 new Column("column2").setColspan(2).setHorizontalAlignment(HorizontalAlignment.CENTER))) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> formatter.writeCell("Line:1 Cell:1", 1)
                    .writeCell("Line:1 Cell:2", 1)
                    .writeCell("Line:1 Cell:3", 1)
                    .writeCell("Line:1 Cell:4", 2)
                    .writeCell("Line:1 Cell:5", 1));
            assertEquals("You have exceded the authorized colspan", e.getMessage());
        }
    }

    @Test
    void testEmptyLines() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             AsciiTableFormatter formatter = new AsciiTableFormatter(writer, null, config,
                 new Column("column1").setColspan(4),
                 new Column("column2").setColspan(2))) {
            formatter.writeCell("Line:1 Cell:1")
                    .writeCell("Line:1 Cell:2")
                    .writeCell("Line:1 Cell:3")
                    .writeEmptyLine();
        }
        assertEquals("+--------------------------------------------------+---------+\n" +
                     "| column1                                          | column2 |\n" +
                     "+--------------------------------------------------+---------+\n" +
                     "| Line:1 Cell:1 | Line:1 Cell:2 | Line:1 Cell:3 |  |  |      |\n" +
                     "+---------------+---------------+---------------+--+--+------+" + System.lineSeparator(),
                new String(bos.toByteArray(), StandardCharsets.UTF_8));
    }
}
