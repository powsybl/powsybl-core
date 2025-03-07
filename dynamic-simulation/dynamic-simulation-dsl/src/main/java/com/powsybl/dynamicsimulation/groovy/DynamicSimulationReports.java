/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.commons.report.ReportNode;

/**
 * @author Alice Caron {@literal <alice.caron at rte-france.com>}
 */
public final class DynamicSimulationReports {

    public static final String BUNDLE_BASE_NAME = "com.powsybl.commons.reports";

    public static ReportNode buildRootDynamicSimulationTool() {
        return ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("core-dynasim-dynamicSimulationTool", BUNDLE_BASE_NAME)
                .build();
    }

    public static ReportNode supplyGroovyDynamicModels(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-dynasim-groovyDynamicModels", BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode supplyGroovyEventModels(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-dynasim-groovyEventModels", BUNDLE_BASE_NAME)
                .add();
    }

    public static ReportNode supplyGroovyOutputVariables(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withLocaleMessageTemplate("core-dynasim-groovyOutputVariables", BUNDLE_BASE_NAME)
                .add();
    }

    private DynamicSimulationReports() {
    }
}
