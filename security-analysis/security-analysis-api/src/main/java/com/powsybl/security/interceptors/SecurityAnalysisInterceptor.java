/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.SecurityAnalysisResult;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface SecurityAnalysisInterceptor {

    /**
     * Callback after the pre-contingency analysis result is created
     * @param context The running context
     * @param preContingencyResult
     */
    void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult);

    /**
     * Deprecated. Use {@link #onPostContingencyResult(ContingencyContext context, PostContingencyResult postContingencyResult)}
     * @param context
     * @param postContingencyResult
     */
    void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult);

    /**
     * Callback after the post-contingency result is created
     * @param context
     * @param postContingencyResult
     */
    default void onPostContingencyResult(ContingencyContext context, PostContingencyResult postContingencyResult) {
        onPostContingencyResult(context.getRunningContext(), postContingencyResult);
    }

    /**
     * Callback after the result is created
     * @param context
     * @param result
     */
    void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result);

    /**
     * Callback after a limit violation detected
     * @param context a violation context
     */
    default void onLimitViolation(ViolationContext context) {

    }

}
