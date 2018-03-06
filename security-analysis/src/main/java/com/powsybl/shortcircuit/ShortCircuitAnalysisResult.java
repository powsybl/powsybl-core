package com.powsybl.shortcircuit;

import java.util.List;

import com.powsybl.security.LimitViolation;

public class ShortCircuitAnalysisResult {
    private List<FaultResult> faultResults;
    private List<LimitViolation> limitViolations;

    public ShortCircuitAnalysisResult(List<FaultResult> faultResults, List<LimitViolation> limitViolations) {
        super();
        this.faultResults = faultResults;
        this.limitViolations = limitViolations;
    }

    public List<FaultResult> getFaultResults() {
        return faultResults;
    }

    public void setFaultResults(List<FaultResult> faultResults) {
        this.faultResults = faultResults;
    }

    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }

    public void setLimitViolations(List<LimitViolation> limitViolations) {
        this.limitViolations = limitViolations;
    }

}
