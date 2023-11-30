/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conformity;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
final class CgmesCatalogsConstants {

    static final String CGMES_3_TEST_MODELS = "/cgmes3-test-models";
    static final String ENTSOE_CONFORMITY_1 = "/conformity/cas-1.1.3-data-4.0.3";
    static final String ENTSOE_CONFORMITY_3 = "/conformity/cas-3-data-3.0.2";

    static final String MICRO_GRID_BE_BASE = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
    static final String MICRO_GRID_BD_BASE = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
    static final String MICRO_GRID_ASSEMBLED_BASE = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
    static final String MICRO_GRID_T4_BASE = ENTSOE_CONFORMITY_1
            + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
    static final String MICRO_GRID_T4_BD_BASE = ENTSOE_CONFORMITY_1
            + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
    static final String MINI_GRID_BUS_BRANCH_BASE = ENTSOE_CONFORMITY_1
            + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
    static final String MINI_GRID_NODE_BREAKER_BASE = ENTSOE_CONFORMITY_1
            + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
    static final String MINI_GRID_NODE_BREAKER_BD_BASE = ENTSOE_CONFORMITY_1
            + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
    static final String SMALL_GRID_BUS_BRANCH_BASE = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
    static final String SMALL_GRID_BUS_BRANCH_BD_BASE = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
    static final String SMALL_GRID_HVDC_BASE = ENTSOE_CONFORMITY_1
            + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
    static final String SMALL_GRID_NODE_BREAKER_BASE = ENTSOE_CONFORMITY_1
            + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
    static final String SMALL_GRID_NODE_BREAKER_BD_BASE = ENTSOE_CONFORMITY_1
            + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";

    static final String MICRO_GRID_3_BE_BASE = ENTSOE_CONFORMITY_3
            + "/MicroGrid/BaseCase/MicroGrid-BE-MAS/";
    static final String MICRO_GRID_3_NL_BASE = ENTSOE_CONFORMITY_3
            + "/MicroGrid/BaseCase/MicroGrid-NL-MAS/";
    static final String MICRO_GRID_3_BD_BASE = ENTSOE_CONFORMITY_3
            + "/MicroGrid/BaseCase/MicroGrid-BD-MAS/";
    static final String MICRO_GRID_3_MERGED_BASE = ENTSOE_CONFORMITY_3
            + "/MicroGrid/BaseCase/MicroGrid-BaseCase-Merged/";

    static final String CGMES_3_MICRO_GRID_BASE = CGMES_3_TEST_MODELS + "/MicroGrid/";
    static final String CGMES_3_MINI_GRID_BASE = CGMES_3_TEST_MODELS + "/MiniGrid/";
    static final String CGMES_3_SMALL_GRID_BASE = CGMES_3_TEST_MODELS + "/SmallGrid/";
    static final String CGMES_3_SVEDALA_BASE = CGMES_3_TEST_MODELS + "/Svedala/";

    static final String MICRO_GRID_BE_EQ = "MicroGridTestConfiguration_BC_BE_EQ_V2.xml";
    static final String MICRO_GRID_BE_TP = "MicroGridTestConfiguration_BC_BE_TP_V2.xml";
    static final String MICRO_GRID_BE_SSH = "MicroGridTestConfiguration_BC_BE_SSH_V2.xml";
    static final String MICRO_GRID_BE_SV = "MicroGridTestConfiguration_BC_BE_SV_V2.xml";

    static final String MICRO_GRID_NL_EQ = "MicroGridTestConfiguration_BC_NL_EQ_V2.xml";
    static final String MICRO_GRID_NL_TP = "MicroGridTestConfiguration_BC_NL_TP_V2.xml";
    static final String MICRO_GRID_NL_SSH = "MicroGridTestConfiguration_BC_NL_SSH_V2.xml";

