/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modules.mcla.ForecastErrorsAnalysisDetails;
import eu.itesla_project.modules.mcla.ForecastErrorsAnalyzerParameters;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.ForecastErrorsStatistics;
import eu.itesla_project.modules.online.TimeHorizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsDataStorageImpl implements ForecastErrorsDataStorage {
	
	private static final String FORECAST_ERRORS_FILENAME_PREFIX = "forecast_errors_";
	private static final String SAMPLES_FILENAME_PREFIX = "forecast_samples_";
	private static final String STATISTICS_FILENAME_PREFIX = "statistics_";
	private static final String UNCERTAINTIES_GUI_FILENAME_PREFIX  = "gui_uncertainties_";
	private static final String PARAMETERS_FILENAME_PREFIX  = "parameters_";
	private static final Logger LOGGER = LoggerFactory.getLogger(ForecastErrorsDataStorageImpl.class);
	
	private ForecastErrorsDataStorageConfig config = null;
	
	public ForecastErrorsDataStorageImpl(ForecastErrorsDataStorageConfig config) {
		this.config = config;
		LOGGER.info(config.toString());
	}
	
	public ForecastErrorsDataStorageImpl() {
		this(ForecastErrorsDataStorageConfig.load());
	}
	
	public boolean isForecastErrorsDataAvailable(String analysisId, TimeHorizon timeHorizon) {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		return isForecastErrorsFileAvailable(analysisId, timeHorizon, forecastErrorsFileName(timeHorizon));
	}

	public boolean isForecastOfflineSamplesDataAvailable(String analysisId, TimeHorizon timeHorizon) {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		return isForecastErrorsFileAvailable(analysisId, timeHorizon, samplesFileName(timeHorizon));
	}

	private boolean isForecastErrorsFileAvailable(String analysisId, TimeHorizon timeHorizon, String forecastErrorsFileName) {
		Path analysisDataFolder = Paths.get(config.getForecastErrorsDir().toString(), analysisId.replaceAll(" ", "_"));
		LOGGER.info("Data folder for {} analysis is {} ", analysisId, analysisDataFolder);
		if ( Files.exists(analysisDataFolder) ) {
			Path forecastErrorsFile = Paths.get(analysisDataFolder.toString(), forecastErrorsFileName);
			LOGGER.info("Forecast errors analysis file for {} analysis and {} time horizon is {} ", analysisId, timeHorizon.getName(), forecastErrorsFile);
			if ( Files.exists(forecastErrorsFile) )
				return true;
		}
		return false;
	}
	
	public void getForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(destinationFile, "destination file is null");
		LOGGER.debug("Getting forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying to file " + destinationFile.toString());
		getForecastErrorsFile(analysisId, timeHorizon, destinationFile, forecastErrorsFileName(timeHorizon));
	}

	public void getForecastOfflineSamplesFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(destinationFile, "destination file is null");
		LOGGER.debug("Getting forecast offline samples data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying to file " + destinationFile.toString());
		getForecastErrorsFile(analysisId, timeHorizon, destinationFile, samplesFileName(timeHorizon));
	}


	private void getForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile, String forecastErrorsFileName) throws IOException {
		if ( !isForecastErrorsFileAvailable(analysisId, timeHorizon, forecastErrorsFileName) ) {
			String errorMessage = "No forecast errors file " + forecastErrorsFileName + " for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		Path forecastErrorsFile = Paths.get(config.getForecastErrorsDir().toString() + File.separator + analysisId.replaceAll(" ", "_"), forecastErrorsFileName);
		Files.copy(forecastErrorsFile, destinationFile);
	}
	
	public void storeForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path originFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(originFile, "origin file is null");
		LOGGER.debug("Storing forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + originFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, originFile, forecastErrorsFileName(timeHorizon));
	}

	public void storeForecastOfflineSamplesFile(String analysisId, TimeHorizon timeHorizon, Path originFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(originFile, "origin file is null");
		LOGGER.debug("Storing forecast offline samples data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + originFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, originFile, samplesFileName(timeHorizon));
	}


	private void storeForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, Path originFile, String forecastErrorsFileName) throws IOException {
		Path analysisDataFolder = Paths.get(config.getForecastErrorsDir().toString(), analysisId.replaceAll(" ", "_"));
		if ( !Files.exists(analysisDataFolder) ) {
			Files.createDirectories(analysisDataFolder);
		}
		Path forecastErrorsFile = Paths.get(analysisDataFolder.toString(), forecastErrorsFileName);
		if ( Files.exists(forecastErrorsFile) ) {  
			String errorMessage = "Forecast errors file " + forecastErrorsFileName + " for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon already exists";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		Files.copy(originFile, forecastErrorsFile);
	}
	
	@Override
	public boolean deleteForecastErrorsData(String analysisId, TimeHorizon timeHorizon) {
		if ( !isForecastErrorsDataAvailable(analysisId, timeHorizon) ) {
			LOGGER.warn("Forecast errors data not available for analysis {} and time horizon {}", analysisId, timeHorizon);
			return false;
		}
		return deleteForecastErrorsFile(analysisId, timeHorizon, forecastErrorsFileName(timeHorizon));
	}

	public boolean deleteForecastOfflineSamplesData(String analysisId, TimeHorizon timeHorizon) {
		if ( !isForecastOfflineSamplesDataAvailable(analysisId, timeHorizon) ) {
			LOGGER.warn("Forecast offline samples data not available for analysis {} and time horizon {}", analysisId, timeHorizon);
			return false;
		}
		return deleteForecastErrorsFile(analysisId, timeHorizon, samplesFileName(timeHorizon));
	}

	private boolean deleteForecastErrorsFile(String analysisId, TimeHorizon timeHorizon, String forecastErrorsFileName) {
		Path analysisDataFolder = Paths.get(config.getForecastErrorsDir().toString(), analysisId.replaceAll(" ", "_"));
		Path forecastErrorsFile = Paths.get(analysisDataFolder.toString(), forecastErrorsFileName);
		boolean deleted = forecastErrorsFile.toFile().delete();
		if ( analysisDataFolder.toFile().list().length == 0 )
			analysisDataFolder.toFile().delete();
		return deleted;
	}
	
	private String forecastErrorsFileName(TimeHorizon timeHorizon) {
		return FORECAST_ERRORS_FILENAME_PREFIX + timeHorizon.getLabel() + ".mat";
	}

	private String samplesFileName(TimeHorizon timeHorizon) {
		return SAMPLES_FILENAME_PREFIX + timeHorizon.getLabel() + ".mat";
	}

	public boolean areStatisticsAvailable(String analysisId, TimeHorizon timeHorizon) {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		return isForecastErrorsFileAvailable(analysisId, timeHorizon, statisticsFileName(timeHorizon));
	}
	
	public void getStatisticsFile(String analysisId, TimeHorizon timeHorizon, Path destinationFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(destinationFile, "destination file is null");
		LOGGER.debug("Getting statistics for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying to file " + destinationFile.toString());
		getForecastErrorsFile(analysisId, timeHorizon, destinationFile, statisticsFileName(timeHorizon));
	}

	public void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path statisticsFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(forecastErrorsFile, "forecast errors file is null");
		Objects.requireNonNull(statisticsFile, "statistics file is null");
		LOGGER.debug("Storing forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + forecastErrorsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, forecastErrorsFile, forecastErrorsFileName(timeHorizon));
		LOGGER.debug("Storing statistics for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + statisticsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, statisticsFile, statisticsFileName(timeHorizon));
	}

	public ForecastErrorsStatistics getStatistics(String analysisId, TimeHorizon timeHorizon) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		LOGGER.debug("Getting statistics for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon");
		if ( !areStatisticsAvailable(analysisId, timeHorizon) ) {
			String errorMessage = "No statistics for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		String statisticsFileName = config.getForecastErrorsDir().toString() + File.separator + analysisId.trim().replaceAll(" ", "_") + File.separator + statisticsFileName(timeHorizon);
		LOGGER.debug("Reading statistics from {} file", statisticsFileName);
		return Utils.readStatisticsFromFile(Paths.get(statisticsFileName));
	}
	
	@Override
	public boolean deleteStatistics(String analysisId, TimeHorizon timeHorizon) {
		if ( !areStatisticsAvailable(analysisId, timeHorizon) ) {
			LOGGER.warn("Statistics not available for analysis {} and time horizon {}", analysisId, timeHorizon);
			return false;
		}
		return deleteForecastErrorsFile(analysisId, timeHorizon, statisticsFileName(timeHorizon));
	}

	private String statisticsFileName(TimeHorizon timeHorizon) {
		return STATISTICS_FILENAME_PREFIX + timeHorizon.getLabel() + ".csv";
	}
	
	public void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path statisticsFile, Path uncertaintiesGuiFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(forecastErrorsFile, "forecast errors file is null");
		Objects.requireNonNull(statisticsFile, "statistics file is null");
		Objects.requireNonNull(uncertaintiesGuiFile, "uncertainties fot the GUI file is null");
		LOGGER.debug("Storing forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + forecastErrorsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, forecastErrorsFile, forecastErrorsFileName(timeHorizon));
		LOGGER.debug("Storing statistics for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + statisticsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, statisticsFile, statisticsFileName(timeHorizon));
		LOGGER.debug("Storing uncertainties (pro GUI) for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + uncertaintiesGuiFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, uncertaintiesGuiFile, uncertaintiesGuiFileName(timeHorizon));
	}

	public void storeForecastErrorsFiles(String analysisId, TimeHorizon timeHorizon, Path forecastErrorsFile, Path samplesFile, Path statisticsFile, Path uncertaintiesGuiFile) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(forecastErrorsFile, "forecast errors file is null");
		Objects.requireNonNull(samplesFile, "samples file is null");
		Objects.requireNonNull(statisticsFile, "statistics file is null");
		Objects.requireNonNull(uncertaintiesGuiFile, "uncertainties fot the GUI file is null");
		LOGGER.debug("Storing forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + forecastErrorsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, forecastErrorsFile, forecastErrorsFileName(timeHorizon));
		LOGGER.debug("Storing forecast errors data for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + forecastErrorsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, samplesFile, samplesFileName(timeHorizon));
		LOGGER.debug("Storing statistics for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + statisticsFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, statisticsFile, statisticsFileName(timeHorizon));
		LOGGER.debug("Storing uncertainties (pro GUI) for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon: copying from file " + uncertaintiesGuiFile.toString());
		storeForecastErrorsFile(analysisId, timeHorizon, uncertaintiesGuiFile, uncertaintiesGuiFileName(timeHorizon));
	}

	@Override
	public boolean areGuiUncertaintiesAvailable(String analysisId, TimeHorizon timeHorizon) {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		return isForecastErrorsFileAvailable(analysisId, timeHorizon, uncertaintiesGuiFileName(timeHorizon));
	}
	
	@Override
	public String[] getGuiUncertaintiesInjectionIds(String analysisId, TimeHorizon timeHorizon) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		LOGGER.debug("Getting GUI uncertainties injection ids for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon");
		if ( !areGuiUncertaintiesAvailable(analysisId, timeHorizon) ) {
			String errorMessage = "No GUI uncertainties for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		String guiUncertaintiesFileName = config.getForecastErrorsDir().toString() + File.separator + analysisId.trim().replaceAll(" ", "_") + File.separator + uncertaintiesGuiFileName(timeHorizon);
		LOGGER.debug("Reading GUI uncertainties injection ids from {} file", guiUncertaintiesFileName);
		return Utils.readStringsArrayFromMat(Paths.get(guiUncertaintiesFileName), "inj_IDGUI");
	}
	
	@Override
	public double[][] getGuiUncertaintiesLinearCorrelation(String analysisId, TimeHorizon timeHorizon) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		LOGGER.debug("Getting GUI uncertainties linear correlation for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon");
		if ( !areGuiUncertaintiesAvailable(analysisId, timeHorizon) ) {
			String errorMessage = "No GUI uncertainties for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		String guiUncertaintiesFileName = config.getForecastErrorsDir().toString() + File.separator + analysisId.trim().replaceAll(" ", "_") + File.separator + uncertaintiesGuiFileName(timeHorizon);
		LOGGER.debug("Reading GUI uncertainties linear correlation from {} file", guiUncertaintiesFileName);
		return Utils.readDoublesMatrixFromMat(Paths.get(guiUncertaintiesFileName), "correlatio");
	}
	
	@Override
	public double[][] getGuiUncertaintiesLoadings(String analysisId, TimeHorizon timeHorizon) throws IOException {
		Objects.requireNonNull(analysisId, "analysis id is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		LOGGER.debug("Getting GUI uncertainties loadings for " + analysisId + " analysis, " + timeHorizon.getName() + " time horizon");
		if ( !areGuiUncertaintiesAvailable(analysisId, timeHorizon) ) {
			String errorMessage = "No GUI uncertainties for " + analysisId + " analysis and " + timeHorizon.getName() + " time horizon";
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		String guiUncertaintiesFileName = config.getForecastErrorsDir().toString() + File.separator + analysisId.trim().replaceAll(" ", "_") + File.separator + uncertaintiesGuiFileName(timeHorizon);
		LOGGER.debug("Reading GUI uncertainties loadings from {} file", guiUncertaintiesFileName);
		return Utils.readDoublesMatrixFromMat(Paths.get(guiUncertaintiesFileName), "loadings");	
	}
	
	@Override
	public boolean deleteGuiUncertainties(String analysisId, TimeHorizon timeHorizon) {
		if ( !areGuiUncertaintiesAvailable(analysisId, timeHorizon) ) {
			LOGGER.warn("GUI uncertainties not available for analysis {} and time horizon {}", analysisId, timeHorizon);
			return false;
		}
		return deleteForecastErrorsFile(analysisId, timeHorizon, uncertaintiesGuiFileName(timeHorizon));
	}

	private String uncertaintiesGuiFileName(TimeHorizon timeHorizon) {
		return UNCERTAINTIES_GUI_FILENAME_PREFIX + timeHorizon.getLabel() + ".mat";
	}

	@Override
	public List<ForecastErrorsAnalysisDetails> listAnalysis() {
		List<ForecastErrorsAnalysisDetails> analysisList = new ArrayList<ForecastErrorsAnalysisDetails>();
		LOGGER.info("Data folder for analysis is {} ", config.getForecastErrorsDir());
		if ( Files.exists(config.getForecastErrorsDir()) ) {
			File[] analysisFiles = config.getForecastErrorsDir().toFile().listFiles();
			Arrays.sort(analysisFiles, new Comparator<File>(){
			    public int compare(File f1, File f2)
			    {
			        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
			    } });
			for (File analysisFile : analysisFiles) {
				if ( analysisFile.isDirectory() ) {
					ForecastErrorsAnalysisDetails analysis = new ForecastErrorsAnalysisDetails(analysisFile.getName());
					analysis.setAnalysisDate(new DateTime(analysisFile.lastModified()));
					// add list of time horizons where forecast errors data is available
					File[] feDataFiles = analysisFile.listFiles(new FilenameFilter() {
					    public boolean accept(File dir, String name) {
					        return name.toLowerCase().startsWith(FORECAST_ERRORS_FILENAME_PREFIX);
					    }
					});
					for (File feDataFile : feDataFiles) {
						analysis.addFEDataTimeHorizon(TimeHorizon.fromName(feDataFile.getName().substring(FORECAST_ERRORS_FILENAME_PREFIX.length(), 
																										  feDataFile.getName().length()-4)));
					}
					// add list of time horizons where statistics data is available
					File[] statsDataFiles = analysisFile.listFiles(new FilenameFilter() {
					    public boolean accept(File dir, String name) {
					        return name.toLowerCase().startsWith(STATISTICS_FILENAME_PREFIX);
					    }
					});
					for (File statsDataFile : statsDataFiles) {
						analysis.addFEStatisticsTimeHorizon(TimeHorizon.fromName(statsDataFile.getName().substring(STATISTICS_FILENAME_PREFIX.length(), 
																												   statsDataFile.getName().length()-4)));
					}
					// add list of time horizons where gui uncertainties data is available
					File[] uncertainDataFiles = analysisFile.listFiles(new FilenameFilter() {
					    public boolean accept(File dir, String name) {
					        return name.toLowerCase().startsWith(UNCERTAINTIES_GUI_FILENAME_PREFIX);
					    }
					});
					for (File uncertainDataFile : uncertainDataFiles) {
						analysis.addFEGuiUncertaintiesTimeHorizon(TimeHorizon.fromName(uncertainDataFile.getName().substring(UNCERTAINTIES_GUI_FILENAME_PREFIX.length(), 
																												   		 	 uncertainDataFile.getName().length()-4)));
					}
					analysisList.add(analysis);
				}
			}
		}
		return analysisList;
	}
	
	@Override
	public boolean deleteAnalysis(String analysisId, TimeHorizon timeHorizon) {
		boolean deleted = true;
		boolean analysisAvailable = false;
		if ( isForecastErrorsDataAvailable(analysisId, timeHorizon) ) {
			analysisAvailable = true;
			deleted = deleted && deleteForecastErrorsData(analysisId, timeHorizon);
		}
		if ( isForecastOfflineSamplesDataAvailable(analysisId, timeHorizon) ) {
			analysisAvailable = true;
			deleted = deleted && deleteForecastOfflineSamplesData(analysisId, timeHorizon);
		}
		if ( areStatisticsAvailable(analysisId, timeHorizon) ) {
			analysisAvailable = true;
			deleted = deleted && deleteStatistics(analysisId, timeHorizon);
		}
		if ( areGuiUncertaintiesAvailable(analysisId, timeHorizon) ) {
			analysisAvailable = true;
			deleted = deleted && deleteGuiUncertainties(analysisId, timeHorizon);
		}
		// delete also analysis parameters
		if ( isForecastErrorsFileAvailable(analysisId, timeHorizon, parametersFileName(timeHorizon)))
			deleteForecastErrorsFile(analysisId, timeHorizon, parametersFileName(timeHorizon));
		return deleted && analysisAvailable;
	}

	@Override
	public void storeParameters(String analysisId, TimeHorizon timeHorizon, ForecastErrorsAnalyzerParameters parameters) throws IOException {
		Path analysisDataFolder = Paths.get(config.getForecastErrorsDir().toString(), analysisId.replaceAll(" ", "_"));
		if ( !Files.exists(analysisDataFolder) ) {
			Files.createDirectories(analysisDataFolder);
		}
		Path parametersFile = Paths.get(analysisDataFolder.toString(), parametersFileName(timeHorizon));
		if ( Files.exists(parametersFile) )
			parametersFile.toFile().delete();
		parameters.toFile(parametersFile);
	}

	@Override
	public ForecastErrorsAnalyzerParameters getParameters(String analysisId, TimeHorizon timeHorizon) throws IOException {
		Path analysisDataFolder = Paths.get(config.getForecastErrorsDir().toString(), analysisId.replaceAll(" ", "_"));
		if ( Files.exists(analysisDataFolder) ) {
			Path parametersFile = Paths.get(analysisDataFolder.toString(), parametersFileName(timeHorizon));
			if ( Files.exists(parametersFile) )
				return ForecastErrorsAnalyzerParameters.fromFile(parametersFile);
		}
		LOGGER.warn("Parameters not available for analysis {} and time horizon {}", analysisId, timeHorizon);
		return null;
	}
	
	private String parametersFileName(TimeHorizon timeHorizon) {
		return PARAMETERS_FILENAME_PREFIX + timeHorizon.getLabel() + ".properties";
	}
 
}
