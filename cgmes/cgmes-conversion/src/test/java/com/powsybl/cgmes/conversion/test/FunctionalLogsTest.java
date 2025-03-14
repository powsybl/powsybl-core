/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundles;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Importers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class FunctionalLogsTest {
    private Properties importParams;

    @BeforeEach
    void setUp() {
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void testImportMicroGridBaseCaseBE() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE.txt",
                importReport(CgmesConformity1Catalog.microGridBaseCaseBE()));
    }

    @Test
    void testImportMicroGridBaseCaseBETargetDeadbandNegative() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE-target-deadband.txt",
                importReport(CgmesConformity1ModifiedCatalog.microGridBaseBETargetDeadbandNegative()));
    }

    @Test
    void testImportMicroGridBaseCaseBEInvalidVoltageBus() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE-invalid-voltage-bus.txt",
                importReport(CgmesConformity1ModifiedCatalog.microGridBaseBEInvalidVoltageBus()));
    }

    @Test
    void testImportMiniGridNodeBreaker() throws IOException {
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        checkResult("/functional-logs/miniGridNodeBreaker.txt",
                importReport(CgmesConformity1Catalog.miniNodeBreaker()));
    }

    private ReportNode importReport(GridModelReference testCase) {
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("testFunctionalLogs")
                .withUntypedValue("name", testCase.name())
                .build();
        Importers.importData("CGMES", testCase.dataSource(), importParams, reportNode);
        return reportNode;
    }

    private void checkResult(String resourceName, ReportNode reportNode) throws IOException {
        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            String expected = new String(is.readAllBytes());
            assertEquals(expected, sw.toString());
        }
    }
}
