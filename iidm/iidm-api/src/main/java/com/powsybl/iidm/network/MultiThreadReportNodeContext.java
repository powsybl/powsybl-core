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
 * <p>Multi-thread {@link ReportNodeContext}'s implementation.</p>
 * <p>To avoid memory leaks, this context must be closed (with the {@link #close()} method) after usage.</p>
 *
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class MultiThreadReportNodeContext extends AbstractReportNodeContext {

    private final ThreadLocal<Deque<ReportNode>> reportNodes;

    public MultiThreadReportNodeContext() {
        this.reportNodes = ThreadLocal.withInitial(() -> {
            Deque<ReportNode> deque = new LinkedList<>();
            deque.push(ReportNode.NO_OP);
            return deque;
        });
    }

    public MultiThreadReportNodeContext(AbstractReportNodeContext reportNodeContext) {
        this();
        copyReportNodes(reportNodeContext);
    }

    @Override
    public ReportNode getReportNode() {
        return this.reportNodes.get().peek();
    }

    @Override
    public void pushReportNode(ReportNode reportNode) {
        this.reportNodes.get().push(reportNode);
    }

    @Override
    public ReportNode popReportNode() {
        ReportNode popped = this.reportNodes.get().pop();
        if (reportNodes.get().isEmpty()) {
            this.reportNodes.get().push(ReportNode.NO_OP);
        }
        return popped;
    }

    public void close() {
        reportNodes.remove();
    }

    @Override
    protected Iterator<ReportNode> descendingIterator() {
        return reportNodes.get().descendingIterator();
    }
}
