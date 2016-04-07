/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors.data;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayTable;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsHistoricalData {
	
	List<String> generatorsIds = new ArrayList<String>();
	List<String> loadsIds = new ArrayList<String>();
	List<StochasticVariable> stochasticVariables = new ArrayList<StochasticVariable>(); 
	ArrayTable<Integer,String,Float> forecastsData = null;
	ArrayTable<Integer,String,Float> snapshotsData = null;
	
	public ForecastErrorsHistoricalData(List<String> generatorsIds, List<String> loadsIds,
										List<StochasticVariable> stochasticVariables,
										ArrayTable<Integer,String,Float> forecastsData,
										ArrayTable<Integer,String,Float> snapshotsData) {
		this.generatorsIds = generatorsIds;
		this.loadsIds = loadsIds;
		this.stochasticVariables = stochasticVariables;
		this.forecastsData = forecastsData;
		this.snapshotsData = snapshotsData;
	}
	
	public List<String> getGeneratorsIds() {
		return generatorsIds;
	}
	public List<String> getLoadsIds() {
		return loadsIds;
	}
	public List<StochasticVariable> getStochasticVariables() {
		return stochasticVariables;
	}
	public ArrayTable<Integer, String, Float> getForecastsData() {
		return forecastsData;
	}
	public ArrayTable<Integer, String, Float> getSnapshotsData() {
		return snapshotsData;
	}
	
	@Override
	public String toString() {
		// generators ids
		String forecastErrorsHistoricalData = "genIds[";
		for ( String generatorId : getGeneratorsIds() ) {
			forecastErrorsHistoricalData += generatorId + ","; 
		}
		if ( getGeneratorsIds().size() > 0 )
			forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
		forecastErrorsHistoricalData += "]";
		// loads ids
		forecastErrorsHistoricalData += "\nloadIds[";
		for ( String loadId : getLoadsIds() ) {
			forecastErrorsHistoricalData += loadId + ","; 
		}
		if ( getLoadsIds().size() > 0 )
			forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
		forecastErrorsHistoricalData += "]";
		// stochastic variables
		forecastErrorsHistoricalData += "\nstochVars[";
		for ( StochasticVariable stocasticVariable : getStochasticVariables() ) {
			forecastErrorsHistoricalData += stocasticVariable + ","; 
		}
		if ( getStochasticVariables().size() > 0 )
			forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
		forecastErrorsHistoricalData += "]";
		// forecasts data
		forecastErrorsHistoricalData += "\nforecastData[\n";
		if ( getForecastsData() != null) {
			forecastErrorsHistoricalData += "[";
			for ( String columnName : getForecastsData().columnKeyList() ) {
				forecastErrorsHistoricalData += columnName + ",";
			}
			if ( getForecastsData().columnKeyList().size() > 0 )
				forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
			forecastErrorsHistoricalData += "]";
			for ( Integer rowName : getForecastsData().rowKeyList() ) {
				forecastErrorsHistoricalData += "\n[";
				for ( String columnName : getForecastsData().columnKeyList() ) {
					forecastErrorsHistoricalData += getForecastsData().get(rowName, columnName) + ",";
				}
				if ( getForecastsData().columnKeyList().size() > 0 )
					forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
				forecastErrorsHistoricalData += "]";
			}
		}
		forecastErrorsHistoricalData += "]";
		// snapshots data
		forecastErrorsHistoricalData += "\nsnapshotData[\n";
		if ( getForecastsData() != null) {
			forecastErrorsHistoricalData += "[";
			for ( String columnName : getSnapshotsData().columnKeyList() ) {
				forecastErrorsHistoricalData += columnName + ",";
			}
			if ( getSnapshotsData().columnKeyList().size() > 0 )
				forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
			forecastErrorsHistoricalData += "]";
			for ( Integer rowName : getSnapshotsData().rowKeyList() ) {
				forecastErrorsHistoricalData += "\n[";
				for ( String columnName : getSnapshotsData().columnKeyList() ) {
					forecastErrorsHistoricalData += getSnapshotsData().get(rowName, columnName) + ",";
				}
				if ( getSnapshotsData().columnKeyList().size() > 0 )
					forecastErrorsHistoricalData = forecastErrorsHistoricalData.substring(0, forecastErrorsHistoricalData.length()-1);
				forecastErrorsHistoricalData += "]";
			}
		}
		forecastErrorsHistoricalData += "]";
		return forecastErrorsHistoricalData;
	}
	
	
}
