/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.ucte.converter.UcteException;
import com.powsybl.ucte.network.UcteAngleRegulation;
import com.powsybl.ucte.network.UctePhaseRegulation;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 */
public final class UcteConverterHelper {

    private UcteConverterHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * calculate the δu(%) for the phase regulation of the two windings transformer
     *
     * du = 100 * (1 / ρmin – 1/ ρmax) / (number of taps - 1).
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the δu needed to create a {@link UctePhaseRegulation}
     */
    public static double calculatePhaseDu(TwoWindingsTransformer twoWindingsTransformer) {
        RatioTapChanger tapChanger = twoWindingsTransformer.getRatioTapChanger();
        // Given the formulas : rho(i) = 1/(1+du.i/100)
        // We have : -n.du = 100.(1/rho(-n) - 1)  and n.du = 100.(1/rho(n) - 1)
        // Which gives n.du - (-n.du) = 100.(1/rho(n) - 1/rho(-n))
        // then du = 100.(1/rho(n) - 1/rho(-n))/(nbTaps-1)
        double rhoStepMin = tapChanger.getStep(tapChanger.getLowTapPosition()).getRho();
        double rhoStepMax = tapChanger.getStep(tapChanger.getHighTapPosition()).getRho();
        double res = 100 * (1 / rhoStepMax - 1 / rhoStepMin) / (tapChanger.getStepCount() - 1);

        return BigDecimal.valueOf(res).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type symmetrical
     *
     * dU = 100 * (2*(tan(αmax/2) – tan(αmin/2))/(number of taps -1))
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */
    public static double calculateSymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger tapChanger = twoWindingsTransformer.getPhaseTapChanger();
        // Given the formulas : alpha(i) = 2.Atan(i/100.du/2)
        // We have : -n.du = 200.tan(alpha(-n)/2)  and n.du = 200.tan(alpha(n)/2)
        // Which gives n.du - (-n.du) = 200.(tan(alpha(n)/2) - tan(alpha(-n)/2))
        // then du = 2.100.(tan(alpha(n)/2) - tan(alpha(-n)/2))/(nbTaps-1)
        double alphaStepMax = Math.toRadians(tapChanger.getStep(tapChanger.getHighTapPosition()).getAlpha());
        double alphaStepMin = Math.toRadians(tapChanger.getStep(tapChanger.getLowTapPosition()).getAlpha());

        // minus sign because in the UCT importer, alpha has sign inverted because in the UCT model PST is on side 2 and side1 on IIDM model
        // we apply here the same transformation back
        return -100 * (2 * (Math.tan(alphaStepMax / 2) - Math.tan(alphaStepMin / 2)) / (tapChanger.getStepCount() - 1));
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type asymmetrical
     *
     * dU = 100 * (distance between the 2 most distant taps) / (number of taps - 1)
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */

    public static Complex calculateAsymmAngleDuAndAngle(TwoWindingsTransformer twoWindingsTransformer, boolean combinePhaseAngleRegulation) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        double lowPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(lowTapPosition).getAlpha());
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(highTapPosition).getAlpha());
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionAlpha);
        double xb = highPositionRho * Math.cos(highPositionAlpha);
        double yb = highPositionRho * Math.sin(highPositionAlpha);

        double theta;
        if (Math.abs(xb - xa) < 0.000000001) {
            // we suppose that theta is equal to Pi/2 in this case
            theta = Math.PI / 2.;
        } else {
            theta = Math.atan((yb - ya) / (xb - xa));
        }
        // the formula above gives actually the module of du, we need to verify the sign of du
        if ((yb * highPositionRho - ya * lowPositionRho) / Math.sin(theta) < 0.) {
            theta = theta - Math.PI;
        }

        int tapNumber = phaseTapChanger.getStepCount();
        double distance = Math.sqrt((xb - xa) * (xb - xa) + (yb - ya) * (yb - ya));
        double absDu = 100 * distance / (tapNumber - 1);
        if (combinePhaseAngleRegulation) {
            double r0Rtc = 1.0;
            if (twoWindingsTransformer.getRatioTapChanger() != null) {
                RatioTapChanger ratioTapChanger = twoWindingsTransformer.getRatioTapChanger();
                int r0TapPosition = ratioTapChanger.getTapPosition();
                r0Rtc = ratioTapChanger.getStep(r0TapPosition).getRho();
            }
            absDu = absDu / r0Rtc; // in the case of a combined RTC and PTC absDu includes rho0 of RTC
        }

        return ComplexUtils.polar2Complex(BigDecimal.valueOf(absDu).setScale(4, RoundingMode.HALF_UP).doubleValue(),
                theta);
    }

    public static char getOrderCode(int index) {
        if (index > UcteConstants.ORDER_CODES.size() || index < 0) {
            throw new UcteException("Order code index out of bounds");
        }
        return UcteConstants.ORDER_CODES.get(index);
    }

}
