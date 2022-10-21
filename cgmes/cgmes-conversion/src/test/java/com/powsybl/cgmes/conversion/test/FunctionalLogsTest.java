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
                "  + Missing" + System.lineSeparator() +
                "    + Identified issues not yet organized" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit BE-Line_1 - CL-2 (f454fa68-0bd5-4e4e-92d6-6108176ad3bf)" + System.lineSeparator() +
                "       Terminal f3b56334-4638-49d3-a6a0-3f417422b8f5 or Equipment null at OperationalLimit BE-Line_3 - CL-2 (3cff44a0-b9a5-4b35-a724-8740d73c07c9)" + System.lineSeparator() +
                "       Terminal f9f29835-8a31-4310-9780-b1ad26f3cbb0 or Equipment null at OperationalLimit BE-Line_4 - CL-2 (3ebc30bc-3b24-4ee1-9534-da22e2cd66d1)" + System.lineSeparator() +
                "       Terminal 02a244ca-8bcb-4e25-8613-e948b8ba1f22 or Equipment null at OperationalLimit BE-Line_5 - CL-2 (e80822fa-fc7f-4a11-a084-10b4893b7546)" + System.lineSeparator() +
                "       Terminal 57ae9251-c022-4c67-a8eb-611ad54c963c or Equipment null at OperationalLimit BE-Line_7 - CL-2 (88240efd-b544-4131-bc99-e6b77d4bac88)" + System.lineSeparator() +
                "       Terminal 57ae9251-c022-4c67-a8eb-611ad54c963c or Equipment null at OperationalLimit 90 (88240efd-b544-4131-bc99-e6b77d4bac881)" + System.lineSeparator() +
                "       Terminal 02a244ca-8bcb-4e25-8613-e948b8ba1f22 or Equipment null at OperationalLimit 90 (e80822fa-fc7f-4a11-a084-10b4893b75461)" + System.lineSeparator() +
                "       Terminal f9f29835-8a31-4310-9780-b1ad26f3cbb0 or Equipment null at OperationalLimit 90 (3ebc30bc-3b24-4ee1-9534-da22e2cd66d11)" + System.lineSeparator() +
                "       Terminal f3b56334-4638-49d3-a6a0-3f417422b8f5 or Equipment null at OperationalLimit 90 (3cff44a0-b9a5-4b35-a724-8740d73c07c91)" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit 90 (f454fa68-0bd5-4e4e-92d6-6108176ad3bf1)" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit BE-Line_1 - CL-1 (e9171d11-8877-48ab-a50a-7ede9d4ec4b6)" + System.lineSeparator() +
                "       Terminal f3b56334-4638-49d3-a6a0-3f417422b8f5 or Equipment null at OperationalLimit BE-Line_3 - CL-1 (235676b8-e13c-4400-878f-b0ddc4c9aeae)" + System.lineSeparator() +
                "       Terminal f9f29835-8a31-4310-9780-b1ad26f3cbb0 or Equipment null at OperationalLimit BE-Line_4 - CL-1 (6db95b07-f943-465c-b6ff-844929c07c8b)" + System.lineSeparator() +
                "       Terminal 02a244ca-8bcb-4e25-8613-e948b8ba1f22 or Equipment null at OperationalLimit BE-Line_5 - CL-1 (a3ecec82-f9e9-4a3c-9cd9-23ae8756ba3c)" + System.lineSeparator() +
                "       Terminal 57ae9251-c022-4c67-a8eb-611ad54c963c or Equipment null at OperationalLimit BE-Line_7 - CL-1 (cce95c87-f08f-47b7-9cdf-f0189a92572f)" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit BE-Line_1 - CL-0 (7156a0ea-cf7a-46b0-91c0-a973e1c6feb2)" + System.lineSeparator() +
                "       Terminal f3b56334-4638-49d3-a6a0-3f417422b8f5 or Equipment null at OperationalLimit BE-Line_3 - CL-0 (e537f16f-6107-47a6-a728-797ac246d777)" + System.lineSeparator() +
                "       Terminal f9f29835-8a31-4310-9780-b1ad26f3cbb0 or Equipment null at OperationalLimit BE-Line_4 - CL-0 (217293df-7f75-4838-8557-d422f7b83c2f)" + System.lineSeparator() +
                "       Terminal 02a244ca-8bcb-4e25-8613-e948b8ba1f22 or Equipment null at OperationalLimit BE-Line_5 - CL-0 (be0d07fc-d20a-430d-82e3-93d48d3f220f)" + System.lineSeparator() +
                "       Terminal 57ae9251-c022-4c67-a8eb-611ad54c963c or Equipment null at OperationalLimit BE-Line_7 - CL-0 (6623e566-3111-4050-9b4e-c98d89ba3e69)" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit BE-Line_1 - CL-3 (ebe4f8e3-7bc8-4162-8bdf-e100742b2363)" + System.lineSeparator() +
                "       Terminal 1ef0715a-d5a9-477b-b6e7-b635529ac140 or Equipment null at OperationalLimit BE-Line_1 - CL-4 (8447ed5e-3d27-42c0-9e67-9affdda0be45)" + System.lineSeparator() +
                "  + Invalid" + System.lineSeparator() +
                "    + Regulating control has a bad target voltage 0.0" + System.lineSeparator() +
                "       955d9cd0-4a10-4031-b008-60c0dc340a07" + System.lineSeparator() +
                "       fe25f43a-7341-446e-a71a-8ab7119ba806" + System.lineSeparator() +
                "  + Pending" + System.lineSeparator() +
                "    + Regulating Terminal" + System.lineSeparator() +
                "       The setting of the regulating control 67971e8d-518a-408f-9e59-c7601da9a989 is not entirely handled." + System.lineSeparator() +
                "       The setting of the regulating control 4d50f86d-0d12-4ca3-9430-56bb05f9eee6 is not entirely handled." + System.lineSeparator();

        StringWriter sw = new StringWriter();
        reporter.export(sw);
        assertEquals(expected, sw.toString());
    }

}
