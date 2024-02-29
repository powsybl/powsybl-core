/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public interface ReportRoot extends ReportNodesContainer {
    RootContext getContext();

    /**
     * Create a new adder to create a <code>ReporterNode</code> child.
     * @return the created <code>ReporterNodeAdder</code>
     */
    ReportNodeChildAdder newReportNode();

    /**
     * Serialize the current report root
     * @param generator the jsonGenerator to use for serialization
     */
    void writeJson(JsonGenerator generator) throws IOException;
}
