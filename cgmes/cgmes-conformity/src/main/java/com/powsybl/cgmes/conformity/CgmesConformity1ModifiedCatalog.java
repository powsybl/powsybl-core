/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class CgmesConformity1ModifiedCatalog {

    private CgmesConformity1ModifiedCatalog() {
    }

    public static GridModelReferenceResources microGridBaseCaseBEUnmergedXnode() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_unmerged_xnode/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-unmergedXnode",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEExplicitBase() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_explicitBase/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-explicitBase",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBERatioPhaseTapChangerTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_tabular/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Tabular",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBERatioPhaseTapChangerFaultyTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_faulty_tabular/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Faulty_Tabular",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BePhaseTapChangerLinear() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_PhaseTapChangerLinear/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Invalid-SVC-mode",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEPtcSide2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_side_2/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Tabular",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBERtcPtcDisabled() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_disabled_in_ssh_data/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Tabular",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEReactiveCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curves/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
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

    public static GridModelReferenceResources microGridBaseCaseBEReactiveCapabilityCurveOnePoint() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curve_1_point/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
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

    public static GridModelReferenceResources microGridBaseCaseBEPtcCurrentLimiter() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_current_limiter/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Ptc-Current-Limiter",
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

    public static GridModelReferenceResources microGridBaseCaseBEInvalidRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Invalid-Regulation-Control",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Missing-Regulation-Control",
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

    public static GridModelReferenceResources microGridBaseCaseBEWithSvInjection() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_sv_injection/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Sv-Injection",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlow() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow_mapped_to_equivalent_injection/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow-Mapped-To-Equivalent-Injection",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlowMappedToSwitch() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow_mapped_to_switch/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow-Mapped-To-Switch",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEInvalidSvInjection() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_sv_injection/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Invalid-Sv-Injection",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentShunt() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_equivalent_shunt/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Equivalent-Shunt",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingShuntRegulatingControlId() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_shunt_regulating_control_id/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Missing-Shunt-Regulating-Control-ID",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEUndefinedPatl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_undefined_PATL/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-UndefinedPATL",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseBEEquivalentInjectionRegulatingVoltage() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_equivalent_injection_regulating_voltage/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Equivalent-Injection-Regulating-Control",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEConformNonConformLoads() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_conform_non_conform_loads/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Conform-Non-Conform-Loads",

                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEHiddenTapChangers() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_hidden_tc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-HiddenTapChangers",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseBESharedRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_shared_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-SharedRegulatingControl",
            null,
            new ResourceSet(baseModified,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBESwitchAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_switch_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-SwitchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBETransformerAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_transformer_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-TransformerAtBoundary",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranchAtBoundary",
            null,
            new ResourceSet(baseModified,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranch() {
        String base = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch",
            null,
            new ResourceSet(baseModified,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchWithDifferentNominals() {
        String base = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_with_different_nominals/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch-with-different-nominals",
            null,
            new ResourceSet(baseModified,
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchWithZeroImpedanceInsideVoltageLevel() {
        String base = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_with_zero_impedance_inside_voltage_level/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch-with-zero-impedance-inside-voltage_level",
            null,
            new ResourceSet(baseModified,
                "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
            new ResourceSet(base,
                "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
            new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBELimits() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_limits/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Limits",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseBEFixedMinPMaxP() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_fixed_minP_maxP/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-fixed-minP-maxP",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEIncorrectDate() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_incorrect_date_and_version/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-incorrect-date",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingLimitValue() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_limit_value/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-missing-limit-value",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBEReactivePowerGen() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_reactive_power_gen/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-reactive-power-gen",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseBERegulatingTerminalsDefinedOnSwitches() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_regulatingTerminalsDefinedOnSwitches/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-regulating-terminals-defined-on-switches",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseMeasurements() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_measurements/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-measurements",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledThreeLinesAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_three_lines_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-three-lines-at-boundary",
                null,
                new ResourceSet(baseModified,
                    "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                    "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                    "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(base,
                    "MicroGridTestConfiguration_BC_Assembled_DL_V2.xml",
                    "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml",
                    "MicroGridTestConfiguration_BC_BE_DY_V2.xml",
                    "MicroGridTestConfiguration_BC_BE_GL_V2.xml",
                    "MicroGridTestConfiguration_BC_NL_DY_V2.xml",
                    "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                    "MicroGridTestConfiguration_BC_NL_GL_V2.xml",
                    "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                    "MicroGridTestConfiguration_BC_NL_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledSwitchAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_switch_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-SwitchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledTransformerAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_transformer_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-TransformerAtBoundary",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledEquivalentBranchAtBoundary() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_at_boundary/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-EquivalentBranchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_Assembled_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbInvalidSvcMode() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_invalid_svc_mode/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Invalid-SVC-mode",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbReactivePowerSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_reactive_power_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Reactive-Power-SVC",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbOffSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Off-SVC",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbOffSvcControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Off-SVC",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbSvcNoRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_svc_no_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-SVC_Without_Regulating_Control",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources microT4BeBbMissingRegControlReactivePowerSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_missing_reg_control_reactive_power_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Reactive_Power_SVC_With_Missing_Regulating_Control",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_T4_BE_EQ_V2.xml"),
                new ResourceSet(base,
                        "MicroGridTestConfiguration_T4_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_T4_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources miniBusBranchRtcRemoteRegulation() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_rtc_with_remote_regulation/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-LimistForEquipment",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniBusBranchT3xTwoRegulatingControlsEnabled() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T3x_two_regulatingControls_enabled/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-TwoRegulatingControlsEnabled",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniBusBranchPhaseAngleClockZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_phaseAngleClockZero/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
            "MiniGrid-BusBranch-PhaseAngleClockZero",
            null,
            new ResourceSet(base,
                "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
            new ResourceSet(baseOriginal,
                "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_TP_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniBusBranchT2xPhaseAngleClock1NonZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T2xPhaseAngleClock1NonZero/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
            "MiniGrid-BusBranch-T2xPhaseAngleClock1NonZero",
            null,
            new ResourceSet(base,
                "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
            new ResourceSet(baseOriginal,
                "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_TP_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniBusBranchT3xAllPhaseAngleClockNonZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T3xAllPhaseAngleClockNonZero/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
            "MiniGrid-BusBranch-T3xAllPhaseAngleClockNonZero",
            null,
            new ResourceSet(base,
                "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
            new ResourceSet(baseOriginal,
                "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_TP_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"));
    }

    public static GridModelReference miniBusBranchExternalInjectionControl() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/MiniGrid/BusBranch/BaseCase_v3_external_injection_control/";
        String baseOriginal = ENTSOE_CONFORMITY_1
            + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new GridModelReferenceResources(
            "MiniGrid-BusBranch-ExternalInjectionControl",
            null,
            new ResourceSet(base,
                "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml"
                ),
            new ResourceSet(baseOriginal,
                "MiniGridTestConfiguration_BC_TP_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerMeasurements() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_measurements/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Measurements",
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

    public static GridModelReferenceResources miniNodeBreakerLimitsforEquipment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_limits/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
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

    public static GridModelReferenceResources miniNodeBreakerInvalidT2w() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_invalid_t2w/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
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

    public static GridModelReferenceResources miniNodeBreakerSvInjection() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_sv_injection/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Sv-Injection",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerLoadBreakSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_load_break_switch/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Sv-Injection",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerCimLine() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_cim_line/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Cim-Line",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerProtectedSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_protected_switch/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Sv-Injection",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerSwitchBetweenVoltageLevelsOpen() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_switchVLsOpen/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-switchBetweenVoltageLevelsOpen",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerJoinVoltageLevelSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_joinVoltageLevelSwitch/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-joinVoltageLevel-switch",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources miniNodeBreakerJoinVoltageLevelTx() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_joinVoltageLevelTx/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-joinVoltageLevel-tx",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReference miniNodeBreakerInternalLineZ0() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_internal_line_z0/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-InternalLineZ0",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReference miniNodeBreakerSubstationNode() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_substation_node/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Substation-Node",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "MiniGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReference miniNodeBreakerMissingSubstationRegion() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_missing_substation_region/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-MissingSubstationRegion",
                null,
                new ResourceSet(base,
                        "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseOriginal,
                        "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallBusBranchTieFlowsWithoutControlArea() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/TieFlow_missing_controlArea";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-BusBranch-TieFlow-missing-ca",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_BC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_BC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_BC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_BC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_BC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_for_1inverter_2rectifier";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1inverter_2rectifier";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1rectifier_2inverter";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcVscReactiveQPcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_vsc_reactive_qPccControl";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-VSC-Reactive",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcNanTargetPpcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_NaN_targetPpcc";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-NaN-targetPpcc",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcMissingDCLineSegment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_missing_DCLineSegment";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-missing-DCLineSegment",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcMissingAcDcConverters() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_missing_acDcConverters";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-missing-acDcConverters",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_two_DCLineSegments";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-DCLineSegments",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoDcLineSegmentsOneTransformer() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_two_DCLineSegments_one_transformer";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-DCLineSegments-one-transformer",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersOneDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_oneDcLineSegment";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-one-DCLineSegment",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithTransformers() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_with_transformers";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-transformers",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithTwoTransformers() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_with_two_transformers";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-two-transformers",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithDifferentConverterTypes() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_withDifferentConverterTypes";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-different-converter-types",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithVsCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_withVsCapabilityCurve";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-VsCapabilityCurve",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerVscConverterRemotePccTerminal() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/VscConverter_remote_PccTerminal";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-SvcConverter-remote-PccTerminal",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_twoDcLineSegments";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-two-DCLineSegments",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegmentsNoAcConnectionAtOneEnd() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_twoDcLineSegments_noAcConnectionAtOneEnd";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-two-DCLineSegments-no-Ac-connection-at-one-end",
                null,
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_DL_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_TP_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_SV_v3.0.0.xml",
                        "SmallGridTestConfiguration_HVDC_GL_v3.0.0.xml"),
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources smallBusBranchWithSvInjection() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
            + "/SmallGrid/WithSvInjection";
        String baseOriginal = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
            + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
            "SmallGrid-BusBranch-WithSvInjection",
            null,
            new ResourceSet(baseOriginal,
                "SmallGridTestConfiguration_BC_SSH_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_EQ_v3.0.0.xml",
                "SmallGridTestConfiguration_BC_TP_v3.0.0.xml"),
            new ResourceSet(base, "SmallGridTestConfiguration_BC_SV_v3.0.0.xml"),
            new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));
    }

    public static GridModelReferenceResources microGridBaseBEGenUnitWithTwoSyncMachines() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_gen_unit_with_two_sync_machines/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-GU-With-2-SMs",
                null,
                new ResourceSet(base, "MicroGridTestConfiguration_BC_BE_EQ_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseAssembledEntsoeCategory() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_gu_description_entsoe_category/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_Assembled_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-Assembled-Entsoe-Category",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_EQ_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference miniNodeBreakerTerminalDisconnected() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_terminal_disconnected/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-terminal-disconnected",
                null,
                new ResourceSet(base, "MiniGridTestConfiguration_BC_SSH_v3.0.0.xml"),
                new ResourceSet(baseOriginal, "MiniGridTestConfiguration_BC_EQ_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "MiniGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "MiniGridTestConfiguration_TP_BD_v3.0.0.xml"));

    }

    public static GridModelReference microGridBaseCaseAssembledBadIds() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_bad_ids/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-bad-ids",
                null,
                new ResourceSet(baseModified,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml"
                        ),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseNLMultipleSlacks() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_multiple_generators_with_reference_priority/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-multiple-slacks",
                null,
                new ResourceSet(base, "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseNLShuntCompensatorGP() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_shunt_compensator_g_p/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_NL_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-sc-g-p",
                null,
                new ResourceSet(base, "MicroGridTestConfiguration_BC_NL_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_SV_V2.xml"),
                new ResourceSet(baseOriginal, "MicroGridTestConfiguration_BC_NL_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseCaseBESingleFile() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_single_file/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-single-file",
                null,
                new ResourceSet(base, "MicroGridTestConfiguration_BC_BE_V2.xml"));
    }

    public static GridModelReference smallNodeBreakerHvdcNoSequenceNumbers() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_no_sequence_numbers/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-no-sequence-numbers",
                null,
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0-no-seq.xml"),
                new ResourceSet(baseOriginal, "SmallGridTestConfiguration_HVDC_SSH_v3.0.0.xml"),
                new ResourceSet(baseBoundary, "SmallGridTestConfiguration_EQ_BD_v3.0.0.xml",
                        "SmallGridTestConfiguration_TP_BD_v3.0.0.xml"));

    }

    public static GridModelReference microGridBaseBEStationSupply() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_station_supply/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-station-supply",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseOriginal, "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseBETargetDeadbandNegative() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_target_deadband_negative/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-target-deadband-negative",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReference microGridBaseBEInvalidVoltageBus() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_voltage_bus/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-invalid-voltage-bus",
                null,
                new ResourceSet(base,
                        "MicroGridTestConfiguration_BC_BE_SV_V2.xml"),
                new ResourceSet(baseOriginal,
                        "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_SSH_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_TP_V2.xml"),
                new ResourceSet(baseBoundary, "MicroGridTestConfiguration_EQ_BD.xml",
                        "MicroGridTestConfiguration_TP_BD.xml"));
    }

    public static GridModelReferenceResources smallGridBusBranchWithBusbarSectionsAndIpMax() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/BusBranch_busbarSections_ipMax";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_BaseCase_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/BusBranch/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new GridModelReferenceResources(
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
