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
        new Column("double"),
        new Column("float"),
        new Column("bool"),
        new Column("empty"),
        new Column("char"),
        new Column("string"),
        new Column("empty2")
    };

    private static void write(TableFormatter formatter) throws IOException {
        formatter.writeCell(2).writeCell(Double.NaN).writeCell(2.4f).writeCell(true).writeEmptyCell()
                .writeCell('a').writeCell("aaa").writeEmptyCell()
                .writeCell(3).writeCell(4.2).writeCell(Float.NaN).writeCell(false).writeEmptyCell()
                .writeCell('b').writeCell("bbb").writeEmptyCell();
    }

    @Test
    public void testCsv() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TableFormatterConfig config = new TableFormatterConfig(Locale.US, ';', "inv", true, true);
        CsvTableFormatterFactory factory = new CsvTableFormatterFactory();
        try (TableFormatter formatter = factory.create(new OutputStreamWriter(bos, StandardCharsets.UTF_8), "csv test", config, COLUMNS)) {
            write(formatter);
        }
        assertEquals(new String(bos.toByteArray(), StandardCharsets.UTF_8),
                "csv test" + System.lineSeparator() +
                "int;double;float;bool;empty;char;string;empty2" + System.lineSeparator() +
                "2;inv;2.40000;true;;a;aaa;" + System.lineSeparator() +
                "3;4.20000;inv;false;;b;bbb;" + System.lineSeparator());
    }

    @Test
    public void testAcsii() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TableFormatterConfig config = new TableFormatterConfig(Locale.US, "inv");
        AsciiTableFormatterFactory factory = new AsciiTableFormatterFactory();
        try (TableFormatter formatter = factory.create(new OutputStreamWriter(bos, StandardCharsets.UTF_8), "ascii test", config, COLUMNS)) {
            write(formatter);
        }
        assertEquals(new String(bos.toByteArray(), StandardCharsets.UTF_8),
                "ascii test:" + System.lineSeparator() +
                "+-----+---------+---------+-------+-------+------+--------+--------+\n" +
                "| int | double  | float   | bool  | empty | char | string | empty2 |\n" +
                "+-----+---------+---------+-------+-------+------+--------+--------+\n" +
                "| 2   | inv     | 2.40000 | true  |       | a    | aaa    |        |\n" +
                "| 3   | 4.20000 | inv     | false |       | b    | bbb    |        |\n" +
                "+-----+---------+---------+-------+-------+------+--------+--------+" + System.lineSeparator());
    }
}
