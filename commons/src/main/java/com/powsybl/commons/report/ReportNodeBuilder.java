/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A builder to create a {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNodeBuilder extends ReportNodeAdderOrBuilder<ReportNodeBuilder> {

    /**
     * Sets the pattern used for timestamps, when a timestamp is added with {@link ReportNodeAdder#withTimestamp()}.
     * If no pattern is given, the default pattern {@link ReportConstants#DEFAULT_TIMESTAMP_PATTERN} is used.
     * Note that the {@link Locale} used to format the timestamps is the one set by {@link #withLocale(Locale)}, or,
     * if not set, the default Locale {@link ReportConstants#DEFAULT_LOCALE}.
     *
     * @param timestampPattern : the pattern to use for the timestamp (see {@link DateTimeFormatter#ofPattern(String, Locale)}})
     * @return a reference to this object
     */
    ReportNodeBuilder withDefaultTimestampPattern(String timestampPattern);

    /**
     * Choose which {@link Locale} to use for formatting all the `ReportNode` messages of the tree.
     * @return a reference to this object
     */
    ReportNodeBuilder withLocale(Locale locale);

    /**
     * Build the corresponding {@link ReportNode}.
     * @return the new {@link ReportNode} corresponding to current <code>ReportNodeBuilder</code>
     */
    ReportNode build();
}
