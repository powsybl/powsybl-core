/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

import static com.powsybl.cgmes.conformity.CgmesCatalogsConstants.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class Cgmes3ModifiedCatalog {

    private Cgmes3ModifiedCatalog() {
    }

    public static GridModelReferenceResources smallGridBaseCaseTieFlowMappedToSwitch() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
            + "/SmallGrid/tieFlowMappedToSwitch/";
        return new GridModelReferenceResources(
            "SmallGrid-TieFlow-Mapped-To-Switch",
            null,
            new ResourceSet(base,
                CGMES_3_SMALL_GRID_EQ),
            new ResourceSet(CGMES_3_SMALL_GRID_BASE,
                "20210112T1742Z_1D_GB_DL_001.xml",
                "20210112T1742Z_1D_GB_SV_001.xml",
                CGMES_3_SMALL_GRID_EQ,
                "20210112T1742Z_1D_GB_GL_001.xml",
                "20210112T1742Z_1D_GB_SSH_001.xml",
                "20210112T1742Z_1D_GB_TP_001.xml"),
            new ResourceSet(CGMES_3_SMALL_GRID_BASE, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallGridBaseCaseTieFlowMappedToEquivalentInjection() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
            + "/SmallGrid/tieFlowMappedToEquivalentInjection/";
        return new GridModelReferenceResources(
            "SmallGrid-TieFlow-Mapped-To-EquivalentInjection",
            null,
            new ResourceSet(base,
                CGMES_3_SMALL_GRID_EQ),
            new ResourceSet(CGMES_3_SMALL_GRID_BASE,
                "20210112T1742Z_1D_GB_DL_001.xml",
                "20210112T1742Z_1D_GB_SV_001.xml",
                CGMES_3_SMALL_GRID_EQ,
                "20210112T1742Z_1D_GB_GL_001.xml",
                "20210112T1742Z_1D_GB_SSH_001.xml",
                "20210112T1742Z_1D_GB_TP_001.xml"),
            new ResourceSet(CGMES_3_SMALL_GRID_BASE, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseRegulatingTerminalsDefinedOnSwitches() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
            + "/MicroGrid/regulatingTerminalsDefinedOnSwitches/";
        return new GridModelReferenceResources(
            "MicroGrid-regulating-terminals-defined-on-switches",
            null,
            new ResourceSet(base,
                "20210209T1930Z_1D_BE_EQ_9.xml"),
            new ResourceSet(CGMES_3_MICRO_GRID_BASE,
                "20210209T1930Z_1D_ASSEMBLED_DL_9.xml",
                "20210209T1930Z_1D_ASSEMBLED_SV_9.xml",
                "20210209T1930Z_1D_BE_GL_9.xml",
                "20210209T1930Z_1D_BE_SSH_9.xml",
                "20210209T1930Z_1D_NL_EQ_9.xml",
                "20210209T1930Z_1D_NL_GL_9.xml",
                "20210209T1930Z_1D_NL_SSH_9.xml",
                "20210209T2323Z_1D_ASSEMBLED_TP_9.xml"),
            new ResourceSet(CGMES_3_MICRO_GRID_BASE, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBESingleFile() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
                + "/MicroGrid/singleFile/";
        return new GridModelReferenceResources(
                "MicroGrid-single-file",
                null,
                new ResourceSet(base, "20210209T1930Z_1D_BE_9.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseGeographicalRegionInBoundary() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
                + "/MicroGrid/geographicalRegionInBoundary/";
        return new GridModelReferenceResources(
                "MicroGrid-geographical-region-in-boundary",
                null,
                new ResourceSet(base, "20210209T1930Z_1D_BE_EQ_9.xml"),
                new ResourceSet(base, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml"));
    }

    public static GridModelReferenceResources microGridBaseCasePhaseTapChangerXMin() {
        String base = CGMES_3_MODIFIED_TEST_MODELS
                + "/MicroGrid/phaseTapChangerXMin/";
        return new GridModelReferenceResources(
                "MicroGrid-phaseTapChanger-xMin",
                null,
                new ResourceSet(base, "20210209T1930Z_1D_BE_EQ_9.xml"),
                new ResourceSet(CGMES_3_MICRO_GRID_BASE,
                        "20210209T1930Z_1D_ASSEMBLED_DL_9.xml",
                        "20210209T1930Z_1D_ASSEMBLED_SV_9.xml",
                        "20210209T1930Z_1D_BE_GL_9.xml",
                        "20210209T1930Z_1D_BE_SSH_9.xml",
                        "20210209T1930Z_1D_NL_EQ_9.xml",
                        "20210209T1930Z_1D_NL_GL_9.xml",
                        "20210209T1930Z_1D_NL_SSH_9.xml",
                        "20210209T2323Z_1D_ASSEMBLED_TP_9.xml"),
                new ResourceSet(CGMES_3_MICRO_GRID_BASE, "20171002T0930Z_ENTSO-E_EQ_BD_2.xml"));
    }

    private static final String CGMES_3_MODIFIED_TEST_MODELS = "/cgmes3-test-models-modified";
}
