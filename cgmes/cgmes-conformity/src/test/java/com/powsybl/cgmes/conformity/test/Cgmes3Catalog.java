/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
public final class Cgmes3Catalog {

    private Cgmes3Catalog() {
    }

    public static TestGridModelResources microGrid() {
        String base = CGMES_3_TEST_MODELS + "/MicroGrid/";
        String baseBoundary = base;
        return new TestGridModelResources(
                "MicroGrid",
                null,
                new ResourceSet(base, "" +
                    "20210209T1930Z_1D_ASSEMBLED_DL_9.xml",
                    "20210209T1930Z_1D_ASSEMBLED_SV_9.xml",
                    "20210209T1930Z_1D_BE_EQ_9.xml",
                    "20210209T1930Z_1D_BE_GL_9.xml",
                    "20210209T1930Z_1D_BE_SSH_9.xml",
                    "20210209T1930Z_1D_NL_EQ_9.xml",
                    "20210209T1930Z_1D_NL_GL_9.xml",
                    "20210209T1930Z_1D_NL_SSH_9.xml",
                    "20210209T2323Z_1D_ASSEMBLED_TP_9.xml"),
                new ResourceSet(baseBoundary, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml"));
    }

    public static TestGridModelResources miniGrid() {
        String base = CGMES_3_TEST_MODELS + "/MiniGrid/";
        String baseBoundary = base;
        return new TestGridModelResources(
                "MiniGrid",
                null,
                new ResourceSet(base, "" +
                    "20210202T1930Z_1D_ASSEMBLED_DL_7.xml",
                    "20210202T1930Z_1D_ASSEMBLED_SV_7.xml",
                    "20210202T1930Z_1D_AA_EQ_7.xml",
                    "20210202T1930Z_1D_AA_SSH_7.xml",
                    "20210202T1930Z_1D_AA_TP_7.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static TestGridModelResources smallGrid() {
        String base = CGMES_3_TEST_MODELS + "/SmallGrid/";
        String baseBoundary = base;
        return new TestGridModelResources(
                "SmallGrid",
                null,
                new ResourceSet(base, "" +
                    "20210112T1742Z_1D_GB_DL_001.xml",
                    "20210112T1742Z_1D_GB_SV_001.xml",
                    "20210112T1742Z_1D_GB_EQ_001.xml",
                    "20210112T1742Z_1D_GB_GL_001.xml",
                    "20210112T1742Z_1D_GB_SSH_001.xml",
                    "20210112T1742Z_1D_GB_TP_001.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static TestGridModelResources svedala() {
        String base = CGMES_3_TEST_MODELS + "/Svedala/";
        return new TestGridModelResources(
                "Svedala",
                null,
                new ResourceSet(base, "" +
                    "20201202T1843Z_1D_Svedala Area_DL_001.xml",
                    "20201202T1843Z_1D_Svedala Area_SV_001.xml",
                    "20201202T1843Z_1D_Svedala Area_EQ_001.xml",
                    "20201202T1843Z_1D_Svedala Area_SSH_001.xml",
                    "20201202T1843Z_1D_Svedala Area_TP_001.xml"));
    }

    private static final String CGMES_3_TEST_MODELS = "/cgmes3-test-models";
}
