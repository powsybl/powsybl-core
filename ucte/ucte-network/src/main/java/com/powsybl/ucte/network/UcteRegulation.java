/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteRegulation implements UcteRecord {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteRegulation.class);

    private static final double LOW_VOLTAGE_FACTOR = 0.8;
    private static final double HIGH_VOLTAGE_FACTOR = 1.2;
    private static final double LOW_NOMINAL_VOLTAGE = 110;

    private final UcteElementId transfoId;

    private UctePhaseRegulation phaseRegulation;

    private UcteAngleRegulation angleRegulation;

    public UcteRegulation(UcteElementId transfoId, UctePhaseRegulation phaseRegulation, UcteAngleRegulation angleRegulation) {
        this.transfoId = Objects.requireNonNull(transfoId);
        this.phaseRegulation = phaseRegulation;
        this.angleRegulation = angleRegulation;
    }

     /**
     * Gets transformer id.
     * <p>Node 1 is non-regulated winding.
     * <p>Node 2 is regulated winding.
     * @return transformer id
     */
    public UcteElementId getTransfoId() {
        return transfoId;
    }

    /**
     * Gets phase regulation (optional).
     * @return phase regulation
     */
    public UctePhaseRegulation getPhaseRegulation() {
        return phaseRegulation;
    }

    /**
     * Sets phase regulation (optional).
     * @param phaseRegulation phase regulation
     */
    public void setPhaseRegulation(UctePhaseRegulation phaseRegulation) {
        this.phaseRegulation = phaseRegulation;
    }

    /**
     * Gets angle regulation (optional).
     * @return angle regulation
     */
    public UcteAngleRegulation getAngleRegulation() {
        return angleRegulation;
    }

    /**
     * Sets angle regulation (optional).
     * @param angleRegulation angle regulation
     */
    public void setAngleRegulation(UcteAngleRegulation angleRegulation) {
        this.angleRegulation = angleRegulation;
    }

    @Override
    public void fix(Reporter reporter) {
        if (phaseRegulation != null) {
            UcteValidation.checkPhaseRegulation(phaseRegulation, transfoId, reporter);
            if (phaseRegulation.getU() <= 0) {
                LOGGER.warn("Phase regulation of transformer '{}' has a bad target voltage {}, set to undefined",
                        transfoId, phaseRegulation.getU());
                phaseRegulation.setU(Double.NaN);
            }
            double nominalVoltage = transfoId.getNodeCode2().getVoltageLevelCode().getVoltageLevel();
            if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (phaseRegulation.getU() < LOW_VOLTAGE_FACTOR * nominalVoltage
                    || phaseRegulation.getU() > HIGH_VOLTAGE_FACTOR * nominalVoltage)) {
                LOGGER.warn("Phase regulation of transformer '{}' has a target voltage {} kV too far from nominal voltage",
                        transfoId, phaseRegulation.getU());
            }
            // FIXME: N should be stricly positive and NP in [-n, n]
            if (phaseRegulation.getN() == null || phaseRegulation.getN() == 0
                || phaseRegulation.getNp() == null || Double.isNaN(phaseRegulation.getDu())) {
                LOGGER.warn("Phase regulation of transformer '{}' removed because incomplete", transfoId);
                phaseRegulation = null;
            }
        }
        if (angleRegulation != null) {
            UcteValidation.checkAngleRegulation(angleRegulation, transfoId, reporter);
            // FIXME: N should be stricly positive and NP in [-n, n]
            if (angleRegulation.getN() == null || angleRegulation.getN() == 0
                    || angleRegulation.getNp() == null || Double.isNaN(angleRegulation.getDu())
                    || Double.isNaN(angleRegulation.getTheta())) {
                LOGGER.warn("Angle regulation of transformer '{}' removed because incomplete", transfoId);
                angleRegulation = null;
            } else {
                // FIXME: type should not be null
                if (angleRegulation.getType() == null) {
                    LOGGER.warn("Type is missing for angle regulation of transformer '{}', default to {}", transfoId, UcteAngleRegulationType.ASYM);
                    angleRegulation.setType(UcteAngleRegulationType.ASYM);
                }
            }
        }
    }

}
