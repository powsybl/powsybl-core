/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.Rule;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;

import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadFlowActionSimulatorObserver {

    void beforePreContingencyAnalysis(Network network);

    void afterPreContingencyAnalysis();

    void beforePostContingencyAnalysis(Contingency contingency);

    void postContingencyAnalysisNetworkLoaded(Contingency contingency, Network network);

    void roundBegin(Contingency contingency, int round);

    void roundEnd(Contingency contingency, int round);

    void loadFlowDiverged(Contingency contingency);

    void loadFlowConverged(Contingency contingency, List<LimitViolation> violations);

    void ruleChecked(Contingency contingency, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions);

    void beforeAction(Contingency contingency, String actionId);

    void afterAction(Contingency contingency, String actionId);

    void noMoreViolations(Contingency contingency);

    void violationsAnymoreAndNoRulesMatch(Contingency contingency);

    void afterPostContingencyAnalysis();
}
