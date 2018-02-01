/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter.util;

import com.powsybl.ampl.converter.AmplException;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.CsvTableFormatter;
import com.powsybl.commons.io.table.TableFormatter;

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

    @Override
    protected void writeHeader() throws IOException {
        writer.append("#").append(title).append(System.lineSeparator())
              .append("#");
        for (int i = 0; i < columns.length; i++) {
            writer.append("\"").append(columns[i].getName()).append("\"");
            if (i < columns.length - 1) {
                writer.append(config.getCsvSeparator());
            }
        }
        writer.append(System.lineSeparator());
    }

    @Override
    public TableFormatter writeCell(String s) throws IOException {
        return write("\"" + s + "\"");
    }

    @Override
    public TableFormatter writeComment(String comment) throws IOException {
        if (column != 0) {
            throw new AmplException("Row has to be completed to start a comment");
        }
        writer.write("#");
        writer.write(comment);
        writer.write(System.lineSeparator());
        return this;
    }

}
