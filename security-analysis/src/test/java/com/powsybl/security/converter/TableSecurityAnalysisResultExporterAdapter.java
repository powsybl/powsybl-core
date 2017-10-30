/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Adapter to override the loading of the TableFormatterConfig configuration.
 *
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TableSecurityAnalysisResultExporterAdapter extends AbstractTableSecurityAnalysisResultExporter {

    private final AbstractTableSecurityAnalysisResultExporter adaptee;

    public TableSecurityAnalysisResultExporterAdapter(AbstractTableSecurityAnalysisResultExporter adaptee) {
        this.adaptee = Objects.requireNonNull(adaptee);
    }

    @Override
    protected TableFormatterFactory getTableFormatterFactory() {
        return adaptee.getTableFormatterFactory();
    }

    @Override
    protected TableFormatterConfig getTableFormatterConfig() {
        return new TableFormatterConfig(Locale.US, ';', "inv", true, true);
    }

    @Override
    public String getFormat() {
        return adaptee.getFormat();
    }

    @Override
    public String getComment() {
        return adaptee.getComment();
    }
}
