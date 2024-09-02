/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.tools;

import com.powsybl.action.ial.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.action.ial.simulator.loadflow.RunningContext;
import com.powsybl.contingency.Contingency;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.results.PostContingencyResult;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisResultBuilder extends DefaultLoadFlowActionSimulatorObserver {

    private LimitViolationsResult preContingencyResult;

    private final Map<String, PostContingencyResult> postContingencyResults = new HashMap<>();

    private final List<String> preContingencyActions = new ArrayList<>();

    private final Map<String, List<String>> postContingencyActions = new HashMap<>();

    private boolean precontingency;

    private LoadFlowResult.ComponentResult.Status preContingencyStatus;

    @Override
    public void beforePreContingencyAnalysis(RunningContext runningContext) {
        precontingency = true;
        preContingencyResult = null;
        preContingencyStatus = null;
    }

    @Override
    public void afterPreContingencyAnalysis() {
        precontingency = false;
        postContingencyResults.clear();
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(Collections.emptyList(), preContingencyActions);
            preContingencyStatus = LoadFlowResult.ComponentResult.Status.FAILED;
        } else {
            Objects.requireNonNull(runningContext.getContingency());
            postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(), PostContingencyComputationStatus.FAILED,
                    Collections.emptyList(), getPostContingencyActions(runningContext.getContingency())));
        }
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(violations, preContingencyActions);
            preContingencyStatus = LoadFlowResult.ComponentResult.Status.CONVERGED;
        } else {
            Objects.requireNonNull(runningContext.getContingency());
            postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(), PostContingencyComputationStatus.CONVERGED,
                    violations,
                    getPostContingencyActions(runningContext.getContingency())));
        }
    }

    private List<String> getPostContingencyActions(Contingency contingency) {
        return postContingencyActions.computeIfAbsent(contingency.getId(), k -> new ArrayList<>());
    }

    @Override
    public void afterAction(RunningContext runningContext, String actionId) {
        Objects.requireNonNull(actionId);
        if (precontingency) {
            preContingencyActions.add(actionId);
        } else {
            Objects.requireNonNull(runningContext.getContingency());
            getPostContingencyActions(runningContext.getContingency()).add(actionId);
        }
    }

    @Override
    public void afterPostContingencyAnalysis() {
        onFinalStateResult(new SecurityAnalysisResult(preContingencyResult, preContingencyStatus,
            new ArrayList<>(postContingencyResults.values())));
    }

    public abstract void onFinalStateResult(SecurityAnalysisResult result);
}
