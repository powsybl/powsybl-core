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
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.loadflow.api.LoadFlow;
import com.powsybl.loadflow.api.LoadFlowFactory;
import com.powsybl.loadflow.api.LoadFlowResult;
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

    private final List<LoadFlowActionSimulatorObserver> observers;

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager) {
        this(network, computationManager, LoadFlowActionSimulatorConfig.load(), (ArrayList) null);
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   LoadFlowActionSimulatorObserver... observers) {
        this(network, computationManager, config, Arrays.asList(observers));
    }

    public LoadFlowActionSimulator(Network network, ComputationManager computationManager, LoadFlowActionSimulatorConfig config,
                                   List<LoadFlowActionSimulatorObserver> observers) {
        this.network = Objects.requireNonNull(network);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.config = Objects.requireNonNull(config);
        this.observers = observers;
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
        if (observers != null) {
            observers.forEach(o -> o.beforePreContingencyAnalysis(network));
        }

        boolean preContingencyAnalysisOk = next(actionDb, new RunningContext(network));

        if (observers != null) {
            observers.forEach(LoadFlowActionSimulatorObserver::afterPreContingencyAnalysis);
        }

        // duplicate the network for each contingency
        byte[] networkXmlGz = NetworkXml.gzip(network);

        if (preContingencyAnalysisOk || config.isIgnorePreContingencyViolations()) {
            for (String contingencyId : contingencyIds) {
                Contingency contingency = actionDb.getContingency(contingencyId);

                if (observers != null) {
                    observers.forEach(o -> o.beforePostContingencyAnalysis(contingency));
                }

                Network network2 = NetworkXml.gunzip(networkXmlGz);

                LOGGER.info("Starting post-contingency analysis '{}'", contingency.getId());
                contingency.toTask().modify(network2, computationManager);

                if (observers != null) {
                    observers.forEach(o -> o.postContingencyAnalysisNetworkLoaded(contingency, network2));
                }

                next(actionDb, new RunningContext(network2, actionDb.getContingency(contingencyId)));
            }
        }

        if (observers != null) {
            observers.forEach(LoadFlowActionSimulatorObserver::afterPostContingencyAnalysis);
        }
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

        if (observers != null) {
            observers.forEach(o -> o.roundBegin(context.getContingency(), context.getRound()));
        }

        LoadFlowFactory loadFlowFactory = newLoadFlowFactory();
        LoadFlow loadFlow = loadFlowFactory.create(context.getNetwork(), computationManager, 0);

        LOGGER.info("Running loadflow ({})", loadFlow.getName());
        LoadFlowResult result;
        try {
            result = loadFlow.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (result.isOk()) {
            List<LimitViolation> violations = LIMIT_VIOLATION_FILTER.apply(Security.checkLimits(context.getNetwork(), 1));
            if (violations.size() > 0) {
                LOGGER.info("Violations: \n{}", Security.printLimitsViolations(violations, NO_FILTER));
            }
            if (observers != null) {
                observers.forEach(o -> o.loadFlowConverged(context.getContingency(), violations));
            }

            // no more violations => work complete
            if (violations.isEmpty()) {
                LOGGER.info("No more violation");
                if (observers != null) {
                    observers.forEach(o -> o.noMoreViolations(context.getContingency()));
                }
                return true;
            }

            Set<String> actionsTaken = new HashSet<>();
            for (Rule rule : actionDb.getRules()) {

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

                if (observers != null) {
                    observers.forEach(o -> o.ruleChecked(context.getContingency(), rule, status, variables, actions));
                }

                if (status == RuleEvaluationStatus.TRUE) {
                    for (String actionId : rule.getActions()) {
                        Action action = actionDb.getAction(actionId);

                        // apply action
                        LOGGER.info("Apply action '{}'", action.getId());
                        if (observers != null) {
                            observers.forEach(o-> o.beforeAction(context.getContingency(), actionId));
                        }

                        action.run(context.getNetwork(), computationManager);

                        if (observers != null) {
                            observers.forEach(o-> o.afterAction(context.getContingency(), actionId));
                        }
                        actionsTaken.add(actionId);
                    }
                }
            }

            // record the action in the time line
            context.getTimeLine().getActions().addAll(actionsTaken);

            if (observers != null) {
                observers.forEach(o -> o.roundEnd(context.getContingency(), context.getRound()));
            }

            if (actionsTaken.size() > 0) {
                context.setRound(context.getRound() + 1);
                return next(actionDb, context);
            } else {
                LOGGER.info("Still some violations and no rule match");
                if (observers != null) {
                    observers.forEach(o -> o.violationsAnymoreAndNoRulesMatch(context.getContingency()));
                }
                return false;
            }
        } else {
            LOGGER.warn("Loadflow diverged: {}", result.getMetrics());
            if (observers != null) {
                observers.forEach(o -> o.loadFlowDiverged(context.getContingency()));
            }
            return false;
        }
    }
}
