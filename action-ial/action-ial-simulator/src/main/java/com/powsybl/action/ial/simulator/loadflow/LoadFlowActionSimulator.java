/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.simulator.loadflow;

import com.powsybl.action.ial.dsl.*;
import com.powsybl.action.ial.dsl.ast.*;
import com.powsybl.action.ial.simulator.ActionSimulator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class LoadFlowActionSimulator implements ActionSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowActionSimulator.class);

    private static final LimitViolationFilter LIMIT_VIOLATION_FILTER = new LimitViolationFilter(EnumSet.of(LimitViolationType.CURRENT), 0.0);

    static final LimitViolationFilter NO_FILTER = new LimitViolationFilter();

    private final Network network;

    private final ComputationManager computationManager;

    private final LoadFlowActionSimulatorConfig config;

    private final boolean applyIfSolvedViolations;

    private final LoadFlowParameters parameters;

    private final List<LoadFlowActionSimulatorObserver> observers;

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager) {
        this(network, computationManager, LoadFlowActionSimulatorConfig.load(), false, Collections.emptyList());
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfSolvedViolations, LoadFlowActionSimulatorObserver... observers) {
        this(network, computationManager, config, applyIfSolvedViolations, Arrays.asList(observers));
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfSolvedViolations, List<LoadFlowActionSimulatorObserver> observers) {
        this(network, computationManager, config, applyIfSolvedViolations, LoadFlowParameters.load(), observers);
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfSolvedViolations, LoadFlowParameters parameters, LoadFlowActionSimulatorObserver... observers) {
        this(network, computationManager, config, applyIfSolvedViolations, parameters, Arrays.asList(observers));
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfSolvedViolations, LoadFlowParameters parameters, List<LoadFlowActionSimulatorObserver> observers) {
        this.network = Objects.requireNonNull(network);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.config = Objects.requireNonNull(config);
        this.observers = Objects.requireNonNull(observers);
        this.applyIfSolvedViolations = applyIfSolvedViolations;
        this.parameters = Objects.requireNonNull(parameters);
    }

    @Override
    public String getName() {
        return "loadflow";
    }

    ComputationManager getComputationManager() {
        return computationManager;
    }

    LoadFlowActionSimulatorConfig getConfig() {
        return config;
    }

    protected Network getNetwork() {
        return network;
    }

    protected boolean isApplyIfSolvedViolations() {
        return applyIfSolvedViolations;
    }

    @Override
    public void start(ActionDb actionDb, String... contingencyIds) {
        start(actionDb, Arrays.asList(contingencyIds));
    }

    @Override
    public void start(ActionDb actionDb, List<String> contingencyIds) {
        Objects.requireNonNull(actionDb);

        LOGGER.info("Starting pre-contingency analysis");
        RunningContext runningContext = new RunningContext(network);
        observers.forEach(o -> o.beforePreContingencyAnalysis(runningContext));

        boolean preContingencyAnalysisOk = next(actionDb, runningContext);

        observers.forEach(LoadFlowActionSimulatorObserver::afterPreContingencyAnalysis);

        NetworkCopyStrategy strategy = NetworkCopyStrategy.getInstance(config.getCopyStrategy(), runningContext.getNetwork());

        if (preContingencyAnalysisOk || config.isIgnorePreContingencyViolations()) {
            for (String contingencyId : contingencyIds) {
                Contingency contingency = actionDb.getContingency(contingencyId);
                Network network2 = strategy.createState(contingencyId);
                RunningContext runningContext2 = new RunningContext(network2, contingency);

                observers.forEach(o -> o.beforePostContingencyAnalysis(runningContext2));

                LOGGER.info("Starting post-contingency analysis '{}'", contingency.getId());
                contingency.toModification().apply(network2, computationManager);

                observers.forEach(o -> o.postContingencyAnalysisNetworkLoaded(runningContext2));

                next(actionDb, runningContext2);

                strategy.removeState();
            }
        }

        observers.forEach(LoadFlowActionSimulatorObserver::afterPostContingencyAnalysis);
    }

    private static final class RuleContext {

        private final RuleEvaluationStatus status;
        private final Map<String, Object> variables;
        private final Map<String, Boolean> actions;

        private RuleContext(RuleEvaluationStatus status, Map<String, Object> variables, Map<String, Boolean> actions) {
            this.status = Objects.requireNonNull(status);
            this.variables = Objects.requireNonNull(variables);
            this.actions = Objects.requireNonNull(actions);
        }

        private RuleEvaluationStatus getStatus() {
            return status;
        }

        private Map<String, Object> getVariables() {
            return variables;
        }

        private Map<String, Boolean> getActions() {
            return actions;
        }
    }

    private RuleContext evaluateRule(Rule rule, RunningContext context) {
        if (rule.getCondition().getType() != ConditionType.EXPRESSION) {
            throw new IllegalStateException("TODO");
        }
        ExpressionNode conditionExpr = ((ExpressionCondition) rule.getCondition()).getNode();
        EvaluationContext evalContext = new EvaluationContext() {
            @Override
            public Network getNetwork() {
                return context.getNetwork();
            }

            @Override
            public Contingency getContingency() {
                return context.getContingency();
            }

            @Override
            public boolean isActionTaken(String actionId) {
                return context.getTimeLine().actionTaken(actionId);
            }
        };
        boolean ok = ActionExpressionEvaluator.evaluate(conditionExpr, evalContext).equals(Boolean.TRUE);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Evaluating {} to {}", ActionExpressionPrinter.toString(conditionExpr), Boolean.toString(ok));
        }

        Map<String, Object> variables = ExpressionVariableLister.list(conditionExpr).stream()
                .collect(Collectors.toMap(ActionExpressionPrinter::toString,
                    n -> ActionExpressionEvaluator.evaluate(n, evalContext),
                    (v1, v2) -> v1,
                    TreeMap::new));

        LOGGER.debug("Variables values: {}", variables);

        RuleEvaluationStatus status;
        if (ok) {
            status = RuleEvaluationStatus.TRUE;
            context.incrementRuleMatchCount(rule.getId());
        } else {
            status = RuleEvaluationStatus.FALSE;
        }

        Map<String, Boolean> actions = ExpressionActionTakenLister.list(conditionExpr).stream()
                .collect(Collectors.toMap(s -> s,
                    s -> context.getTimeLine().actionTaken(s),
                    (s1, s2) -> s1,
                    TreeMap::new));

        return new RuleContext(status, variables, actions);
    }

    private void applyActions(ActionDb actionDb, RunningContext context, Rule rule, Set<String> actionsTaken) {
        for (String actionId : rule.getActions()) {
            Action action = actionDb.getAction(actionId);

            // apply action
            LOGGER.info("Apply action '{}'", action.getId());
            observers.forEach(o -> o.beforeAction(context, actionId));

            action.run(context.getNetwork(), computationManager);

            observers.forEach(o -> o.afterAction(context, actionId));
            actionsTaken.add(actionId);
        }
    }

    private boolean checkViolations(ActionDb actionDb, RunningContext context) {
        List<LimitViolation> violations = LIMIT_VIOLATION_FILTER.apply(Security.checkLimits(context.getNetwork(), 1), context.getNetwork());
        observers.forEach(o -> o.loadFlowConverged(context, violations));
        // no more violations => work complete
        if (violations.isEmpty()) {
            LOGGER.info("No more violation");
            observers.forEach(o -> o.noMoreViolations(context));
            return true;
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Violations: \n{}", Security.printLimitsViolations(violations, network, NO_FILTER));
        }

        if (context.getRound() + 1 == config.getMaxIterations()) {
            LOGGER.info("Max number of iterations reached");
            observers.forEach(o -> o.maxIterationsReached(context));
            return false;
        }

        runTests(actionDb, context);
        if (context.isTestWorks() && applyIfSolvedViolations) {
            return true;
        }

        Set<String> actionsTaken = new HashSet<>();
        for (Rule rule : actionDb.getRules()) {
            if (rule.getType().equals(RuleType.TEST)) {
                continue;
            }
            RuleContext ruleContext;
            if (context.getRuleMatchCount(rule.getId()) >= rule.getLife()) {
                ruleContext = new RuleContext(RuleEvaluationStatus.DEAD, Collections.emptyMap(), Collections.emptyMap());
            } else {
                ruleContext = evaluateRule(rule, context);
            }

            observers.forEach(o -> o.ruleChecked(context, rule, ruleContext.getStatus(), ruleContext.getVariables(), ruleContext.getActions()));

            if (ruleContext.getStatus() == RuleEvaluationStatus.TRUE) {
                applyActions(actionDb, context, rule, actionsTaken);
            }
        }

        // record the action in the time line
        context.getTimeLine().getActions().addAll(actionsTaken);

        observers.forEach(o -> o.roundEnd(context));

        if (!actionsTaken.isEmpty()) {
            context.setRound(context.getRound() + 1);
            return next(actionDb, context);
        } else {
            LOGGER.info("Still some violations and no rule match");
            observers.forEach(o -> o.violationsAnymoreAndNoRulesMatch(context));
            return false;
        }
    }

    private boolean next(ActionDb actionDb, RunningContext context) {
        observers.forEach(o -> o.roundBegin(context));

        LOGGER.info("Running loadflow");
        LoadFlowResult result;
        try {
            String loadFlowName = config.getLoadFlowName().orElse(null);
            result = LoadFlow.find(loadFlowName).run(context.getNetwork(), context.getNetwork().getVariantManager().getWorkingVariantId(), computationManager, parameters);
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
        if (result.isPartiallyConverged() || result.isFullyConverged()) {
            return checkViolations(actionDb, context);
        } else {
            LOGGER.warn("Loadflow diverged: {}", result.getMetrics());
            observers.forEach(o -> o.loadFlowDiverged(context));
            return false;
        }
    }

    private void runTests(ActionDb actionDb, RunningContext context) {
        // test actions
        EvaluationContext evalContext = new EvaluationContext() {
            @Override
            public Network getNetwork() {
                return context.getNetwork();
            }

            @Override
            public Contingency getContingency() {
                return context.getContingency();
            }

            @Override
            public boolean isActionTaken(String actionId) {
                return context.getTimeLine().actionTaken(actionId);
            }
        };

        List<Rule> activedRules = actionDb.getRules().stream()
                .filter(rule -> rule.getType().equals(RuleType.TEST))
                .filter(rule -> {
                    ExpressionNode conditionExpr = ((ExpressionCondition) rule.getCondition()).getNode();
                    return ActionExpressionEvaluator.evaluate(conditionExpr, evalContext).equals(Boolean.TRUE);
                })
                .toList();
        List<String> testActionIds = activedRules.stream()
                                .flatMap(r -> r.getActions().stream())
                                .distinct()
                                .filter(id -> !context.isTested(id))
                                .toList();

        if (testActionIds.isEmpty()) {
            return;
        }

        NetworkCopyStrategy strategy = NetworkCopyStrategy.getInstance(config.getCopyStrategy(), context.getNetwork());

        for (String actionId : testActionIds) {
            Action action = actionDb.getAction(actionId);

            Network networkForTest = strategy.createState(actionId);

            LoadFlowResult testResult = runTest(context, networkForTest, action);
            context.addTested(actionId);
            if (testResult.isPartiallyConverged() || testResult.isFullyConverged()) {
                List<LimitViolation> violationsInTest =
                        LIMIT_VIOLATION_FILTER.apply(Security.checkLimits(networkForTest, 1), networkForTest);
                if (violationsInTest.isEmpty()) {
                    context.addWorkedTest(action.getId());
                    if (applyIfSolvedViolations) {
                        LOGGER.info("Loadflow with test '{}' works already and exits simulation", action.getId());
                        observers.forEach(o -> o.noMoreViolationsAfterTest(context, action.getId()));
                        observers.forEach(o -> o.beforeApplyTest(context, action.getId()));
                        action.run(context.getNetwork(), computationManager);
                        context.getTimeLine().getActions().add(actionId);
                        observers.forEach(o -> o.loadFlowConverged(context, violationsInTest));
                        observers.forEach(o -> o.noMoreViolations(context));
                        observers.forEach(o -> o.afterApplyTest(context, action.getId()));
                        return;
                    } else {
                        LOGGER.info("Loadflow with test '{}' works already and continues simulation", action.getId());
                        observers.forEach(o -> o.noMoreViolationsAfterTest(context, action.getId()));
                    }
                } else {
                    LOGGER.info("Loadflow with test '{}' exits with violations", action.getId());
                    observers.forEach(o -> o.violationsAfterTest(action.getId(), violationsInTest));
                }
            } else {
                LOGGER.info("Loadflow with test '{}' diverged", action.getId());
                observers.forEach(o -> o.divergedAfterTest(action.getId()));
            }
            strategy.removeState();
        }
    }

    private LoadFlowResult runTest(RunningContext context, Network networkForTry, Action action) {
        String actionId = action.getId();
        LOGGER.info("Test action '{}'", actionId);
        action.run(networkForTry, computationManager);
        try {
            observers.forEach(o -> o.beforeTest(context, actionId));
            String loadFlowName = config.getLoadFlowName().orElse(null);
            LoadFlowResult testResult = LoadFlow.find(loadFlowName)
                                                .run(networkForTry, networkForTry.getVariantManager().getWorkingVariantId(), computationManager, parameters);
            observers.forEach(o -> o.afterTest(context, actionId));
            return testResult;
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
    }
}
