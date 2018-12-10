/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.table;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CsvTableFormatter extends AbstractTableFormatter {

    protected final String title;

    protected boolean headerDone = false;

    public CsvTableFormatter(Writer writer, String title, TableFormatterConfig config, Column... columns) {
        super(writer, config, columns);
        this.title = Objects.requireNonNull(title);
        this.headerDone = !config.getPrintHeader();
    }

    public CsvTableFormatter(Writer writer, String title, char separator, String invalidString, boolean writeHeader, Locale locale, Column... columns) {
        this(writer, title, separator, invalidString, writeHeader, true, locale, columns);
    }

    public CsvTableFormatter(Writer writer, String title, char separator, String invalidString, boolean writeHeader, boolean writeTitle, Locale locale, Column... columns) {
        this(writer, title, new TableFormatterConfig(locale, separator, invalidString, writeHeader, writeTitle), columns);
    }

    private void writeHeaderIfNotDone() throws IOException {
        if (headerDone) {
            return;
        }
        writeHeader();
        headerDone = true;
    }

    protected void writeHeader() throws IOException {
        if (config.getPrintTitle()) {
            writer.append(title).append(System.lineSeparator());
        }
        for (int i = 0; i < columns.length; i++) {
            writer.append(columns[i].getName());
            if (i < columns.length - 1) {
                writer.append(config.getCsvSeparator());
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
        if (column < columns.length - 1) {
            writer.append(config.getCsvSeparator());
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
        writer.flush();
    }
}
