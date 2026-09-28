/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.twopasssecurity.security.analysis;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}
 */
class TwoPassSecurityAnalysisReportsTest {

    @Test
    void testCreateRootReportNode() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode csaNode = TwoPassSecurityAnalysisReports
                .createTwoPassSecurityAnalysisReportNode(rootNode, "test-network");

        assertNotNull(csaNode);
        assertEquals("core.securityAnalysis.twoPass.start", csaNode.getMessageKey());
    }

    @Test
    void testReportTotalContingenciesBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports
                .reportTotalContingencies(rootNode, 10);

        assertNotNull(resultNode);
        assertEquals("core.securityAnalysis.twoPass.totalContingencies", resultNode.getMessageKey());
    }

    @Test
    void testReportFirstPassStartedBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports
                .reportFirstPassStarted(rootNode, "LoadFlow");

        assertNotNull(resultNode);
        assertEquals("core.securityAnalysis.twoPass.firstPassStarted", resultNode.getMessageKey());
    }

    @Test
    void testReportTotalContingencies() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports.reportTotalContingencies(reportNode, 42);

        assertNotNull(resultNode);
        assertEquals("core.securityAnalysis.twoPass.totalContingencies", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("count").isPresent());
        assertEquals("42", resultNode.getValue("count").get().toString());
    }

    @Test
    void testReportFirstPassStarted() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports.reportFirstPassStarted(reportNode, "load-flow");

        assertNotNull(resultNode);
        assertEquals("core.securityAnalysis.twoPass.firstPassStarted", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("providerName").isPresent());
        assertEquals("load-flow", resultNode.getValue("providerName").get().toString());
    }
}
