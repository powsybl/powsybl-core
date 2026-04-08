/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.loadflow.LoadFlowResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yichen Tang {@literal <yichen.tang at rte-france.com>}
 */
public final class SecurityAnalysisResultMerger {

    private static final LimitViolationsResult FAILED_N_STATE_RESULT = new LimitViolationsResult(Collections.emptyList());
    public static final SecurityAnalysisResult FAILED_SECURITY_ANALYSIS_RESULT = new SecurityAnalysisResult(FAILED_N_STATE_RESULT,
            LoadFlowResult.ComponentResult.Status.FAILED, Collections.emptyList());

    public static SecurityAnalysisResult merge(SecurityAnalysisResult[] results) {
        //If one of the subtasks has failed, return a failed result
        Objects.requireNonNull(results);
        for (SecurityAnalysisResult subResult : results) {
            if (subResult.getPreContingencyResult().getStatus() != LoadFlowResult.ComponentResult.Status.CONVERGED) {
                return FAILED_SECURITY_ANALYSIS_RESULT;
            }
        }
        return new SecurityAnalysisResult(results[0].getPreContingencyLimitViolationsResult(), results[0].getPreContingencyResult().getStatus(),
                Arrays.stream(results).flatMap(result -> result.getPostContingencyResults().stream()).collect(Collectors.toList()))
                .setNetworkMetadata(results[0].getNetworkMetadata());

    }

    public static SecurityAnalysisResult merge(Collection<SecurityAnalysisResult> results) {
        Objects.requireNonNull(results);
        return merge(results.toArray(new SecurityAnalysisResult[0]));
    }

    private SecurityAnalysisResultMerger() {
    }
}
