/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;

/**
 * Exports a short circuit analysis result in CSV format.
 *
 * @author Boubakeur Brahimi
 */
@AutoService(ShortCircuitAnalysisResultExporter.class)
public class CsvShortCircuitAnalysisResultExporter extends AbstractTableShortCircuitAnalysisResultExporter {

    private static final TableFormatterFactory FACTORY = new CsvTableFormatterFactory();

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String getComment() {
        return "Export a result in a CSV-like format";
    }

    @Override
    protected TableFormatterFactory getTableFormatterFactory() {
        return FACTORY;
    }
}
