/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.io.table.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityValueCsvWriter implements SensitivityValueWriter {

    private static final char CSV_SEPARATOR = ',';

    private final TableFormatter formatter;

    public SensitivityValueCsvWriter(TableFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    public static TableFormatter createTableFormatter(Writer writer) {
        Objects.requireNonNull(writer);
        TableFormatterFactory factory = new CsvTableFormatterFactory();
        var tfc = new TableFormatterConfig(Locale.US, CSV_SEPARATOR, "N/A", true, false);
        return factory.create(writer, "", tfc,
                new Column("Contingency ID"),
                new Column("Variable ID"),
                new Column("Function ID"),
                new Column("Function ref value"),
                new Column("Sensitivity value"));
    }

    @Override
    public void write(String contingencyId, String variableId, String functionId, int factorIndex, int contingencyIndex, double value, double functionReference) {
        try {
            formatter.writeCell(Objects.toString(contingencyId, ""));
            formatter.writeCell(variableId);
            formatter.writeCell(functionId);
            formatter.writeCell(functionReference);
            formatter.writeCell(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
