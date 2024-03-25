/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.results.*;
import com.powsybl.security.results.OperatorStrategyResult;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SecurityAnalysisResult extends AbstractExtendable<SecurityAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<PostContingencyResult> postContingencyResults;

    private final PreContingencyResult preContingencyResult;

    private final List<OperatorStrategyResult> operatorStrategyResults;

    public static SecurityAnalysisResult empty() {
        return new SecurityAnalysisResult(new PreContingencyResult(LoadFlowResult.ComponentResult.Status.CONVERGED, LimitViolationsResult.empty(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList()),
                Collections.emptyList(), Collections.emptyList());
    }

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  LoadFlowResult.ComponentResult.Status preContingencyStatus,
                                  List<PostContingencyResult> postContingencyResults) {
        this(new PreContingencyResult(preContingencyStatus, preContingencyResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                postContingencyResults, Collections.emptyList());
    }

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  LoadFlowResult.ComponentResult.Status preContingencyStatus,
                                  List<PostContingencyResult> postContingencyResults,
                                  List<BranchResult> preContingencyBranchResults,
                                  List<BusResult> preContingencyBusResults,
                                  List<ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults,
                                  List<OperatorStrategyResult> operatorStrategyResults) {
        this(new PreContingencyResult(preContingencyStatus, preContingencyResult, preContingencyBranchResults,
                        preContingencyBusResults,
                        preContingencyThreeWindingsTransformerResults),
                postContingencyResults, operatorStrategyResults);
    }

    public SecurityAnalysisResult(PreContingencyResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults,
                                  List<OperatorStrategyResult> operatorStrategyResults) {
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = ImmutableList.copyOf(Objects.requireNonNull(postContingencyResults));
        this.operatorStrategyResults = ImmutableList.copyOf(Objects.requireNonNull(operatorStrategyResults));
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public SecurityAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

    public LimitViolationsResult getPreContingencyLimitViolationsResult() {
        return preContingencyResult.getLimitViolationsResult();
    }

    public List<PostContingencyResult> getPostContingencyResults() {
        return postContingencyResults;
    }

    public PreContingencyResult getPreContingencyResult() {
        return preContingencyResult;
    }

    public List<OperatorStrategyResult> getOperatorStrategyResults() {
        return operatorStrategyResults;
    }

}
