/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
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
            LoadFlowResult result = loadFlow.run(LoadFlowParameters.load());
            if (!result.isOk()) {
                throw new PowsyblException("Load flow diverged during phase shifter optimization");
            }
        } catch (Exception e) {
            throw new PowsyblException(e);
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
            throw new PowsyblException("Phase shifter '" + phaseShifterId + "' not found");
        }
        if (phaseShifter.getPhaseTapChanger() == null) {
            throw new PowsyblException("Transformer '" + phaseShifterId + "' is not a phase shifter");
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
                throw new PowsyblException("Phase shifter already overloaded");
            }
            int tapPosInc = 1; // start by incrementing tap +1
            float i;
            float limit = getLimit(phaseShifter);
            int tapPos = phaseShifter.getPhaseTapChanger().getTapPosition();
            int maxTap = phaseShifter.getPhaseTapChanger().getHighTapPosition();

            // increment tap until going above permanent limit
            while ((i = getI(phaseShifter)) < limit && tapPos < maxTap) {
                // increment tap
                tapPos += tapPosInc;
                phaseShifter.getPhaseTapChanger().setTapPosition(tapPos);

                // run load flow
                runLoadFlow(loadFlow);

                // wrong direction, negate the increment
                if (getI(phaseShifter) < i) {
                    // we don't go in the right direction
                    tapPosInc *= -1;
                }
            }

            if (i < limit) {
                // we reached the maximal (ou minimal) tap and phase shifter is not overloaded
                optimalTap = phaseShifter.getPhaseTapChanger().getTapPosition();
            } else {
                // with the last tap, phase shifter is overloaded, in that case we take the previous tap as the optimmal one
                optimalTap = phaseShifter.getPhaseTapChanger().getTapPosition() - tapPosInc;
                phaseShifter.getPhaseTapChanger().setTapPosition(optimalTap);

                // just to be sure, check that with the previous tap, phase shifter is not overloaded...
                runLoadFlow(loadFlow);
                // check there phase shifter is not overloaded
                if (getI(phaseShifter) >= limit) {
                    throw new AssertionError("Phase shifter should not be overload");
                }
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
    }
}
