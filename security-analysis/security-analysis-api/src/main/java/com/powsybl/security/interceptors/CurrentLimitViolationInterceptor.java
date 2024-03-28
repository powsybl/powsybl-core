/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.results.PreContingencyResult;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class CurrentLimitViolationInterceptor extends DefaultSecurityAnalysisInterceptor {

    @Override
    public void onPreContingencyResult(PreContingencyResult preContingencyResult, SecurityAnalysisResultContext context) {
        for (LimitViolation limitViolation : preContingencyResult.getLimitViolationsResult().getLimitViolations()) {
            if (limitViolation.getLimitType() == LimitViolationType.CURRENT) {
                Branch branch = context.getNetwork().getBranch(limitViolation.getSubjectId());

                double preContingencyValue = branch.getTerminal(limitViolation.getSideAsTwoSides()).getP();
                limitViolation.addExtension(ActivePowerExtension.class, new ActivePowerExtension(preContingencyValue));
            }
        }
    }

    @Override
    public void onPostContingencyResult(PostContingencyResult postContingencyResult, SecurityAnalysisResultContext context) {
        String workingStateId = context.getNetwork().getVariantManager().getWorkingVariantId();

        if (context instanceof RunningContext runningContext) {
            for (LimitViolation limitViolation : postContingencyResult.getLimitViolationsResult().getLimitViolations()) {
                if (limitViolation.getLimitType() == LimitViolationType.CURRENT) {
                    Branch branch = context.getNetwork().getBranch(limitViolation.getSubjectId());

                    context.getNetwork().getVariantManager().setWorkingVariant(runningContext.getInitialStateId());
                    limitViolation.addExtension(CurrentExtension.class, new CurrentExtension(branch.getTerminal(limitViolation.getSideAsTwoSides()).getI()));
                    double preContingencyValue = branch.getTerminal(limitViolation.getSideAsTwoSides()).getP();

                    context.getNetwork().getVariantManager().setWorkingVariant(workingStateId);
                    double postContingencyValue = branch.getTerminal(limitViolation.getSideAsTwoSides()).getP();

                    limitViolation.addExtension(ActivePowerExtension.class, new ActivePowerExtension(preContingencyValue, postContingencyValue));
                }
            }
        }
    }
}
