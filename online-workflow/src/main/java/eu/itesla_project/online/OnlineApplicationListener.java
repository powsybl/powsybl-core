/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineApplicationListener {

    void onBusyCoresUpdate(int[] busyCores);
    
    void onWorkflowUpdate(StatusSynthesis status);

    void onWcaUpdate(RunningSynthesis wcaRunning);

    void onStatesWithActionsUpdate(ContingencyStatesActionsSynthesis acts);

    void onStatesWithIndexesUpdate(ContingencyStatesIndexesSynthesis stindex);

	void onWorkflowStateUpdate(WorkSynthesis work);

	//stable and unstable contingencies where merged into wcaContingencies
	//void onStableContingencies(StableContingenciesSynthesis stableContingencies);	
	//void onUnstableContingencies(UnstableContingenciesSynthesis unstableContingencies);
	//new 
	void onWcaContingencies(WcaContingenciesSynthesis wcaContingencies);
	
	void onStatesWithSecurityRulesResultsUpdate(IndexSecurityRulesResultsSynthesis indexesResults) ;
 
	void onDisconnection();
	
	void onConnection();

	//void onWorkflowEnd(OnlineWorkflowContext context, OnlineDb onlineDb, OnlineWorkflowParameters parameters);

	void onWorkflowEnd(OnlineWorkflowContext context, OnlineDb onlineDb,
			ContingenciesAndActionsDatabaseClient cadbClient, OnlineWorkflowParameters parameters);



}
