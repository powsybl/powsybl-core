/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.ReportNode;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public interface ReporterContext {

    /**
     * Peek the current {@link ReportNode}.
     * @see #pushReporter(ReportNode)
     * @see #popReporter()
     * @return the last defined {@link ReportNode}
     */
    ReportNode peekReporter();

    /**
     * Get the current {@link ReportNode}.
     * @see #pushReporter(ReportNode)
     * @see #popReporter()
     * @return the last defined {@link ReportNode}
     */
    ReportNode getReporter();

    /**
     * Use the given {@link ReportNode} instead of the current one.<br/>
     * The reporters are stacked and the previous one should be restored later using {@link #popReporter()}.
     * @see #popReporter()
     *
     * @param reportNode The new reporter to use.
     */
    void pushReporter(ReportNode reportNode);

    /**
     * Pop the current {@link ReportNode} (defined via {@link ReportNode}) and restore the previous one.
     * @see #pushReporter(ReportNode)
     *
     * @return the current {@link ReportNode}
     */
    ReportNode popReporter();

}
