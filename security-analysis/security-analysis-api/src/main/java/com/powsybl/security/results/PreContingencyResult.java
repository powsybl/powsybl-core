/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.results;

import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;

import java.util.*;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class PreContingencyResult {
    private final LoadFlowResult.ComponentResult.Status status;
    private final LimitViolationsResult limitViolationsResult;
    private final NetworkResult networkResult;

    public PreContingencyResult() {
        this(LoadFlowResult.ComponentResult.Status.CONVERGED, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status, LimitViolationsResult limitViolationsResult, Collection<BranchResult> branchResults,
                                Collection<BusResult> busResults,
                                Collection<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(status, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status, LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.status = Objects.requireNonNull(status);
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
    }

    public LoadFlowResult.ComponentResult.Status getStatus() {
        return status;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }
}
