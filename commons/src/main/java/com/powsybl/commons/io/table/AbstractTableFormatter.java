/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractTableFormatter implements TableFormatter {

    protected final Locale locale;

    protected final String invalidString;

    protected AbstractTableFormatter(Locale locale, String invalidString) {
        this.locale = Objects.requireNonNull(locale);
        this.invalidString = Objects.requireNonNull(invalidString);
    }

    @Deprecated
    protected TableFormatter write(String value) throws IOException {
        return write(value, HorizontalAlignment.LEFT);
    }

    protected abstract TableFormatter write(String value, HorizontalAlignment horizontalAlignment) throws IOException;

    @Override
    public TableFormatter writeCell(String s) throws IOException {
        return writeCell(s, HorizontalAlignment.LEFT);
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
        return writeCell(c, HorizontalAlignment.LEFT);
    }

    @Override
    public TableFormatter writeCell(char c, HorizontalAlignment horizontalAlignment) throws IOException {
        return writeCell(Character.toString(c), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(int i) throws IOException {
        return writeCell(i, HorizontalAlignment.LEFT);
    }

    @Override
    public TableFormatter writeCell(int i, HorizontalAlignment horizontalAlignment) throws IOException {
        return writeCell(Integer.toString(i), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(int i, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return writeCell(numberFormat.format(i), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(float f) throws IOException {
        return writeCell(f, HorizontalAlignment.LEFT);
    }

    @Override
    public TableFormatter writeCell(float f, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Float.isNaN(f) ? invalidString : String.format(locale, "%g", f), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(float f, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return write(Float.isNaN(f) ? invalidString : numberFormat.format(f), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(double d) throws IOException {
        return writeCell(d, HorizontalAlignment.LEFT);
    }

    @Override
    public TableFormatter writeCell(double d, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Double.isNaN(d) ? invalidString : String.format(locale, "%g", d), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(double d, HorizontalAlignment horizontalAlignment, NumberFormat numberFormat) throws IOException {
        Objects.requireNonNull(numberFormat);

        return write(Double.isNaN(d) ? invalidString : numberFormat.format(d), horizontalAlignment);
    }

    @Override
    public TableFormatter writeCell(boolean b) throws IOException {
        return writeCell(b, HorizontalAlignment.LEFT);
    }

    @Override
    public TableFormatter writeCell(boolean b, HorizontalAlignment horizontalAlignment) throws IOException {
        return write(Boolean.toString(b), horizontalAlignment);
    }
}
