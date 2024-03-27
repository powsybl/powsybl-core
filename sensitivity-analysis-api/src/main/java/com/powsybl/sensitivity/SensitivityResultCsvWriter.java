/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.io.table.*;
import com.powsybl.contingency.Contingency;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityResultCsvWriter implements SensitivityResultWriter {

    private final TableFormatter formatter;

    private final TableFormatter formatterContingencyStatus;

    private final List<Contingency> contingencies;

    public SensitivityResultCsvWriter(TableFormatter formatter, TableFormatter formatterContingencyStatus,
                                      List<Contingency> contingencies) {
        this.formatter = Objects.requireNonNull(formatter);
        this.formatterContingencyStatus = Objects.requireNonNull(formatterContingencyStatus);
        this.contingencies = Objects.requireNonNull(contingencies);
    }

    public static TableFormatter createTableFormatter(Writer writer) {
        Objects.requireNonNull(writer);
        TableFormatterFactory factory = new CsvTableFormatterFactory();
        var tfc = TableFormatterConfig.load();
        return factory.create(writer, "Sensitivity analysis result", tfc,
                new Column("Contingency ID"),
                new Column("Factor index"),
                new Column("Function ref value"),
                new Column("Sensitivity value"));
    }

    public static TableFormatter createContingencyStatusTableFormatter(Writer writer) {
        Objects.requireNonNull(writer);
        TableFormatterFactory factory = new CsvTableFormatterFactory();
        var tfc = TableFormatterConfig.load();
        return factory.create(writer, "Sensitivity analysis contingency status result", tfc,
                new Column("Contingency ID"),
                new Column("Contingency Status"));
    }

    @Override
    public void writeSensitivityValue(int factorIndex, int contingencyIndex, double value, double functionReference) {
        Contingency contingency = contingencyIndex != -1 ? contingencies.get(contingencyIndex) : null;
        try {
            formatter.writeCell(contingency != null ? contingency.getId() : "");
            formatter.writeCell(factorIndex);
            formatter.writeCell(functionReference);
            formatter.writeCell(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeContingencyStatus(int contingencyIndex, SensitivityAnalysisResult.Status status) {
        try {
            formatterContingencyStatus.writeCell(contingencies.get(contingencyIndex).getId());
            formatterContingencyStatus.writeCell(status.name());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
