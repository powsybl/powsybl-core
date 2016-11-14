/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.table;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvTableFormatter extends AbstractTableFormatter {

    protected final Writer writer;

    protected final String title;

    protected final char separator;

    protected final Column[] columns;

    protected boolean headerDone = false;

    protected int column = 0;

    protected final boolean writeTitle;

    public CsvTableFormatter(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        this(writer, title, config.getCsvSeparator(), config.getInvalidString(), config.getPrintHeader(), config.getPrintTitle(), config.getLocale(), columns);
    }

    public CsvTableFormatter(Writer writer, String title, char separator, String invalidString, boolean writeHeader, Locale locale, Column... columns) {
        this(writer, title, separator, invalidString, writeHeader, true, locale, columns);
    }

    public CsvTableFormatter(Writer writer, String title, char separator, String invalidString, boolean writeHeader, boolean writeTitle, Locale locale, Column... columns) {
        super(locale, invalidString);
        this.writer = Objects.requireNonNull(writer);
        this.title = Objects.requireNonNull(title);
        this.separator = separator;
        this.columns = Objects.requireNonNull(columns);
        headerDone = !writeHeader;
        this.writeTitle = writeTitle;
    }

    private void writeHeaderIfNotDone() throws IOException {
        if (headerDone) {
            return;
        }
        writeHeader();
        headerDone = true;
    }

    protected void writeHeader() throws IOException {
        if (writeTitle) {
            writer.append(title).append(System.lineSeparator());
        }
        for (int i = 0; i < columns.length; i++) {
            writer.append(columns[i].getName());
            if (i < columns.length-1) {
                writer.append(separator);
            }
        }
        writer.append(System.lineSeparator());
    }

    @Override
    public TableFormatter writeComment(String comment) throws IOException {
        return this;
    }

    @Override
    protected TableFormatter write(String value) throws IOException {
        writeHeaderIfNotDone();
        writer.append(value);
        if (column < columns.length-1) {
            writer.append(separator);
        }
        column++;
        if (column == columns.length) {
            writer.write(System.lineSeparator());
            column = 0;
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        writeHeaderIfNotDone();
        writer.close();
    }
}