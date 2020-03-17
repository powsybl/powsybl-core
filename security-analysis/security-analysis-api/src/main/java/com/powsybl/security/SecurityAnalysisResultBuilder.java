/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.interceptors.RunningContext;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;

import java.util.*;

/**
 *
 * Facilitates the creation of security analysis results, in particular
 * for subclasses of {@link AbstractSecurityAnalysis}.
 *
 * Encapsulates filtering of limit violations with a provided {@link LimitViolationFilter},
 * as well as notifications to {@link SecurityAnalysisInterceptor}s.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisResultBuilder extends
        AbstractBaseSecurityAnalysisResultBuilder<SecurityAnalysisResultBuilder.PreContingencyResultBuilder, SecurityAnalysisResultBuilder.PostContingencyResultBuilder> {

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context,
            Collection<SecurityAnalysisInterceptor> interceptors) {
        super(filter, context, interceptors);
    }

    public SecurityAnalysisResultBuilder(LimitViolationFilter filter, RunningContext context) {
        super(filter, context);
    }

    public PreContingencyResultBuilder preContingency() {
        return new PreContingencyResultBuilder();
    }

    public PostContingencyResultBuilder contingency(Contingency contingency) {
        return new PostContingencyResultBuilder(contingency);
    }

    public class PreContingencyResultBuilder extends AbstractBaseSecurityAnalysisResultBuilder<
                SecurityAnalysisResultBuilder.PreContingencyResultBuilder,
                SecurityAnalysisResultBuilder.PostContingencyResultBuilder
            >.BasePreContingencyResultBuilder<PreContingencyResultBuilder> {
        protected PreContingencyResultBuilder() {
            super();
        }
    }

    public class PostContingencyResultBuilder extends AbstractBaseSecurityAnalysisResultBuilder<
                SecurityAnalysisResultBuilder.PreContingencyResultBuilder,
                SecurityAnalysisResultBuilder.PostContingencyResultBuilder
            >.BasePostContingencyResultBuilder<PostContingencyResultBuilder> {
        protected PostContingencyResultBuilder(Contingency contingency) {
            super(contingency);
        }
    }
}
