/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
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
public final class CgmesConformity1ModifiedCatalog {

    private CgmesConformity1ModifiedCatalog() {
    }

    public static TestGridModelResources microGridBaseCaseBERatioPhaseTapChangerTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_tabular/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEPtcSide2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_side_2/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBERtcPtcEnabledBySsh() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_enabled_in_ssh_data/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEReactiveCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curves/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEReactiveCapabilityCurveOnePoint() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curve_1_point/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEPtcCurrentLimiter() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_current_limiter/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEInvalidRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEMissingRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEWithSvInjection() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_sv_injection/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEInvalidSvInjection() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_sv_injection/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEEquivalentShunt() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_equivalent_shunt/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microGridBaseCaseBEMissingShuntRegulatingControlId() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_shunt_regulating_control_id/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources("MicroGrid-BaseCase-BE-Missing-Shunt-Regulating-Control-ID",
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

    public static TestGridModelResources microT4BeBbInvalidSvcMode() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_invalid_svc_mode/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microT4BeBbReactivePowerSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_reactive_power_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microT4BeBbOffSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microT4BeBbOffSvcControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microT4BeBbSvcNoRegulatingControl() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_svc_no_regulating_control/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources microT4BeBbMissingRegControlReactivePowerSvc() {
        String base = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_T4_BE_BB_Complete_v2/";
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_missing_reg_control_reactive_power_svc/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MicroGrid/Type4_T4/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniBusBranchRtcRemoteRegulation() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_rtc_with_remote_regulation/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniBusBranchT3xTwoRegulatingControlsEnabled() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T3x_two_regulatingControls_enabled/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/BusBranch/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniNodeBreakerLimitsforEquipment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_limits/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniNodeBreakerInvalidT2w() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_invalid_t2w/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniNodeBreakerSvInjection() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_sv_injection/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniNodeBreakerLoadBreakSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_load_break_switch/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources miniNodeBreakerProtectedSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_protected_switch/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_BaseCase_Complete_v3/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/MiniGrid/NodeBreaker/CGMES_v2.4.15_MiniGridTestConfiguration_Boundary_v3/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_for_1inverter_2rectifier";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1inverter_2rectifier";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1rectifier_2inverter";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBreakerHvdcVscReactiveQPcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_vsc_reactive_qPccControl";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBrokerHvdcNanTargetPpcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_NaN_targetPpcc";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    public static TestGridModelResources smallNodeBrokerHvdcMissingDCLineSegment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_missing_DCLineSegment";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_HVDC_Complete_v3.0.0/";
        String baseBoundary = ENTSOE_CONFORMITY_1
                + "/SmallGrid/NodeBreaker/CGMES_v2.4.15_SmallGridTestConfiguration_Boundary_v3.0.0/";
        return new TestGridModelResources(
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

    private static final String ENTSOE_CONFORMITY_1 = "/conformity/cas-1.1.3-data-4.0.3";
    private static final String ENTSOE_CONFORMITY_1_MODIFIED = "/conformity-modified/cas-1.1.3-data-4.0.3";
}
