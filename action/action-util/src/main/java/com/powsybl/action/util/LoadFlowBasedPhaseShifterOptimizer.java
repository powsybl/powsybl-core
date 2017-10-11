/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowBasedPhaseShifterOptimizer implements PhaseShifterOptimizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowBasedPhaseShifterOptimizer.class);

    private final ComputationManager computationManager;

    private final LoadFlowBasedPhaseShifterOptimizerConfig config;

    public LoadFlowBasedPhaseShifterOptimizer(ComputationManager computationManager, LoadFlowBasedPhaseShifterOptimizerConfig config) {
        this.computationManager = Objects.requireNonNull(computationManager);
        this.config = Objects.requireNonNull(config);
    }

    public LoadFlowBasedPhaseShifterOptimizer(ComputationManager computationManager) {
        this(computationManager, LoadFlowBasedPhaseShifterOptimizerConfig.load());
    }

    private void runLoadFlow(LoadFlow loadFlow) {
        try {
            LoadFlowResult result = loadFlow.run();
            if (!result.isOk()) {
                throw new RuntimeException("Load flow diverged during phase shifter optimization");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static float getI(TwoWindingsTransformer phaseShifter) {
        return phaseShifter.getTerminal1().getI();
    }

    private static float getLimit(TwoWindingsTransformer phaseShifter) {
        return phaseShifter.getCurrentLimits1().getPermanentLimit();
    }

    @Override
    public void findMaximalFlowTap(Network network, String phaseShifterId) {
        TwoWindingsTransformer phaseShifter = network.getTwoWindingsTransformer(phaseShifterId);
        if (phaseShifter == null) {
            throw new RuntimeException("Phase shifter '" + phaseShifterId + "' not found");
        }
        if (phaseShifter.getPhaseTapChanger() == null) {
            throw new RuntimeException("Transformer '" + phaseShifterId + "' is not a phase shifter");
        }

        int optimalTap;

        // fromNode a temporary state that will be used to move the phase shifter tap without changing the current state
        String stateId = network.getStateManager().getWorkingStateId();
        String tmpStateId = "phase-shifter-optim-" + UUID.randomUUID();
        network.getStateManager().cloneState(stateId, tmpStateId);
        try {
            network.getStateManager().setWorkingState(tmpStateId);
            LoadFlowFactory loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
            LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);
            runLoadFlow(loadFlow);
            if (phaseShifter.getTerminal1().getI() >= phaseShifter.getCurrentLimits1().getPermanentLimit()) {
                throw new RuntimeException("Phase shifter already overloaded");
            }
            int tapPosInc = 1; // start by incrementing tap +1
            float i = getI(phaseShifter);
            float limit = getLimit(phaseShifter);
            int tapPos = phaseShifter.getPhaseTapChanger().getTapPosition();
            int maxTap = phaseShifter.getPhaseTapChanger().getHighTapPosition();
            int minTap = phaseShifter.getPhaseTapChanger().getLowTapPosition();
            int topTap = maxTap;
            int btmTap = minTap;

            phaseShifter.getPhaseTapChanger().setTapPosition(maxTap);
            runLoadFlow(loadFlow);
            float iMax = getI(phaseShifter);
            phaseShifter.getPhaseTapChanger().setTapPosition(minTap);
            runLoadFlow(loadFlow);
            float iMin = getI(phaseShifter);
            float direction = iMax > i ? 1.0f : -1.0f;
            if (direction < 0) {
                float itmp = iMin;
                iMin = iMax;
                iMax = itmp;
                int ttmp = minTap;
                minTap = maxTap;
                maxTap = ttmp;
            }

            if ((iMax - limit) * (iMin - limit) < 0) {
                // we can find a optimal tap by dichotomy-newton algo
                optimalTap = tapPos;
                if (direction > 0) {
                    btmTap = tapPos;
                } else {
                    topTap = tapPos;
                }
                iMin = i;
                do {
                    float ratio = (limit - i) / (iMax - iMin);
                    tapPosInc = calculTapInc(ratio, direction, topTap, tapPos, btmTap);

                    tapPos += tapPosInc;
                    phaseShifter.getPhaseTapChanger().setTapPosition(tapPos);
                    runLoadFlow(loadFlow);
                    i = getI(phaseShifter);

                    topTap = updateTopTap(i, limit, direction, topTap, tapPos);
                    btmTap = updateBtmTap(i, limit, direction, btmTap, tapPos);
                    iMax = updateIMax(i, limit, iMax);
                    iMin = updateIMin(i, limit, iMin);
                    optimalTap = updateOptimal(i, limit, tapPos, optimalTap);
                } while (topTap - btmTap > 1);
            } else {
                optimalTap = getOptimalDirectly(iMax, iMin, limit, maxTap, minTap);
            }
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        } catch (InstantiationException e) {
            throw new UncheckedInstantiationException(e);
        } finally {
            // don't forget to remove the temporary state!
            network.getStateManager().removeState(tmpStateId);
            network.getStateManager().setWorkingState(stateId);
        }

        LOGGER.debug("Optimal phase shifter '{}' tap is {} (from {})",
                phaseShifter, optimalTap, phaseShifter.getPhaseTapChanger().getTapPosition());

        // set the best optimal tap on the current state
        phaseShifter.getPhaseTapChanger().setTapPosition(optimalTap);
        phaseShifter.getPhaseTapChanger().setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
    }

    private int getOptimalDirectly(float iMax, float iMin, float limit, int maxTap, int minTap) {
        if (iMax == limit) {
            return maxTap - 1;
        } else if (iMin == limit) {
            return minTap + 1;
        } else {
            // all the tap under limit
            return iMax > iMin ? maxTap : minTap;
        }
    }

    private int updateBtmTap(float i, float limit, float direction, int previousBtmTap, int tapPos) {
        if (i >= limit && direction < 0) {
            return tapPos;
        }
        if (i < limit && direction > 0) {
            return tapPos;
        }
        return previousBtmTap;
    }

    private int updateTopTap(float i, float limit, float direction, int previousTopTap, int tapPos) {
        if (i >= limit && direction > 0) {
            return tapPos;
        }
        if (i < limit && direction < 0) {
            return tapPos;
        }
        return previousTopTap;
    }

    private float updateIMax(float i, float limit, float previousIMax) {
        return i >= limit ? i : previousIMax;
    }

    private float updateIMin(float i, float limit, float previousIMin) {
        return i < limit ? i : previousIMin;
    }

    private int updateOptimal(float i, float limit, int tapPos, int previousOptimal) {
        return i < limit ? tapPos : previousOptimal;
    }

    private int calculTapInc(float ratio, float direction, int topTap, int tapPos, int btmTap) {
        int tapPosInc = (direction > 0) ? (int) (ratio * (topTap - tapPos)) : (int) (ratio * (tapPos - btmTap));
        if (tapPosInc == 0) {
            // move one step at least
            tapPosInc = ratio > 0 ? 1 : -1;
        }
        tapPosInc = (int) direction * tapPosInc;
        return tapPosInc;
    }
}
