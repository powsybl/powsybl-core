/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.mcla.MontecarloSampler;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineRulesFacade;
import eu.itesla_project.modules.online.OnlineStep;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.RulesFacadeResults;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizer;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerResult;
import eu.itesla_project.modules.optimizer.PostContingencyState;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.Security;
import eu.itesla_project.modules.security.Security.CurrentLimitType;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.simulation.ImpactAnalysis;
import eu.itesla_project.modules.simulation.ImpactAnalysisResult;
import eu.itesla_project.modules.simulation.Stabilization;
import eu.itesla_project.modules.simulation.StabilizationResult;
import eu.itesla_project.modules.simulation.StabilizationStatus;
import eu.itesla_project.online.OnlineWorkflowImpl.StateAnalizerListener;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class StateAnalyzer implements Callable<Void> {

	Logger logger = LoggerFactory.getLogger(StateAnalyzer.class);

	private OnlineWorkflowContext context;
	private MontecarloSampler sampler;
	private LoadFlow loadFlow;
	private OnlineRulesFacade rulesFacade;
	private CorrectiveControlOptimizer optimizer;
	private Stabilization stabilization;
	private ImpactAnalysis impactAnalysis;
	private OnlineDb onlineDb;
	private Integer stateId;
	private OnlineWorkflowParameters parameters;
	private StateAnalizerListener stateListener;
	private EnumMap<OnlineTaskType,OnlineTaskStatus> status=new EnumMap<OnlineTaskType, OnlineTaskStatus>(OnlineTaskType.class);
	Map<String, Boolean> loadflowResults = new HashMap<String, Boolean>();

	public StateAnalyzer(OnlineWorkflowContext context, MontecarloSampler sampler, LoadFlow loadFlow,
			OnlineRulesFacade rulesFacade, CorrectiveControlOptimizer optimizer, Stabilization stabilization,
			ImpactAnalysis impactAnalysis, OnlineDb onlineDb, StateAnalizerListener stateListener, OnlineWorkflowParameters parameters) {
		this.context = context;
		this.sampler = sampler;
		this.loadFlow = loadFlow;
		this.rulesFacade = rulesFacade;
		this.optimizer = optimizer;
		this.stabilization = stabilization;
		this.impactAnalysis = impactAnalysis;
		this.onlineDb = onlineDb;
		this.stateListener=stateListener;
		this.parameters = parameters;
		//stateId = "STATE-" + context.incrementStateCounter();
		stateId =  context.incrementStateCounter();
		initStatus();
		stateListener.onUpdate(stateId, status,context.timeHorizon);
	}

	private void initStatus(){
		status.put(OnlineTaskType.SAMPLING, OnlineTaskStatus.IDLE);
		status.put(OnlineTaskType.LOAD_FLOW, OnlineTaskStatus.IDLE);
		status.put(OnlineTaskType.SECURITY_RULES, OnlineTaskStatus.IDLE);
		status.put(OnlineTaskType.OPTIMIZER, OnlineTaskStatus.IDLE);
		status.put(OnlineTaskType.TIME_DOMAIN_SIM, OnlineTaskStatus.IDLE);
	}
	
	@Override
	public Void call() throws Exception {
		OnlineTaskType currentStatus=OnlineTaskType.SAMPLING;
		
		try {
			// create new state
			logger.info("Analyzing state {}", stateId);
			String stateIdStr=String.valueOf(stateId);
			context.getNetwork().getStateManager().cloneState(StateManager.INITIAL_STATE_ID, stateIdStr);
			context.getNetwork().getStateManager().setWorkingState(stateIdStr);
			// sample
			logger.info("{}: sampling started", stateId);
			status.put(currentStatus, OnlineTaskStatus.RUNNING);
			stateListener.onUpdate(stateId, status,context.timeHorizon);
			if ( !parameters.analyseBasecase() || stateId > 0 )
				sampler.sample();
			else
				logger.info("{}: state = basecase", stateId);
			status.put(currentStatus, OnlineTaskStatus.SUCCESS);
			stateListener.onUpdate(stateId, status,context.timeHorizon);
			logger.info("{}: sampling terminated", stateId);
			
			// complete state with loadflow
			currentStatus=OnlineTaskType.LOAD_FLOW;
			status.put(currentStatus, OnlineTaskStatus.RUNNING);
			stateListener.onUpdate(stateId, status,context.timeHorizon);
			logger.info("{}: loadflow started", stateId);
            LoadFlowResult result = loadFlow.run();
			status.put(currentStatus, result.isOk()?OnlineTaskStatus.SUCCESS:OnlineTaskStatus.FAILED);
			stateListener.onUpdate(stateId, status,context.timeHorizon);
			logger.info("{}: loadflow terminated", stateId);
			if ( result.getMetrics() != null ) {
				logger.info("{}: loadflow metrics: {}", stateId, result.getMetrics());
				if ( !result.getMetrics().isEmpty() )
					onlineDb.storeMetrics(context.getWorkflowId(), stateId, OnlineStep.LOAD_FLOW, result.getMetrics());
			}
			status.put(currentStatus, result.isOk()?OnlineTaskStatus.SUCCESS:OnlineTaskStatus.FAILED);
			
			if ( parameters.storeStates() ) {
				logger.info("{}: storing state in online db", stateId);
				onlineDb.storeState(context.getWorkflowId(), stateId, context.getNetwork());
			}
			
			if ( result.isOk() ) {
				// stores violations only if loadflow converges
				logger.info("{}: storing violations after {} in online db", stateId, OnlineStep.LOAD_FLOW);
				List<LimitViolation> violations = Security.checkLimits(context.getNetwork(), CurrentLimitType.PATL, Integer.MAX_VALUE, parameters.getLimitReduction());
				if ( violations != null && !violations.isEmpty() )
					onlineDb.storeViolations(context.getWorkflowId(), stateId, OnlineStep.LOAD_FLOW, violations);
				else
					logger.info("{}: no violations after {}", stateId, OnlineStep.LOAD_FLOW);

				
				stateListener.onUpdate(stateId, status,context.timeHorizon);
				// check state against contingencies
				boolean isStateSafe = true;
				List<Contingency> contingenciesForOptimizer = new ArrayList<Contingency>();
				List<Contingency> contingenciesForSimulator = new ArrayList<Contingency>();
				currentStatus=OnlineTaskType.SECURITY_RULES;
				status.put(currentStatus, OnlineTaskStatus.RUNNING);
				stateListener.onUpdate(stateId, status,context.timeHorizon);
				
				for (Contingency contingency : context.getContingenciesToAnalyze()) {
					logger.info("{}: check security rules against contingency {}", stateId, contingency.getId());
					RulesFacadeResults rulesResults = rulesFacade.evaluate(contingency, context.getNetwork());
					if ( rulesResults.getStateStatus() == StateStatus.SAFE ) {  // check if this contingency is ok
						logger.info("{}: is safe for contingency {}", stateId, contingency.getId());
						if ( parameters.validation() ) { // if validation
							// send all [contingency,state] pairs to simulation
							contingenciesForSimulator.add(contingency);
							// send safe [contingency,state] pairs to optimizer
							contingenciesForOptimizer.add(contingency);
						}
					} else if( rulesResults.getStateStatus() == StateStatus.SAFE_WITH_CORRECTIVE_ACTIONS ) { // check if this contingency could be ok with corrective actions
						logger.info("{}: requires corrective actions for contingency {}", stateId, contingency.getId());
						isStateSafe = false;
						contingenciesForOptimizer.add(contingency);
						if ( parameters.validation() ) { // if validation
							// send all [contingency,state] pairs to simulation
							contingenciesForSimulator.add(contingency);
						}
					} else { // we need to perform a time-domain simulation on this state for this contingency
						logger.info("{}: requires time-domain simulation for contingency {}", stateId, contingency.getId());
						isStateSafe = false;
						contingenciesForSimulator.add(contingency);
					}

					synchronized (context.getSecurityRulesResults()) {
						context.getSecurityRulesResults().addStateWithSecurityRulesResults(contingency.getId(), stateId, rulesResults.getStateStatus(), rulesResults.getIndexesResults());
						stateListener.onSecurityRulesApplicationResults(contingency.getId(),stateId, context);
					}
					
					if ( parameters.validation() ) {
						RulesFacadeResults wcaRulesResults = rulesFacade.wcaEvaluate(contingency, context.getNetwork());
						synchronized (context.getWcaSecurityRulesResults()) {
							context.getWcaSecurityRulesResults().addStateWithSecurityRulesResults(contingency.getId(), stateId, wcaRulesResults.getStateStatus(), wcaRulesResults.getIndexesResults());
						}
					}	
                }
				status.put(currentStatus, OnlineTaskStatus.SUCCESS);
				stateListener.onUpdate(stateId, status,context.timeHorizon);
				computeAndStorePostContingencyViolations(context.getNetwork(), context.getContingenciesToAnalyze());
				if ( isStateSafe && !parameters.validation() ) {
					// state is safe: stop analysis and destroy the state
					logger.info("{}: is safe for every contingency: stopping analysis", stateId);
		            //context.getNetwork().getStateManager().removeState(stateIdStr); // the state is still needed
		            return null;
				} else {
					if ( contingenciesForOptimizer.size() > 0 ) {
						// perform corrective control optimization
						currentStatus=OnlineTaskType.OPTIMIZER;
						status.put(currentStatus, OnlineTaskStatus.RUNNING);
						stateListener.onUpdate(stateId, status,context.timeHorizon);
						logger.info("{}: corrective control optimization started - working on {} contingencies", stateId, contingenciesForOptimizer.size());
						runOptimizer(context.getNetwork(), contingenciesForOptimizer, contingenciesForSimulator, context.getResults());
						// the optimizer could possibly have changed the network working state: set the original one
						context.getNetwork().getStateManager().setWorkingState(stateIdStr);
						stateListener.onOptimizerResults(stateId,context);
						logger.info("{}: corrective control optimization terminated", stateId);
						status.put(OnlineTaskType.OPTIMIZER, OnlineTaskStatus.SUCCESS);
						stateListener.onUpdate(stateId, status,context.timeHorizon);
					}
					if ( contingenciesForSimulator.size() > 0 ) {
						// perform time-domain simulation
						currentStatus=OnlineTaskType.TIME_DOMAIN_SIM;
						status.put(currentStatus, OnlineTaskStatus.RUNNING);
						stateListener.onUpdate(stateId, status,context.timeHorizon);
						logger.info("{}: time-domain simulation started - working on {} contingencies", stateId, contingenciesForSimulator.size());
						logger.info("{}: stabilization started", stateId);
						StabilizationResult stabilizationResult = stabilization.run();
						logger.info("{}: stabilization terminated", stateId);
						if ( stabilizationResult.getMetrics() != null ) {
							logger.info("{}: stabilization metrics: {}", stateId, stabilizationResult.getMetrics());
							if ( !stabilizationResult.getMetrics().isEmpty() )
								onlineDb.storeMetrics(context.getWorkflowId(), stateId, OnlineStep.STABILIZATION, stabilizationResult.getMetrics());
						}
                        if (stabilizationResult.getStatus() == StabilizationStatus.COMPLETED) {
                            ImpactAnalysisResult impactAnalysisResult = impactAnalysis.run(stabilizationResult.getState(), OnlineUtils.getContingencyIds(contingenciesForSimulator));
                            logger.info("{}: impact analysis terminated", stateId);
    						if ( impactAnalysisResult.getMetrics() != null ) {
    							logger.info("{}: impact analysis metrics: {}", stateId, impactAnalysisResult.getMetrics());
    							if ( !impactAnalysisResult.getMetrics().isEmpty() )
    								onlineDb.storeMetrics(context.getWorkflowId(), stateId, OnlineStep.IMPACT_ANALYSIS, impactAnalysisResult.getMetrics());
    						}
                            putResultsIntoContext(stateId, impactAnalysisResult, context.getResults());
                            stateListener.onImpactAnalysisResults(stateId, context);
                            logger.info("{}: time-domain simulation terminated", stateId);
                            status.put(OnlineTaskType.TIME_DOMAIN_SIM, OnlineTaskStatus.SUCCESS);
                            stateListener.onUpdate(stateId, status,context.timeHorizon);
                        } else {
                            logger.info("{}: time-domain simulation failed (stabilization)", stateId);
                            status.put(OnlineTaskType.TIME_DOMAIN_SIM, OnlineTaskStatus.FAILED);
                            stateListener.onUpdate(stateId, status,context.timeHorizon, "time-domain simulation failed (stabilization): metrics = "+stabilizationResult.getMetrics());
                        }
					}
				}
				stateListener.onUpdate(stateId, status,context.timeHorizon);
			} else {
				logger.error("{}: stop analisys of state: loadflow does not converge: metrics = {}", stateIdStr, result.getMetrics());
				stateListener.onUpdate(stateId, status,context.timeHorizon, "LoadFLow does not converge: metrics = " + result.getMetrics());
			}
		} catch (Throwable t) {
			status.put(currentStatus, OnlineTaskStatus.FAILED);
			//TODO  manage string ifo detail 
			stateListener.onUpdate(stateId, status,context.timeHorizon ,currentStatus +" failed ... ");
            logger.error("{}: Error working on state: {}", stateId, t.toString(), t);
        }
		return null;
	}
	
	private void runOptimizer(Network network, List<Contingency> contingencies, List<Contingency> contingenciesForSimulator, ForecastAnalysisResults results) {
		String stateId = network.getStateManager().getWorkingStateId();
		logger.info("{}: running optimizer", stateId);
		List<Callable<Void>> postContingencyStateComputations = new ArrayList<>(contingencies.size());
		for (Contingency contingency : contingencies) {
			postContingencyStateComputations.add(
					new Callable<Void>() {

						@Override
						public Void call() throws Exception {
								String postContingencyStateId = stateId + "-post-" + contingency.getId();
								boolean loadflowConverge = computePostContingencyState(network, stateId, contingency, postContingencyStateId);
								if ( loadflowConverge ) {
				            		logger.info("{}: adding state {} to post contingency states for optimizer", stateId, postContingencyStateId);
				            		PostContingencyState postContingencyState = new PostContingencyState(network, postContingencyStateId, contingency);
				            		logger.info("{}: running optimizer on post contingency state {} of contingency {}", stateId, postContingencyStateId, contingency.getId());
				            		CorrectiveControlOptimizerResult optimizerResult = null;
				            		try {
				            			optimizerResult = optimizer.run(postContingencyState);
				            		} catch (Throwable t) {
										logger.error("{}: Error running optimizer on contingency {}: {}", stateId, contingency.getId(), t.getMessage(), t);
										optimizerResult = new CorrectiveControlOptimizerResult(contingency.getId(), false);
										optimizerResult.setFinalStatus(CCOFinalStatus.OPTIMIZER_EXECUTION_ERROR);
										optimizerResult.setCause(t.getMessage());
									}
				            		logger.info("{}: optimizer results for contingency {}: action found = {}, status = {}, cause = {}", stateId, contingency.getId(), optimizerResult.areActionsFound(), optimizerResult.getFinalStatus(), optimizerResult.getCause());
				            		Map<String, Map<String,ActionParameters>> actions = null;
				            		if ( optimizerResult.areActionsFound() ) {
				            			logger.info("{}: optimizer results: action plan {}, actions {} for contingency {}", stateId, optimizerResult.getActionPlan(), optimizerResult.getActionsIds(), contingency.getId());
				            			actions = new HashMap<String, Map<String,ActionParameters>>();
					            		for(String actionId : optimizerResult.getActionsIds())
					            			actions.put(actionId, optimizerResult.getEquipmentsWithParameters(actionId));
				            		} else {
				            			logger.error("{}: Error: optimizer didn't find actions for post contingency state {}", stateId, postContingencyStateId);
					            		if ( !parameters.validation() ) { // if validation -> all the [contingency,state] pairs have already been added to the list for simulation -> no need to do it here
						            		// add to contingencies for simulator
					            			synchronized(contingenciesForSimulator) {
					            				contingenciesForSimulator.add(contingency);
					            			}
					            		}
				            		}
				            		synchronized(results) {
				            			results.addStateWithActions(contingency.getId(), 
				            										Integer.valueOf(stateId),
				            										optimizerResult.areActionsFound(),
				            										optimizerResult.getFinalStatus(),
				            										optimizerResult.getCause(),
				            										optimizerResult.getActionPlan(), 
				            										actions);
				            		}
					            } else {
					            	logger.info("{}: loadflow does not converge on post contigency state {}, the contingency {} will be analyzed by T-D simulation", stateId, postContingencyStateId, contingency.getId());
				            		if ( !parameters.validation() ) { // if validation -> all the [contingency,state] pairs have already been added to the list for simulation -> no need to do it here
					            		// add to contingencies for simulator
				            			synchronized(contingenciesForSimulator) {
				            				contingenciesForSimulator.add(contingency);
				            			}
				            		}
				            	}
							
							return null;
						}
						
					}
				);
		}
		ExecutorService taskExecutor = Executors.newFixedThreadPool(contingencies.size());
		try {
			taskExecutor.invokeAll(postContingencyStateComputations);
		} catch (InterruptedException e) {
			logger.error("{}: Error running optimizer: {}", stateId, e.getMessage());
		}
		taskExecutor.shutdown();
		network.getStateManager().setWorkingState(stateId);
	}
	
	private void computeAndStorePostContingencyViolations(Network network, List<Contingency> contingencies) {
		String stateId = network.getStateManager().getWorkingStateId();
		logger.info("{}: computing post contingency violations", stateId);
		List<Callable<Void>> postContingencyViolationsComputations = new ArrayList<>(contingencies.size());
		for (Contingency contingency : contingencies) {
			postContingencyViolationsComputations.add(
					new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							List<LimitViolation> violations = new ArrayList<LimitViolation>();
							// compute post contingency state
							String postContingencyStateId = stateId + "-post-" + contingency.getId();
							boolean loadflowConverge = computePostContingencyState(network, stateId, contingency, postContingencyStateId);
							if ( loadflowConverge ) {
								logger.info("{}: computing post contingency violations for contingency {}", stateId, contingency.getId());
								violations = Security.checkLimits(network, CurrentLimitType.PATL, Integer.MAX_VALUE, parameters.getLimitReduction());
								if ( violations == null || violations.isEmpty() ) {
									logger.info("{}: no post contingency violations for state {} and contingency {}", stateId, contingency.getId());
									violations = new ArrayList<LimitViolation>();
								}
							} else {
								logger.info("{}: post contingency loadflow does not converge for contingency {}, skipping computing post contingency violations", stateId, contingency.getId());
							}
							logger.info("{}: storing post contingency violations for state {} and contingency {} in online db", stateId, contingency.getId());
							onlineDb.storePostContingencyViolations(context.getWorkflowId(), Integer.valueOf(stateId), contingency.getId(), loadflowConverge, violations);
							network.getStateManager().setWorkingState(stateId);
//							network.getStateManager().removeState(postContingencyStateId);
							return null;
						}
					}
				);
		}
		ExecutorService taskExecutor = Executors.newFixedThreadPool(contingencies.size());
		try {
			taskExecutor.invokeAll(postContingencyViolationsComputations);
		} catch (InterruptedException e) {
			logger.error("{}: Error computing post contingency vioations: {}", stateId, e.getMessage());
		}
		taskExecutor.shutdown();
	}
	
	private boolean computePostContingencyState(Network network, String stateId, Contingency contingency, String postContingencyStateId) {
		boolean loadflowConverge = false;
		logger.info("{}: computing post contingency state for contingency {}", stateId, contingency.getId());
		//String postContingencyStateId = stateId + "-post-" + contingency.getId();
		boolean alreadyProcessed = false;
		synchronized (loadflowResults) {
			if ( loadflowResults.containsKey(postContingencyStateId) ) {
				alreadyProcessed = true;
				loadflowConverge = loadflowResults.get(postContingencyStateId);
			}
		}
		if ( alreadyProcessed && network.getStateManager().getStateIds().contains(postContingencyStateId) ) {
			// post contingency state already computed, avoid to run the load flow again
			logger.info("{}: post contingency state {} already computed", stateId, postContingencyStateId);
			network.getStateManager().setWorkingState(postContingencyStateId);
		} else {
			// create post contingency state
			logger.info("{}: creating post contingency state {}", stateId, postContingencyStateId);
			network.getStateManager().cloneState(stateId, postContingencyStateId);
			network.getStateManager().setWorkingState(postContingencyStateId);
			// apply contingency to post contingency state
			logger.info("{}: applying contingency {} to post contingency state {}", stateId, contingency.getId(), postContingencyStateId);
			contingency.toTask().modify(network);
			try {
				// run load flow on post contingency state
				logger.info("{}: running load flow on post contingency state {}", stateId, postContingencyStateId);
				LoadFlowResult result = loadFlow.run();
				if ( result.isOk() ) {
					logger.info("{}: load flow on post contingency state {} converge", stateId, postContingencyStateId);
					loadflowConverge = true;
				} else {
					logger.info("{}: load flow on post contingency state {} does not converge", stateId, postContingencyStateId);
					loadflowConverge = false;
				}
				synchronized (loadflowResults) {
					loadflowResults.put(postContingencyStateId, loadflowConverge);
				}
			} catch (Exception e) {
				logger.info("{}: error running load flow on post contingency state {}: {}", stateId, postContingencyStateId, e.getMessage());
				loadflowConverge = false;
			}
		}
		//network.getStateManager().setWorkingState(stateId);
		return loadflowConverge;
	}

	private void putResultsIntoContext(Integer stateId, ImpactAnalysisResult simulationResult, ForecastAnalysisResults results) {
		Objects.requireNonNull(stateId, "state id is null");
		Objects.requireNonNull(simulationResult, "simulation result is null");
		Objects.requireNonNull(results, "forecast analysis result is null");
		List<SecurityIndex> securityIndexesList = new ArrayList<SecurityIndex>();
		if ( parameters.getSecurityIndexes() == null )
			securityIndexesList = simulationResult.getSecurityIndexes();
		else {
			securityIndexesList = simulationResult.getSecurityIndexes().stream().filter(x -> parameters.getSecurityIndexes().contains(x.getId().getSecurityIndexType())).collect(Collectors.toList());
			if (securityIndexesList.isEmpty()) {
				logger.info("Empty filter security indexes -> using all the indexes");
				securityIndexesList = simulationResult.getSecurityIndexes();
			}
		}
        //Multimap<String, SecurityIndex> securityIndexes = Multimaps.index(simulationResult.getSecurityIndexes(), new Function<SecurityIndex, String>() {
		Multimap<String, SecurityIndex> securityIndexes = Multimaps.index(securityIndexesList, new Function<SecurityIndex, String>() {
            @Override
            public String apply(SecurityIndex index){
                return index.getId().getContingencyId();
            }
        });
		synchronized (results) {
			for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexes.asMap().entrySet()) {
				boolean isSafe = OnlineUtils.isSafe(entry.getValue());
				if ( !isSafe ) {
					logger.info("{}: unsafe for contingency {} afer time domain simulation", stateId, entry.getKey());
					results.addUnsafeStateWithIndexes(entry.getKey(), stateId, new ArrayList<>(entry.getValue()));
				} else {
		        	logger.info("{}: safe for contingency {} afer time domain simulation", stateId, entry.getKey());
		        	if ( parameters.validation() ) // if validation add anyway to results
		        		results.addUnsafeStateWithIndexes(entry.getKey(), stateId, new ArrayList<>(entry.getValue()));
		        }
	        }
		}
	}

}
