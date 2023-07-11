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
import java.util.LinkedList;

/**
 * Multi-thread ReporterContext's implementation.
 *
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class MultiThreadReporterContext implements ReporterContext {

    private final ThreadLocal<Deque<Reporter>> reporters;

    public MultiThreadReporterContext() {
        this.reporters = ThreadLocal.withInitial(() -> {
            Deque<Reporter> deque = new LinkedList<>();
            deque.push(Reporter.NO_OP);
            return deque;
        });
    }

    @Override
    public Reporter getReporter() {
        return this.reporters.get().peekFirst();
    }

    @Override
    public void pushReporter(Reporter reporter) {
        this.reporters.get().push(reporter);
    }

    @Override
    public Reporter popReporter() {
        Reporter popped = this.reporters.get().pop();
        if (reporters.get().isEmpty()) {
            this.reporters.get().push(Reporter.NO_OP);
        }
        return popped;
    }

}
