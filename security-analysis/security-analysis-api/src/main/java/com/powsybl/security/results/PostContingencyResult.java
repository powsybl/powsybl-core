/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@ at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PostContingencyResult {

    private final Contingency contingency;

    private final PostContingencyComputationStatus status;

    private final LimitViolationsResult limitViolationsResult;

    private final NetworkResult networkResult;

    private final int createdComponentCount;

    private final double lossOfLoad;

    private final double lossOfGeneration;

    private final Set<String> elementsLost;

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult) {
        this(contingency, status, limitViolationsResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 0, 0.0, 0.0, Collections.emptySet());
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this(contingency, status, limitViolationsResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), createdComponentCount, lossOfLoad, lossOfGeneration, elementsLost);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult, List<BranchResult> branchResults,
                                 List<BusResult> busResults, List<ThreeWindingsTransformerResult> threeWindingsTransformerResults, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this(contingency, status, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults), createdComponentCount, lossOfLoad, lossOfGeneration, elementsLost);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult, NetworkResult networkResult, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this.contingency = Objects.requireNonNull(contingency);
        this.status = Objects.requireNonNull(status);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
        this.networkResult = Objects.requireNonNull(networkResult);
        this.createdComponentCount = createdComponentCount;
        this.lossOfLoad = lossOfLoad;
        this.lossOfGeneration = lossOfGeneration;
        this.elementsLost = elementsLost;
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations,
                                 List<BranchResult> branchResults, List<BusResult> busResults,
                                 List<ThreeWindingsTransformerResult> threeWindingsTransformerResults, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this(contingency, status, new LimitViolationsResult(limitViolations, Collections.emptyList()), branchResults, busResults, threeWindingsTransformerResults, createdComponentCount, lossOfLoad, lossOfGeneration, elementsLost);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this(contingency, status, new LimitViolationsResult(limitViolations, Collections.emptyList()), createdComponentCount, lossOfLoad, lossOfGeneration, elementsLost);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations, List<String> actionsTaken, int createdComponentCount, double lossOfLoad, double lossOfGeneration, Set<String> elementsLost) {
        this(contingency, status, new LimitViolationsResult(limitViolations, actionsTaken), createdComponentCount, lossOfLoad, lossOfGeneration, elementsLost);
    }

    public Contingency getContingency() {
        return contingency;
    }

    public PostContingencyComputationStatus getStatus() {
        return status;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }

    public int getCreatedComponentCount() {
        return createdComponentCount;
    }

    public double getLossOfLoad() {
        return lossOfLoad;
    }

    public double getLossOfGeneration() {
        return lossOfGeneration;
    }

    public Set<String> getElementsLost() {
        return elementsLost;
    }
}
