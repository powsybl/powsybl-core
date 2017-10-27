/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;

import java.io.*;

/**
 * A SecurityAnalysisResultExporter implementation which export the result as CSV-like format
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(SecurityAnalysisResultExporter.class)
public class CsvSecurityAnalysisResultExporter extends AbstractTableSecurityAnalysisResultExporter {

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in a CSV-like format";
    }

    @Override
    protected TableFormatterFactory getTableFormatterFactory() {
        return new CsvTableFormatterFactory();
    }
}
