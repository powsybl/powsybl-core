/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.Objects;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

class LossFactor {

    LossFactor(Context context, HvdcLine.ConvertersMode mode, double pAC1, double pAC2, double poleLossP1,
        double poleLossP2) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(mode);
        this.context = context;
        this.mode = mode;
        this.pAC1 = pAC1;
        this.pAC2 = pAC2;
        this.poleLossP1 = poleLossP1;
        this.poleLossP2 = poleLossP2;
        this.lossFactor1 = Double.NaN;
        this.lossFactor2 = Double.NaN;
    }

    void compute() {
        // compute loss factors

        if (pAC1 != 0 && pAC2 != 0) {

            // we only keep one as we are not sure if pAC1 and pAC2 are consistent
            if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) { // we ignore pAC2
                computeLossFactor1(pAC1, poleLossP1, mode);
                computeLossFactor2FromPAC1(pAC1, poleLossP1, poleLossP2, mode);
            } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) { // we ignore pAC1
                computeLossFactor1FromPAC2(pAC2, poleLossP1, poleLossP2, mode);
                computeLossFactor2(pAC2, poleLossP2, mode);
            }
        } else if (pAC1 != 0) { // pAC2 == 0

            computeLossFactor1(pAC1, poleLossP1, mode);
            computeLossFactor2FromPAC1(pAC1, poleLossP1, poleLossP2, mode);
        } else if (pAC2 != 0) { // pAC1 == 0

            computeLossFactor1FromPAC2(pAC2, poleLossP1, poleLossP2, mode);
            computeLossFactor2(pAC2, poleLossP2, mode);
        } else {
            this.lossFactor1 = 0.0;
            this.lossFactor2 = 0.0;
        }

        if (Double.isNaN(this.lossFactor1)) {
            this.lossFactor1 = 0.0;
            context.fixed("lossFactor1", "was NaN", Double.NaN, this.lossFactor1);
        }
        if (Double.isNaN(this.lossFactor2)) {
            this.lossFactor2 = 0.0;
            context.fixed("lossFactor2", "was NaN", Double.NaN, this.lossFactor2);
        }

        // else (i.e. pAC1 == 0 && pAC2 == 0) do nothing: loss factors are null and
        // stations are probably disconnected
    }

    private void computeLossFactor1(double pAC1, double poleLossP1, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) { // pAC1 > 0
            this.lossFactor1 = poleLossP1 / pAC1 * 100;
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && Math.abs(pAC1) + poleLossP1 != 0) { // pAC1 < 0
            this.lossFactor1 = poleLossP1 / (Math.abs(pAC1) + poleLossP1) * 100;
        }
    }

    private void computeLossFactor2(double pAC2, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)) { // pAC2 > 0
            this.lossFactor2 = poleLossP2 / pAC2 * 100;
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && Math.abs(pAC2) + poleLossP2 != 0) { // pAC2 < 0
            this.lossFactor2 = poleLossP2 / (Math.abs(pAC2) + poleLossP2) * 100;
        }
    }

    private void computeLossFactor1FromPAC2(double pAC2, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && (Math.abs(pAC2) + poleLossP2 + poleLossP1) != 0) { // pAC2 < 0
            // lossFactor1 = poleLossP1 / pAC1 * 100
            // pAC1 = pDC + poleLossP1 = pAC2 + poleLossP2 + poleLossP1
            this.lossFactor1 = poleLossP1 / (Math.abs(pAC2) + poleLossP2 + poleLossP1) * 100;
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && (pAC2 - poleLossP2) != 0) { // pAC2 > 0
            // lossFactor1 = poleLossP1 / pDC * 100
            // pDC = pAC2 - poleLossP2
            this.lossFactor1 = poleLossP1 / (pAC2 - poleLossP2) * 100;
        }
    }

    private void computeLossFactor2FromPAC1(double pAC1, double poleLossP1, double poleLossP2, HvdcLine.ConvertersMode mode) {
        if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER) && (pAC1 - poleLossP1) != 0) { // pAC1 > 0
            // lossFactor2 = poleLossP2 / pDC * 100
            // pDC = pAC1 - poleLossP1
            this.lossFactor2 = poleLossP2 / (pAC1 - poleLossP1) * 100;
        } else if (mode.equals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER) && (Math.abs(pAC1) + poleLossP1 + poleLossP2) != 0) { // pAC1 < 0
            // lossFactor2 = poleLossP2 / (pDC + poleLossP2) * 100
            // pDC = pAC1 + poleLossP1
            this.lossFactor2 = poleLossP2 / (Math.abs(pAC1) + poleLossP1 + poleLossP2) * 100;
        }
    }

    double getLossFactor1() {
        return this.lossFactor1;
    }

    double getLossFactor2() {
        return this.lossFactor2;
    }

    private final Context context;
    private final HvdcLine.ConvertersMode mode;
    private final double pAC1;
    private final double pAC2;
    private final double poleLossP1;
    private final double poleLossP2;
    private double lossFactor1;
    private double lossFactor2;
}
