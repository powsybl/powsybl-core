/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;
import com.powsybl.loadflow.validation.CandidateComputation;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(CandidateComputation.class)
public class LoadFlowResultsCompletion  implements CandidateComputation {

    public static final String NAME = "loadflowResultsCompletion";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowResultsCompletion.class);

    private final LoadFlowResultsCompletionParameters parameters;
    private final LoadFlowParameters lfParameters;

    public LoadFlowResultsCompletion(LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this.parameters = Objects.requireNonNull(parameters);
        this.lfParameters = Objects.requireNonNull(lfParameters);
    }

    public LoadFlowResultsCompletion() {
        this(LoadFlowResultsCompletionParameters.load(), LoadFlowParameters.load());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        LOGGER.info("Running {} on network {}, state {}", getName(), network.getId(), network.getStateManager().getWorkingStateId());
        LOGGER.info("LoadFlowResultsCompletionParameters={}", parameters);
        LOGGER.info("LoadFlowParameters={}", lfParameters);

        network.getLineStream().forEach(line -> {
            BranchData lineData = new BranchData(line,
                                                 parameters.getEpsilonX(),
                                                 parameters.isApplyReactanceCorrection());
            completeTerminalData(line.getTerminal(Side.ONE), Side.ONE, lineData);
            completeTerminalData(line.getTerminal(Side.TWO), Side.TWO, lineData);
        });

        network.getTwoWindingsTransformerStream().forEach(twt -> {
            BranchData twtData = new BranchData(twt,
                                                parameters.getEpsilonX(),
                                                parameters.isApplyReactanceCorrection(),
                                                lfParameters.isSpecificCompatibility());
            completeTerminalData(twt.getTerminal(Side.ONE), Side.ONE, twtData);
            completeTerminalData(twt.getTerminal(Side.TWO), Side.TWO, twtData);
        });

    }

    private void completeTerminalData(Terminal terminal, Side side, BranchData branchData) {
        if (terminal.isConnected() && terminal.getBusView().getBus().isInMainConnectedComponent()) {
            if (Float.isNaN(terminal.getP())) {
                LOGGER.debug("Branch {}, Side {}: setting p = {}", branchData.getId(), side, branchData.getComputedP(side));
                terminal.setP((float) branchData.getComputedP(side));
            }
            if (Float.isNaN(terminal.getQ())) {
                LOGGER.debug("Branch {}, Side {}: setting q = {}", branchData.getId(), side, branchData.getComputedQ(side));
                terminal.setQ((float) branchData.getComputedQ(side));
            }
        }
    }

}
