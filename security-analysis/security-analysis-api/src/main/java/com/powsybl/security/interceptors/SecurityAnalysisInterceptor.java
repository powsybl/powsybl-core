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
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface SecurityAnalysisInterceptor {

    void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult);

    void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult);

    void onSecurityAnalysisResult(RunningContext context, SecurityAnalysisResult result);
}
