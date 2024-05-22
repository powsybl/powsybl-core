/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;

/**
 * A builder to create a {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNodeBuilder extends ReportNodeAdderOrBuilder<ReportNodeBuilder> {

    /**
     * Enable/disable timestamps on build ReportNode and all descendants
     * @param timestampPattern: the pattern to use for the timestamp (see {@link DateTimeFormatter#ofPattern}), or null to disable the timestamp
     * @return a reference to this object
     */
    ReportNodeBuilder withTimestamps(String timestampPattern);

    /**
     * Enable timestamps on build ReportNode and all descendants with default pattern (see {@link ReportConstants#DEFAULT_TIMESTAMP_PATTERN})
     * @return a reference to this object
     */
    default ReportNodeBuilder withTimestamps() {
        return withTimestamps(ReportConstants.DEFAULT_TIMESTAMP_PATTERN);
    }


    /**
     * Build the corresponding {@link ReportNode}.
     * @return the new {@link ReportNode} corresponding to current <code>ReportNodeBuilder</code>
     */
    ReportNode build();
}
