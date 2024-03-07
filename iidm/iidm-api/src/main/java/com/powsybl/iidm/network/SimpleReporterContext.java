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
 * Simple mono-thread ReporterContext's implementation.
 *
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class SimpleReporterContext extends AbstractReporterContext {

    private final Deque<ReportNode> reportNodes;

    public SimpleReporterContext() {
        this.reportNodes = new LinkedList<>();
        this.reportNodes.push(ReportNode.NO_OP);
    }

    public SimpleReporterContext(AbstractReporterContext reporterContext) {
        this();
        copyReporters(reporterContext);
    }

    @Override
    public ReportNode getReporter() {
        return this.reportNodes.peekFirst();
    }

    @Override
    public void pushReporter(ReportNode reportNode) {
        this.reportNodes.push(reportNode);
    }

    @Override
    public ReportNode popReporter() {
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
