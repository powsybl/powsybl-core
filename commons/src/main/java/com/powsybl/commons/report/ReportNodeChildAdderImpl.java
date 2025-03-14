/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Objects;

/**
 * An adder to create a {@link ReportNode} object as a child of given {@link ReportNode} parent.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeChildAdderImpl extends AbstractReportNodeAdderOrBuilder<ReportNodeAdder> implements ReportNodeAdder {

    private final ReportNodeImpl parent;

    ReportNodeChildAdderImpl(ReportNodeImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public ReportNode add() {
        ReportNodeImpl node = ReportNodeImpl.createChildReportNode(key, messageTemplate, values, withTimestamp, timestampPattern, parent);
        parent.addChild(node);
        return node;
    }

    @Override
    public ReportNodeAdder self() {
        return this;
    }
}
