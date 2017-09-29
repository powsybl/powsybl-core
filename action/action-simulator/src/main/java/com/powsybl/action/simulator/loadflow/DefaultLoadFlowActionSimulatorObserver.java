/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.simulator.loadflow;

import eu.itesla_project.action.dsl.Rule;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.security.LimitViolation;

import java.util.List;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultLoadFlowActionSimulatorObserver implements LoadFlowActionSimulatorObserver {
    @Override
    public void beforePreContingencyAnalysis(Network network) {
    }

    @Override
    public void afterPreContingencyAnalysis() {
    }

    @Override
    public void beforePostContingencyAnalysis(Contingency contingency) {
    }

    @Override
    public void postContingencyAnalysisNetworkLoaded(Contingency contingency, Network network) {
    }

    @Override
    public void roundBegin(Contingency contingency, int round) {
    }

    @Override
    public void roundEnd(Contingency contingency, int round) {
    }

    @Override
    public void loadFlowDiverged(Contingency contingency) {
    }

    @Override
    public void loadFlowConverged(Contingency contingency, List<LimitViolation> violations) {
    }

    @Override
    public void ruleChecked(Contingency contingency, Rule rule, RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
    }

    @Override
    public void beforeAction(Contingency contingency, String actionId) {
    }

    @Override
    public void afterAction(Contingency contingency, String actionId) {
    }

    @Override
    public void noMoreViolations(Contingency contingency) {
    }

    @Override
    public void violationsAnymoreAndNoRulesMatch(Contingency contingency) {
    }

    @Override
    public void afterPostContingencyAnalysis() {
    }
}
