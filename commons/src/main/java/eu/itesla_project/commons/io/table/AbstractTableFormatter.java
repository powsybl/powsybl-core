/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.table;

import java.io.IOException;
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
        return write(Character.toString(c));
    }

    @Override
    public TableFormatter writeCell(int i) throws IOException {
        return write(Integer.toString(i));
    }

    @Override
    public TableFormatter writeCell(float f) throws IOException {
        return write(Float.isNaN(f) ? invalidString : String.format(locale, "%g", f));
    }

    @Override
    public TableFormatter writeCell(double d) throws IOException {
        return write(Double.isNaN(d) ? invalidString : String.format(locale, "%g", d));
    }

    @Override
    public TableFormatter writeCell(boolean b) throws IOException {
        return write(Boolean.toString(b));
    }

}
