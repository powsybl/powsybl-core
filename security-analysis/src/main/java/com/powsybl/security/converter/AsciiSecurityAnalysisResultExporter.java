/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.Security;
import com.powsybl.security.SecurityAnalysisResult;

import java.io.Writer;

/**
 * A SecurityAnalysisResultExporter implementation which renders the result in ASCII tables.
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(SecurityAnalysisResultExporter.class)
public class AsciiSecurityAnalysisResultExporter implements SecurityAnalysisResultExporter {

    @Override
    public String getFormat() {
        return "ASCII";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in ASCII tables";
    }

    @Override
    public void export(SecurityAnalysisResult result, LimitViolationFilter limitViolationFilter, Writer writer) {
        TableFormatterFactory tableFormatterFactory = new AsciiTableFormatterFactory();
        Security.printPreContingencyViolations(result, writer, tableFormatterFactory, limitViolationFilter);
        Security.printPostContingencyViolations(result, writer, tableFormatterFactory, limitViolationFilter);
    }
}
