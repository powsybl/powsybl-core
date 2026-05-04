package com.powsybl.hybrid.security.analysis;

import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>} */
public class HybridSecurityAnalysisReportsTest {
    @Test
    void testReportTotalContingencies() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = HybridSecurityAnalysisReports.reportTotalContingencies(reportNode, 42);

        assertNotNull(resultNode);
        assertEquals("hybridSecurityAnalysisTotalContingencies", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("count").isPresent());
        assertEquals("42", resultNode.getValue("count").get().toString());
    }

    @Test
    void testReportFirstPassStarted() {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("test")
                .build();

        ReportNode resultNode = HybridSecurityAnalysisReports.reportFirstPassStarted(reportNode, "load-flow");

        assertNotNull(resultNode);
        assertEquals("hybridSecurityAnalysisFirstPassStarted", resultNode.getMessageKey());
        assertTrue(resultNode.getValue("providerName").isPresent());
        assertEquals("load-flow", resultNode.getValue("providerName").get().toString());
    }
}
