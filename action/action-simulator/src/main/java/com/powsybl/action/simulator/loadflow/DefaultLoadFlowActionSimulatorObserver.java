/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
public class DefaultLoadFlowActionSimulatorObserver implements LoadFlowActionSimulatorObserver {
    @Override
    public void beforePreContingencyAnalysis(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void afterPreContingencyAnalysis() {
        // empty default implementation
    }

    @Override
    public void beforePostContingencyAnalysis(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void postContingencyAnalysisNetworkLoaded(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void roundBegin(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void roundEnd(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void loadFlowDiverged(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void loadFlowConverged(RunningContext runningContext, List<LimitViolation> violations) {
        // empty default implementation
    }

    @Override
    public void ruleChecked(RunningContext runningContext, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
        // empty default implementation
    }

    @Override
    public void beforeAction(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void beforeTrydo(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void afterAction(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void afterTrydo(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void violationsAfterTry(String actionId, List<LimitViolation> violations) {
        // empty default implementation
    }

    @Override
    public void divergedAfterTry(String actionId) {
        // empty default implementation
    }

    @Override
    public void noMoreViolations(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void noMoreViolationsAfterTry(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void beforeApplyTrydo(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void afterApplyTrydo(RunningContext runningContext, String actionId) {
        // empty default implementation
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(RunningContext runningContext) {
        // empty default implementation
    }

    @Override
    public void afterPostContingencyAnalysis() {
        // empty default implementation
    }
}
