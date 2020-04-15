/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.io.table.*;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.SensitivityValue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Locale;

/**
 *
 * A SensitivityComputationResultExporter implementation which export the result in CSV
 *
 * @author Agnes Leroy <agnes.leroy@rte-france.com>
 */
@AutoService(SensitivityComputationResultExporter.class)
public class CsvSensitivityComputationResultExporter implements SensitivityComputationResultExporter {

    private static final char CSV_SEPARATOR = ',';

    private void writeCells(TableFormatter formatter, SensitivityValue sensitivityValue, String state) throws IOException {
        formatter.writeCell(state);
        formatter.writeCell(sensitivityValue.getFactor().getVariable().getId());
        formatter.writeCell(sensitivityValue.getFactor().getVariable().getName());
        formatter.writeCell(sensitivityValue.getFactor().getFunction().getId());
        formatter.writeCell(sensitivityValue.getFactor().getFunction().getName());
        formatter.writeCell(sensitivityValue.getVariableReference());
        formatter.writeCell(sensitivityValue.getFunctionReference());
        formatter.writeCell(sensitivityValue.getValue());
    }

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String getComment() {
        return "Export a sensitivity analysis result in CSV format";
    }

    @Override
    public void export(SensitivityComputationResults result, Writer writer) {
        TableFormatterFactory factory = new CsvTableFormatterFactory();
        TableFormatterConfig tfc = new TableFormatterConfig(Locale.US, CSV_SEPARATOR, "N/A", true, false);
        try (TableFormatter formatter = factory.create(writer, "", tfc,
                new Column("Variant"),
                new Column("VariableId"),
                new Column("VariableName"),
                new Column("FunctionId"),
                new Column("FunctionName"),
                new Column("VariableRefValue"),
                new Column("FunctionRefValue"),
                new Column("SensitivityValue"))) {
            result.getSensitivityValues().forEach(sensitivityValue -> {
                try {
                    writeCells(formatter, sensitivityValue, "State N");
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            if (result.contingenciesArePresent()) {
                result.getSensitivityValuesContingencies().forEach((contId, sensitivityValues) -> sensitivityValues.forEach(sensitivityValue -> {
                    try {
                        writeCells(formatter, sensitivityValue, "Contingency " + contId);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
