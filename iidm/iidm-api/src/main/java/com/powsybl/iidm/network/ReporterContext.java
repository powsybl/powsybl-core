/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.Reporter;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public interface ReporterContext {

    /**
     * Peek the current {@link Reporter}.
     * @see #pushReporter(Reporter)
     * @see #popReporter()
     * @return the last defined {@link Reporter}
     */
    Reporter peekReporter();

    /**
     * Get the current {@link Reporter}.
     * @see #pushReporter(Reporter)
     * @see #popReporter()
     * @return the last defined {@link Reporter}
     */
    Reporter getReporter();

    /**
     * Use the given {@link Reporter} instead of the current one.<br/>
     * The reporters are stacked and the previous one should be restored later using {@link #popReporter()}.
     * @see #popReporter()
     *
     * @param reporter The new reporter to use.
     */
    void pushReporter(Reporter reporter);

    /**
     * Pop the current {@link Reporter} (defined via {@link Reporter}) and restore the previous one.
     * @see #pushReporter(Reporter)
     *
     * @return the current {@link Reporter}
     */
    Reporter popReporter();

}
