/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.iidm.network.Branch;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyResult;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class CurrentLimitViolationInterceptor extends DefaultSecurityAnalysisInterceptor {

    @Override
    public void onPreContingencyResult(RunningContext context, LimitViolationsResult preContingencyResult) {
        for (LimitViolation limitViolation : preContingencyResult.getLimitViolations()) {
            if (limitViolation.getLimitType() == LimitViolationType.CURRENT) {
                Branch branch = context.getNetwork().getBranch(limitViolation.getSubjectId());

                double preContingencyValue = branch.getTerminal(limitViolation.getSide()).getP();
                limitViolation.addExtension(ActivePowerExtension.class, new ActivePowerExtension(preContingencyValue));
            }
        }
    }

    @Override
    public void onPostContingencyResult(RunningContext context, PostContingencyResult postContingencyResult) {
        String workingStateId = context.getNetwork().getStateManager().getWorkingStateId();

        for (LimitViolation limitViolation : postContingencyResult.getLimitViolationsResult().getLimitViolations()) {
            if (limitViolation.getLimitType() == LimitViolationType.CURRENT) {
                Branch branch = context.getNetwork().getBranch(limitViolation.getSubjectId());

                context.getNetwork().getStateManager().setWorkingState(context.getInitialStateId());
                limitViolation.addExtension(CurrentExtension.class, new CurrentExtension(branch.getTerminal(limitViolation.getSide()).getI()));
                double preContingencyValue = branch.getTerminal(limitViolation.getSide()).getP();

                context.getNetwork().getStateManager().setWorkingState(workingStateId);
                double postContingencyValue = branch.getTerminal(limitViolation.getSide()).getP();

                limitViolation.addExtension(ActivePowerExtension.class, new ActivePowerExtension(preContingencyValue, postContingencyValue));
            }
        }
    }
}
