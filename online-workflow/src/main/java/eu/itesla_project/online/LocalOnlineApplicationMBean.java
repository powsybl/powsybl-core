/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisParameters;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface LocalOnlineApplicationMBean {

    public static final String BEAN_NAME = "eu.itesla_project.online:type=LocalOnlineApplicationMBean";
    
    public static final String BUSY_CORES_ATTRIBUTE = "BusyCores";
    
    public static final String WORK_STATES_ATTRIBUTE = "WorkflowStates";
    
    public static final String RUNNING_ATTRIBUTE = "Running";
    
    public static final String WCA_RUNNING_ATTRIBUTE = "WcaRunning";
    
    public static final String STATES_ACTIONS_ATTRIBUTE = "StateActions";
    
   // public static final String STABLE_CONTINGENCIES_ATTRIBUTE = "StableContingencies";
    
	public static final String STATES_INDEXES_ATTRIBUTE = "StatesIndexes";
	
	public static final String INDEXES_SECURITY_RULES_ATTRIBUTE ="IndexSecurityRulesResults";
	
	//public static final String UNSTABLE_CONTINGENCIES_ATTRIBUTE = "UnstableContingencies";
	
	public static final String WCA_CONTINGENCIES_ATTRIBUTE = "WcaContingencies";
	
	//Apogée
	public static final String GENERATE_CARD_ATTRIBUTE = "Card";
    
	void ping();

    int getAvailableCores();

    int[] getBusyCores();
    
    void startWorkflow(OnlineWorkflowStartParameters start, OnlineWorkflowParameters params);

    void stopWorkflow();

    void notifyListeners();

    void shutdown();

    void runFeaAnalysis(OnlineWorkflowStartParameters startconfig, ForecastErrorsAnalysisParameters parameters, String timeHorizonS);

    void runTDSimulations(OnlineWorkflowStartParameters startconfig, String caseFile, String contingenciesIds, String emptyContingencyS, String outputFolderS);
}
