/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.powsybl.action.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.action.simulator.loadflow.RunningContext;
import com.powsybl.contingency.Contingency;
import com.powsybl.security.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractSecurityAnalysisResultBuilder extends DefaultLoadFlowActionSimulatorObserver {

    private LimitViolationsResult preContingencyResult;

    private final Map<String, PostContingencyResult> postContingencyResults = new HashMap<>();

    private final List<String> preContingencyActions = new ArrayList<>();

    private final Map<String, List<String>> postContingencyActions = new HashMap<>();

    private boolean precontingency;

    @Override
    public void beforePreContingencyAnalysis(RunningContext runningContext) {
        precontingency = true;
        preContingencyResult = null;
    }

    @Override
    public void afterPreContingencyAnalysis() {
        precontingency = false;
        postContingencyResults.clear();
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(false, Collections.emptyList(), preContingencyActions);
        } else {
            Objects.requireNonNull(runningContext.getContingency());
            postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(), false, Collections.emptyList(), getPostContingencyActions(runningContext.getContingency())));
        }
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(true, violations, preContingencyActions);
        } else {
            Objects.requireNonNull(runningContext.getContingency());
            postContingencyResults.put(runningContext.getContingency().getId(), new PostContingencyResult(runningContext.getContingency(),
                    true,
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
        onFinalStateResult(new SecurityAnalysisResult(preContingencyResult,
                postContingencyResults.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())));
    }

    public abstract void onFinalStateResult(SecurityAnalysisResult result);
}
