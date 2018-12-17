/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class Transformers {

    private Transformers() {
    }

    /**
     * Get ratio on side 1.
     */
    public static double getRatio(TwoWindingsTransformer twt) {
        double rho = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            rho *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            rho *= twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    /**
     * Get shift angle on side 1.
     */
    public static double getAngle(TwoWindingsTransformer twt) {
        return twt.getPhaseTapChanger() != null ? Math.toRadians(twt.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    public static double getR(TwoWindingsTransformer twt) {
        return getValue(twt.getR(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getR() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getR() : 0);
    }

    public static double getX(TwoWindingsTransformer twt) {
        return getValue(twt.getX(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getX() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getX() : 0);
    }

    public static double getG1(TwoWindingsTransformer twt) {
        return getG1(twt, false);
    }

    public static double getG1(TwoWindingsTransformer twt, boolean specificCompatibility) {
        return getValue(specificCompatibility ? twt.getG() / 2 : twt.getG(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getG() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    public static double getB1(TwoWindingsTransformer twt) {
        return getB1(twt, false);
    }

    public static double getB1(TwoWindingsTransformer twt, boolean specificCompatibility) {
        return getValue(specificCompatibility ? twt.getB() / 2 : twt.getB(),
                twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getB() : 0,
                twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getB() : 0);
    }

}
