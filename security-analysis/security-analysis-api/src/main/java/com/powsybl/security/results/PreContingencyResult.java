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
    private NetworkResult preContingencyNetworkResult;

    public PreContingencyResult() {
        this(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PreContingencyResult(LimitViolationsResult preContingencyResult, Collection<BranchResult> preContingencyBranchResults,
                                Collection<BusResult> preContingencyBusResults,
                                Collection<ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults) {
        this(preContingencyResult, new NetworkResult(preContingencyBranchResults, preContingencyBusResults, preContingencyThreeWindingsTransformerResults));
    }

    public PreContingencyResult(LimitViolationsResult preContingencyResult, NetworkResult preContingencyNetworkResult) {
        this.limitViolationsResult = preContingencyResult;
        this.preContingencyNetworkResult = Objects.requireNonNull(preContingencyNetworkResult);
    }

    public void setLimitViolationsResult(LimitViolationsResult limitViolationsResult) {
        this.limitViolationsResult = limitViolationsResult;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }

    public void setPreContingencyNetworkResult(NetworkResult networkResult) {
        this.preContingencyNetworkResult = networkResult;
    }

    public NetworkResult getPreContingencyNetworkResult() {
        return preContingencyNetworkResult;
    }
}
