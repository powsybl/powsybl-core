/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.powsybl.action.simulator.loadflow.DefaultLoadFlowActionSimulatorObserver;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
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
    public void beforePreContingencyAnalysis(Network network) {
        precontingency = true;
        preContingencyResult = null;
    }

    @Override
    public void afterPreContingencyAnalysis() {
        precontingency = false;
        postContingencyResults.clear();
    }

    @Override
    public void loadFlowDiverged(Contingency contingency) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(false, Collections.emptyList(), preContingencyActions);
        } else {
            Objects.requireNonNull(contingency);
            postContingencyResults.put(contingency.getId(), new PostContingencyResult(contingency, false, Collections.emptyList(), getPostContingencyActions(contingency)));
        }
    }

    @Override
    public void loadFlowConverged(Contingency contingency, Network network, List<LimitViolation> violations) {
        if (precontingency) {
            preContingencyResult = new LimitViolationsResult(true, violations, preContingencyActions);
        } else {
            Objects.requireNonNull(contingency);
            postContingencyResults.put(contingency.getId(), new PostContingencyResult(contingency,
                                                                                      true,
                                                                                      violations,
                                                                                      getPostContingencyActions(contingency)));
        }
    }

    private List<String> getPostContingencyActions(Contingency contingency) {
        return postContingencyActions.computeIfAbsent(contingency.getId(), k -> new ArrayList<>());
    }

    @Override
    public void afterAction(Contingency contingency, String actionId) {
        Objects.requireNonNull(actionId);
        if (precontingency) {
            preContingencyActions.add(actionId);
        } else {
            Objects.requireNonNull(contingency);
            getPostContingencyActions(contingency).add(actionId);
        }
    }

    @Override
    public void afterPostContingencyAnalysis() {
        onFinalStateResult(new SecurityAnalysisResult(preContingencyResult,
                                                      postContingencyResults.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList())));
    }

    public abstract void onFinalStateResult(SecurityAnalysisResult result);
}
