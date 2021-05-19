/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.powsybl.security.LimitViolationsResult;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PreContingencyResult {
    private LimitViolationsResult limitViolationsResult;
    private final Map<String, BranchResult> preContingencyBranchResults = new HashMap<>();
    private final Map<String, BusResults> preContingencyBusResults = new HashMap<>();
    private final Map<String, ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults = new HashMap<>();

    public PreContingencyResult() {
        this(null, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    public PreContingencyResult(LimitViolationsResult preContingencyResult, Map<String, BranchResult> preContingencyBranchResults,
                                Map<String, BusResults> preContingencyBusResults,
                                Map<String, ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults) {
        this.limitViolationsResult = preContingencyResult;
        this.preContingencyBranchResults.putAll(Objects.requireNonNull(preContingencyBranchResults));
        this.preContingencyBusResults.putAll(Objects.requireNonNull(preContingencyBusResults));
        this.preContingencyThreeWindingsTransformerResults.putAll(Objects.requireNonNull(preContingencyThreeWindingsTransformerResults));
    }

    public void setLimitViolationsResult(LimitViolationsResult limitViolationsResult) {
        this.limitViolationsResult = limitViolationsResult;
    }

    public void addPreContingencyBranchResults(Map<String, BranchResult> branchResults) {
        this.preContingencyBranchResults.putAll(Objects.requireNonNull(branchResults));
    }

    public void addPreContingencyBusResults(Map<String, BusResults> busResults) {
        this.preContingencyBusResults.putAll(Objects.requireNonNull(busResults));
    }

    public void addPreContingencyThreeWindingsTransformerResults(Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this.preContingencyThreeWindingsTransformerResults.putAll(threeWindingsTransformerResults);
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public List<BusResults> getPreContingencyBusResults() {
        return List.copyOf(preContingencyBusResults.values());
    }

    public List<BranchResult> getPreContingencyBranchResults() {
        return List.copyOf(preContingencyBranchResults.values());
    }

    public List<ThreeWindingsTransformerResult> getPreContingencyThreeWindingsTransformerResults() {
        return List.copyOf(preContingencyThreeWindingsTransformerResults.values());
    }
}
