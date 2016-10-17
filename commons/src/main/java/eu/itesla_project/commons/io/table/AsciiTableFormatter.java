/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io.table;

import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AsciiTableFormatter extends AbstractTableFormatter {

    private final String title;

    private final Writer writer;

    private final Table table ;

    public AsciiTableFormatter(String title, Locale locale, Column... columns) {
        this(new OutputStreamWriter(System.out), title, locale, columns);
    }

    public AsciiTableFormatter(Writer writer, String title, Locale locale, Column... columns) {
        super(locale, "inv");
        this.writer = writer;
        this.title = title;
        this.table = new Table(columns.length, BorderStyle.CLASSIC_WIDE);
        for (Column column : columns) {
            table.addCell(column.getName());
        }
    }

    @Override
    public AsciiTableFormatter writeComment(String comment) throws IOException {
        // not supported
        return this;
    }

    @Override
    protected TableFormatter write(String value) throws IOException {
        table.addCell(value);
        return this;
    }

    @Override
    public void close() throws IOException {
        writer.write(title + ":" + System.lineSeparator());
        writer.write(table.render() + System.lineSeparator());
        writer.close();
    }
}
