/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import eu.itesla_project.mcla.forecast_errors.data.ForecastErrorsHistoricalData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayTable;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLStructure;

import eu.itesla_project.mcla.forecast_errors.data.StochasticVariable;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class FEAMatFileWriter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FEAMatFileWriter.class);
	
	private Path matFile;
	
	public FEAMatFileWriter(Path matFile) {
		Objects.requireNonNull(matFile, "mat file is null");
		this.matFile = matFile;
	}
	
	public void writeHistoricalData(ForecastErrorsHistoricalData forecastErrorsHistoricalData) throws IOException {
		Objects.requireNonNull(forecastErrorsHistoricalData, "forecast errors historical data is null");
//		LOGGER.debug("Preparing stochastic variables mat data");
//		MLStructure stochasticVariables = stochasticVariablesAsMLStructure(forecastErrorsHistoricalData.getStochasticVariables());
		LOGGER.debug("Preparing injections mat data");
		MLCell injections = histoDataHeadersAsMLChar(forecastErrorsHistoricalData.getForecastsData().columnKeyList());
		LOGGER.debug("Preparing injections countries mat data");
		MLCell injectionsCountries =  injectionCountriesAsMLChar(forecastErrorsHistoricalData.getStochasticVariables());
		LOGGER.debug("Preparing forecasts mat data");
		MLDouble forecastsData = histoDataAsMLDouble("forec_filt", forecastErrorsHistoricalData.getForecastsData());
		LOGGER.debug("Preparing snapshots mat data");
		MLDouble snapshotsData = histoDataAsMLDouble("snap_filt", forecastErrorsHistoricalData.getSnapshotsData());
		LOGGER.debug("Saving mat data into " + matFile.toString());
		List<MLArray> mlarray= new ArrayList<>();
//		mlarray.add((MLArray) stochasticVariables );
		mlarray.add((MLArray) injections );
		mlarray.add((MLArray) forecastsData );
		mlarray.add((MLArray) snapshotsData );
		mlarray.add((MLArray) injectionsCountries );
		MatFileWriter writer = new MatFileWriter();
        writer.write(matFile.toFile(), mlarray);
	}

	private MLCell histoDataHeadersAsMLChar(List<String> histoDataHeaders) {
		int colsSize =histoDataHeaders.size() - 2;
		MLCell injections = new MLCell("inj_ID", new int[]{1,colsSize});
		int i = 0;
		for ( String colkey : histoDataHeaders ) {
			if ( colkey.equals("forecastTime") || colkey.equals("datetime"))
				continue;
			injections.set(new MLChar("", colkey), 0, i);
			i++;
		}
		return injections;
	}

	private MLStructure stochasticVariablesAsMLStructure(List<StochasticVariable> stochasticVariables) {
		MLStructure stochVars = null;
		stochVars = new MLStructure("stochasticVariables", new int[]{1,stochasticVariables.size()});
		int i = 0;
		for (StochasticVariable stochasticVariable : stochasticVariables) {
			putStochasticVariablesIntoMLStructure(stochasticVariable, stochVars, i);
			i++;
		}
		return stochVars;
	}

	private void putStochasticVariablesIntoMLStructure(StochasticVariable stochasticVariable, MLStructure stochVars, int i) {
		LOGGER.debug("Preparing mat data for stochastic variable " + stochasticVariable.getId());
		// id
		MLChar id = new MLChar("", stochasticVariable.getId());
		stochVars.setField("id", id, 0, i);
		// type
		MLChar type = new MLChar("", stochasticVariable.getType());
		stochVars.setField("type", type, 0, i);
	}
	
	private MLCell injectionCountriesAsMLChar(List<StochasticVariable> stochasticVariables) {
		int colsSize =stochasticVariables.size()*2;
		MLCell injectionsCountries = new MLCell("nat_ID", new int[]{1,colsSize});
		int i = 0;
		for ( StochasticVariable injection : stochasticVariables ) {
			injectionsCountries.set(new MLChar("", injection.getCountry().name()), 0, i);
			i++;
			injectionsCountries.set(new MLChar("", injection.getCountry().name()), 0, i); // twice, for P and Q
			i++;
		}
		return injectionsCountries;
	}

	private MLDouble histoDataAsMLDouble(String name, ArrayTable<Integer, String, Float> histoData) {
		int rowsSize = histoData.rowKeySet().size();
        int colsSize = histoData.columnKeySet().size();
        MLDouble mlDouble = new MLDouble( name, new int[] {rowsSize, colsSize} );
        int i = 0;
        for ( Integer rowKey : histoData.rowKeyList() ) {
        	int j = 0;
			for ( String colkey : histoData.columnKeyList() ) {
				Float v = histoData.get(rowKey, colkey);
				mlDouble.set(new Double(v), i, j);
				j++;
			}
			i++;
		}
        return mlDouble;
	}

}
