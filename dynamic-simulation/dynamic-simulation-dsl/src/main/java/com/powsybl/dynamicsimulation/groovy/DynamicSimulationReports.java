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

    public static ReportNode buildRootDynamicSimulationTool() {
        return ReportNode.newRootReportNode()
                .withMessageTemplate("dynamicSimulationTool", "Dynamic Simulation Tool")
                .build();
    }

    public static ReportNode supplyGroovyDynamicModels(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("groovyDynamicModels", "Groovy Dynamic Models Supplier")
                .add();
    }

    public static ReportNode supplyGroovyEventModels(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("groovyEventModels", "Groovy Event Models Supplier")
                .add();
    }

    public static ReportNode supplyGroovyOutputVariables(ReportNode reportNode) {
        return reportNode.newReportNode()
                .withMessageTemplate("groovyOutputVariables", "Groovy Output Variables Supplier")
                .add();
    }

    private DynamicSimulationReports() {
    }
}
