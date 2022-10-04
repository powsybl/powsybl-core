package com.powsybl.cgmes.shortcircuit; /**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.commons.datasource.ResourceSet;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class ShortCircuitCgmesConformity1ModifiedCatalog {

    private ShortCircuitCgmesConformity1ModifiedCatalog() {
    }

    // BaseCase is taken from powsybl-core repository
    public static TestGridModelResources smallGridBusBranchWithBusbarSectionsAndIpMax() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/SmallGrid/BusBranch_busbarSections_ipMax";
        String baseOriginal = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
            "SmallGrid-BusBranch-With-BusbarSecions-And-ipMax",
            null,
            new ResourceSet(baseOriginal, "SmallGridTestConfiguration_BC_DL_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_GL_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_SV_v3.0.0.xml"),
            new ResourceSet(base, "SmallGridTestConfiguration_BC_EQ_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_TP_v3.0.0.xml"),
            new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    private static final String ENTSOE_CONFORMITY_1 = "/conformity/cas-1.1.3-data-4.0.3";
    private static final String ENTSOE_CONFORMITY_1_MODIFIED = "/conformity-modified/cas-1.1.3-data-4.0.3";
}
