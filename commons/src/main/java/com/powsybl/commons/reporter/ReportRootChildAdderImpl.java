/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import java.util.Collections;
import java.util.Objects;

/**
 * An adder to create a {@link ReportNode} object as a child of given {@link ReportRoot} parent.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportRootChildAdderImpl extends AbstractReportNodeChildAdder {

    private final ReportRootImpl parent;

    public ReportRootChildAdderImpl(ReportRootImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public ReportNode add() {
        ReportNode node = new ReportNodeImpl(key, messageTemplate, values, Collections.emptyList(), parent.getContext());
        parent.addChild(node);
        return node;
    }
}
