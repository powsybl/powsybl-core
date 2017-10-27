/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.Security;
import com.powsybl.security.SecurityAnalysisResult;

import java.io.Writer;

/**
 * A base-class for security analysis results exporters based on table
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractTableSecurityAnalysisResultExporter implements SecurityAnalysisResultExporter {

    protected abstract TableFormatterFactory getTableFormatterFactory();

    protected TableFormatterConfig getTableFormatterConfig() {
        return TableFormatterConfig.load();
    }

    @Override
    public void export(SecurityAnalysisResult result, LimitViolationFilter limitViolationFilter, Writer writer) {
        TableFormatterFactory tableFormatterFactory = getTableFormatterFactory();
        TableFormatterConfig tableFormatterConfig = getTableFormatterConfig();
        Security.printPreContingencyViolations(result, writer, tableFormatterFactory, tableFormatterConfig, limitViolationFilter);
        Security.printPostContingencyViolations(result, writer, tableFormatterFactory, tableFormatterConfig, limitViolationFilter, true);
    }
}
