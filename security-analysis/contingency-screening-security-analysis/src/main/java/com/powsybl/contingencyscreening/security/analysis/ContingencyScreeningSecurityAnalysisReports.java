/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyscreening.security.analysis;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}
 */
public final class ContingencyScreeningSecurityAnalysisReports {

    private ContingencyScreeningSecurityAnalysisReports() {
    }

    public static ReportNode createContingencyScreeningSecurityAnalysisReportNode(ReportNode reportNode, String networkId) {
        return reportNode.newReportNode()
                .withMessageTemplate("contingencyScreeningSecurityAnalysis")
                .withTypedValue("networkId", networkId, TypedValue.ID)
                .add();
    }

    public static ReportNode reportTotalContingencies(ReportNode reportNode, int count) {
        return reportNode.newReportNode()
                .withMessageTemplate("contingencyScreeningSecurityAnalysisTotalContingencies")
                .withUntypedValue("count", count)
                .add();
    }

    public static ReportNode reportFirstPassStarted(ReportNode reportNode, String providerName) {
        return reportNode.newReportNode()
                .withMessageTemplate("contingencyScreeningSecurityAnalysisFirstPassStarted")
                .withUntypedValue("providerName", providerName)
                .add();
    }

    public static void reportSecondPassRequired(ReportNode reportNode, int count) {
        reportNode.newReportNode()
                .withMessageTemplate("contingencyScreeningSecurityAnalysisSecondPassRequired")
                .withUntypedValue("count", count)
                .add();
    }

    public static void reportSecondPassStarted(ReportNode reportNode, String providerName) {
        reportNode.newReportNode()
                .withMessageTemplate("contingencyScreeningSecurityAnalysisSecondPassStarted")
                .withUntypedValue("providerName", providerName)
                .add();
    }
}
