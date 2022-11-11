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
    private final LimitViolationsResult limitViolationsResult;
    private final NetworkResult networkResult;

    public PreContingencyResult() {
        this(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PreContingencyResult(LimitViolationsResult limitViolationsResult, Collection<BranchResult> branchResults,
                                Collection<BusResult> busResults,
                                Collection<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
    }

    public PreContingencyResult(LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        this.limitViolationsResult = limitViolationsResult;
        this.networkResult = Objects.requireNonNull(networkResult);
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public NetworkResult getNetworkResult() {
        return networkResult;
    }
}
