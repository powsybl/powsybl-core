/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.MergeOptimizerFactory;
import eu.itesla_project.modules.cases.CaseRepository;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.MontecarloSamplerFactory;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.RulesFacadeFactory;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.simulation.SimulatorFactory;
import eu.itesla_project.modules.wca.UncertaintiesAnalyserFactory;
import eu.itesla_project.modules.wca.WCAFactory;

/**
*
* @author Quinary <itesla@quinary.com>
*/
public class OnlineWorkflowFactoryImpl implements OnlineWorkflowFactory {

	/* (non-Javadoc)
	 * @see eu.itesla_project.online.OnlineWorkflowFactoryInterface#create(eu.itesla_project.computation.ComputationManager, eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient, eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory, eu.itesla_project.modules.histo.HistoDbClient, eu.itesla_project.modules.rules.RulesDbClient, eu.itesla_project.modules.wca.WCAFactory, eu.itesla_project.loadflow.api.LoadFlowFactory, eu.itesla_project.modules.mcla.ForecastErrorsDataStorage, eu.itesla_project.modules.online.OnlineDb, eu.itesla_project.modules.wca.UncertaintiesAnalyserFactory, eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory, eu.itesla_project.modules.simulation.SimulatorFactory, eu.itesla_project.modules.cases.CaseRepository, eu.itesla_project.modules.mcla.MontecarloSamplerFactory, eu.itesla_project.modules.MergeOptimizerFactory, eu.itesla_project.modules.online.OnlineWorkflowParameters, eu.itesla_project.online.OnlineWorkflowStartParameters)
	 */
	@Override
	public OnlineWorkflow create(ComputationManager computationManager,
			ContingenciesAndActionsDatabaseClient cadbClient,
			DynamicDatabaseClientFactory ddbClientFactory,
			HistoDbClient histoDbClient,
			RulesDbClient rulesDbClient,
            WCAFactory wcaFactory,
            LoadFlowFactory loadFlowFactory,
			ForecastErrorsDataStorage feDataStorage,
			OnlineDb onlineDB,
            UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory,
            CorrectiveControlOptimizerFactory optimizerFactory,
			SimulatorFactory simulatorFactory,
            CaseRepository caseRepository,
			MontecarloSamplerFactory montecarloSamplerFactory,
			MergeOptimizerFactory mergeOptimizerFactory,
			RulesFacadeFactory rulesFacadeFactory,
            OnlineWorkflowParameters parameters,
            OnlineWorkflowStartParameters startParameters){
		
		return   new OnlineWorkflowImpl(computationManager, cadbClient, ddbClientFactory, histoDbClient, rulesDbClient, wcaFactory, loadFlowFactory, feDataStorage,
				onlineDB, uncertaintiesAnalyserFactory, optimizerFactory, simulatorFactory, caseRepository,
                montecarloSamplerFactory,mergeOptimizerFactory, rulesFacadeFactory, parameters, startParameters); 
		
	}
}
