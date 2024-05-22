/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

/**
 * An adder to create a {@link ReportNode} object as a child of given {@link ReportRoot} parent.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Caio Luke {@literal <caio.luke at artelys.com>}
 */
public class ReportNodeRootBuilderNoOp extends AbstractReportNodeAdderOrBuilder<ReportNodeBuilder> implements ReportNodeBuilder {

    public ReportNodeRootBuilderNoOp() {
        // Empty constructor
    }

    @Override
    public ReportNode build() {
        return new ReportNodeNoOp();
    }

    @Override
    public ReportNodeBuilder self() {
        return this;
    }
}
