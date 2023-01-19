/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesConformity3Catalog {

    private CgmesConformity3Catalog() {
    }

    public static ResourceSet microGridBaseCaseBoundaries() {
        return new ResourceSet(MICRO_GRID_3_BD_BASE, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml");
    }

    public static GridModelReferenceResources microGridBaseCaseBE() {
        return new GridModelReferenceResources(
                "MicroGrid-3-BaseCase-BE",
                null,
                new ResourceSet(MICRO_GRID_3_BE_BASE,
                        "20210325T1530Z_1D_BE_DL_001.xml",
                        "20210325T1530Z_1D_BE_EQ_001.xml",
                        "20210325T1530Z_1D_BE_GL_001.xml",
                        "20210325T1530Z_1D_BE_SSH_001.xml",
                        "20210325T1530Z_1D_BE_SV_001.xml",
                        "20210325T1530Z_1D_BE_TP_001.xml",
                        "20210420T1730Z_1D_BE_DY_001.xml"
                ),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseNL() {
        return new GridModelReferenceResources(
                "MicroGrid-3-BaseCase-NL",
                null,
                new ResourceSet(MICRO_GRID_3_NL_BASE,
                        "20210325T1530Z_1D_NL_DL_001.xml",
                        "20210325T1530Z_1D_NL_EQ_001.xml",
                        "20210325T1530Z_1D_NL_GL_001.xml",
                        "20210325T1530Z_1D_NL_SSH_001.xml",
                        "20210325T1530Z_1D_NL_SV_001.xml",
                        "20210325T1530Z_1D_NL_TP_001.xml",
                        "20210420T1730Z_1D_NL_DY_001.xml"
                ),
                microGridBaseCaseBoundaries());
    }
}
