package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesReports;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
class CgmesReportsTest {

    @BeforeEach
    void setup() {
        Locale.setDefault(Locale.US);
    }

    @Test
    void importingCgmesFileReportTest() {
        ReportNode reportRoot = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("test")
                .build();
        ReadOnlyDataSource ds = new ResourceDataSource("CGMES import file(s)", new ResourceSet("/", "GeneratingUnitTypes.xml", "groundTest.xml"));
        ReportNode reportNode = CgmesReports.importingCgmesFileReport(reportRoot, ds.getBaseName());
        assertEquals("Importing CGMES file(s) with basename 'CGMES import file(s)'", reportNode.getMessage());
    }
}
