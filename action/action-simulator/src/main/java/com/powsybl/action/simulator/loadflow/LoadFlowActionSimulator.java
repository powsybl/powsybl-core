/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.loadflow;

import com.powsybl.action.dsl.*;
import com.powsybl.action.dsl.ast.*;
import com.powsybl.action.simulator.ActionSimulator;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
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
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadFlowActionSimulator implements ActionSimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowActionSimulator.class);

    public static final LimitViolationFilter LIMIT_VIOLATION_FILTER = new LimitViolationFilter(EnumSet.of(LimitViolationType.CURRENT), 0f);
    public static final LimitViolationFilter NO_FILTER = new LimitViolationFilter();

    private final Network network;

    private final ComputationManager computationManager;

    private final LoadFlowActionSimulatorConfig config;

    private final boolean applyIfWorks;

    private final List<LoadFlowActionSimulatorObserver> observers;

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager) {
        this(network, computationManager, LoadFlowActionSimulatorConfig.load(), false, Collections.emptyList());
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfWorks, LoadFlowActionSimulatorObserver... observers) {
        this(network, computationManager, config, applyIfWorks, Arrays.asList(observers));
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   boolean applyIfWorks, List<LoadFlowActionSimulatorObserver> observers) {
        this.network = Objects.requireNonNull(network);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.config = Objects.requireNonNull(config);
        this.observers = Objects.requireNonNull(observers);
        this.applyIfWorks = applyIfWorks;
    }

    @Override
    public String getName() {
        return "loadflow";
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

        // duplicate the network for each contingency
        byte[] networkXmlGz = NetworkXml.gzip(network);

        if (preContingencyAnalysisOk || config.isIgnorePreContingencyViolations()) {
            for (String contingencyId : contingencyIds) {
                Contingency contingency = actionDb.getContingency(contingencyId);
                Network network2 = NetworkXml.gunzip(networkXmlGz);
                RunningContext runningContext2 = new RunningContext(network2, contingency);

                observers.forEach(o -> o.beforePostContingencyAnalysis(runningContext2));

                LOGGER.info("Starting post-contingency analysis '{}'", contingency.getId());
                contingency.toTask().modify(network2, computationManager);

                observers.forEach(o -> o.postContingencyAnalysisNetworkLoaded(runningContext2));

                next(actionDb, runningContext2);
            }
        }

        observers.forEach(LoadFlowActionSimulatorObserver::afterPostContingencyAnalysis);
    }

    protected LoadFlowFactory newLoadFlowFactory() {
        try {
            return config.getLoadFlowFactoryClass().newInstance();
        } catch (InstantiationException e) {
            throw new UncheckedInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        }
    }

    private boolean next(ActionDb actionDb, RunningContext context) {
        if (context.getRound() >= config.getMaxIterations()) {
            return false;
        }

        observers.forEach(o -> o.roundBegin(context));

        LoadFlowFactory loadFlowFactory = newLoadFlowFactory();
        LoadFlow loadFlow = loadFlowFactory.create(context.getNetwork(), computationManager, 0);

        LOGGER.info("Running loadflow ({})", loadFlow.getName());
        LoadFlowResult result;
        try {
            result = loadFlow.run(LoadFlowParameters.load());
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
        if (result.isOk()) {
            List<LimitViolation> violations = LIMIT_VIOLATION_FILTER.apply(Security.checkLimits(context.getNetwork(), 1), context.getNetwork());
            // no more violations => work complete
            if (violations.isEmpty()) {
                observers.forEach(o -> o.loadFlowConverged(context, violations));
                LOGGER.info("No more violation");
                observers.forEach(o -> o.noMoreViolations(context));
                return true;
            }

            LOGGER.info("Violations: \n{}", Security.printLimitsViolations(violations, network, NO_FILTER));
            observers.forEach(o -> o.loadFlowConverged(context, violations));

            runTests(actionDb, context);
            if (context.isTestWorks() && applyIfWorks) {
                return true;
            }

            Set<String> actionsTaken = new HashSet<>();
            for (Rule rule : actionDb.getRules()) {
                if (rule.getType().equals(RuleType.TEST)) {
                    continue;
                }
                RuleEvaluationStatus status;
                final Map<String, Object> variables;
                final Map<String, Boolean> actions;
                if (context.getRuleMatchCount(rule.getId()) >= rule.getLife()) {
                    status = RuleEvaluationStatus.DEAD;
                    variables = Collections.emptyMap();
                    actions = Collections.emptyMap();
                } else {
                    // re-evaluate the condition
                    if (rule.getCondition().getType() != ConditionType.EXPRESSION) {
                        throw new AssertionError("TODO");
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
                    boolean ok = ExpressionEvaluator.evaluate(conditionExpr, evalContext).equals(Boolean.TRUE);

                    LOGGER.debug("Evaluating {} to {}", ExpressionPrinter.toString(conditionExpr), Boolean.toString(ok));

                    variables = ExpressionVariableLister.list(conditionExpr).stream()
                        .collect(Collectors.toMap(ExpressionPrinter::toString,
                            n -> ExpressionEvaluator.evaluate(n, evalContext),
                            (v1, v2) -> v1,
                            TreeMap::new));

                    LOGGER.debug("Variables values: {}", variables);

                    if (ok) {
                        status = RuleEvaluationStatus.TRUE;
                        context.incrementRuleMatchCount(rule.getId());
                    } else {
                        status = RuleEvaluationStatus.FALSE;
                    }

                    actions = ExpressionActionTakenLister.list(conditionExpr).stream()
                        .collect(Collectors.toMap(s -> s,
                            s -> context.getTimeLine().actionTaken(s),
                            (s1, s2) -> s1,
                            TreeMap::new));
                }

                observers.forEach(o -> o.ruleChecked(context, rule, status, variables, actions));

                if (status == RuleEvaluationStatus.TRUE) {
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
                    return ExpressionEvaluator.evaluate(conditionExpr, evalContext).equals(Boolean.TRUE);
                })
                .collect(Collectors.toList());
        List<String> testActionIds = activedRules.stream()
                                .flatMap(r -> r.getActions().stream())
                                .distinct()
                                .filter(id -> !context.isTested(id))
                                .collect(Collectors.toList());
        byte[] contextNetwork = NetworkXml.gzip(context.getNetwork());

        for (String actionId : testActionIds) {
            Action action = actionDb.getAction(actionId);
            Network networkForTest = NetworkXml.gunzip(contextNetwork);
            LoadFlowResult testResult = runTest(context, networkForTest, action);
            context.addTested(actionId);
            if (testResult.isOk()) {
                List<LimitViolation> violationsInTest =
                        LIMIT_VIOLATION_FILTER.apply(Security.checkLimits(networkForTest, 1), networkForTest);
                if (violationsInTest.isEmpty()) {
                    context.addWorkedTest(action.getId());
                    if (applyIfWorks) {
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
        }
    }

    private LoadFlowResult runTest(RunningContext context, Network networkForTry, Action action) {
        String actionId = action.getId();
        LOGGER.info("Test action '{}'", actionId);
        action.run(networkForTry, computationManager);
        LoadFlowFactory loadFlowFactory = newLoadFlowFactory();
        LoadFlow testLoadFlow = loadFlowFactory.create(networkForTry, computationManager, 0);
        try {
            observers.stream().forEach(o -> o.beforeTest(context, actionId));
            LoadFlowResult testResult = testLoadFlow.run(LoadFlowParameters.load());
            observers.stream().forEach(o -> o.afterTest(context, actionId));
            return testResult;
        } catch (Exception e) {
            throw new PowsyblException(e);
        }
    }
}
