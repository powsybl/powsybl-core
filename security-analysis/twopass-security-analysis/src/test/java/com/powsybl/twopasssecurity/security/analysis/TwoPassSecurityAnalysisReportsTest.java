package com.powsybl.twopasssecurity.security.analysis;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>} */
class TwoPassSecurityAnalysisReportsTest {

    @Test
    void testCreateRootReportNode() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode csaNode = TwoPassSecurityAnalysisReports
                .createTwoPassSecurityAnalysisReportNode(rootNode, "test-network");

        assertNotNull(csaNode);
        assertEquals("twoPassSecurityAnalysis", csaNode.getMessageKey());
    }

    @Test
    void testReportTotalContingenciesBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports
                .reportTotalContingencies(rootNode, 10);

        assertNotNull(resultNode);
        assertEquals("twoPassSecurityAnalysisTotalContingencies", resultNode.getMessageKey());
    }

    @Test
    void testReportFirstPassStartedBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports
                .reportFirstPassStarted(rootNode, "LoadFlow");

        assertNotNull(resultNode);
        assertEquals("twoPassSecurityAnalysisFirstPassStarted", resultNode.getMessageKey());
    }

    @Test
    void testReportTotalContingencies() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = TwoPassSecurityAnalysisReports.reportTotalContingencies(reportNode, 42);

        assertNotNull(resultNode);
        assertEquals("twoPassSecurityAnalysisTotalContingencies", resultNode.getMessageKey());
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
        assertEquals("twoPassSecurityAnalysisFirstPassStarted", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("providerName").isPresent());
        assertEquals("load-flow", resultNode.getValue("providerName").get().toString());
    }
}
