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
 *
 * Security analysis interceptors are notified at various steps of the construction of
 * the {@link SecurityAnalysisResult}, for instance when adding new {@link LimitViolation limit violations}.
 * This mechanism allows for example users of the {@link com.powsybl.security.SecurityAnalysis} to add
 * additional information in the result, as {@link com.powsybl.commons.extensions.Extension extensions}.
 *
 * <p>Some of this information can be retrieved from the provided {@link SecurityAnalysisResultContext}.
 * Implementations of the security analysis can provide implementation-specific information by providing
 * their own implementation of this context.
 *
 * <p>Note that the contexts provided to the various methods can be different objects, in order
 * to provide more specific information, for example for violations or contingencies.
 * This can also be helpful to guarantee thread safety, if the result builder is used concurrently.
 *
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface SecurityAnalysisInterceptor {

    /**
     * @deprecated Use {@link #onPreContingencyResult(LimitViolationsResult, SecurityAnalysisResultContext)}
     * Callback after the pre-contingency analysis result is created
     * @param context The running context
     * @param preContingencyResult
     */
    @Deprecated
    default void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult) {
        onPreContingencyResult(preContingencyResult, context);
    }

    /**
     * @deprecated Use {@link #onPostContingencyResult(PostContingencyResult, SecurityAnalysisResultContext)}
     * @param context
     * @param postContingencyResult
     */
    @Deprecated
    default void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
        onPostContingencyResult(postContingencyResult, context);
    }

    /**
     * @deprecated Use {@link #onSecurityAnalysisResult(SecurityAnalysisResult, SecurityAnalysisResultContext)}
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
        if (context instanceof RunningContext) {
            onPreContingencyResult((RunningContext) context, preContingencyResult);
        }
    }

    /**
     * Callback after the post-contingency result is built.
     * @param context
     * @param postContingencyResult
     */
    default void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        if (context instanceof RunningContext) {
            onPostContingencyResult((RunningContext) context, postContingencyResult);
        }
    }

    /**
     * Callback after the security-analysis result is built.
     * @param result
     * @param context
     */
    default void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context) {
        if (context instanceof RunningContext) {
            onSecurityAnalysisResult((RunningContext) context, result);
        }
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
