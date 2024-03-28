/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian@ at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class PostContingencyResult extends AbstractContingencyResult {

    private final Contingency contingency;

    private final PostContingencyComputationStatus status;

    private final ConnectivityResult connectivityResult;

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult) {
        this(contingency, status, limitViolationsResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new ConnectivityResult(0, 0, 0.0, 0.0, Collections.emptySet()));
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult, List<BranchResult> branchResults,
                                 List<BusResult> busResults, List<ThreeWindingsTransformerResult> threeWindingsTransformerResults, ConnectivityResult connectivityResult) {
        this(contingency, status, limitViolationsResult, new NetworkResult(branchResults, busResults, threeWindingsTransformerResults), connectivityResult);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, LimitViolationsResult limitViolationsResult, NetworkResult networkResult, ConnectivityResult connectivityResult) {
        super(limitViolationsResult, networkResult);
        this.contingency = Objects.requireNonNull(contingency);
        this.status = Objects.requireNonNull(status);
        this.connectivityResult = Objects.requireNonNull(connectivityResult);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations,
                                 List<BranchResult> branchResults, List<BusResult> busResults,
                                 List<ThreeWindingsTransformerResult> threeWindingsTransformerResults,
                                 ConnectivityResult connectivityResult) {
        this(contingency, status, new LimitViolationsResult(limitViolations, Collections.emptyList()), branchResults, busResults, threeWindingsTransformerResults, connectivityResult);
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations) {
        this(contingency, status, new LimitViolationsResult(limitViolations, Collections.emptyList()));
    }

    public PostContingencyResult(Contingency contingency, PostContingencyComputationStatus status, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this(contingency, status, new LimitViolationsResult(limitViolations, actionsTaken));
    }

    public Contingency getContingency() {
        return contingency;
    }

    public PostContingencyComputationStatus getStatus() {
        return status;
    }

    public ConnectivityResult getConnectivityResult() {
        return connectivityResult;
    }
}
