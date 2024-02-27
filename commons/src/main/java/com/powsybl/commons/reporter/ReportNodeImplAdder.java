/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import java.util.Map;

/**
 * An adder to create {@link ReportNode} objects.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeImplAdder extends AbstractReportNodeAdder {

    public ReportNodeImplAdder(ReportNode parent) {
        super(parent);
    }

    @Override
    protected ReportNode createReportNode(String key, String messageTemplate, Map<String, TypedValue> values, ReportNode parent) {
        return new ReportNodeImpl(key, messageTemplate, values, parent.getValuesDeque());
    }
}
