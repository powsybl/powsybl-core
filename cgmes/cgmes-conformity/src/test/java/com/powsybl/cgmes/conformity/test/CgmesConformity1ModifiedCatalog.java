/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity.test;

import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.commons.datasource.ResourceSet;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1ModifiedCatalog {

    public final TestGridModelResources microGridBaseCaseBERatioPhaseTapChangerTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_tabular/";
        String baseOriginal = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
            "MicroGrid-BaseCase-BE-RTC-PTC-Tabular",
            null,
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
            new ResourceSet(baseOriginal,
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public TestGridModelResources microGridBaseCaseBEReactiveCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_q_curves/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
            "MicroGrid-BaseCase-BE-Q-Curves",
            null,
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public TestGridModelResources microGridBaseCaseBEReactiveCapabilityCurveOnePoint() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curve_1_point/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
                "MicroGrid-BaseCase-BE-Q-Curves-1-point",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                                              "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public final TestGridModelResources miniNodeBreakerLimitsforEquipment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_limits/";
        String baseOriginal = ENTSOE_CONFORMITY_1
            + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
            "MiniGrid-NodeBreaker-LimistForEquipment",
            null,
            new ResourceSet(base,
                "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
            new ResourceSet(baseOriginal,
                "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
            new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    private static final String ENTSOE_CONFORMITY_1 = "/conformity/cas-1.1.3-data-4.0.3";
    private static final String ENTSOE_CONFORMITY_1_MODIFIED = "/conformity-modified/cas-1.1.3-data-4.0.3";
}
