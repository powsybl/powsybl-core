/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.commons.reporter.TypedValue;
import com.powsybl.iidm.import_.Importers;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FunctionalLogsTest {

    @Test
    public void testImport() throws IOException {
        TestGridModel testCase = CgmesConformity1Catalog.microGridBaseCaseBE();
        ReporterModel reporter = new ReporterModel("testFunctionalLogs",
                "Test importing ${name}", Map.of("name", new TypedValue(testCase.name(), TypedValue.UNTYPED)));
        Importers.importData("CGMES", testCase.dataSource(), null, reporter);

        StringWriter sw = new StringWriter();
        reporter.export(sw);
        String expected = new String(getClass().getResourceAsStream("/microGridBaseCaseBE-functional-logs.txt").readAllBytes());
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testImportSmallNodeBreakerHvdcOnlyEq() {
        TestGridModel testCase = CgmesConformity1Catalog.smallNodeBreakerHvdcOnlyEQ();
        ReporterModel reporter = new ReporterModel("testFunctionalLogs",
                "Test importing ${name}", Map.of("name", new TypedValue(testCase.name(), TypedValue.UNTYPED)));
        Importers.importData("CGMES", testCase.dataSource(), null, reporter);
        StringWriter sw = new StringWriter();
        reporter.export(sw);
        String reported = sw.toString();
        // Check the instance files that have been read
        String readExpected = "  + Read" + System.lineSeparator() +
                              "     SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml" + System.lineSeparator() +
                              "     SmallGridTestConfiguration_EQ_BD_v3.0.0.xml" + System.lineSeparator() +
                              "  +";
        assertTrue(reported.contains(readExpected));
        // Check that the issue of empty vsc regulation has been considered for grouping
        assertTrue(reported.contains("+ EmptyVscRegulation"));
    }
}
