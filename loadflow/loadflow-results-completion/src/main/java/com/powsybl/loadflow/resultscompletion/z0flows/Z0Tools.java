/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.LegData;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class Z0Tools {

    private Z0Tools() {

    }

    static Terminal ofOtherBus(Branch<?> branch, Bus bus) {
        Objects.requireNonNull(branch);
        if (busAtTerminal(branch.getTerminal1()) == bus) {
            return branch.getTerminal2();
        } else if (busAtTerminal(branch.getTerminal2()) == bus) {
            return branch.getTerminal1();
        }
        return null;
    }

    static Terminal ofBus(Branch<?> branch, Bus bus) {
        Objects.requireNonNull(branch);
        if (busAtTerminal(branch.getTerminal1()) == bus) {
            return branch.getTerminal1();
        } else if (busAtTerminal(branch.getTerminal2()) == bus) {
            return branch.getTerminal2();
        }
        return null;
    }

    private static Bus busAtTerminal(Terminal t) {
        if (t.isConnected()) {
            return t.getBusView().getBus();
        }
        return null;
    }

    public static Complex getVstarFromZ0Leg(ThreeWindingsTransformer t3wt, List<Leg> z0Legs,
        LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {

        for (Leg z0Leg : z0Legs) {
            int phaseAngleClock = getPhaseAngleClock(t3wt, z0Leg);
            LegData z0LegData = new LegData(z0Leg, t3wt.getRatedU0(), phaseAngleClock, parameters.getEpsilonX(),
                parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());

            Complex vStar = z0LegData.getComplexVFromZ0AtStarBus();
            if (isValidVoltage(vStar)) {
                return vStar;
            }
        }
        return new Complex(Double.NaN, Double.NaN);
    }

    public static Complex getLegFlow(ThreeWindingsTransformer t3wt, Leg leg, Complex vstar,
        LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {

        int phaseAngleClock = getPhaseAngleClock(t3wt, leg);
        LegData legData = new LegData(leg, t3wt.getRatedU0(), phaseAngleClock, parameters.getEpsilonX(),
            parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());
        return legData.getFlow(vstar);
    }

    public static Complex getFlowLegAtStarBus(ThreeWindingsTransformer t3wt, Leg leg, Complex vstar,
        LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {

        int phaseAngleClock = Z0Tools.getPhaseAngleClock(t3wt, leg);
        LegData legData = new LegData(leg, t3wt.getRatedU0(), phaseAngleClock, parameters.getEpsilonX(),
            parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());

        return legData.getFlowAtStarBus(vstar);
    }

    public static int getPhaseAngleClock(ThreeWindingsTransformer t3wt, Leg leg) {
        if (t3wt.getLeg1().equals(leg)) {
            return 0;
        }
        ThreeWindingsTransformerPhaseAngleClock phaseAngleClockExtension = t3wt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);

        if (t3wt.getLeg2().equals(leg)) {
            return phaseAngleClockExtension != null ? phaseAngleClockExtension.getPhaseAngleClockLeg2() : 0;
        }
        if (t3wt.getLeg3().equals(leg)) {
            return phaseAngleClockExtension != null ? phaseAngleClockExtension.getPhaseAngleClockLeg3() : 0;
        }
        return 0;
    }

    public static boolean isValidVoltage(Complex v) {
        return !v.isNaN();
    }

    public static boolean isValidVoltage(Double v, double theta) {
        return !Double.isNaN(v) && v >= 0 && !Double.isNaN(theta);
    }
}
