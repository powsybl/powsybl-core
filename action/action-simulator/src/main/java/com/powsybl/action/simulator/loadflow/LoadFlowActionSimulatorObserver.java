/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.Rule;
import com.powsybl.security.LimitViolation;
import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadFlowActionSimulatorObserver {

    void beforePreContingencyAnalysis(RunningContext runningContext);

    void afterPreContingencyAnalysis();

    void beforePostContingencyAnalysis(RunningContext runningContext);

    void postContingencyAnalysisNetworkLoaded(RunningContext runningContext);

    void roundBegin(RunningContext runningContext);

    void roundEnd(RunningContext runningContext);

    void loadFlowDiverged(RunningContext runningContext);

    void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations);

    void ruleChecked(RunningContext runningContext, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions);

    void beforeAction(RunningContext runningContext, String actionId);

    void beforeTest(RunningContext runningContext, String actionId);

    void afterAction(RunningContext runningContext, String actionId);

    void afterTest(RunningContext runningContext, String actionId);

    void violationsAfterTest(String actionId, List<LimitViolation> violations);

    void divergedAfterTest(String actionId);

    void noMoreViolations(RunningContext runningContext);

    void noMoreViolationsAfterTest(RunningContext runningContext, String actionId);

    void beforeApplyTest(RunningContext runningContext, String actionId);

    void afterApplyTest(RunningContext runningContext, String actionId);

    void violationsAnymoreAndNoRulesMatch(RunningContext runningContext);

    void afterPostContingencyAnalysis();

    void maxIterationsReached(RunningContext runningContext);

}
