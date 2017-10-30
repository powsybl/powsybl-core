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

/**
 * A SecurityAnalysisResultExporter implementation which renders the result in ASCII tables.
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(SecurityAnalysisResultExporter.class)
public class AsciiSecurityAnalysisResultExporter extends AbstractTableSecurityAnalysisResultExporter {

    @Override
    public String getFormat() {
        return "ASCII";
    }

    @Override
    public String getComment() {
        return "Export a security analysis result in ASCII tables";
    }

    @Override
    protected TableFormatterFactory getTableFormatterFactory() {
        return new AsciiTableFormatterFactory();
    }
}
