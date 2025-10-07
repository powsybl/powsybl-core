/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
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
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public final class ReliCapGridCatalog {

    private static final String EQUIPMENT_BOUNDARY = "CommonData_and_Boundary_merged.xml";

    private ReliCapGridCatalog() {
    }

    public static GridModelReferenceResources belgovia() {
        return new GridModelReferenceResources(
                "Belgovia",
                null,
                new ResourceSet(RELI_CAP_GRID_BELGOVIA,
                        "20220615T2230Z__Belgovia_EQ_1.xml",
                        "20220615T2230Z_2D_Belgovia_SSH_1.xml",
                        "20220615T2230Z_2D_Belgovia_TP_1.xml",
                        "20220615T2230Z_2D_Belgovia_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources britheim() {
        return new GridModelReferenceResources(
                "Britheim",
                null,
                new ResourceSet(RELI_CAP_GRID_BRITHEIM,
                        "20220615T2230Z__Britheim_EQ_1.xml",
                        "20220615T2230Z_2D_Britheim_SSH_1.xml",
                        "20220615T2230Z_2D_Britheim_TP_1.xml",
                        "20220615T2230Z_2D_Britheim_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources espheim() {
        return new GridModelReferenceResources(
                "Espheim",
                null,
                new ResourceSet(RELI_CAP_GRID_ESPHEIM,
                        "20220615T2230Z__Espheim_EQ_1.xml",
                        "20220615T2230Z_2D_Espheim_SSH_1.xml",
                        "20220615T2230Z_2D_Espheim_TP_1.xml",
                        "20220615T2230Z_2D_Espheim_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources galia() {
        return new GridModelReferenceResources(
                "Galia",
                null,
                new ResourceSet(RELI_CAP_GRID_GALIA,
                        "20220615T2230Z__Galia_EQ_1.xml",
                        "20220615T2230Z_2D_Galia_SSH_1.xml",
                        "20220615T2230Z_2D_Galia_TP_1.xml",
                        "20220615T2230Z_2D_Galia_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources nordheim() {
        return new GridModelReferenceResources(
                "Nordheim",
                null,
                new ResourceSet(RELI_CAP_GRID_NORDHEIM,
                        "20220615T2230Z__Nordheim_EQ_1.xml",
                        "20220615T2230Z_2D_Nordheim_SSH_1.xml",
                        "20220615T2230Z_2D_Nordheim_TP_1.xml",
                        "20220615T2230Z_2D_Nordheim_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources svedala() {
        return new GridModelReferenceResources(
                "Svedala",
                null,
                new ResourceSet(RELI_CAP_GRID_SVEDALA,
                        "20220615T2230Z__Svedala_EQ_1.xml",
                        "20220615T2230Z_2D_Svedala_SSH_1.xml",
                        "20220615T2230Z_2D_Svedala_TP_1.xml",
                        "20220615T2230Z_2D_Svedala_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources hvdcEspheimSvedala() {
        return new GridModelReferenceResources(
                "HVDC-Espheim-Svedala",
                null,
                new ResourceSet(RELI_CAP_GRID_HVDC_ESPHEIM_SVEDALA,
                        "20220615T2230Z__HVDC-Espheim-Svedala_EQ_1.xml",
                        "20220615T2230Z_2D_HVDC-Espheim-Svedala_SSH_1.xml",
                        "20220615T2230Z_2D_HVDC-Espheim-Svedala_TP_1.xml",
                        "20220615T2230Z_2D_HVDC-Espheim-Svedala_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources hvdcNordheimGalia() {
        return new GridModelReferenceResources(
                "HVDC-Nordheim-Galia",
                null,
                new ResourceSet(RELI_CAP_GRID_HVDC_NORDHEIM_GALIA,
                        "20220615T2230Z__HVDC-Nordheim-Galia_EQ_1.xml",
                        "20220615T2230Z_2D_HVDC-Nordheim-Galia_SSH_1.xml",
                        "20220615T2230Z_2D_HVDC-Nordheim-Galia_TP_1.xml",
                        "20220615T2230Z_2D_HVDC-Nordheim-Galia_SV_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

    public static GridModelReferenceResources nineRealms() {
        return new GridModelReferenceResources(
                "Nine realms",
                null,
                new ResourceSet(RELI_CAP_GRID_BELGOVIA,
                        "20220615T2230Z__Belgovia_EQ_1.xml",
                        "20220615T2230Z_2D_Belgovia_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_BRITHEIM,
                        "20220615T2230Z__Britheim_EQ_1.xml",
                        "20220615T2230Z_2D_Britheim_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_ESPHEIM,
                        "20220615T2230Z__Espheim_EQ_1.xml",
                        "20220615T2230Z_2D_Espheim_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_GALIA,
                        "20220615T2230Z__Galia_EQ_1.xml",
                        "20220615T2230Z_2D_Galia_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_NORDHEIM,
                        "20220615T2230Z__Nordheim_EQ_1.xml",
                        "20220615T2230Z_2D_Nordheim_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_SVEDALA,
                        "20220615T2230Z__Svedala_EQ_1.xml",
                        "20220615T2230Z_2D_Svedala_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_HVDC_ESPHEIM_SVEDALA,
                        "20220615T2230Z__HVDC-Espheim-Svedala_EQ_1.xml",
                        "20220615T2230Z_2D_HVDC-Espheim-Svedala_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_HVDC_NORDHEIM_GALIA,
                        "20220615T2230Z__HVDC-Nordheim-Galia_EQ_1.xml",
                        "20220615T2230Z_2D_HVDC-Nordheim-Galia_SSH_1.xml"),
                new ResourceSet(RELI_CAP_GRID_COMMON,
                        EQUIPMENT_BOUNDARY)
        );
    }

}
