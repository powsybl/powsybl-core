/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.util.Arrays;
import java.util.Collections;

public final class SecurityAnalysisResultMerger {

    public static SecurityAnalysisResult merge(SecurityAnalysisResult[] results) {
        //If one of the subtasks has failed, return a failed result
        for (SecurityAnalysisResult subResult : results) {
            if (!subResult.getPreContingencyResult().isComputationOk()) {
                return computationFailed();
            }
        }

        //Else, actually merge results
        final SecurityAnalysisResult res = results[0];
        if (results.length > 1) {
            Arrays.stream(results, 1, results.length).forEach(r -> res.getPostContingencyResults().addAll(r.getPostContingencyResults()));
        }
        return res;
    }

    private static LimitViolationsResult nStateFailed() {
        return new LimitViolationsResult(false, Collections.emptyList());
    }

    private static SecurityAnalysisResult computationFailed() {
        return new SecurityAnalysisResult(nStateFailed(), Collections.emptyList());
    }

    private SecurityAnalysisResultMerger() {
    }
}
