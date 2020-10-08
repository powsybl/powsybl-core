/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.util.*;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public final class SecurityAnalysisResultMerger {

    private static final LimitViolationsResult FAILED_N_STATE_RESULT = new LimitViolationsResult(false, Collections.emptyList());
    public static final SecurityAnalysisResult FAILED_SECURITY_ANALYSIS_RESULT = new SecurityAnalysisResult(FAILED_N_STATE_RESULT, Collections.emptyList());

    public static SecurityAnalysisResult merge(SecurityAnalysisResult[] results) {
        //If one of the subtasks has failed, return a failed result
        Objects.requireNonNull(results);
        for (SecurityAnalysisResult subResult : results) {
            if (!subResult.getPreContingencyResult().isComputationOk()) {
                return FAILED_SECURITY_ANALYSIS_RESULT;
            }
        }

        //Else, actually merge results
        final SecurityAnalysisResult res = new SecurityAnalysisResult(
                                results[0].getPreContingencyResult(), new ArrayList<>(results[0].getPostContingencyResults()))
                                .setNetworkMetadata(results[0].getNetworkMetadata());
        if (results.length > 1) {
            Arrays.stream(results, 1, results.length).forEach(r -> res.getPostContingencyResults().addAll(r.getPostContingencyResults()));
        }
        return res;
    }

    public static SecurityAnalysisResult merge(Collection<SecurityAnalysisResult> results) {
        Objects.requireNonNull(results);
        return merge(results.toArray(new SecurityAnalysisResult[results.size()]));
    }

    private SecurityAnalysisResultMerger() {
    }
}
