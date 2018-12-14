/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import org.apache.commons.lang3.StringUtils;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AsciiTableFormatter extends AbstractTableFormatter {

    private final String title;

    private final Table table;

    private final List<Integer> indexes;

    private int columnIndex = 0;

    public AsciiTableFormatter(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        super(writer, config, columns);
        this.title = title;
        this.table = new Table(tabLength, BorderStyle.CLASSIC_WIDE);
        indexes = new ArrayList<>();
        indexes.add(columns[0].getColspan());
        table.addCell(columns[0].getName(), convertCellStyle(columns[0].getHorizontalAlignment()), columns[0].getColspan());
        for (int i = 1; i < columns.length; i++) {
            table.addCell(columns[i].getName(), convertCellStyle(columns[i].getHorizontalAlignment()), columns[i].getColspan());
            indexes.add(columns[i].getColspan() + indexes.get(i - 1));
        }
    }

    public AsciiTableFormatter(String title, Column... columns) {
        this(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), title, TableFormatterConfig.load(), columns);
    }

    public AsciiTableFormatter(Writer writer, String title, Column... columns) {
        this(writer, title, TableFormatterConfig.load(), columns);
    }

    @Override
    public TableFormatter writeComment(String comment) throws IOException {
        // not supported
        return this;
    }

    private  int getColumnIndex(int column) {
        for (int i = 0; i < columns.length; i++) {
            if (column < indexes.get(i)) {
                return i;
            }
        }
        return columns.length - 1;
    }

    @Override
    protected TableFormatter write(String value, int colspan) throws IOException {
        if (colspan > indexes.get(columnIndex) - column) {
            throw new IllegalArgumentException("You have exceded the authorized colspan");
        }

        if (colspan > 1) {
            table.addCell(value, convertCellStyle(HorizontalAlignment.CENTER), colspan);
        } else {
            HorizontalAlignment horizontalAlignment = columns[columnIndex].getHorizontalAlignment();
            table.addCell(value, convertCellStyle(horizontalAlignment), colspan);
        }
        column = (column + colspan) % tabLength;
        columnIndex = getColumnIndex(column);
        return this;
    }

    @Override
    protected TableFormatter write(String value) throws IOException {
        return write(value, 1);
    }

    @Override
    public void close() throws IOException {
        if (!StringUtils.isEmpty(title)) {
            writer.write(title + ":" + System.lineSeparator());
        }
        writer.write(table.render() + System.lineSeparator());
        writer.flush();
    }

    private static CellStyle convertCellStyle(HorizontalAlignment horizontalAlignment) {
        switch (horizontalAlignment) {
            case LEFT:
                return new CellStyle(CellStyle.HorizontalAlign.left);
            case CENTER:
                return new CellStyle(CellStyle.HorizontalAlign.center);
            case RIGHT:
                return new CellStyle(CellStyle.HorizontalAlign.right);
            default:
                return new CellStyle();
        }
    }
}
