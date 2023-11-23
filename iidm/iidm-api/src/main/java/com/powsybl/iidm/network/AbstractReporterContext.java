/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.Reporter;

import java.util.Iterator;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public abstract class AbstractReporterContext implements ReporterContext {

    protected AbstractReporterContext() {
    }

    @Override
    public Reporter peekReporter() {
        return getReporter();
    }

    protected void copyReporters(AbstractReporterContext reporterContext) {
        Iterator<Reporter> it = reporterContext.descendingIterator();
        // Since we don't want to copy the always present NO_OP, we skip the 1st item
        it.next();
        while (it.hasNext()) {
            pushReporter(it.next());
        }
    }

    /**
     * <p>Return a descending iterator on the elements (first pushed first)</p>
     *
     * @return an Iterator on the elements
     */
    protected abstract Iterator<Reporter> descendingIterator();
}
