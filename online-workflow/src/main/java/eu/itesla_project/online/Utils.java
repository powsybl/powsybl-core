/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import java.io.Writer;
import java.util.*;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.simulation.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import net.sf.json.JSONSerializer;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class Utils {

    private static final String EMPTY_CONTINGENCY_ID = "Empty-Contingency";

    public static String actionsToJson(OnlineWorkflowResults wfResults, String contingencyId, Integer stateId) {
        Map<String, Object> actionInfo = new HashMap<>();
        actionInfo.put("actions_found", wfResults.getUnsafeStatesWithActions(contingencyId).get(stateId));
        actionInfo.put("status", wfResults.getStateStatus(contingencyId, stateId));
        List<Object> actions = new ArrayList<>();
        for (String actionId : wfResults.getActionsIds(contingencyId, stateId)) {
            if (wfResults.getEquipmentsIds(contingencyId, stateId, actionId) != null
                    && !wfResults.getEquipmentsIds(contingencyId, stateId, actionId).isEmpty()) {
                Map<String, Object> action = new HashMap<>();
                List<Object> equipments = new ArrayList<>();
                for (String equipmentId : wfResults.getEquipmentsIds(contingencyId, stateId, actionId)) {
                    ActionParameters actionParameters = wfResults.getParameters(contingencyId, stateId, actionId, equipmentId);
                    if (actionParameters != null && !actionParameters.getNames().isEmpty()) {
                        Map<String, Map<String, Object>> equipment = new HashMap<>();
                        Map<String, Object> params = new HashMap<>();
                        for (String param : actionParameters.getNames()) {
                            params.put(param, actionParameters.getValue(param));
                        }
                        equipment.put(equipmentId, params);
                        equipments.add(equipment);
                    } else
                        equipments.add(equipmentId);
                }
                action.put(actionId, equipments);
                actions.add(action);
            } else
                actions.add(actionId);
        }
        if (wfResults.getActionPlan(contingencyId, stateId) != null) {
            actionInfo.put(wfResults.getActionPlan(contingencyId, stateId), actions);
        } else {
            if (!actions.isEmpty())
                actionInfo.put("actions", actions);
        }
        return JSONSerializer.toJSON(actionInfo).toString();
    }

    public static String actionsToJsonExtended(OnlineWorkflowResults wfResults, String contingencyId, Integer stateId) {
        Map<String, Object> actionInfo = new HashMap<>();
        actionInfo.put("actions_found", wfResults.getUnsafeStatesWithActions(contingencyId).get(stateId));
        actionInfo.put("status", wfResults.getStateStatus(contingencyId, stateId));
        String cause = wfResults.getCause(contingencyId, stateId);
        if (cause != null)
            actionInfo.put("cause", cause);
        if (wfResults.getActionsIds(contingencyId, stateId) != null && !wfResults.getActionsIds(contingencyId, stateId).isEmpty()) {
            List<Object> actions = new ArrayList<>();
            for (String actionId : wfResults.getActionsIds(contingencyId, stateId)) {
                Map<String, Object> action = new HashMap<>();
                action.put("action_id", actionId);
                if (wfResults.getEquipmentsIds(contingencyId, stateId, actionId) != null
                        && !wfResults.getEquipmentsIds(contingencyId, stateId, actionId).isEmpty()) {
                    List<Object> equipments = new ArrayList<>();
                    for (String equipmentId : wfResults.getEquipmentsIds(contingencyId, stateId, actionId)) {
                        Map<String, Object> equipment = new HashMap<>();
                        equipment.put("equipment_id", equipmentId);
                        ActionParameters actionParameters = wfResults.getParameters(contingencyId, stateId, actionId, equipmentId);
                        if (actionParameters != null && !actionParameters.getNames().isEmpty()) {
                            Map<String, Object> params = new HashMap<>();
                            for (String param : actionParameters.getNames()) {
                                params.put(param, actionParameters.getValue(param));
                            }
                            equipment.put("parameters", params);
                        }
                        equipments.add(equipment);
                    }
                    action.put("equipments", equipments);
                }
                actions.add(action);
            }
            actionInfo.put("actions", actions);
        }
        if (wfResults.getActionPlan(contingencyId, stateId) != null) {
            actionInfo.put("action_plan", wfResults.getActionPlan(contingencyId, stateId));
        }
        return JSONSerializer.toJSON(actionInfo).toString();
    }


    public static Map<String, Boolean> runTDSimulation(Network network, Set<String> contingencyIds, boolean emptyContingency,
                                                       ComputationManager computationManager, SimulatorFactory simulatorFactory,
                                                       ContingenciesAndActionsDatabaseClient contingencyDb,
                                                       Writer metricsContent) throws Exception {
        Map<String, Boolean> tdSimulationResults = new HashMap<>();
        Map<String, Object> initContext = new HashMap<>();
        SimulationParameters simulationParameters = SimulationParameters.load();
        // run stabilization
        Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0);
        stabilization.init(simulationParameters, initContext);
        System.out.println("running stabilization on network " + network.getId());
        StabilizationResult stabilizationResults = stabilization.run();
        metricsContent.write("****** BASECASE " + network.getId() + "\n");
        metricsContent.write("*** Stabilization Metrics ***\n");
        Map<String, String> stabilizationMetrics = stabilizationResults.getMetrics();
        if (stabilizationMetrics != null && !stabilizationMetrics.isEmpty()) {
            for (String parameter : stabilizationMetrics.keySet())
                metricsContent.write(parameter + " = " + stabilizationMetrics.get(parameter) + "\n");
        }
        metricsContent.flush();
        if (stabilizationResults.getStatus() == StabilizationStatus.COMPLETED) {
            if (emptyContingency) // store data for t-d simulation on empty contingency, i.e. stabilization
                tdSimulationResults.put(EMPTY_CONTINGENCY_ID, true);
            // check if there are contingencies to run impact analysis
            if (contingencyIds == null && contingencyDb.getContingencies(network).size() == 0)
                contingencyIds = new HashSet<>();
            if (contingencyIds == null || !contingencyIds.isEmpty()) {
                // run impact analysis
                ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingencyDb);
                impactAnalysis.init(simulationParameters, initContext);
                System.out.println("running impact analysis on network " + network.getId());
                ImpactAnalysisResult impactAnalisResults = impactAnalysis.run(stabilizationResults.getState(), contingencyIds);
                for (SecurityIndex index : impactAnalisResults.getSecurityIndexes()) {
                    tdSimulationResults.put(index.getId().toString(), index.isOk());
                }
                metricsContent.write("*** Impact Analysis Metrics ***\n");
                Map<String, String> impactAnalysisMetrics = impactAnalisResults.getMetrics();
                if (impactAnalysisMetrics != null && !impactAnalysisMetrics.isEmpty()) {
                    for (String parameter : impactAnalysisMetrics.keySet())
                        metricsContent.write(parameter + " = " + impactAnalysisMetrics.get(parameter) + "\n");
                }
                metricsContent.flush();
            }
        } else {
            if (emptyContingency) // store data for t-d simulation on empty contingency, i.e. stabilization
                tdSimulationResults.put(EMPTY_CONTINGENCY_ID, false);
        }
        return tdSimulationResults;
    }
}
