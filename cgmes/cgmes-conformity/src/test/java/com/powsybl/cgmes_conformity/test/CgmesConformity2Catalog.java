package com.powsybl.cgmes_conformity.test;

/*
 * #%L
 * CGMES conformity
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Path;
import java.nio.file.Paths;

import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.commons.datasource.CompressionFormat;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity2Catalog {

    public TestGridModel entsoeExplicitLoadFlow() {
        return new TestGridModel(
                ENTSOE_CONFORMITY_2.resolve("ENTSOE_ExplicitLoadFlowCalculation"),
                // The documentation and the solved case data do not take into account
                // the attribute TapChangerTablePoint.x
                // so we have set it to zero in the fixed test configuration
                "fixed-ENTSOE_CGMES_v2.4_ExplicitLoadFlowCalculation",
                CompressionFormat.ZIP,
                null,
                false,
                true);
    }

    public TestGridModel transformerLineTest() {
        return new TestGridModel(
                ENTSOE_CONFORMITY_2.resolve("TransformerLineTest"),
                "TransformerLineTest",
                CompressionFormat.ZIP,
                null,
                false,
                true);
    }

    public final TestGridModel microBaseCaseBE() {
        return new TestGridModel(
                ENTSOE_CONFORMITY_2.resolve("MicroGrid/BaseCase_BC"),
                "CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2",
                CompressionFormat.ZIP,
                null,
                false,
                true);
    }

    public final TestGridModel microBaseCaseNL() {
        return new TestGridModel(
                ENTSOE_CONFORMITY_2.resolve("MicroGrid/BaseCase_BC"),
                "CGMES_v2.4.15_MicroGridTestConfiguration_BC_NL_v2",
                CompressionFormat.ZIP,
                null,
                false,
                true);
    }

    public final TestGridModel microBaseCaseAssembled() {
        return new TestGridModel(
                ENTSOE_CONFORMITY_2.resolve("MicroGrid/BaseCase_BC"),
                "CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2",
                CompressionFormat.ZIP,
                null,
                false,
                true);
    }

    private static final Path ENTSOE_CONFORMITY_2 = Paths.get("../../data/conformity/cas-2.0");
}
