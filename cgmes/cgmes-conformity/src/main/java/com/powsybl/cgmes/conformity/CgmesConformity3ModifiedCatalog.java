/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesConformity3ModifiedCatalog {

    private CgmesConformity3ModifiedCatalog() {
    }

    public static GridModelReference microGridBE3DanglingLinesSameBoundary1Disconnected() {
        String base = ENTSOE_CONFORMITY_3_MODIFIED
                + "/MicroGrid/BE-3dls-same-boundary-node-1disconnected/";
        return new GridModelReferenceResources(
                "MicroGrid-BE-3dls-same-boundary-node-1disconnected",
                null,
                new ResourceSet(base,
                        "20210325T1530Z_1D_BE_EQ_001_3dls_1disconnected.xml",
                        "20210325T1530Z_1D_BE_SSH_001_3dls_1disconnected.xml"
                ),
                CgmesConformity3Catalog.microGridBaseCaseBoundaries());
    }

    private static final String ENTSOE_CONFORMITY_3_MODIFIED = "/conformity-modified/cas-3-data-3.0.2";
}
