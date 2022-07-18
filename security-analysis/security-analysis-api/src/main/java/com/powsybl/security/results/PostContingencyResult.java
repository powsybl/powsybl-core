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

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@ at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PostContingencyResult {

    private final Contingency contingency;

    private final LimitViolationsResult limitViolationsResult;

    private final NetworkResult networkResult;

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult) {
        this(contingency, limitViolationsResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult, List<BranchResult> branchResults, List<BusResult> busResults, List<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(contingency, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
    }

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.contingency = Objects.requireNonNull(contingency);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
        this.networkResult = Objects.requireNonNull(networkResult);
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations,
                                 List<BranchResult> branchResults, List<BusResult> busResults,
                                 List<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, Collections.emptyList()), branchResults, busResults, threeWindingsTransformerResults);
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, Collections.emptyList()));
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, actionsTaken));
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
}
