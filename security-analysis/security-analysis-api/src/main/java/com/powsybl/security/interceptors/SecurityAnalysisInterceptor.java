/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface SecurityAnalysisInterceptor {

    /**
     * @Deprected.
     * Callback after the pre-contingency analysis result is created
     * @param context The running context
     * @param preContingencyResult
     */
    @Deprecated
    void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult);

    /**
     * @Deprecated. Use {@link #onPostContingencyResult(PostContingencyResult, SecurityAnalysisResultContext)}
     * @param context
     * @param postContingencyResult
     */
    @Deprecated
    default void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
        onPostContingencyResult(postContingencyResult, context);
    }

    /**
     * Callback after the post-contingency result is created
     * @param context
     * @param postContingencyResult
     */
    default void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        onPostContingencyResult(new RunningContext(context.getNetwork(), context.getInitialStateId()), postContingencyResult);
    }

    /**
     * Callback after the result is created
     * @param context
     * @param result
     */
    void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result);

    /**
     * Callback after a limit violation accepted by {@link com.powsybl.security.LimitViolationFilter}
     * @param context a violation context
     * @param limitViolation the limit violation; never {@literal null}
     */
    default void onLimitViolation(ViolationContext context, LimitViolation limitViolation) {

    }

    void onPreContingencyResult(LimitViolationsResult preContingencyResult, SecurityAnalysisResultContext context);

    void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context);

    void onLimitViolation(LimitViolation limitViolation, SecurityAnalysisResultContext context);

    void onLimitViolation(Contingency contingency, LimitViolation limitViolation, SecurityAnalysisResultContext context);

}
