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
        this(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PreContingencyResult(LimitViolationsResult preContingencyResult, Collection<BranchResult> preContingencyBranchResults,
                                Collection<BusResults> preContingencyBusResults,
                                Collection<ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults) {
        this.limitViolationsResult = preContingencyResult;
        Objects.requireNonNull(preContingencyBranchResults).forEach(r -> this.preContingencyBranchResults.put(r.getBranchId(), r));
        Objects.requireNonNull(preContingencyBusResults).forEach(r -> this.preContingencyBusResults.put(r.getBusId(), r));
        Objects.requireNonNull(preContingencyThreeWindingsTransformerResults).forEach(r -> this.preContingencyThreeWindingsTransformerResults.put(r.getThreeWindingsTransformerId(), r));
    }

    public void setLimitViolationsResult(LimitViolationsResult limitViolationsResult) {
        this.limitViolationsResult = limitViolationsResult;
    }

    public void addPreContingencyBranchResults(Collection<BranchResult> branchResults) {
        Objects.requireNonNull(branchResults).forEach(r -> this.preContingencyBranchResults.put(r.getBranchId(), r));
    }

    public void addPreContingencyBusResults(Collection<BusResults> busResults) {
        Objects.requireNonNull(busResults).forEach(r -> this.preContingencyBusResults.put(r.getBusId(), r));
    }

    public void addPreContingencyThreeWindingsTransformerResults(Collection<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        Objects.requireNonNull(threeWindingsTransformerResults).forEach(r -> this.preContingencyThreeWindingsTransformerResults.put(r.getThreeWindingsTransformerId(), r));
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public List<BusResults> getPreContingencyBusResults() {
        return List.copyOf(preContingencyBusResults.values());
    }

    public BusResults getPreContingencyBusResult(String id) {
        return preContingencyBusResults.get(Objects.requireNonNull(id));
    }

    public List<BranchResult> getPreContingencyBranchResults() {
        return List.copyOf(preContingencyBranchResults.values());
    }

    public BranchResult getPreContingencyBranchResult(String id) {
        return preContingencyBranchResults.get(Objects.requireNonNull(id));
    }

    public List<ThreeWindingsTransformerResult> getPreContingencyThreeWindingsTransformerResults() {
        return List.copyOf(preContingencyThreeWindingsTransformerResults.values());
    }

    public ThreeWindingsTransformerResult getPreContingencyThreeWindingsTransformerResult(String id) {
        return preContingencyThreeWindingsTransformerResults.get(Objects.requireNonNull(id));
    }
}
