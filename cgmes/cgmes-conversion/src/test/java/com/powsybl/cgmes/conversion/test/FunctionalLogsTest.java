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

import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FunctionalLogsTest {

    @Test
    public void testImport() {
        TestGridModel testCase = CgmesConformity1Catalog.microGridBaseCaseBE();
        ReporterModel reporter = new ReporterModel("testFunctionalLogs",
                "Test importing ${name}", Map.of("name", new TypedValue(testCase.name(), TypedValue.UNTYPED)));
        Importers.importData("CGMES", testCase.dataSource(), null, reporter);

        String expected = "+ Test importing MicroGrid-BaseCase-BE" + System.lineSeparator() +
                "  + Read" + System.lineSeparator() +
                "     MicroGridTestConfiguration_EQ_BD.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_TP_BD.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_TP_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_EQ_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_SV_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_DL_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_GL_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_SSH_V2.xml" + System.lineSeparator() +
                "     MicroGridTestConfiguration_BC_BE_DY_V2.xml" + System.lineSeparator() +
                "  + Invalid data" + System.lineSeparator() +
                "    + Regulating control has a bad target voltage 0.0" + System.lineSeparator() +
                "       955d9cd0-4a10-4031-b008-60c0dc340a07" + System.lineSeparator() +
                "       fe25f43a-7341-446e-a71a-8ab7119ba806" + System.lineSeparator();

        StringWriter sw = new StringWriter();
        reporter.export(sw);
        assertEquals(expected, sw.toString());
    }

}
