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

    private final LimitViolationsResult limitViolationsResult;

    private final NetworkResult networkResult;

    private final PostContingencyComputationStatus status;

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, LimitViolationsResult limitViolationsResult) {
        this(status, contingency, limitViolationsResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, LimitViolationsResult limitViolationsResult, List<BranchResult> branchResults,
                                 List<BusResult> busResults, List<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(status, contingency, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
    }

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.contingency = Objects.requireNonNull(contingency);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
        this.networkResult = Objects.requireNonNull(networkResult);
        this.status = Objects.requireNonNull(status);
    }

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, List<LimitViolation> limitViolations,
                                 List<BranchResult> branchResults, List<BusResult> busResults,
                                 List<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(status, contingency, new LimitViolationsResult(limitViolations, Collections.emptyList()), branchResults, busResults, threeWindingsTransformerResults);
    }

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, List<LimitViolation> limitViolations) {
        this(status, contingency, new LimitViolationsResult(limitViolations, Collections.emptyList()));
    }

    public PostContingencyResult(PostContingencyComputationStatus status, Contingency contingency, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this(status, contingency, new LimitViolationsResult(limitViolations, actionsTaken));
    }

    public Contingency getContingency() {
        return contingency;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }

    public PostContingencyComputationStatus getStatus() {
        return status;
    }
}
