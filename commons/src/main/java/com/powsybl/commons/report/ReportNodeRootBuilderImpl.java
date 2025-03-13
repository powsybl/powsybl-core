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
 * A builder to create a root {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeRootBuilderImpl extends AbstractReportNodeAdderOrBuilder<ReportNodeBuilder>
        implements ReportNodeBuilder {

    @Override
    public ReportNodeBuilder withReportTreeFactory(ReportTreeFactory reportTreeFactory) {
        this.reportTreeFactory = Objects.requireNonNull(reportTreeFactory);
        return this;
    }

    @Override
    public ReportNode build() {
        TreeContext treeContext = reportTreeFactory.createTreeContext();
        if (withTimestamp) {
            addTimeStampValue(treeContext);
        }
        updateTreeDictionary(treeContext);
        return reportTreeFactory.createRoot(key, values, treeContext, messageTemplateProvider);
    }

    @Override
    public ReportNodeRootBuilderImpl self() {
        return this;
    }
}
