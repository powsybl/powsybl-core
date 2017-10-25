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

    protected AbstractTableFormatter(Writer writer, TableFormatterConfig config) {
        this.writer = Objects.requireNonNull(writer);
        this.config = Objects.requireNonNull(config);
    }

    /**
     * @deprecated Use write(String, HorizontalAlignment) instead
     */
    @Deprecated
    protected TableFormatter write(String value) throws IOException {
        return write(value, config.getHorizontalAlignment());
    }

    protected abstract TableFormatter write(String value, HorizontalAlignment horizontalAlignment) throws IOException;

    @Override
    public TableFormatter writeCell(String s) throws IOException {
        return writeCell(s, config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(String s, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(s, horizontalAlignment);
    }

    @Override
    public TableFormatter writeEmptyCell() throws IOException {
        return writeCell("");
    }

    @Override
    public TableFormatter writeCell(char c) throws IOException {
        return writeCell(c, config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(char c, HorizontalAlignment horizontalAlignment) throws IOException {
        return writeCell(Character.toString(c), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(int i) throws IOException {
        return writeCell(Integer.toString(i), config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(int i, HorizontalAlignment horizontalAlignment) throws IOException {
        return writeCell(i, horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(int i, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return writeCell(numberFormat.format(i), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(float f) throws IOException {
        return write(Float.isNaN(f) ? config.getInvalidString() : String.format(config.getLocale(), "%g", f), config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(float f, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Float.isNaN(f) ? config.getInvalidString() : String.format(config.getLocale(), "%g", f), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(float f, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return write(Float.isNaN(f) ? config.getInvalidString() : numberFormat.format(f), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(double d) throws IOException {
        return write(Double.isNaN(d) ? config.getInvalidString() : String.format(config.getLocale(), "%g", d), config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(double d, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Double.isNaN(d) ? config.getInvalidString() : String.format(config.getLocale(), "%g", d), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(double d, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return write(Double.isNaN(d) ? config.getInvalidString() : numberFormat.format(d), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(boolean b) throws IOException {
        return writeCell(b, config.getHorizontalAlignment());
    }

    @Override
    public TableFormatter writeCell(boolean b, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Boolean.toString(b), horizontalAlignment);
    }
}
