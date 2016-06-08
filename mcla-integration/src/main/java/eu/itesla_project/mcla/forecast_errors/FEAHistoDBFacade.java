/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.forecast_errors;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modules.online.TimeHorizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class FEAHistoDBFacade {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FEAHistoDBFacade.class);
	
	private final int MAXRECORDSNUM = 100000;
	
	DataMiningFacadeRestConfig config;
	TimeHorizon timeHorizon;
	Interval histoInterval;
    ArrayList<String> generatorsIds;
    ArrayList<String> loadsIds;

	public FEAHistoDBFacade(DataMiningFacadeRestConfig config, TimeHorizon timeHorizon, Interval histoInterval,
						    ArrayList<String> generatorsIds, ArrayList<String> loadsIds) {
		Objects.requireNonNull(config, "config is null");
		Objects.requireNonNull(timeHorizon, "time horizon is null");
		Objects.requireNonNull(histoInterval, "histo interval is null");
		Objects.requireNonNull(generatorsIds, "generatorsIds is null");
		Objects.requireNonNull(loadsIds, "loadsIds is null");
		
		this.config = config;
		this.timeHorizon = timeHorizon;
		this.histoInterval = histoInterval;
		this.generatorsIds = generatorsIds;
		this.loadsIds = loadsIds;
	}

	public void historicalDataToCsvFile(Path historicalDataCsvFile) throws Exception {
		String query = historicalDataQuery();
		LOGGER.info("Downloading data from HistoDB to file " + historicalDataCsvFile);
		LOGGER.debug("HistoDB query = " + query);
		HttpsClientHelper.remoteDataToFilePOST(
				config.getRestServiceUrl(), 
				query, 
				config.getServiceUser(), 
				config.getServicePassword(), 
				historicalDataCsvFile.toString());
	}
	
	protected String historicalDataQuery() {
		String query = "headers=true";
		query += "&count=" + MAXRECORDSNUM;
		DateTimeFormatter dateFormatter = ISODateTimeFormat.date();
		DateTime intervalStart = histoInterval.getStart();
	    DateTime intervalEnd = histoInterval.getEnd();
		query += "&time=[" + intervalStart.toString(dateFormatter) + "," + intervalEnd.toString(dateFormatter) + "]";
		switch (timeHorizon) {
			case DACF:
				query += "&horizon=" + timeHorizon.getName();
				break;
			default:
				throw new AssertionError();
		}
		if ( timeHorizon.getForecastTime() >= 0 )
			query += "&forecast=" + timeHorizon.getForecastTime();
		query += "&cols=datetime,horizon,forecastTime";
		for ( String generatorId : generatorsIds ) {
			query += "," + generatorId + "_P" + "," + generatorId + "_Q";
		}
		for ( String loadId : loadsIds ) {
			query += "," + loadId + "_P" + "," + loadId + "_Q";
		}
		return query;
	}

}
