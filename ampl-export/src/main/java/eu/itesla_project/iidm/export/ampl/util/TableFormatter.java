/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TableFormatter {

    private final Locale locale;

    private final Writer writer;

    private final String title;

    private final float invalidFloatValue;

    private final Column[] columns;

    private boolean headerDone = false;

    private boolean bodyOngoing = false;

    private int column = 0;

    public TableFormatter(Locale locale, Writer writer, String title, float invalidFloatValue, Column... columns) {
        this.locale = locale;
        this.writer = writer;
        this.title = title;
        this.invalidFloatValue = invalidFloatValue;
        this.columns = columns;
    }

    public TableFormatter writeHeader() throws IOException {
        if (headerDone) {
            throw new IllegalArgumentException("Table header already written");
        }
        if (bodyOngoing) {
            throw new IllegalArgumentException("Table body is ongoing, cannot insert a header");
        }
        writer.append("#").append(title).append("\n")
              .append("#");
        for (int i = 0; i < columns.length; i++) {
            writer.append("\"").append(columns[i].getName()).append("\" ");
        }
        writer.append("\n");
        headerDone = true;
        return this;
    }

    public TableFormatter writeComment(String comment) throws IOException {
        if (column != 0) {
            throw new IllegalStateException("Row has to be completed to start a comment");
        }
        writer.write("#");
        writer.write(comment);
        writer.write("\n");
        return this;
    }

    public TableFormatter newRow() throws IOException {
        if (column < columns.length) {
            throw new IllegalStateException("The current row does not have enough column: "
                    + column + ", required " + columns.length);
        }
        writer.write("\n");
        column = 0;
        return this;
    }

    private TableFormatter write(String value) throws IOException {
        if (column >= columns.length) {
            throw new IllegalStateException("The current row has too many column: "
                    + (column + 1) + ", required " + columns.length);
        }
        writer.append(value).append(" ");
        column++;
        bodyOngoing = true;
        return this;
    }

    public TableFormatter writeCell(String s) throws IOException {
        return write("\"" + s + "\"");
    }

    public TableFormatter writeCell(char c) throws IOException {
        return write(Character.toString(c));
    }

    public TableFormatter writeCell(int i) throws IOException {
        return write(Integer.toString(i));
    }

    public TableFormatter writeCell(float f) throws IOException {
        return write(String.format(locale, "%g", Float.isNaN(f) ? invalidFloatValue : f));
    }

    public TableFormatter writeCell(double d) throws IOException {
        return write(String.format(locale, "%g", Double.isNaN(d) ? invalidFloatValue : d));
    }

    public TableFormatter writeCell(boolean b) throws IOException {
        return write(Boolean.toString(b));
    }

}