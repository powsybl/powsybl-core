/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTableFormatter implements TableFormatter {

    protected final Writer writer;

    protected final TableFormatterConfig config;

    protected final Column[] columns;

    protected int column;

    protected AbstractTableFormatter(Writer writer, TableFormatterConfig config, Column... columns) {
        this.writer = Objects.requireNonNull(writer);
        this.config = Objects.requireNonNull(config);
        this.columns = Objects.requireNonNull(columns);
        this.column = 0;
    }

    protected abstract TableFormatter write(String value) throws IOException;

    @Override
    public TableFormatter writeCell(String s) throws IOException {
        return write(s);
    }

    @Override
    public TableFormatter writeEmptyCell() throws IOException {
        return writeCell("");
    }

    @Override
    public TableFormatter writeCell(char c) throws IOException {
        return writeCell(Character.toString(c));
    }

    @Override
    public TableFormatter writeCell(int i) throws IOException {
        return writeCell(Integer.toString(i));
    }

    @Override
    public TableFormatter writeCell(float f) throws IOException {
        return writeCell(Float.isNaN(f) ? config.getInvalidString() : format(f));
    }

    @Override
    public TableFormatter writeCell(double d) throws IOException {
        return writeCell(Double.isNaN(d) ? config.getInvalidString() : format(d));
    }

    @Override
    public TableFormatter writeCell(boolean b) throws IOException {
        return writeCell(Boolean.toString(b));
    }

    private <T> String format(T value) {
        NumberFormat format = columns[column].getNumberFormat();
        if (format == null) {
            return String.format(config.getLocale(), "%g", value);
        } else {
            return format.format(value);
        }
    }
}
