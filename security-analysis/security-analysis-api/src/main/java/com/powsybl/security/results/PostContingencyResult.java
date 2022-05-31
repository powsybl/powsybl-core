/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.google.common.collect.ImmutableMap;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@ at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PostContingencyResult {

    private final Contingency contingency;

    private final LimitViolationsResult limitViolationsResult;

    private final Map<String, BranchResult> branchResults;

    private final Map<String, BusResult> busResults;

    private final Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults;

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult) {
        this(contingency, limitViolationsResult, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult, List<BranchResult> branchResults, List<BusResult> busResults, List<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(contingency, limitViolationsResult,
            branchResults.stream().collect(Collectors.toMap(BranchResult::getBranchId, Function.identity())),
            busResults.stream().collect(Collectors.toMap(BusResult::getBusId, Function.identity())),
            threeWindingsTransformerResults.stream().collect(Collectors.toMap(ThreeWindingsTransformerResult::getThreeWindingsTransformerId, Function.identity())));
    }

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult, Map<String, BranchResult> branchResults, Map<String, BusResult> busResults, Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this.contingency = Objects.requireNonNull(contingency);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
        this.branchResults = ImmutableMap.copyOf(Objects.requireNonNull(branchResults));
        this.busResults = ImmutableMap.copyOf(Objects.requireNonNull(busResults));
        this.threeWindingsTransformerResults = ImmutableMap.copyOf(Objects.requireNonNull(threeWindingsTransformerResults));
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations,
                                 Map<String, BranchResult> branchResults, Map<String, BusResult> busResults, Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, Collections.emptyList()), branchResults, busResults, threeWindingsTransformerResults);
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, Collections.emptyList()));
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, actionsTaken));
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations,
                                 List<String> actionsTaken, Map<String, BranchResult> branchResults, Map<String, BusResult> busResults, Map<String, ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, actionsTaken), branchResults, busResults, threeWindingsTransformerResults);
    }

    public Contingency getContingency() {
        return contingency;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public BranchResult getBranchResult(String id) {
        Objects.requireNonNull(id);
        return branchResults.get(id);
    }

    public List<BranchResult> getBranchResults() {
        return List.copyOf(branchResults.values());
    }

    public BusResult getBusResult(String id) {
        Objects.requireNonNull(id);
        return busResults.get(id);
    }

    public List<BusResult> getBusResults() {
        return List.copyOf(busResults.values());
    }

    public ThreeWindingsTransformerResult getThreeWindingsTransformerResult(String id) {
        Objects.requireNonNull(id);
        return threeWindingsTransformerResults.get(id);
    }

    public List<ThreeWindingsTransformerResult> getThreeWindingsTransformerResult() {
        return List.copyOf(threeWindingsTransformerResults.values());
    }
}
