/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A builder to create a root {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeRootBuilderImpl extends AbstractReportNodeAdderOrBuilder<ReportNodeBuilder> implements ReportNodeBuilder {

    private String timestampPattern;
    private Locale timestampLocale;
    private boolean timestamps;

    @Override
    public ReportNodeBuilder withTimestampPattern(String timestampPattern, Locale locale) {
        this.timestampPattern = timestampPattern;
        this.timestampLocale = locale;
        return this;
    }

    @Override
    public ReportNodeBuilder withTimestamps(boolean enabled) {
        this.timestamps = true;
        return this;
    }

    @Override
    public ReportNode build() {
        return ReportNodeImpl.createRootReportNode(key, messageTemplate, values, timestamps, createDateTimeFormatter(timestampPattern, timestampLocale));
    }

    private static DateTimeFormatter createDateTimeFormatter(String timestampPattern, Locale timestampLocale) {
        if (timestampPattern == null && timestampLocale == null) {
            return ReportConstants.DEFAULT_TIMESTAMP_FORMATTER;
        }
        if (timestampPattern == null) {
            return DateTimeFormatter.ofPattern(ReportConstants.DEFAULT_TIMESTAMP_PATTERN, timestampLocale);
        }
        if (timestampLocale == null) {
            return DateTimeFormatter.ofPattern(timestampPattern, ReportConstants.DEFAULT_TIMESTAMP_LOCALE);
        }
        return DateTimeFormatter.ofPattern(timestampPattern, timestampLocale);
    }

    @Override
    public ReportNodeBuilder self() {
        return this;
    }
}
