/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.dc;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.powsybl.cgmes.model.CgmesNames.DC_LINE_SEGMENT;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
public class DCLink {

    private ConvertersMode mode;
    private double r;
    private double ratedUdc;
    private double targetP;
    private double pDcInverter;
    private double lossFactor1;
    private double lossFactor2;

    private final PropertyBag converter1;
    private final PropertyBag converter2;
    private final PropertyBag dcLine1;
    private final PropertyBag dcLine2;

    private static final String OPERATING_MODE = "operatingMode";
    private static final String TARGET_PPCC = "targetPpcc";
    private static final String POLE_LOSS_P = "poleLossP";
    private static final Logger LOG = LoggerFactory.getLogger(DCLink.class);

    public DCLink(PropertyBag converter1, PropertyBag converter2, PropertyBag dcLine1, PropertyBag dcLine2) {
        this.converter1 = converter1;
        this.converter2 = converter2;
        this.dcLine1 = dcLine1;
        this.dcLine2 = dcLine2;

        computeMode();
        computeR();
        computeRatedUdc();
        computeActivePowers();
        computeLossFactors();
    }

    private void computeMode() {
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
            mode = SIDE_1_RECTIFIER_SIDE_2_INVERTER;
            String dcLine1Id = dcLine1.getId(DC_LINE_SEGMENT);
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

    private void computeR() {
        double r1 = dcLine1.asDouble("r");
        r1 = Double.isNaN(r1) ? 0.1 : r1;
        if (dcLine2 == null) {
            r = r1;
        } else {
            double r2 = dcLine2.asDouble("r", 0.1);
            r2 = Double.isNaN(r2) ? 0.1 : r2;
            r = r1 + r2;
        }

        if (r < 0.0) {
            String dcLine1Id = dcLine1.getId(DC_LINE_SEGMENT);
            LOG.warn("Invalid r for DCLink with DCLineSegment: {}. Was: {}, fixed to 0.1.", dcLine1Id, r);
            r = 0.1;
        }
    }

    private void computeRatedUdc() {
        ratedUdc = converter1.asDouble(CgmesNames.RATED_UDC);
        if (ratedUdc == 0.0) {
            ratedUdc = converter2.asDouble(CgmesNames.RATED_UDC);
        }
    }

    private void computeActivePowers() {
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
            targetP = 0.0;
            pDcInverter = 0.0;
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
        double idc = ratedUdc != 0.0 ? pDcRectifier / ratedUdc : 0.0;
        return r * idc * idc;
    }

    private double resistiveLossesFromPdcInverter(double pDcInverter) {
        double idc = r != 0.0 ? (ratedUdc - Math.sqrt(ratedUdc * ratedUdc - 4 * r * Math.abs(pDcInverter))) / (2 * r) : 0.0;
        return r * idc * idc;
    }

    private void computeLossFactors() {
        // Loss factor is pole losses divided by incoming power.
        if (targetP == 0.0) {
            lossFactor1 = 0.0;
            lossFactor2 = 0.0;
        } else if (mode == SIDE_1_RECTIFIER_SIDE_2_INVERTER) {
            lossFactor1 = getPoleLossesRectifier() / targetP * 100;
            lossFactor2 = getPoleLossesInverter() / Math.abs(pDcInverter) * 100;
        } else {
            lossFactor1 = getPoleLossesInverter() / Math.abs(pDcInverter) * 100;
            lossFactor2 = getPoleLossesRectifier() / targetP * 100;
        }
    }

    public ConvertersMode getMode() {
        return mode;
    }

    public double getR() {
        return r;
    }

    public double getRatedUdc() {
        return ratedUdc;
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

    public PropertyBag getConverter1() {
        return converter1;
    }

    public PropertyBag getConverter2() {
        return converter2;
    }

    public PropertyBag getDcLine1() {
        return dcLine1;
    }

    public PropertyBag getDcLine2() {
        return dcLine2;
    }
}
