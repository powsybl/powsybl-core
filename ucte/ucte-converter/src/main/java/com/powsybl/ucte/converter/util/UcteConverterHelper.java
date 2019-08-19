/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.ucte.network.UcteAngleRegulation;
import com.powsybl.ucte.network.UctePhaseRegulation;

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
        double rhoMin = Double.MAX_VALUE;
        double rhoMax = -Double.MAX_VALUE;

        RatioTapChanger tapChanger = twoWindingsTransformer.getRatioTapChanger();
        for (int i = tapChanger.getLowTapPosition(); i <= tapChanger.getHighTapPosition(); ++i) {
            rhoMin = Double.min(rhoMin, tapChanger.getStep(i).getRho());
            rhoMax = Double.max(rhoMax, tapChanger.getStep(i).getRho());
        }

        double res = 100 * (1 / rhoMin - 1 / rhoMax) / (tapChanger.getStepCount() - 1);
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
        double alphaMin = Double.MAX_VALUE;
        double alphaMax = -Double.MAX_VALUE;

        PhaseTapChanger tapChanger = twoWindingsTransformer.getPhaseTapChanger();
        for (int i = tapChanger.getLowTapPosition(); i <= tapChanger.getHighTapPosition(); ++i) {
            alphaMin = Double.min(alphaMin, tapChanger.getStep(i).getAlpha());
            alphaMax = Double.max(alphaMax, tapChanger.getStep(i).getAlpha());
        }
        alphaMin = Math.toRadians(alphaMin);
        alphaMax = Math.toRadians(alphaMax);

        return 100 * (2 * (Math.tan(alphaMax / 2) - Math.tan(alphaMin / 2)) / (tapChanger.getStepCount() - 1));
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
    public static double calculateAsymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        int tapNumber = phaseTapChanger.getStepCount();
        double lowPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(lowTapPosition).getAlpha());
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(highTapPosition).getAlpha());
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionAlpha);
        double xb = highPositionRho * Math.cos(highPositionAlpha);
        double yb = highPositionRho * Math.sin(highPositionAlpha);
        double distance = Math.sqrt((xb - xa) * (xb - xa) + (yb - ya) * (yb - ya));
        double du = 100 * distance / (tapNumber - 1);
        return BigDecimal.valueOf(du).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate the Θ for the angle regulation of the two windings transformer
     *
     * Theta is the angle between the line (created by the 2 points Alpha and rho) and the abscissa line
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the Θ needed to create a {@link UcteAngleRegulation}
     */
    public static double calculateAsymmAngleTheta(TwoWindingsTransformer twoWindingsTransformer) {
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
        return Math.toDegrees(Math.atan((yb - ya) / (xb - xa)));
    }
}
