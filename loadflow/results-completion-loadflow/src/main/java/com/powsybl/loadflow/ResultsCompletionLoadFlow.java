/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ResultsCompletionLoadFlow implements LoadFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsCompletionLoadFlow.class);

    Network network;

    public ResultsCompletionLoadFlow(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    @Override
    public String getName() {
        return "Results Completion LoadFlow";
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public LoadFlowResult run() throws Exception {
        return run(LoadFlowParameters.load());
    }

    @Override
    public LoadFlowResult run(LoadFlowParameters parameters) throws Exception {
        LOGGER.info("Running {}", getName());

        LOGGER.info("LoadFlowParameters=" + parameters.toString());
        ResultsCompletionLoadFlowParametersExtension resultsCompletionLfParameters = parameters.getExtension(ResultsCompletionLoadFlowParametersExtension.class);
        LOGGER.info("ResultsCompletionLoadFlowParameters=" + resultsCompletionLfParameters.toString());

        network.getLineStream().forEach(line -> {
            BranchData lineData = new BranchData(line,
                                                 resultsCompletionLfParameters.getEpsilonX(),
                                                 resultsCompletionLfParameters.isApplyReactanceCorrection());
            completeTerminalData(line.getTerminal(Side.ONE), Side.ONE, lineData);
            completeTerminalData(line.getTerminal(Side.TWO), Side.TWO, lineData);
        });

        network.getTwoWindingsTransformerStream().forEach(twt -> {
            BranchData twtData = new BranchData(twt,
                                                resultsCompletionLfParameters.getEpsilonX(),
                                                resultsCompletionLfParameters.isApplyReactanceCorrection(),
                                                parameters.isSpecificCompatibility());
            completeTerminalData(twt.getTerminal(Side.ONE), Side.ONE, twtData);
            completeTerminalData(twt.getTerminal(Side.TWO), Side.TWO, twtData);
        });

        return new LoadFlowResult() {

            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public Map<String, String> getMetrics() {
                return Collections.emptyMap();
            }

            @Override
            public String getLogs() {
                return "";
            }
        };
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

    @Override
    public CompletableFuture<LoadFlowResult> runAsync(String workingStateId, LoadFlowParameters parameters) {
        try {
            return CompletableFuture.completedFuture(run(parameters));
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
    }

}
