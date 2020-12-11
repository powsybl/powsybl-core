/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public final class UcteValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteValidation.class);

    public static final double ZERO_EPS = 1e-4;
    public static final double REACTANCE_EPS = 0.05;

    private UcteValidation() {
    }

    // Data: line characteristics
    public static void checkValidLineCharacteristics(UcteLine line) {
        switch (line.getStatus()) {
            case EQUIVALENT_ELEMENT_IN_OPERATION:
            case EQUIVALENT_ELEMENT_OUT_OF_OPERATION:
            case REAL_ELEMENT_IN_OPERATION:
            case REAL_ELEMENT_OUT_OF_OPERATION:
                if (line.getResistance() < ZERO_EPS) {
                    LOGGER.error("Real line resistance cannot be negative");
                }
                if (Math.abs(line.getReactance()) > ZERO_EPS) {
                    LOGGER.warn("Busbar coupler reactance must be zero. For lines this value must be larger than 0.05 ohm");
                }
                break;
            case BUSBAR_COUPLER_IN_OPERATION:
            case BUSBAR_COUPLER_OUT_OF_OPERATION:
                if (Math.abs(line.getResistance()) > ZERO_EPS) {
                    LOGGER.warn("Busbar coupler resistance must be zero");
                }
                if (Math.abs(line.getReactance()) > ZERO_EPS) {
                    LOGGER.warn("Busbar coupler reactance must be zero");
                }
                if (Math.abs(line.getSusceptance()) > ZERO_EPS) {
                    LOGGER.warn("Busbar coupler susceptance must be zero");
                }
            default:
        }
    }

    // Data: transformer characteristics
    public static void checkValidTransformerCharacteristics(UcteTransformer ucteTransformer) {
        if (ucteTransformer.getNominalPower() > ZERO_EPS) {
            LOGGER.error("Value must be positive, blank and zero is not allowed");
        }
        if (ucteTransformer.getResistance() > ZERO_EPS) {
            LOGGER.error("Blank is not allowed, real transformer resistance must be greater than or equal to zero");
        }
        if (ucteTransformer.getReactance() > REACTANCE_EPS) {
            LOGGER.error("Blank is not allowed, absolute value of reactance must be greater than 0.05 ohm");
        }
        if (ucteTransformer.getSusceptance() > ZERO_EPS) {
            LOGGER.warn("Blank is not allowed");
        }
        if (ucteTransformer.getConductance() > ZERO_EPS) {
            LOGGER.warn("Transformer shunt conductance must be greater than or equal to zero");
        }
    }

    // Data: transformer regulation
    // Phase regulation
    public static void checkPhaseRegulation(UctePhaseRegulation uctePhaseRegulation) {
        if (uctePhaseRegulation.getDu() < ZERO_EPS || uctePhaseRegulation.getDu() > 6) {
            LOGGER.warn("For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 %");
        }
        if (uctePhaseRegulation.getN() < ZERO_EPS || uctePhaseRegulation.getDu() > 35) {
            LOGGER.warn("The number of phase regulating taps cannot be negative and cannot exceed 35");
        }
    }

    // Angle regulation
    public static void checkAngleRegulation(UcteAngleRegulation ucteAngleRegulation) {
        if (ucteAngleRegulation.getDu() < ZERO_EPS || ucteAngleRegulation.getDu() > 6) {
            LOGGER.warn("For LTCs, transformer phase regulation voltage per tap should not be zero. Its absolute value should not be above 6 %");
        }
        if (ucteAngleRegulation.getN() < ZERO_EPS || ucteAngleRegulation.getDu() > 35) {
            LOGGER.warn("The value cannot be negative and cannot exceed 35");
        }
        if (Math.abs(ucteAngleRegulation.getTheta()) > 180) {
            LOGGER.warn("The absolute value of the angle cannot exceed 180°");
        }
    }
}
