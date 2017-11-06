/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.Security;
import com.powsybl.security.SecurityAnalysisResult;

import java.io.Writer;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractTableSecurityAnalysisResultExporter implements SecurityAnalysisResultExporter {

    protected abstract TableFormatterFactory getTableFormatterFactory();

    protected TableFormatterConfig getTableFormatterConfig() {
        return TableFormatterConfig.load();
    }

    @Override
    public void export(SecurityAnalysisResult result, Network network, Writer writer) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(network);
        Objects.requireNonNull(writer);

        TableFormatterFactory tableFormatterFactory = getTableFormatterFactory();
        TableFormatterConfig tableFormatterConfig = getTableFormatterConfig();

        Security.printPreContingencyViolations(result, writer, tableFormatterFactory, tableFormatterConfig, null);
        Security.printPostContingencyViolations(result, writer, tableFormatterFactory, tableFormatterConfig, null, true);
    }
}
