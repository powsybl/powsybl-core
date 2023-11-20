/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.Reporter;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Simple mono-thread ReporterContext's implementation.
 *
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class SimpleReporterContext extends AbstractReporterContext {

    private final Deque<Reporter> reporters;

    public SimpleReporterContext() {
        this.reporters = new LinkedList<>();
        this.reporters.push(Reporter.NO_OP);
    }

    public SimpleReporterContext(AbstractReporterContext reporterContext) {
        this();
        copyReporters(reporterContext);
    }

    @Override
    public Reporter getReporter() {
        return this.reporters.peekFirst();
    }

    @Override
    public void pushReporter(Reporter reporter) {
        this.reporters.push(reporter);
    }

    @Override
    public Reporter popReporter() {
        Reporter popped = this.reporters.pop();
        if (reporters.isEmpty()) {
            this.reporters.push(Reporter.NO_OP);
        }
        return popped;
    }

    @Override
    protected Iterator<Reporter> descendingIterator() {
        return reporters.descendingIterator();
    }
}
