/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.forecast_errors;

import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.MergeOptimizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.modules.cases.CaseRepositoryFactory;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerFactory;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorageFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalysisConfig {

	/*
	 * example of forecastErrorsAnalysis.properties file.
	 *

	caseRepositoryFactoryClass=eu.itesla_project.modules.cases.EntsoeCaseRepositoryFactory
	forecastErrorsDataStorageFactoryClass=eu.itesla_project.matlab.mcla.ForecastErrorsDataStorageFactoryImpl
	forecastErrorsAnalyzerFactoryClass=eu.itesla_project.matlab.mcla.forecast_errors.ForecastErrorsAnalyzerFactoryImpl
	
	*/

	 private static final Logger LOGGER = LoggerFactory.getLogger(ForecastErrorsAnalysisConfig.class);

	 private final Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass;
	 private final Class<? extends ForecastErrorsDataStorageFactory> forecastErrorsDataStorageFactoryClass;
	 private final Class<? extends ForecastErrorsAnalyzerFactory> forecastErrorsAnalyzerFactoryClass;
	 private final Class<? extends LoadFlowFactory> loadFlowFactoryClass;
	 private final Class<? extends MergeOptimizerFactory> mergeOtimizerFactoryClass;

	 public ForecastErrorsAnalysisConfig(Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass,
			 							 Class<? extends ForecastErrorsDataStorageFactory> forecastErrorsDataStorageFactoryClass,
			 							 Class<? extends ForecastErrorsAnalyzerFactory> forecastErrorsAnalyzerFactoryClass,
										 Class<? extends LoadFlowFactory> loadFlowFactoryClass,
										 Class<? extends MergeOptimizerFactory> mergeOtimizerFactoryClass) {
		 this.caseRepositoryFactoryClass = caseRepositoryFactoryClass;
		 this.forecastErrorsDataStorageFactoryClass = forecastErrorsDataStorageFactoryClass;
		 this.forecastErrorsAnalyzerFactoryClass = forecastErrorsAnalyzerFactoryClass;
		 this.loadFlowFactoryClass = loadFlowFactoryClass;
		 this.mergeOtimizerFactoryClass = mergeOtimizerFactoryClass;
	 }

	public static ForecastErrorsAnalysisConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("forecastErrorsAnalysis");
        
        Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass = config.getClassProperty("caseRepositoryFactoryClass", CaseRepositoryFactory.class);
        Class<? extends ForecastErrorsDataStorageFactory> forecastErrorsDataStorageFactoryClass = config.getClassProperty("forecastErrorsDataStorageFactoryClass", ForecastErrorsDataStorageFactory.class);
        Class<? extends ForecastErrorsAnalyzerFactory> forecastErrorsAnalyzerFactoryClass = config.getClassProperty("forecastErrorsAnalyzerFactoryClass", ForecastErrorsAnalyzerFactory.class);
		Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);
		Class<? extends MergeOptimizerFactory> mergeOtimizerFactoryClass = config.getClassProperty("mergeOptimizerFactoryClass", MergeOptimizerFactory.class);

        return new ForecastErrorsAnalysisConfig(caseRepositoryFactoryClass, forecastErrorsDataStorageFactoryClass, forecastErrorsAnalyzerFactoryClass,
				loadFlowFactoryClass, mergeOtimizerFactoryClass);
    }

	public static Logger getLogger() {
		return LOGGER;
	}

	public Class<? extends CaseRepositoryFactory> getCaseRepositoryFactoryClass() {
        return caseRepositoryFactoryClass;
    }
	
	public Class<? extends ForecastErrorsDataStorageFactory> getForecastErrorsDataStorageFactoryClass() {
        return forecastErrorsDataStorageFactoryClass;
    }
	
	public Class<? extends ForecastErrorsAnalyzerFactory> getForecastErrorsAnalyzerFactoryClass() {
        return forecastErrorsAnalyzerFactoryClass;
    }

	public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
		return loadFlowFactoryClass;
	}

	public Class<? extends MergeOptimizerFactory> getMergeOtimizerFactoryClass() {
		return mergeOtimizerFactoryClass;
	}

	@Override
	public String toString() {
		return "ForecastErrorsAnalsysConfig [caseRepositoryFactoryClass=" + caseRepositoryFactoryClass
				+ " forecastErrorsDataStorageFactoryClass=" + forecastErrorsDataStorageFactoryClass
				+ " forecastErrorsAnalyzerFactoryClass=" + forecastErrorsAnalyzerFactoryClass 
				+ " loadFlowFactoryClass=" + loadFlowFactoryClass
				+ " mergeOtimizerFactoryClass=" + mergeOtimizerFactoryClass
				+ "]";
	}

}
