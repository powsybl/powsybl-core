/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.forecast_errors;

import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.merge.MergeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzer;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerFactory;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerParameters;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.online.TimeHorizon;


/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalysis {

	final Logger logger = LoggerFactory.getLogger(ForecastErrorsAnalysis.class);

	private final ComputationManager computationManager;
	private final ForecastErrorsAnalysisParameters parameters;
    private final CaseRepository caseRepository;
	private final ForecastErrorsDataStorage feDataStorage;
    private final ForecastErrorsAnalyzerFactory forecastErrorsAnalyzerFactory;
	private final LoadFlowFactory loadFlowFactory;
	private final MergeOptimizerFactory mergeOptimizerFactory;

	public ForecastErrorsAnalysis(
			ComputationManager computationManager,
			ForecastErrorsAnalysisConfig config,
			ForecastErrorsAnalysisParameters parameters) throws InstantiationException, IllegalAccessException {
		this.computationManager = computationManager;
		this.parameters = parameters;
		this.caseRepository = config.getCaseRepositoryFactoryClass().newInstance().create(computationManager);
		this.feDataStorage = config.getForecastErrorsDataStorageFactoryClass().newInstance().create();
		this.forecastErrorsAnalyzerFactory = config.getForecastErrorsAnalyzerFactoryClass().newInstance();
		this.loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
		this.mergeOptimizerFactory = config.getMergeOtimizerFactoryClass().newInstance();
		logger.info(config.toString());
	}

	public ForecastErrorsAnalysis(ComputationManager cpClient) throws InstantiationException, IllegalAccessException {
		this(cpClient, ForecastErrorsAnalysisConfig.load(), ForecastErrorsAnalysisParameters.load());
	}

	public void start() { runFEA(TimeHorizon.values()); }
	
	public void start(TimeHorizon timehorizon) {
		runFEA(new TimeHorizon[]{timehorizon});
	}
	
	private void runFEA(TimeHorizon[] timeHorizons) {
		logger.info("Forecast errors analysis, started.");

		Network network = MergeUtil.merge(caseRepository, parameters.getBaseCaseDate(), parameters.getCaseType(), parameters.getCountries(),
										  loadFlowFactory, 0, mergeOptimizerFactory, computationManager, false);

        logger.info("- Network id: " + network.getId());
        logger.info("- Network name: "+ network.getName());

		ForecastErrorsAnalyzer feAnalyzer = forecastErrorsAnalyzerFactory.create(network, computationManager, feDataStorage);
		feAnalyzer.init(new ForecastErrorsAnalyzerParameters(
				parameters.getHistoInterval(), parameters.getFeAnalysisId(),
				parameters.getIr(), parameters.getFlagPQ(), parameters.getMethod(),
				parameters.getnClusters(), parameters.getPercentileHistorical(),
				parameters.getModalityGaussian(), parameters.getOutliers(),
				parameters.getConditionalSampling(), parameters.getnSamples()));

		// sequential analysis of the different time horizons
		// it could/should be parallelized
		for(TimeHorizon timeHorizon : timeHorizons) {
			try {
				logger.info("Performing forecast error analysis on {} time horizon for {} network", timeHorizon.getName(), network.getId());
				feAnalyzer.run(timeHorizon);
			} catch (Exception e) {
				logger.error("Error analysing {} time horizon for {} network: {}", timeHorizon.getName(), network.getId(), e.getMessage());
			}

		}

		logger.info("Forecast errors analysis, terminated.");
	}

}