    static final String MICRO_GRID_T4_EQ = "MicroGridTestConfiguration_T4_BE_EQ_V2.xml";
    static final String MICRO_GRID_T4_TP = "MicroGridTestConfiguration_T4_BE_TP_V2.xml";
    static final String MICRO_GRID_T4_SSH = "MicroGridTestConfiguration_T4_BE_SSH_V2.xml";
    static final String MICRO_GRID_T4_SV = "MicroGridTestConfiguration_T4_BE_SV_V2.xml";

    static final String MICRO_GRID_ASSEMBLED_SV = "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml";

    static final String MICRO_GRID_BD_EQ = "MicroGridTestConfiguration_EQ_BD.xml";
    static final String MICRO_GRID_BD_TP = "MicroGridTestConfiguration_TP_BD.xml";

    static final String MINI_GRID_EQ = "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml";
    static final String MINI_GRID_TP = "MiniGridTestConfiguration_BC_TP_v3.0.0.xml";
    static final String MINI_GRID_SSH = "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml";
    static final String MINI_GRID_SV = "MiniGridTestConfiguration_BC_SV_v3.0.0.xml";
    static final String MINI_GRID_DL = "MiniGridTestConfiguration_BC_DL_v3.0.0.xml";

    static final String MINI_GRID_BD_EQ = "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml";
    static final String MINI_GRID_BD_TP = "MiniGridTestConfiguration_TP_BD_v3.0.0.xml";

    static final String SMALL_GRID_EQ = "SmallGridTestConfiguration_BC_EQ_v3.0.0.xml";
    static final String SMALL_GRID_TP = "SmallGridTestConfiguration_BC_TP_v3.0.0.xml";
    static final String SMALL_GRID_SSH = "SmallGridTestConfiguration_BC_SSH_v3.0.0.xml";
    static final String SMALL_GRID_SV = "SmallGridTestConfiguration_BC_SV_v3.0.0.xml";

    static final String SMALL_GRID_HVDC_EQ = "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml";
    static final String SMALL_GRID_HVDC_TP = "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml";
    static final String SMALL_GRID_HVDC_SSH = "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml";
    static final String SMALL_GRID_HVDC_SV = "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml";
    static final String SMALL_GRID_HVDC_DL = "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml";
    static final String SMALL_GRID_HVDC_GL = "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml";

    static final String SMALL_GRID_BD_EQ = "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml";
    static final String SMALL_GRID_BD_TP = "SmallGridTestConfiguration_TP_BD_v3.0.0.xml";

    static final String CGMES_3_SMALL_GRID_EQ = "20210112T1742Z_1D_GB_EQ_001.xml";

    static final String CGMES_3_MICRO_GRID_ASSEMBLED_DL = "20210209T1930Z_1D_ASSEMBLED_DL_9.xml";
    static final String CGMES_3_MICRO_GRID_ASSEMBLED_TP = "20210209T2323Z_1D_ASSEMBLED_TP_9.xml";
    static final String CGMES_3_MICRO_GRID_ASSEMBLED_SV = "20210209T1930Z_1D_ASSEMBLED_SV_9.xml";
    static final String CGMES_3_MICRO_GRID_BE_EQ = "20210209T1930Z_1D_BE_EQ_9.xml";
    static final String CGMES_3_MICRO_GRID_BE_GL = "20210209T1930Z_1D_BE_GL_9.xml";
    static final String CGMES_3_MICRO_GRID_BE_SSH = "20210209T1930Z_1D_BE_SSH_9.xml";
    static final String CGMES_3_MICRO_GRID_NL_EQ = "20210209T1930Z_1D_NL_EQ_9.xml";
    static final String CGMES_3_MICRO_GRID_NL_GL = "20210209T1930Z_1D_NL_GL_9.xml";
    static final String CGMES_3_MICRO_GRID_NL_SSH = "20210209T1930Z_1D_NL_SSH_9.xml";
    static final String CGMES_3_MICRO_GRID_EQ_BD = "20171002T0930Z_ENTSO-E_EQ_BD_2.xml";

    private CgmesCatalogsConstants() {
    }
}
