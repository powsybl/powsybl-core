/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

import static com.powsybl.cgmes.conformity.CgmesCatalogsConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class Cgmes3Catalog {

    private Cgmes3Catalog() {
    }

    public static GridModelReferenceResources microGrid() {
        return new GridModelReferenceResources(
                "MicroGrid",
                null,
                new ResourceSet(CGMES_3_MICRO_GRID_BASE,
                    "20210209T1930Z_1D_ASSEMBLED_DL_9.xml",
                    "20210209T1930Z_1D_ASSEMBLED_SV_9.xml",
                    "20210209T1930Z_1D_BE_EQ_9.xml",
                    "20210209T1930Z_1D_BE_GL_9.xml",
                    "20210209T1930Z_1D_BE_SSH_9.xml",
                    "20210209T1930Z_1D_NL_EQ_9.xml",
                    "20210209T1930Z_1D_NL_GL_9.xml",
                    "20210209T1930Z_1D_NL_SSH_9.xml",
                    "20210209T2323Z_1D_ASSEMBLED_TP_9.xml"),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridWithoutTpSv() {
        return new GridModelReferenceResources(
                "MicroGrid",
                null,
                new ResourceSet(CGMES_3_MICRO_GRID_BASE,
                    "20210209T1930Z_1D_ASSEMBLED_DL_9.xml",
                    "20210209T1930Z_1D_BE_EQ_9.xml",
                    "20210209T1930Z_1D_BE_GL_9.xml",
                    "20210209T1930Z_1D_BE_SSH_9.xml",
                    "20210209T1930Z_1D_NL_EQ_9.xml",
                    "20210209T1930Z_1D_NL_GL_9.xml",
                    "20210209T1930Z_1D_NL_SSH_9.xml"),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources miniGrid() {
        return new GridModelReferenceResources(
                "MiniGrid",
                null,
                new ResourceSet(CGMES_3_MINI_GRID_BASE,
                    "20210202T1930Z_1D_ASSEMBLED_DL_7.xml",
                    "20210202T1930Z_1D_ASSEMBLED_SV_7.xml",
                    "20210202T1930Z_1D_AA_EQ_7.xml",
                    "20210202T1930Z_1D_AA_SSH_7.xml",
                    "20210202T1930Z_1D_AA_TP_7.xml"),
                new ResourceSet(CGMES_3_MINI_GRID_BASE, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniGridWithoutTpSv() {
        return new GridModelReferenceResources(
                "MiniGrid",
                null,
                new ResourceSet(CGMES_3_MINI_GRID_BASE,
                    "20210202T1930Z_1D_ASSEMBLED_DL_7.xml",
                    "20210202T1930Z_1D_AA_EQ_7.xml",
                    "20210202T1930Z_1D_AA_SSH_7.xml"),
                new ResourceSet(CGMES_3_MINI_GRID_BASE, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallGrid() {
        return new GridModelReferenceResources(
                "SmallGrid",
                null,
                new ResourceSet(CGMES_3_SMALL_GRID_BASE,
                    "20210112T1742Z_1D_GB_DL_001.xml",
                    "20210112T1742Z_1D_GB_SV_001.xml",
                    CGMES_3_SMALL_GRID_EQ,
                    "20210112T1742Z_1D_GB_GL_001.xml",
                    "20210112T1742Z_1D_GB_SSH_001.xml",
                    "20210112T1742Z_1D_GB_TP_001.xml"),
                new ResourceSet(CGMES_3_SMALL_GRID_BASE, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallGridWithoutTpSv() {
        return new GridModelReferenceResources(
                "SmallGrid",
                null,
                new ResourceSet(CGMES_3_SMALL_GRID_BASE,
                    "20210112T1742Z_1D_GB_DL_001.xml",
                    CGMES_3_SMALL_GRID_EQ,
                    "20210112T1742Z_1D_GB_GL_001.xml",
                    "20210112T1742Z_1D_GB_SSH_001.xml"),
                new ResourceSet(CGMES_3_SMALL_GRID_BASE, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources svedala() {
        return new GridModelReferenceResources(
                "Svedala",
                null,
                new ResourceSet(CGMES_3_SVEDALA_BASE,
                    "20201202T1843Z_1D_Svedala Area_DL_001.xml",
                    "20201202T1843Z_1D_Svedala Area_SV_001.xml",
                    "20201202T1843Z_1D_Svedala Area_EQ_001.xml",
                    "20201202T1843Z_1D_Svedala Area_SSH_001.xml",
                    "20201202T1843Z_1D_Svedala Area_TP_001.xml"));
    }

    public static GridModelReferenceResources svedalaWithoutTpSv() {
        return new GridModelReferenceResources(
                "Svedala",
                null,
                new ResourceSet(CGMES_3_SVEDALA_BASE,
                    "20201202T1843Z_1D_Svedala Area_DL_001.xml",
                    "20201202T1843Z_1D_Svedala Area_EQ_001.xml",
                    "20201202T1843Z_1D_Svedala Area_SSH_001.xml"));
    }

    public static ResourceSet microGridBaseCaseBoundaries() {
        return new ResourceSet(CGMES_3_MICRO_GRID_BASE, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml");
    }
}
