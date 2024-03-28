/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.interceptors;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.results.PreContingencyResult;

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
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface SecurityAnalysisInterceptor {

    /**
     * Callback after the pre-contingency result is built.
     * @param preContingencyResult
     * @param context
     */
    void onPreContingencyResult(PreContingencyResult preContingencyResult, SecurityAnalysisResultContext context);

    /**
     * Callback after the post-contingency result is built.
     * @param context
     * @param postContingencyResult
     */
    void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context);

    /**
     * Callback after the security-analysis result is built.
     * @param result
     * @param context
     */
    void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context);

    /**
     * Callback when a violation is detected on N situation.
     * @param limitViolation
     * @param context
     */
    void onLimitViolation(LimitViolation limitViolation, SecurityAnalysisResultContext context);

    /**
     * Callback when a violation is detected on N-1 situation.
     * @param contingency
     * @param limitViolation
     * @param context
     */
    void onLimitViolation(Contingency contingency, LimitViolation limitViolation, SecurityAnalysisResultContext context);
}
