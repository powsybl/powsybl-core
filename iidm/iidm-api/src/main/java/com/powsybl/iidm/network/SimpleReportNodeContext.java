/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.ReportNode;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Simple mono-thread ReportNodeContext's implementation.
 *
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class SimpleReportNodeContext extends AbstractReportNodeContext {

    private final Deque<ReportNode> reportNodes;

    public SimpleReportNodeContext() {
        this.reportNodes = new LinkedList<>();
        this.reportNodes.push(ReportNode.NO_OP);
    }

    public SimpleReportNodeContext(AbstractReportNodeContext reportNodeContext) {
        this();
        copyReportNodes(reportNodeContext);
    }

    @Override
    public ReportNode getReportNode() {
        return this.reportNodes.peekFirst();
    }

    @Override
    public void pushReportNode(ReportNode reportNode) {
        this.reportNodes.push(reportNode);
    }

    @Override
    public ReportNode popReportNode() {
        ReportNode popped = this.reportNodes.pop();
        if (reportNodes.isEmpty()) {
            this.reportNodes.push(ReportNode.NO_OP);
        }
        return popped;
    }

    @Override
    protected Iterator<ReportNode> descendingIterator() {
        return reportNodes.descendingIterator();
    }
}
