/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.table;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TableFormatterTest {

    private static final Column[] COLUMNS = {
            new Column("int"),
            new Column("float"),
            new Column("bool"),
            new Column("string")
    };

    private static void write(TableFormatter formatter) throws IOException {
        formatter.writeCell(2).writeCell(1.3).writeCell(true).writeCell("aaa")
                .writeCell(3).writeCell(4.2).writeCell(false).writeCell("bbb");
    }

    @Test
    public void testCsv() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TableFormatterConfig config = new TableFormatterConfig(Locale.US, ';', "inv", true, true);
        try (TableFormatter formatter = new CsvTableFormatter(new OutputStreamWriter(bos), "csv test", config, COLUMNS)) {
            write(formatter);
        }
        assertEquals(new String(bos.toByteArray(), StandardCharsets.UTF_8),
                "csv test\n" +
                "int;float;bool;string\n" +
                "2;1.30000;true;aaa\n" +
                "3;4.20000;false;bbb\n");
    }

    @Test
    public void testAcsii() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TableFormatterConfig config = new TableFormatterConfig(Locale.US, "inv");
        try (TableFormatter formatter = new AsciiTableFormatter(new OutputStreamWriter(bos), "ascii test", config, COLUMNS)) {
            write(formatter);
        }
        assertEquals(new String(bos.toByteArray(), StandardCharsets.UTF_8),
                "ascii test:\n" +
                "+-----+---------+-------+--------+\n" +
                "| int | float   | bool  | string |\n" +
                "+-----+---------+-------+--------+\n" +
                "| 2   | 1.30000 | true  | aaa    |\n" +
                "| 3   | 4.20000 | false | bbb    |\n" +
                "+-----+---------+-------+--------+\n");
    }
}