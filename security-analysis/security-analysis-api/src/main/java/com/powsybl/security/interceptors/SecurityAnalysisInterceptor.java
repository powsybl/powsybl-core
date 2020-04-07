/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface SecurityAnalysisInterceptor {

    /**
     * @Deprected. Use {@link #onPreContingencyResult(LimitViolationsResult, SecurityAnalysisResultContext)}
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
     * @Deprecated. Use {@link #onSecurityAnalysisResult(SecurityAnalysisResult, SecurityAnalysisResultContext)}
     * Callback after the result is created
     * @param context
     * @param result
     */
    @Deprecated
    default void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result) {
        onSecurityAnalysisResult(result, context);
    }

    /**
     * Callback after the pre-contingency result is built.
     * @param preContingencyResult
     * @param context
     */
    default void onPreContingencyResult(LimitViolationsResult preContingencyResult, SecurityAnalysisResultContext context) {
        onPreContingencyResult(new RunningContext(context.getNetwork(), VariantManagerConstants.INITIAL_VARIANT_ID), preContingencyResult);
    }

    /**
     * Callback after the post-contingency result is built.
     * @param context
     * @param postContingencyResult
     */
    default void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        onPostContingencyResult(new RunningContext(context.getNetwork(), VariantManagerConstants.INITIAL_VARIANT_ID), postContingencyResult);
    }

    /**
     * Callback after the security-analysis result is built.
     * @param result
     * @param context
     */
    default void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context) {
        onSecurityAnalysisResult(new RunningContext(context.getNetwork(), VariantManagerConstants.INITIAL_VARIANT_ID), result);
    }

    /**
     * Callback when a violation is detected on N situation.
     * @param limitViolation
     * @param context
     */
    default void onLimitViolation(LimitViolation limitViolation, SecurityAnalysisResultContext context) {

    }

    /**
     * Callback when a violation is detected on N-1 situation.
     * @param contingency
     * @param limitViolation
     * @param context
     */
    default void onLimitViolation(Contingency contingency, LimitViolation limitViolation, SecurityAnalysisResultContext context) {

    }

}
