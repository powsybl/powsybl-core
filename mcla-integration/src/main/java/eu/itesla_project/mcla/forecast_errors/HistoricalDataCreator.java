/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.forecast_errors.data.ForecastErrorsHistoricalData;
import eu.itesla_project.mcla.forecast_errors.data.StochasticVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.ArrayTable;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.sampling.util.Utils;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class HistoricalDataCreator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalDataCreator.class);
	
	Network network;
	private ArrayList<String> generatorsIds = new ArrayList<String>();
	private ArrayList<String> loadsIds = new ArrayList<String>();
	private ForecastErrorsHistoricalData forecastErrorsHistoricalData;

	public HistoricalDataCreator(Network network, ArrayList<String> generatorsIds, ArrayList<String> loadsIds) {
		Objects.requireNonNull(network, "network is null");
		Objects.requireNonNull(generatorsIds, "generatorsIds is null");
		Objects.requireNonNull(loadsIds, "loadsIds is null");
		this.network = network;
		this.generatorsIds = generatorsIds;
		this.loadsIds = loadsIds;
	}
	
	public HistoricalDataCreator(Network network) {
		this(network, NetworkUtils.getRenewableGeneratorsIds(network), NetworkUtils.getLoadsIds(network));
	}
	
	public ForecastErrorsHistoricalData createForecastErrorsHistoricalData(Path historicalDataCsvFile) throws IOException {
		LOGGER.debug("Getting stochastic variables from {} network", network.getId());
		ArrayList<StochasticVariable> stochasticVariables = FEANetworkUtils.getStochasticVariables(network, generatorsIds, loadsIds);
		LOGGER.debug("Reading historical data from cvs file {}", historicalDataCsvFile.toString());
		forecastErrorsHistoricalData = loadHistoricalDataFromCsvFile(historicalDataCsvFile, generatorsIds, loadsIds, stochasticVariables);
		//LOGGER.debug("Forecast errors historical data:\n" + forecastErrorsHistoricalData.toString());
		return forecastErrorsHistoricalData;
	}
	
	public ForecastErrorsHistoricalData getForecastErrorsHistoricalData() {
		return forecastErrorsHistoricalData;
	}
	
	private ForecastErrorsHistoricalData loadHistoricalDataFromCsvFile(Path historicalDataCsvFile, ArrayList<String> generatorsIds, ArrayList<String> loadsIds,
																	   ArrayList<StochasticVariable> stochasticVariables) throws IOException {
		ForecastErrorsHistoricalData forecastErrorsHistoricalData = null;

		Integer rowsIndexes[] = getRowsIndexes(historicalDataCsvFile);
		String columnsIndexes[] = getColumnsIndexes(generatorsIds, loadsIds);
		ArrayTable<Integer, String, Float> forecastsData = ArrayTable.create(Arrays.asList(rowsIndexes), Arrays.asList(columnsIndexes));
		ArrayTable<Integer, String, Float> snapshotsData = ArrayTable.create(Arrays.asList(rowsIndexes), Arrays.asList(columnsIndexes));

		ICsvMapReader csvMapReader = null;
		int rowcount = 0;
		boolean odd = false;
		try {
			csvMapReader = new CsvMapReader(new FileReader(historicalDataCsvFile.toFile()), CsvPreference.STANDARD_PREFERENCE);
			final String[] headers = csvMapReader.getHeader(true);
			final CellProcessor[] rowProcessors = new CellProcessor[headers.length];
			Map<String, Object> componentMap;
			while ((componentMap = csvMapReader.read(headers, rowProcessors)) != null) {
				String datetime = (String) componentMap.get("datetime");
				int forecastTime = (int) Float.parseFloat((String) componentMap.get("forecastTime"));
				if (forecastTime == 0) {
					snapshotsData.put(rowcount, "datetime", Float.valueOf(datetime));
					snapshotsData.put(rowcount, "forecastTime", new Float(forecastTime));
				} else {
					forecastsData.put(rowcount, "datetime", Float.valueOf(datetime));
					forecastsData.put(rowcount, "forecastTime", new Float(forecastTime));
				}
				for (String generatorId : generatorsIds) {
					// generators active power
					String activePowerValue = (String) componentMap.get(generatorId + "_P");
					if (forecastTime == 0)
						snapshotsData.put(rowcount, generatorId + "_P", (activePowerValue != null) ? Float.valueOf(activePowerValue) : Float.NaN);
					else
						forecastsData.put(rowcount, generatorId + "_P", (activePowerValue != null) ? Float.valueOf(activePowerValue) : Float.NaN);
					// generators reactive power
					String reactivePowerValue = (String) componentMap.get(generatorId + "_Q");
					if (forecastTime == 0)
						snapshotsData.put(rowcount, generatorId + "_Q", (reactivePowerValue != null) ? Float.valueOf(reactivePowerValue) : Float.NaN);
					else
						forecastsData.put(rowcount, generatorId + "_Q", (reactivePowerValue != null) ? Float.valueOf(reactivePowerValue) : Float.NaN);
				}
				for (String loadId : loadsIds) {
					// loads active power
					String activePowerValue = (String) componentMap.get(loadId+ "_P");
					if (forecastTime == 0)
						snapshotsData.put(rowcount, loadId + "_P", (activePowerValue != null) ? Float.valueOf(activePowerValue) : Float.NaN);
					else
						forecastsData.put(rowcount, loadId + "_P", (activePowerValue != null) ? Float.valueOf(activePowerValue) : Float.NaN);
					// loads reactive power
					String reactivePowerValue = (String) componentMap.get(loadId+ "_Q");
					if (forecastTime == 0)
						snapshotsData.put(rowcount, loadId + "_Q", (reactivePowerValue != null) ? Float.valueOf(reactivePowerValue) : Float.NaN);
					else
						forecastsData.put(rowcount, loadId + "_Q", (reactivePowerValue != null) ? Float.valueOf(reactivePowerValue) : Float.NaN);
				}
				if (odd) {
					rowcount++;
					odd = false;
				} else {
					odd = true;
				}
			}
			LOGGER.info("Loaded {} records of historical data from csv file {}", rowcount, historicalDataCsvFile.toString());
		} catch (IOException e) {
			LOGGER.error("Error loading historical data from cvs file" + historicalDataCsvFile.toString() + ": " + e.getMessage());
			throw e;
		} finally {
			if (csvMapReader != null)
				try {
					csvMapReader.close();
				} catch (IOException e) {
					LOGGER.error("Error closing CSV map reader: " + e.getMessage());
				}
		}
		forecastErrorsHistoricalData = new ForecastErrorsHistoricalData(generatorsIds, loadsIds, stochasticVariables, forecastsData, snapshotsData);
		return forecastErrorsHistoricalData;
	}

	protected Integer[] getRowsIndexes(Path csvFilePath) throws IOException {
		int csvLength = Utils.countLines(csvFilePath);
		int rowsIndex = (csvLength - 1) / 2;
		Integer rowsIndexes[] = new Integer[rowsIndex];
		for (int i = 0; i < rowsIndexes.length; i++) {
			rowsIndexes[i] = i;
		}
		return rowsIndexes;
	}

	protected String[] getColumnsIndexes(ArrayList<String> generatorsIds, ArrayList<String> loadsIds) {
		int columnsNumber = 2 + ( ( generatorsIds.size() + loadsIds.size() ) * 2 );
		String columnsIndexes[] = new String[columnsNumber];
		int count = 0;
		// datetime
		columnsIndexes[count] = "datetime";
		count++;
		// forecast time
		columnsIndexes[count] = "forecastTime";
		count++;
		// generators
		for (String generatorId : generatorsIds) {
			columnsIndexes[count] = generatorId + "_P";
			count++;
			columnsIndexes[count] = generatorId + "_Q";
			count++;
		}
		// loads
		for (String loadId : loadsIds) {
			columnsIndexes[count] = loadId + "_P";
			count++;
			columnsIndexes[count] = loadId + "_Q";
			count++;
		}
		return columnsIndexes;
	}

}
