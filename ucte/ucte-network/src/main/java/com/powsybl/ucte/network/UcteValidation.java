/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public final class UcteValidation {

    private static final UcteLogger LOGGER = new UcteLogger();

    public static final double ZERO_EPS = 1e-4;
    public static final double REACTANCE_EPS = 0.05;
    public static final double DU_LIMIT = 6;
    public static final double THETA_ABS_LIMIT = 180;
    public static final double N_LIMIT = 35;
    public static final String LINE_ID_KEY = "lineId";

    private UcteValidation() {
    }

    // Data: line characteristics
    public static void checkValidLineCharacteristics(UcteLine line, ReportNode reportNode) {
        String lineId = line.getId().toString();
        switch (line.getStatus()) {
            case EQUIVALENT_ELEMENT_IN_OPERATION:
            case EQUIVALENT_ELEMENT_OUT_OF_OPERATION:
            case REAL_ELEMENT_IN_OPERATION:
            case REAL_ELEMENT_OUT_OF_OPERATION:
                if (line.getResistance() < ZERO_EPS) {
                    reportNode.newReportNode()
                        .withMessageTemplate("negativeLineResistance", "${lineId} - Real line resistance cannot be negative (${resistance} ohm)")
                        .withUntypedValue(LINE_ID_KEY, lineId)
                        .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                        .withSeverity(TypedValue.ERROR_SEVERITY)
                        .add();
                    LOGGER.error(lineId, "Real line resistance cannot be negative", line.getResistance() + " ohm");
                }
                if (Math.abs(line.getReactance()) < REACTANCE_EPS) {
                    reportNode.newReportNode()
                        .withMessageTemplate("epsilonLineReactance", "${lineId} - Real line reactance must be larger than 0.05 ohm (${reactance} ohm)")
                        .withUntypedValue(LINE_ID_KEY, lineId)
                        .withTypedValue("reactance", line.getReactance(), TypedValue.REACTANCE)
                        .withSeverity(TypedValue.WARN_SEVERITY)
                        .add();
                    LOGGER.warn(lineId, "Real line reactance must be larger than 0.05 ohm",
                            line.getReactance() + " ohm");
                }
                break;
            case BUSBAR_COUPLER_IN_OPERATION:
            case BUSBAR_COUPLER_OUT_OF_OPERATION:
                if (Math.abs(line.getResistance()) > ZERO_EPS) {
                    reportNode.newReportNode()
                        .withMessageTemplate("nonZeroBusbarCouplerResistance", "${lineId} - Busbar coupler resistance must be zero (${resistance} ohm)")
                        .withUntypedValue(LINE_ID_KEY, lineId)
                        .withTypedValue("resistance", line.getResistance(), TypedValue.RESISTANCE)
                        .withSeverity(TypedValue.WARN_SEVERITY)
                        .add();
                    LOGGER.warn(lineId, "Busbar coupler resistance must be zero", line.getResistance() + " ohm");
                }
                if (Math.abs(line.getReactance()) > ZERO_EPS) {
                    reportNode.newReportNode()
                        .withMessageTemplate("nonZeroBusbarCouplerReactance", "${lineId} - Busbar coupler reactance must be zero (${reactance} ohm)")
                        .withUntypedValue(LINE_ID_KEY, lineId)
                        .withTypedValue("reactance", line.getReactance(), TypedValue.REACTANCE)
                        .withSeverity(TypedValue.WARN_SEVERITY)
                        .add();
                    LOGGER.warn(lineId, "Busbar coupler reactance must be zero", line.getReactance() + " ohm");
                }
                if (Math.abs(line.getSusceptance()) > ZERO_EPS) {
                    reportNode.newReportNode()
                        .withMessageTemplate("nonZeroBusbarCouplerSusceptance", "${lineId} - Busbar coupler susceptance must be zero (${susceptance} ohm)")
                        .withUntypedValue(LINE_ID_KEY, lineId)
                        .withTypedValue("susceptance", line.getSusceptance(), TypedValue.SUSCEPTANCE)
                        .withSeverity(TypedValue.WARN_SEVERITY)
                        .add();
                    LOGGER.warn(lineId, "Busbar coupler susceptance must be zero", line.getSusceptance() + " S");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected line status");
        }
    }

    // Data: transformer characteristics
    public static void checkValidTransformerCharacteristics(UcteTransformer ucteTransformer, ReportNode reportNode) {
        String transformerId = ucteTransformer.getId().toString();
        if (ucteTransformer.getNominalPower() < ZERO_EPS) {
            reportNode.newReportNode()
                .withMessageTemplate("epsilonTransformerNominalPower", "${transformerId} - Value must be positive, blank and zero is not allowed (${nominalPower} ohm)")
                .withUntypedValue("transformerId", transformerId)
                .withUntypedValue("nominalPower", ucteTransformer.getNominalPower())
                .withSeverity(TypedValue.ERROR_SEVERITY)
                .add();
            LOGGER.error(transformerId, "Value must be positive, blank and zero is not allowed", ucteTransformer.getNominalPower() + " MW");
        }
        if (ucteTransformer.getResistance() < ZERO_EPS) {
            LOGGER.error(transformerId, "Blank is not allowed, real transformer resistance must be greater than or equal to zero",
                    ucteTransformer.getResistance() + " ohm");
        }
        if (ucteTransformer.getReactance() < REACTANCE_EPS) {
            LOGGER.error(transformerId, "Blank is not allowed, absolute value of reactance must be greater than 0.05 ohm", ucteTransformer.getReactance() + " ohm");
        }
        if (ucteTransformer.getSusceptance() < ZERO_EPS) {
            LOGGER.warn(transformerId, "Blank is not allowed", ucteTransformer.getSusceptance() + " S");
        }
        if (ucteTransformer.getConductance() < ZERO_EPS) {
            LOGGER.warn(transformerId, "Transformer shunt conductance must be greater than or equal to zero", ucteTransformer.getConductance() + " S");
        }
    }

    // Data: transformer regulation
    // Phase regulation
    public static void checkPhaseRegulation(UctePhaseRegulation uctePhaseRegulation, UcteElementId transfoId, ReportNode reportNode) {
        if (uctePhaseRegulation.getDu() < ZERO_EPS || uctePhaseRegulation.getDu() > DU_LIMIT) {
            reportNode.newReportNode()
                .withMessageTemplate("wrongPhaseRegulationDu", "${transfoId} - For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withUntypedValue("transfoId", transfoId.toString())
                .withUntypedValue("du", uctePhaseRegulation.getDu())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
            LOGGER.warn(transfoId.toString(), "For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 %",
                    uctePhaseRegulation.getDu() + " %");
        }
        if (uctePhaseRegulation.getN() != null && (uctePhaseRegulation.getN() < ZERO_EPS || uctePhaseRegulation.getN() > N_LIMIT)) {
            LOGGER.warn(transfoId.toString(), "The number of phase regulating taps cannot be negative and cannot exceed 35", uctePhaseRegulation.getN().toString());
        }
    }

    // Angle regulation
    public static void checkAngleRegulation(UcteAngleRegulation ucteAngleRegulation, UcteElementId transfoId, ReportNode reportNode) {
        if (ucteAngleRegulation.getDu() < ZERO_EPS || ucteAngleRegulation.getDu() > DU_LIMIT) {
            reportNode.newReportNode()
                .withMessageTemplate("wrongAngleRegulationDu", "${transfoId} - For LTCs, transformer angle regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withUntypedValue("transfoId", transfoId.toString())
                .withUntypedValue("du", ucteAngleRegulation.getDu())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
            LOGGER.warn(transfoId.toString(), "For LTCs, transformer angle regulation voltage per tap should not be zero. Its absolute value should not be above 6 %",
                    ucteAngleRegulation.getDu() + " %");
        }
        if (ucteAngleRegulation.getN() != null && (ucteAngleRegulation.getN() < ZERO_EPS || ucteAngleRegulation.getN() > N_LIMIT)) {
            LOGGER.warn(transfoId.toString(), "The value cannot be negative and cannot exceed 35", ucteAngleRegulation.getN().toString());
        }
        if (Math.abs(ucteAngleRegulation.getTheta()) > THETA_ABS_LIMIT) {
            LOGGER.warn(transfoId.toString(), "The absolute value of the angle cannot exceed 180°", ucteAngleRegulation.getTheta() + " °");
        }
    }
}
