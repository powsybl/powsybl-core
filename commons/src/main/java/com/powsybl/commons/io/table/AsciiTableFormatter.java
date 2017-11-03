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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AsciiTableFormatter extends AbstractTableFormatter {

    private final String title;

    private final Table table;

    public AsciiTableFormatter(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        super(writer, config, columns);
        this.title = title;
        this.table = new Table(columns.length, BorderStyle.CLASSIC_WIDE);
        for (Column column : columns) {
            table.addCell(column.getName());
        }
    }

    public AsciiTableFormatter(String title, Column... columns) {
        this(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), title, TableFormatterConfig.load(), columns);
    }

    @Override
    public TableFormatter writeComment(String comment) throws IOException {
        // not supported
        return this;
    }

    @Override
    protected TableFormatter write(String value) throws IOException {
        HorizontalAlignment horizontalAlignment = columns[column].getHorizontalAlignment();
        column = (column + 1) % columns.length;

        table.addCell(value, convertCellStyle(horizontalAlignment));

        return this;
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
