/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

/**
 * An adder to create a {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportNodeAdder extends ReportNodeAdderOrBuilder<ReportNodeAdder> {
    /**
     * Build and add the corresponding {@link ReportNode}.
     * @return the new {@link ReportNode} corresponding to current <code>ReportNodeAdder</code>
     */
    ReportNode add();
}
