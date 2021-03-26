/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.reporter.Report;
import com.powsybl.commons.reporter.ReportBuilder;
import com.powsybl.commons.reporter.Reporter;

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

    private UcteValidation() {
    }

    // Data: line characteristics
    public static void checkValidLineCharacteristics(UcteLine line, Reporter reporter) {
        String lineId = line.getId().toString();
        switch (line.getStatus()) {
            case EQUIVALENT_ELEMENT_IN_OPERATION:
            case EQUIVALENT_ELEMENT_OUT_OF_OPERATION:
            case REAL_ELEMENT_IN_OPERATION:
            case REAL_ELEMENT_OUT_OF_OPERATION:
                if (line.getResistance() < ZERO_EPS) {
                    reporter.report(new ReportBuilder()
                        .withKey("negativeLineResistance")
                        .withDefaultMessage("${lineId} - Real line resistance cannot be negative (${resistance} ohm)")
                        .withValue("lineId", lineId)
                        .withValue("resistance", line.getResistance())
                        .withSeverity(Report.SEVERITY_ERROR)
                        .build());
                    LOGGER.error(lineId, "Real line resistance cannot be negative", line.getResistance() + " ohm");
                }
                if (Math.abs(line.getReactance()) < REACTANCE_EPS) {
                    reporter.report(new ReportBuilder()
                        .withKey("epsilonLineReactance")
                        .withDefaultMessage("${lineId} - Real line reactance must be larger than 0.05 ohm (${reactance} ohm)")
                        .withValue("lineId", lineId)
                        .withValue("reactance", line.getReactance())
                        .withSeverity(Report.SEVERITY_WARN)
                        .build());
                    LOGGER.warn(lineId, "Real line reactance must be larger than 0.05 ohm",
                            line.getReactance() + " ohm");
                }
                break;
            case BUSBAR_COUPLER_IN_OPERATION:
            case BUSBAR_COUPLER_OUT_OF_OPERATION:
                if (Math.abs(line.getResistance()) > ZERO_EPS) {
                    reporter.report(new ReportBuilder()
                        .withKey("nonZeroBusbarCouplerResistance")
                        .withDefaultMessage("${lineId} - Busbar coupler resistance must be zero (${resistance} ohm)")
                        .withValue("lineId", lineId)
                        .withValue("resistance", line.getResistance())
                        .withSeverity(Report.SEVERITY_WARN)
                        .build());
                    LOGGER.warn(lineId, "Busbar coupler resistance must be zero", line.getResistance() + " ohm");
                }
                if (Math.abs(line.getReactance()) > ZERO_EPS) {
                    reporter.report(new ReportBuilder()
                        .withKey("nonZeroBusbarCouplerReactance")
                        .withDefaultMessage("${lineId} - Busbar coupler reactance must be zero (${reactance} ohm)")
                        .withValue("lineId", lineId)
                        .withValue("reactance", line.getReactance())
                        .withSeverity(Report.SEVERITY_WARN)
                        .build());
                    LOGGER.warn(lineId, "Busbar coupler reactance must be zero", line.getReactance() + " ohm");
                }
                if (Math.abs(line.getSusceptance()) > ZERO_EPS) {
                    reporter.report(new ReportBuilder()
                        .withKey("nonZeroBusbarCouplerSusceptance")
                        .withDefaultMessage("${lineId} - Busbar coupler susceptance must be zero (${susceptance} ohm)")
                        .withValue("lineId", lineId)
                        .withValue("susceptance", line.getSusceptance())
                        .withSeverity(Report.SEVERITY_WARN)
                        .build());
                    LOGGER.warn(lineId, "Busbar coupler susceptance must be zero", line.getSusceptance() + " S");
                }
                break;
            default:
                throw new AssertionError("Unexpected line status");
        }
    }

    // Data: transformer characteristics
    public static void checkValidTransformerCharacteristics(UcteTransformer ucteTransformer, Reporter reporter) {
        String transformerId = ucteTransformer.getId().toString();
        if (ucteTransformer.getNominalPower() < ZERO_EPS) {
            reporter.report(new ReportBuilder()
                .withKey("epsilonTransformerNominalPower")
                .withDefaultMessage("${transformerId} - Value must be positive, blank and zero is not allowed (${nominalPower} ohm)")
                .withValue("transformerId", transformerId)
                .withValue("nominalPower", ucteTransformer.getNominalPower())
                .withSeverity(Report.SEVERITY_ERROR)
                .build());
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
    public static void checkPhaseRegulation(UctePhaseRegulation uctePhaseRegulation, UcteElementId transfoId, Reporter reporter) {
        if (uctePhaseRegulation.getDu() < ZERO_EPS || uctePhaseRegulation.getDu() > DU_LIMIT) {
            reporter.report(new ReportBuilder()
                .withKey("wrongPhaseRegulationDu")
                .withDefaultMessage("${transfoId} - For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withValue("transfoId", transfoId)
                .withValue("du", uctePhaseRegulation.getDu())
                .withSeverity(Report.SEVERITY_WARN)
                .build());
            LOGGER.warn(transfoId.toString(), "For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 %",
                    uctePhaseRegulation.getDu() + " %");
        }
        if (uctePhaseRegulation.getN() != null && (uctePhaseRegulation.getN() < ZERO_EPS || uctePhaseRegulation.getN() > N_LIMIT)) {
            LOGGER.warn(transfoId.toString(), "The number of phase regulating taps cannot be negative and cannot exceed 35", uctePhaseRegulation.getN().toString());
        }
    }

    // Angle regulation
    public static void checkAngleRegulation(UcteAngleRegulation ucteAngleRegulation, UcteElementId transfoId, Reporter reporter) {
        if (ucteAngleRegulation.getDu() < ZERO_EPS || ucteAngleRegulation.getDu() > DU_LIMIT) {
            reporter.report(new ReportBuilder()
                .withKey("wrongAngleRegulationDu")
                .withDefaultMessage("${transfoId} - For LTCs, transformer angle regulation voltage per tap should not be zero. Its absolute value should not be above 6 % (${du} %)")
                .withValue("transfoId", transfoId)
                .withValue("du", ucteAngleRegulation.getDu())
                .withSeverity(Report.SEVERITY_WARN)
                .build());
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
