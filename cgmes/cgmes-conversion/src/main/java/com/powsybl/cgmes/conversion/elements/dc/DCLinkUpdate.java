/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCLinkUpdate {

    private ConvertersMode mode;
    private double targetP;
    private double pDcInverter;
    private double lossFactor1;
    private double lossFactor2;

    private final HvdcLine hvdcLine;
    private final PropertyBag converter1;
    private final PropertyBag converter2;

    private static final String OPERATING_MODE = "operatingMode";
    private static final String TARGET_PPCC = "targetPpcc";
    private static final String POLE_LOSS_P = "poleLossP";
    private static final Logger LOG = LoggerFactory.getLogger(DCLinkUpdate.class);

    public DCLinkUpdate(HvdcLine hvdcLine, PropertyBag converter1, PropertyBag converter2, DefaultData defaultData) {
        this.hvdcLine = hvdcLine;
        this.converter1 = converter1;
        this.converter2 = converter2;

        computeMode();
        computeActivePowers();
        computeLossFactors();
    }

    private void computeMode(HvdcLine.ConvertersMode defaultMode) {
        String mode1 = converter1.getLocal(OPERATING_MODE);
        String mode2 = converter2.getLocal(OPERATING_MODE);
        if (isRectifier(mode1) && isInverter(mode2)) {
            mode = SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        } else if (isInverter(mode1) && isRectifier(mode2)) {
            mode = SIDE_1_INVERTER_SIDE_2_RECTIFIER;
        } else if (targetPpcc1() > 0.0 || targetPpcc2() < 0.0) {
            mode = SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        } else if (targetPpcc1() < 0.0 || targetPpcc2() > 0.0) {
            mode = SIDE_1_INVERTER_SIDE_2_RECTIFIER;
        } else {
            mode = defaultData.mode();
            String dcLine1Id = hvdcLine.getId();
            LOG.info("Default mode: {} for DCLink with DCLineSegment: {}.", mode, dcLine1Id);
        }
    }

    private boolean isRectifier(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("rectifier");
    }

    private boolean isInverter(String operatingMode) {
        return operatingMode != null && operatingMode.toLowerCase().endsWith("inverter");
    }

    private double targetPpcc1() {
        double targetPpcc = converter1.asDouble(TARGET_PPCC);
        return Double.isNaN(targetPpcc) ? 0.0 : targetPpcc;
    }

    private double targetPpcc2() {
        double targetPpcc = converter2.asDouble(TARGET_PPCC);
        return Double.isNaN(targetPpcc) ? 0.0 : targetPpcc;
    }

    private double pole1Losses() {
        double poleLosses = converter1.asDouble(POLE_LOSS_P);
        return Double.isNaN(poleLosses) ? 0.0 : poleLosses;
    }

    private double pole2Losses() {
        double poleLosses = converter2.asDouble(POLE_LOSS_P);
        return Double.isNaN(poleLosses) ? 0.0 : poleLosses;
    }

    private void computeActivePowers(double defaultTargetP, double defaultPdcInverter) {
        // targetP is AC active power on rectifier side.
        double pDcRectifier;
        if (getTargetPpccRectifier() != 0.0) {
            targetP = getTargetPpccRectifier();
            pDcRectifier = targetP - getPoleLossesRectifier();
            pDcInverter = -1 * (pDcRectifier - resistiveLossesFromPdcRectifier(pDcRectifier));
        } else if (getTargetPpccInverter() != 0.0) {
            pDcInverter = -1 * (Math.abs(getTargetPpccInverter()) + getPoleLossesInverter());
            pDcRectifier = Math.abs(pDcInverter) + resistiveLossesFromPdcInverter(pDcInverter);
            targetP = pDcRectifier + getPoleLossesRectifier();
        } else {
            targetP = defaultData.targetP();
            // No need to calculate DC powers when using default data.
        }
    }

    private double getTargetPpccRectifier() {
        if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            return targetPpcc1();
        }
        return targetPpcc2();
    }

    private double getTargetPpccInverter() {
        if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            return targetPpcc2();
        }
        return targetPpcc1();
    }

    private double getPoleLossesRectifier() {
        if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            return pole1Losses();
        }
        return pole2Losses();
    }

    private double getPoleLossesInverter() {
        if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            return pole2Losses();
        }
        return pole1Losses();
    }

    private double resistiveLossesFromPdcRectifier(double pDcRectifier) {
        double r = hvdcLine.getR();
        double ratedUdc = hvdcLine.getNominalV();
        double idc = ratedUdc != 0.0 ? pDcRectifier / ratedUdc : 0.0;
        return r * idc * idc;
    }

    private double resistiveLossesFromPdcInverter(double pDcInverter) {
        double r = hvdcLine.getR();
        double ratedUdc = hvdcLine.getNominalV();
        double idc = r != 0.0 ? (ratedUdc - Math.sqrt(ratedUdc * ratedUdc - 4 * r * Math.abs(pDcInverter))) / (2 * r) : 0.0;
        return r * idc * idc;
    }

    private void computeLossFactors(boolean isDefaultTargetP, double defaultLossFactor1, double defaultLossFactor2) {
        // Loss factor is pole losses divided by incoming power.
        if (targetP == 0.0 || targetP == defaultData.targetP()) {
            lossFactor1 = defaultData.lossFactor1();
            lossFactor2 = defaultData.lossFactor2();
        } else if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            lossFactor1 = getPoleLossesRectifier() / targetP * 100;
            lossFactor2 = getPoleLossesInverter() / Math.abs(pDcInverter) * 100;
        } else {
            lossFactor1 = getPoleLossesInverter() / Math.abs(pDcInverter) * 100;
            lossFactor2 = getPoleLossesRectifier() / targetP * 100;
        }
    }


    public record DefaultData(HvdcLine.ConvertersMode mode, double targetP, double lossFactor1, double lossFactor2) {
    }

    public ConvertersMode getMode() {
        return mode;
    }

    public double getTargetP() {
        return targetP;
    }

    public double getLossFactor1() {
        return lossFactor1;
    }

    public double getLossFactor2() {
        return lossFactor2;
    }
}
