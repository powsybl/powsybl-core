package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.strategy.OperatorStrategy;

public interface SecurityAnalysisResultHandler {

    void writeBranchResult(Contingency contingency, OperatorStrategy operatorStrategy, BranchResult branchResult);

    void writeThreeWindingsTransformerResult(Contingency contingency, OperatorStrategy operatorStrategy, ThreeWindingsTransformerResult branchResult);

    void writeBusResult(Contingency contingency, OperatorStrategy operatorStrategy, BusResult busResult);

}
