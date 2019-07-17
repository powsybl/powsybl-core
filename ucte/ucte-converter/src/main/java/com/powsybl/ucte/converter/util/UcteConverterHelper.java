package com.powsybl.ucte.converter.util;

import com.google.common.primitives.Doubles;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.ucte.network.UcteAngleRegulation;
import com.powsybl.ucte.network.UctePhaseRegulation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface UcteConverterHelper {

    /**
     * calculate the δu(%) for the phase regulation of the two windings transformer
     *
     * du = 100 * (1 / ρmin – 1/ ρmax) / (number of taps - 1).
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the δu needed to create a {@link UctePhaseRegulation}
     */
    static double calculatePhaseDu(TwoWindingsTransformer twoWindingsTransformer) {
        double rhoMax = 0;
        double rhoMin = 0;
        int tapCount =  twoWindingsTransformer.getRatioTapChanger().getStepCount();
        double[] rhoList = new double[tapCount];

        for (int i = twoWindingsTransformer.getRatioTapChanger().getLowTapPosition(), j = 0;
             i <= twoWindingsTransformer.getRatioTapChanger().getHighTapPosition(); i++, j++) {
            rhoList[j] = twoWindingsTransformer.getRatioTapChanger().getStep(i).getRho();
        }
        rhoMax = Doubles.max(rhoList);
        rhoMin = Doubles.min(rhoList);
        double res = 100 * (1 / rhoMin - 1 / rhoMax) / (tapCount - 1);
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
    static double calculateSymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        double alphaMin = 0;
        double alphaMax = 0;
        int tabCount = twoWindingsTransformer.getPhaseTapChanger().getStepCount();
        double[] alphaList = new double[tabCount];
        for (int i = twoWindingsTransformer.getPhaseTapChanger().getLowTapPosition(), j = 0;
             i <= twoWindingsTransformer.getPhaseTapChanger().getHighTapPosition(); i++, j++) {
            alphaList[j] = twoWindingsTransformer.getPhaseTapChanger().getStep(i).getAlpha();
        }
        alphaMin = Math.toRadians(Doubles.min(alphaList));
        alphaMax = Math.toRadians(Doubles.max(alphaList));

        return 100 * (2 * (Math.tan(alphaMax / 2) - Math.tan(alphaMin / 2)) / (tabCount - 1));
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
    static double calculateAsymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
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
        double distance = Math.sqrt(Math.pow(xb - xa, 2) + Math.pow(yb - ya, 2));
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
    static double calculateAsymmAngleTheta(TwoWindingsTransformer twoWindingsTransformer) {
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
