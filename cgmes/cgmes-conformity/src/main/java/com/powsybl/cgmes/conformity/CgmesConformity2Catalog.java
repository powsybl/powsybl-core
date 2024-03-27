/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesConformity2Catalog {

    private CgmesConformity2Catalog() {
    }

    public static GridModelReferenceResources microGridType2Assembled() {
        String base = ENTSOE_CONFORMITY_2 + "/MicroGrid/Type2_T2/CGMES_v2.4.15_MicroGridTestConfiguration_T2_Assembled_Complete_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-Type2-Assembled",
                null,
                new ResourceSet(base, "20171002T0930Z_1D_BE_SSH_4.xml",
                        "20171002T0930Z_1D_BE_TP_4.xml",
                        "20171002T0930Z_1D_ENTSO-E_SV_4.xml",
                        "20171002T0930Z_1D_HVDC_SSH_1.xml",
                        "20171002T0930Z_1D_HVDC_TP_1.xml",
                        "20171002T0930Z_1D_NL_SSH_4.xml",
                        "20171002T0930Z_1D_NL_TP_4.xml",
                        "20171002T0930Z_BE_DY_4.xml",
                        "20171002T0930Z_BE_EQ_4.xml",
                        "20171002T0930Z_BE_GL_4.xml",
                        "20171002T0930Z_ENTSO-E_DL_4.xml",
                        "20171002T0930Z_ENTSO-E_EQ_BD_2.xml",
                        "20171002T0930Z_ENTSO-E_TP_BD_2.xml",
                        "20171002T0930Z_HVDC_DY_1.xml",
                        "20171002T0930Z_HVDC_EQ_1.xml",
                        "20171002T0930Z_HVDC_GL_1.xml",
                        "20171002T0930Z_NL_DY_4.xml",
                        "20171002T0930Z_NL_EQ_4.xml",
                        "20171002T0930Z_NL_GL_4.xml"));
    }

    private static final String ENTSOE_CONFORMITY_2 = "/conformity/cas-2";
}
