/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.network.Importers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FunctionalLogsTest {

    @Test
    public void testImportMicroGridBaseCaseBE() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE.txt",
                importReport(CgmesConformity1Catalog.microGridBaseCaseBE()));
    }

    @Test
    public void testImportMicroGridBaseCaseBETargetDeadbandNegative() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE-target-deadband.txt",
                importReport(CgmesConformity1ModifiedCatalog.microGridBaseBETargetDeadbandNegative()));
    }

    @Test
    public void testImportMicroGridBaseCaseBEInvalidVoltageBus() throws IOException {
        checkResult("/functional-logs/microGridBaseCaseBE-invalid-voltage-bus.txt",
                importReport(CgmesConformity1ModifiedCatalog.microGridBaseBEInvalidVoltageBus()));
    }

    @Test
    public void testImportMiniGridNodeBreaker() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        checkResult("/functional-logs/miniGridNodeBreaker.txt",
                importReport(CgmesConformity1Catalog.miniNodeBreaker(), importParams));
    }

    private ReporterModel importReport(TestGridModel testCase) {
        return importReport(testCase, null);
    }

    private ReporterModel importReport(TestGridModel testCase, Properties importParams) {
        ReporterModel reporter = new ReporterModel("testFunctionalLogs",
                "Test importing ${name}", Map.of("name", new TypedValue(testCase.name(), TypedValue.UNTYPED)));
        Importers.importData("CGMES", testCase.dataSource(), importParams, reporter);
        return reporter;
    }

    private void checkResult(String resourceName, ReporterModel reporter) throws IOException {
        StringWriter sw = new StringWriter();
        reporter.export(sw);
        String expected = new String(getClass().getResourceAsStream(resourceName).readAllBytes());
        assertEquals(expected, sw.toString());
    }
}
