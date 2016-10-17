/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl.util;

import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.CsvTableFormatter;
import eu.itesla_project.commons.io.table.TableFormatter;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

/**
 * Specialization of CSV table formatter for AMPL .dat file generation.
 * 3 differences:
 *   - separator is white space
 *   - comments are allowed and start with # (header is also a comment)
 *   - strings are quoted because of the white space separator
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplDatTableFormatter extends CsvTableFormatter {

    public AmplDatTableFormatter(Writer writer, String title, float invalidFloatValue, boolean writeHeader, Locale locale, Column... columns) {
        super(writer, title, ' ', Float.toString(invalidFloatValue), writeHeader, locale, columns);
    }

    protected void writeHeader() throws IOException {
        writer.append("#").append(title).append(System.lineSeparator())
              .append("#");
        for (int i = 0; i < columns.length; i++) {
            writer.append("\"").append(columns[i].getName()).append("\"");
            if (i < columns.length-1) {
                writer.append(separator);
            }
        }
        writer.append(System.lineSeparator());
    }

    @Override
    public AmplDatTableFormatter writeCell(String s) throws IOException {
        write("\"" + s + "\"");
        return this;
    }

    @Override
    public AmplDatTableFormatter writeComment(String comment) throws IOException {
        if (column != 0) {
            throw new IllegalStateException("Row has to be completed to start a comment");
        }
        writer.write("#");
        writer.write(comment);
        writer.write(System.lineSeparator());
        return this;
    }

}