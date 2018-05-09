package com.powsybl.loadflow;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

public class ResultsCompletionLoadFlowFactory implements LoadFlowFactory {

    @Override
    public LoadFlow create(Network network, ComputationManager computationManager, int priority) {
        return new ResultsCompletionLoadFlow(network);
    }

}
