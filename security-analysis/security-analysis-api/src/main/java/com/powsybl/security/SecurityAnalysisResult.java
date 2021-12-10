/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.security.results.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisResult extends AbstractExtendable<SecurityAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final List<PostContingencyResult> postContingencyResults;

    private final PreContingencyResult preContingencyResult;

    public static SecurityAnalysisResult empty() {
        return new SecurityAnalysisResult(LimitViolationsResult.empty(), Collections.emptyList());
    }

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults) {
        this(new PreContingencyResult(preContingencyResult, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), postContingencyResults);
    }

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults,
                                  List<BranchResult> preContingencyBranchResults,
                                  List<BusResults> preContingencyBusResults,
                                  List<ThreeWindingsTransformerResult> preContingencyThreeWindingsTransformerResults) {
        this(new PreContingencyResult(preContingencyResult, preContingencyBranchResults,
                preContingencyBusResults,
                preContingencyThreeWindingsTransformerResults),
            postContingencyResults);
    }

    public SecurityAnalysisResult(PreContingencyResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults) {
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = Objects.requireNonNull(postContingencyResults);
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
}
