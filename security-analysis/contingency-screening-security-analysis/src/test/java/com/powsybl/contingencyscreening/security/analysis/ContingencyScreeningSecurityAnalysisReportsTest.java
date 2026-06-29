package com.powsybl.contingencyscreening.security.analysis;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>} */
class ContingencyScreeningSecurityAnalysisReportsTest {

    @Test
    void testCreateRootReportNode() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode csaNode = ContingencyScreeningSecurityAnalysisReports
                .createContingencyScreeningSecurityAnalysisReportNode(rootNode, "test-network");

        assertNotNull(csaNode);
        assertEquals("contingencyScreeningSecurityAnalysis", csaNode.getMessageKey());
    }

    @Test
    void testReportTotalContingenciesBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = ContingencyScreeningSecurityAnalysisReports
                .reportTotalContingencies(rootNode, 10);

        assertNotNull(resultNode);
        assertEquals("contingencyScreeningSecurityAnalysisTotalContingencies", resultNode.getMessageKey());
    }

    @Test
    void testReportFirstPassStartedBasic() {
        ReportNode rootNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = ContingencyScreeningSecurityAnalysisReports
                .reportFirstPassStarted(rootNode, "LoadFlow");

        assertNotNull(resultNode);
        assertEquals("contingencyScreeningSecurityAnalysisFirstPassStarted", resultNode.getMessageKey());
    }

    @Test
    void testReportTotalContingencies() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = ContingencyScreeningSecurityAnalysisReports.reportTotalContingencies(reportNode, 42);

        assertNotNull(resultNode);
        assertEquals("contingencyScreeningSecurityAnalysisTotalContingencies", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("count").isPresent());
        assertEquals("42", resultNode.getValue("count").get().toString());
    }

    @Test
    void testReportFirstPassStarted() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = ContingencyScreeningSecurityAnalysisReports.reportFirstPassStarted(reportNode, "load-flow");

        assertNotNull(resultNode);
        assertEquals("contingencyScreeningSecurityAnalysisFirstPassStarted", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("providerName").isPresent());
        assertEquals("load-flow", resultNode.getValue("providerName").get().toString());
    }
}
