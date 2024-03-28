/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;

import java.util.*;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class PreContingencyResult extends AbstractContingencyResult {

    private final LoadFlowResult.ComponentResult.Status status;

    public PreContingencyResult() {
        this(LoadFlowResult.ComponentResult.Status.CONVERGED, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status, LimitViolationsResult limitViolationsResult, Collection<BranchResult> branchResults,
                                Collection<BusResult> busResults,
                                Collection<ThreeWindingsTransformerResult> threeWindingsTransformerResults) {
        this(status, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults));
    }

    public PreContingencyResult(LoadFlowResult.ComponentResult.Status status, LimitViolationsResult limitViolationsResult, NetworkResult networkResult) {
        super(limitViolationsResult, networkResult);
        this.status = Objects.requireNonNull(status);
    }

    public LoadFlowResult.ComponentResult.Status getStatus() {
        return status;
    }
}
