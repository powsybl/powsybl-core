/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conformity;

import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;

import static com.powsybl.cgmes.conformity.CgmesCatalogsConstants.*;
import static com.powsybl.cgmes.conformity.CgmesConformity1Catalog.microGridBaseCaseBoundaries;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class CgmesConformity1ModifiedCatalog {

    private static final String MICROGRID_REF_SWITCH = "/MicroGrid/BaseCase/BC_BE_v2_switch_at_boundary/";
    private static final String MICROGRID_REF_TRANSFORMER = "/MicroGrid/BaseCase/BC_BE_v2_transformer_at_boundary/";
    private static final String MICROGRID_REF_EQBRANCH = "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_at_boundary/";
    private static final String MICROGRID_CONFIGURATION = "/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_NL_v2/";

    private CgmesConformity1ModifiedCatalog() {
    }

    public static GridModelReferenceResources microGridBaseCaseDuplicateRegion() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_duplicate_region/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-duplicate-region",
                null,
                new ResourceSet(base, MICRO_GRID_BD_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                new ResourceSet(MICRO_GRID_BD_BASE,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microGridBaseCaseBEUnmergedXnode() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_unmerged_xnode/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-unmergedXnode",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                new ResourceSet(MICRO_GRID_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microGridBaseCaseBEExplicitBase() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_explicitBase/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-explicitBase",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBERatioPhaseTapChangerTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_tabular/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Tabular",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBERatioPhaseTapChangerFaultyTabular() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_faulty_tabular/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Faulty_Tabular",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microT4BePhaseTapChangerLinear() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_PhaseTapChangerLinear/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Invalid-SVC-mode",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ,
                        MICRO_GRID_T4_SSH),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microGridBaseCaseBEPtcSide2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_side_2/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Side-2",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBERtcPtcDisabled() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_rtc_ptc_disabled_in_ssh_data/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-RTC-PTC-Disabled",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEReactiveCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curves/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Q-Curves",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEReactiveCapabilityCurveOnePoint() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_q_curve_1_point/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Q-Curves-1-point",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEPtcCurrentLimiter() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_ptc_current_limiter/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Ptc-Current-Limiter",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEInvalidRegulatingControl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_regulating_control/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Invalid-Regulation-Control",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingRegulatingControl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_regulating_control/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Missing-Regulation-Control",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithSvInjection() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_sv_injection/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Sv-Injection",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlow() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow_mapped_to_equivalent_injection/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow-Mapped-To-Equivalent-Injection",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEWithTieFlowMappedToSwitch() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_with_tie_flow_mapped_to_switch/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-With-Tie-Flow-Mapped-To-Switch",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEInvalidSvInjection() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_sv_injection/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Invalid-Sv-Injection",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_SV),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentShunt() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_equivalent_shunt/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-Equivalent-Shunt",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingShuntRegulatingControlId() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_shunt_regulating_control_id/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Missing-Shunt-Regulating-Control-ID",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEUndefinedPatl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_undefined_PATL/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-UndefinedPATL",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseBEEquivalentInjectionRegulatingVoltage() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_equivalent_injection_regulating_voltage/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Equivalent-Injection-Regulating-Control",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEConformNonConformLoads() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_conform_non_conform_loads/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Conform-Non-Conform-Loads",

                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEHiddenTapChangers() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_hidden_tc/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-HiddenTapChangers",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseBESharedRegulatingControl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_shared_regulating_control/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-SharedRegulatingControl",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBESwitchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_SWITCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-SwitchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBETransformerAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_TRANSFORMER;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-TransformerAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_EQBRANCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranch() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchWithDifferentNominals() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_with_different_nominals/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch-with-different-nominals",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEEquivalentBranchWithZeroImpedanceInsideVoltageLevel() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_eqbranch_with_zero_impedance_inside_voltage_level/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranch-with-zero-impedance-inside-voltage_level",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBELimits() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_limits/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-Limits",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseBEFixedMinPMaxP() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_fixed_minP_maxP/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-fixed-minP-maxP",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEIncorrectDate() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_incorrect_date_and_version/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-incorrect-date",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEMissingLimitValue() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_missing_limit_value/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-missing-limit-value",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBEReactivePowerGen() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_reactive_power_gen/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-reactive-power-gen",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseBERegulatingTerminalsDefinedOnSwitches() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_regulatingTerminalsDefinedOnSwitches/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-regulating-terminals-defined-on-switches",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseMeasurements() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_measurements/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-measurements",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_SSH),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledThreeLinesAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_three_lines_at_boundary/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-three-lines-at-boundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE,
                        "MicroGridTestConfiguration_BC_Assembled_DL_V2.xml",
                        MICRO_GRID_ASSEMBLED_SV,
                        "MicroGridTestConfiguration_BC_BE_DY_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_GL_V2.xml",
                        "MicroGridTestConfiguration_BC_NL_DY_V2.xml",
                        MICRO_GRID_NL_EQ,
                        "MicroGridTestConfiguration_BC_NL_GL_V2.xml",
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledSwitchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_SWITCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-SwitchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_NL_EQ,
                        MICRO_GRID_NL_TP,
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_ASSEMBLED_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBESwitchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_SWITCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-SwitchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBETransformerAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_TRANSFORMER;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-TransformerAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBEEquivalentBranchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_EQBRANCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-BE-EquivalentBranchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledTransformerAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_TRANSFORMER;
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-TransformerAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_NL_EQ,
                        MICRO_GRID_NL_TP,
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_ASSEMBLED_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledEquivalentBranchAtBoundary() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + MICROGRID_REF_EQBRANCH;
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-EquivalentBranchAtBoundary",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_NL_EQ,
                        MICRO_GRID_NL_TP,
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_ASSEMBLED_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources microT4BeBbInvalidSvcMode() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_invalid_svc_mode/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Invalid-SVC-mode",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SSH,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbReactivePowerSvc() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_reactive_power_svc/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Reactive-Power-SVC",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SSH,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbOffSvc() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Off-SVC",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_SSH),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_EQ,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbOffSvcControl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc_control/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Off-SVC",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ,
                        MICRO_GRID_T4_SSH),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbOffSvcControlV() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_off_svc_control_v/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Off-SVC-V",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ,
                        MICRO_GRID_T4_SSH),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbSvcNoRegulatingControl() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_svc_no_regulating_control/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-SVC_Without_Regulating_Control",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SSH,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources microT4BeBbMissingRegControlReactivePowerSvc() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/Type4_T4/BE_BB_Complete_v2_missing_reg_control_reactive_power_svc/";
        return new GridModelReferenceResources(
                "MicroGrid-T4-Reactive_Power_SVC_With_Missing_Regulating_Control",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_T4_EQ),
                new ResourceSet(MICRO_GRID_T4_BASE,
                        MICRO_GRID_T4_SSH,
                        MICRO_GRID_T4_SV,
                        MICRO_GRID_T4_TP),
                new ResourceSet(MICRO_GRID_T4_BD_BASE, MICRO_GRID_BD_EQ,
                        MICRO_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniBusBranchRtcRemoteRegulation() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_rtc_with_remote_regulation/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-RtcRemoteRegulation",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_DL,
                        MINI_GRID_SV));
    }

    public static GridModelReferenceResources miniBusBranchT3xTwoRegulatingControlsEnabled() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T3x_two_regulatingControls_enabled/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-TwoRegulatingControlsEnabled",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ,
                        MINI_GRID_SSH),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_DL,
                        MINI_GRID_SV,
                        MINI_GRID_TP));
    }

    public static GridModelReferenceResources miniBusBranchPhaseAngleClockZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_phaseAngleClockZero/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-PhaseAngleClockZero",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_SSH,
                        MINI_GRID_TP,
                        MINI_GRID_DL,
                        MINI_GRID_SV));
    }

    public static GridModelReferenceResources miniBusBranchT2xPhaseAngleClock1NonZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T2xPhaseAngleClock1NonZero/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-T2xPhaseAngleClock1NonZero",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_SSH,
                        MINI_GRID_TP,
                        MINI_GRID_DL,
                        MINI_GRID_SV));
    }

    public static GridModelReferenceResources miniBusBranchT3xAllPhaseAngleClockNonZero() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_T3xAllPhaseAngleClockNonZero/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-T3xAllPhaseAngleClockNonZero",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_SSH,
                        MINI_GRID_TP,
                        MINI_GRID_DL,
                        MINI_GRID_SV));
    }

    public static GridModelReference miniBusBranchExternalInjectionControl() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/BusBranch/BaseCase_v3_external_injection_control/";
        return new GridModelReferenceResources(
                "MiniGrid-BusBranch-ExternalInjectionControl",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ,
                        MINI_GRID_SSH
                ),
                new ResourceSet(MINI_GRID_BUS_BRANCH_BASE,
                        MINI_GRID_TP,
                        MINI_GRID_DL,
                        MINI_GRID_SV));
    }

    public static GridModelReferenceResources miniNodeBreakerMeasurements() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_measurements/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Measurements",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_SV,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));

    }

    public static GridModelReferenceResources miniNodeBreakerLimitsforEquipment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_limits/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-LimistForEquipment",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_SV,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerInvalidT2w() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_invalid_t2w/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-InvalidT2w",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_SV,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerSvInjection() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_sv_injection/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Sv-Injection",
                null,
                new ResourceSet(base,
                        MINI_GRID_SV),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_EQ,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerLoadBreakSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_load_break_switch/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Load-Break-Switch",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerCimLine() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_cim_line/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Cim-Line",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerProtectedSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_protected_switch/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Protected-Switch",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerSwitchBetweenVoltageLevelsOpen() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_switchVLsOpen/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-switchBetweenVoltageLevelsOpen",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ,
                        MINI_GRID_SSH),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerJoinVoltageLevelSwitch() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_joinVoltageLevelSwitch/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-joinVoltageLevel-switch",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniNodeBreakerJoinVoltageLevelTx() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_joinVoltageLevelTx/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-joinVoltageLevel-tx",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReference miniNodeBreakerInternalLineZ0() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_internal_line_z0/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-InternalLineZ0",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_DL,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReference miniNodeBreakerSubstationNode() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_substation_node/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-Substation-Node",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_SSH,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReference miniNodeBreakerMissingSubstationRegion() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_missing_substation_region/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-MissingSubstationRegion",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SSH),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallBusBranchTieFlowsWithoutControlArea() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/TieFlow_missing_controlArea";
        return new GridModelReferenceResources(
                "SmallGrid-BusBranch-TieFlow-missing-ca",
                null,
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BASE, "SmallGridTestConfiguration_BC_DL_v3.0.0.xml",
                        SMALL_GRID_SSH,
                        "SmallGridTestConfiguration_BC_GL_v3.0.0.xml",
                        SMALL_GRID_SV,
                        SMALL_GRID_TP),
                new ResourceSet(base, SMALL_GRID_EQ),
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_for_1inverter_2rectifier";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2Inverter1Rectifier2",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(base, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1inverter_2rectifier";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2BothConvertersTargetPpcc1inverter2rectifier",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(base, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/HVDC_dcLine2_targetPpcc_both_1rectifier_2inverter";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-Line2BothConvertersTargetPpcc1rectifier2inverter",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(base, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcVscReactiveQPcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_vsc_reactive_qPccControl";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-VSC-Reactive",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(base, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcNanTargetPpcc() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_NaN_targetPpcc";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-NaN-targetPpcc",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(base, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcMissingDCLineSegment() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_missing_DCLineSegment";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-missing-DCLineSegment",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcMissingAcDcConverters() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_missing_acDcConverters";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-missing-acDcConverters",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_two_DCLineSegments";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-DCLineSegments",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoDcLineSegmentsOneTransformer() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_two_DCLineSegments_one_transformer";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-DCLineSegments-one-transformer",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_GL,
                        SMALL_GRID_HVDC_SV),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_TP),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersOneDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_oneDcLineSegment";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-one-DCLineSegment",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithTransformers() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_with_transformers";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-transformers",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithTwoTransformers() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_with_two_transformers";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-two-transformers",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithDifferentConverterTypes() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_withDifferentConverterTypes";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-different-converter-types",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcWithVsCapabilityCurve() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_withVsCapabilityCurve";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-with-VsCapabilityCurve",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerVscConverterRemotePccTerminal() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/VscConverter_remote_PccTerminal";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-SvcConverter-remote-PccTerminal",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegments() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_twoDcLineSegments";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-two-DCLineSegments",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegmentsNoAcConnectionAtOneEnd() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED + "/SmallGrid/HVDC_twoAcDcConverters_twoDcLineSegments_noAcConnectionAtOneEnd";
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-two-AcDcConverters-two-DCLineSegments-no-Ac-connection-at-one-end",
                null,
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_DL,
                        SMALL_GRID_HVDC_TP,
                        SMALL_GRID_HVDC_SSH,
                        SMALL_GRID_HVDC_SV,
                        SMALL_GRID_HVDC_GL),
                new ResourceSet(base, SMALL_GRID_HVDC_EQ),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources smallBusBranchWithSvInjection() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/WithSvInjection";
        return new GridModelReferenceResources(
                "SmallGrid-BusBranch-WithSvInjection",
                null,
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BASE,
                        SMALL_GRID_SSH,
                        SMALL_GRID_EQ,
                        SMALL_GRID_TP),
                new ResourceSet(base, SMALL_GRID_SV),
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReferenceResources microGridBaseBEGenUnitWithTwoSyncMachines() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_gen_unit_with_two_sync_machines/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-GU-With-2-SMs",
                null,
                new ResourceSet(base, MICRO_GRID_BE_EQ),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_SV,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseAssembledEntsoeCategory() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_gu_description_entsoe_category/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-Assembled-Entsoe-Category",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_NL_EQ),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference miniNodeBreakerTerminalDisconnected() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_terminal_disconnected/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-terminal-disconnected",
                null,
                new ResourceSet(base, MINI_GRID_SSH),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE, MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));

    }

    public static GridModelReference microGridBaseCaseAssembledBadIds() {
        String baseModified = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_bad_ids/";
        return new GridModelReferenceResources("MicroGrid-BaseCase-Assembled-bad-ids",
                null,
                new ResourceSet(baseModified,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP,
                        MICRO_GRID_NL_EQ,
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP
                ),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseNLMultipleReferencePriorities() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_multiple_generators_with_reference_priority/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-multiple-slacks",
                null,
                new ResourceSet(base, MICRO_GRID_NL_EQ,
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseNLShuntCompensatorGP() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_shunt_compensator_g_p/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + MICROGRID_CONFIGURATION;
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-sc-g-p",
                null,
                new ResourceSet(base, MICRO_GRID_NL_EQ,
                        "MicroGridTestConfiguration_BC_NL_SV_V2.xml"),
                new ResourceSet(baseOriginal, MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseNLSwitchWithoutName() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_switch_without_name/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + MICROGRID_CONFIGURATION;
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-switch-no-name",
                null,
                new ResourceSet(base, MICRO_GRID_NL_EQ),
                new ResourceSet(baseOriginal, MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseNLSwitchTypePreserved() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_NL_v2_switch_type_preserved/";
        String baseOriginal = ENTSOE_CONFORMITY_1
                + MICROGRID_CONFIGURATION;
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-NL-switch-type-preserved",
                null,
                new ResourceSet(base, MICRO_GRID_NL_EQ, MICRO_GRID_NL_SSH),
                new ResourceSet(baseOriginal, MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
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
        return new GridModelReferenceResources(
                "SmallGrid-NodeBreaker-HVDC-no-sequence-numbers",
                null,
                new ResourceSet(base, "SmallGridTestConfiguration_HVDC_EQ_v3.0.0-no-seq.xml"),
                new ResourceSet(SMALL_GRID_HVDC_BASE, SMALL_GRID_HVDC_SSH),
                new ResourceSet(SMALL_GRID_NODE_BREAKER_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));

    }

    public static GridModelReference microGridBaseBEStationSupply() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_station_supply/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-station-supply",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                new ResourceSet(MICRO_GRID_BE_BASE, MICRO_GRID_BE_SV),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseBETargetDeadbandNegative() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_target_deadband_negative/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-target-deadband-negative",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseBEInvalidVoltageBus() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_invalid_voltage_bus/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-invalid-voltage-bus",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_SV),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReference microGridBaseCaseBELineDisconnectedAtBoundaryNode() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_BE_v2_line_disconnected_at_boundary_node/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-BE-line-disconnected-at-boundary-node",
                null,
                new ResourceSet(base,
                        MICRO_GRID_BE_SSH),
                new ResourceSet(MICRO_GRID_BE_BASE,
                        MICRO_GRID_BE_EQ,
                        MICRO_GRID_BE_TP),
                microGridBaseCaseBoundaries());
    }

    public static GridModelReferenceResources smallGridBusBranchWithBusbarSectionsAndIpMax() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/SmallGrid/BusBranch_busbarSections_ipMax";
        return new GridModelReferenceResources(
                "SmallGrid-BusBranch-With-BusbarSecions-And-ipMax",
                null,
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BASE, "SmallGridTestConfiguration_BC_DL_v3.0.0.xml",
                        SMALL_GRID_SSH,
                        "SmallGridTestConfiguration_BC_GL_v3.0.0.xml",
                        SMALL_GRID_SV),
                new ResourceSet(base, SMALL_GRID_EQ,
                        SMALL_GRID_TP),
                new ResourceSet(SMALL_GRID_BUS_BRANCH_BD_BASE, SMALL_GRID_BD_EQ,
                        SMALL_GRID_BD_TP));
    }

    public static GridModelReference miniGridNodeBreakerMissingVoltageLevel() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_missing_voltage_level/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-BaseCase-Complete-v3-missing-voltage-levels",
                null,
                new ResourceSet(base,
                        MINI_GRID_EQ),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SSH,
                        MINI_GRID_SV,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources miniGridNodeBreakerSwitchTypePreserved() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MiniGrid/NodeBreaker/BaseCase_Complete_v3_switch_type_preserved/";
        return new GridModelReferenceResources(
                "MiniGrid-NodeBreaker-BaseCase-Complete-v3-switch-type-preserved",
                null,
                new ResourceSet(base, MINI_GRID_EQ, MINI_GRID_SSH),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BASE,
                        MINI_GRID_SV,
                        MINI_GRID_TP),
                new ResourceSet(MINI_GRID_NODE_BREAKER_BD_BASE, MINI_GRID_BD_EQ,
                        MINI_GRID_BD_TP));
    }

    public static GridModelReferenceResources microGridBaseCaseAssembledSvWithMas() {
        String base = ENTSOE_CONFORMITY_1_MODIFIED
                + "/MicroGrid/BaseCase/BC_Assembled_v2_sv_with_mas/";
        return new GridModelReferenceResources(
                "MicroGrid-BaseCase-Assembled-SvWithMas",
                null,
                new ResourceSet(base, MICRO_GRID_ASSEMBLED_SV),
                new ResourceSet(MICRO_GRID_ASSEMBLED_BASE, "MicroGridTestConfiguration_BC_Assembled_DL_V2.xml",
                        "MicroGridTestConfiguration_BC_BE_DY_V2.xml",
                        MICRO_GRID_BE_EQ,
                        "MicroGridTestConfiguration_BC_BE_GL_V2.xml",
                        MICRO_GRID_BE_SSH,
                        MICRO_GRID_BE_TP,
                        "MicroGridTestConfiguration_BC_NL_DY_V2.xml",
                        MICRO_GRID_NL_EQ,
                        "MicroGridTestConfiguration_BC_NL_GL_V2.xml",
                        MICRO_GRID_NL_SSH,
                        MICRO_GRID_NL_TP),
                microGridBaseCaseBoundaries());
    }

    private static final String ENTSOE_CONFORMITY_1_MODIFIED = "/conformity-modified/cas-1.1.3-data-4.0.3";

}
