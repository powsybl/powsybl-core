/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.mcla;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import eu.itesla_project.modules.online.TimeHorizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface ForecastErrorsDataStorage {

	boolean isForecastErrorsDataAvailable(String analysisId, TimeHorizon timeHorizon);
	void getForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException;
	void storeForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path originFile) throws IOException;
	boolean deleteForecastErrorsData(String analysisId, TimeHorizon timeHorizon);

	boolean isForecastOfflineSamplesDataAvailable(String analysisId, TimeHorizon timeHorizon);
	void getForecastOfflineSamplesFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException;
	void storeForecastOfflineSamplesFile(String analysisId, TimeHorizon timeHorizon, Path originFile) throws IOException;
	boolean deleteForecastOfflineSamplesData(String analysisId, TimeHorizon timeHorizon);

	boolean areStatisticsAvailable(String analysisId, TimeHorizon timeHorizon);
	void getStatisticsFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException;
	ForecastErrorsStatistics getStatistics(String analysisId, TimeHorizon timeHorizon) throws IOException;
	void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path statisticsFile) throws IOException;
	void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path samplesFile, Path statisticsFile, Path uncertaintiesGuiFile) throws IOException;
	boolean deleteStatistics(String analysisId, TimeHorizon timeHorizon);
	
	boolean areGuiUncertaintiesAvailable(String analysisId, TimeHorizon timeHorizon);
	String[] getGuiUncertaintiesInjectionIds(String analysisId, TimeHorizon timeHorizon) throws IOException;
	double[][] getGuiUncertaintiesLinearCorrelation(String analysisId, TimeHorizon timeHorizon) throws IOException;
	double[][] getGuiUncertaintiesLoadings(String analysisId, TimeHorizon timeHorizon) throws IOException;
	void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path statisticsFile, Path uncertaintiesGuiFile) throws IOException;
	boolean deleteGuiUncertainties(String analysisId, TimeHorizon timeHorizon);
	
	List<ForecastErrorsAnalysisDetails> listAnalysis();
	boolean deleteAnalysis(String analysisId, TimeHorizon timeHorizon);
	
	void storeParameters(String analysisId, TimeHorizon timeHorizon, ForecastErrorsAnalyzerParameters parameters) throws IOException;
	ForecastErrorsAnalyzerParameters getParameters(String analysisId, TimeHorizon timeHorizon) throws IOException;

}
