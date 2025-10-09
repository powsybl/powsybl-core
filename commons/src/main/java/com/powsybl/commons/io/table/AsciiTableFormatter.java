/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
import java.util.Arrays;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class AsciiTableFormatter extends AbstractTableFormatter {

    private final String title;

    private final Table table;

    private final int cellsCount;

    // Cell index in the current row
    private int rowCellIndex = 0;

    // Cell index in the current column
    private int cellIndex = 0;

    public AsciiTableFormatter(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        super(writer, config, columns);
        this.title = title;
        cellsCount = Arrays.stream(columns).mapToInt(Column::getColspan).sum();

        this.table = new Table(cellsCount, BorderStyle.CLASSIC_WIDE);
        for (Column column : columns) {
            table.addCell(column.getName(), convertCellStyle(column.getTitleHorizontalAlignment()), column.getColspan());
        }
    }

    public AsciiTableFormatter(String title, Column... columns) {
        this(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), title, TableFormatterConfig.load(), columns);
    }

    public AsciiTableFormatter(Writer writer, String title, Column... columns) {
        this(writer, title, TableFormatterConfig.load(), columns);
    }

    @Override
    public TableFormatter writeEmptyLine() throws IOException {
        return writeEmptyCells(cellsCount - rowCellIndex);
    }

    @Override
    public TableFormatter writeComment(String comment) {
        // not supported
        return this;
    }

    @Override
    public TableFormatter writeCell(String value, int colspan) {
        if (colspan > columns[column].getColspan() - cellIndex) {
            throw new IllegalArgumentException("You have exceded the authorized colspan");
        }

        HorizontalAlignment horizontalAlignment = (colspan == 1) ? columns[column].getHorizontalAlignment() : HorizontalAlignment.CENTER;
        table.addCell(value, convertCellStyle(horizontalAlignment), colspan);

        rowCellIndex = (rowCellIndex + 1) % cellsCount;
        cellIndex = (cellIndex + colspan) % columns[column].getColspan();
        if (cellIndex == 0) {
            column = (column + 1) % columns.length;
        }
        return this;
    }

    @Override
    protected TableFormatter write(String value) throws IOException {
        return writeCell(value, 1);
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
