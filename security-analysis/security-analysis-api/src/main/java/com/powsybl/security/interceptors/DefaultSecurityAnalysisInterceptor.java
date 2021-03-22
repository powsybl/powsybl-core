/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
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
 * Default implementation of the SecurityAnalysisInterceptor interface.
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultSecurityAnalysisInterceptor implements SecurityAnalysisInterceptor {

    @Override
    public void onPreContingencyResult(LimitViolationsResult preContingencyResult, SecurityAnalysisResultContext context) {
        // nothing to do
    }

    @Override
    public void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        // nothing to do
    }

    @Override
    public void onSecurityAnalysisResult(SecurityAnalysisResult result, SecurityAnalysisResultContext context) {
        // nothing to do
    }

    @Override
    public void onLimitViolation(LimitViolation limitViolation, SecurityAnalysisResultContext context) {
        // nothing to do
    }

    @Override
    public void onLimitViolation(Contingency contingency, LimitViolation limitViolation, SecurityAnalysisResultContext context) {
        // nothing to do
    }

}
