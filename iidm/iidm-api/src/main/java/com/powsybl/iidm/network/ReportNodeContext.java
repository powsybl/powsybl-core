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
public interface ReportNodeContext {

    /**
     * Peek the current {@link ReportNode}.
     * @see #pushReportNode(ReportNode)
     * @see #popReportNode()
     * @return the last defined {@link ReportNode}
     */
    ReportNode peekReportNode();

    /**
     * Get the current {@link ReportNode}.
     * @see #pushReportNode(ReportNode)
     * @see #popReportNode()
     * @return the last defined {@link ReportNode}
     */
    ReportNode getReportNode();

    /**
     * Use the given {@link ReportNode} instead of the current one.<br/>
     * The reportNodes are stacked and the previous one should be restored later using {@link #popReportNode()}.
     * @see #popReportNode()
     *
     * @param reportNode The new reportNode to use.
     */
    void pushReportNode(ReportNode reportNode);

    /**
     * Pop the current {@link ReportNode} (defined via {@link ReportNode}) and restore the previous one.
     * @see #pushReportNode(ReportNode)
     *
     * @return the current {@link ReportNode}
     */
    ReportNode popReportNode();

}
