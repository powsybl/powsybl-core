/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.mcla;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import eu.itesla_project.modules.online.TimeHorizon;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ForecastErrorsAnalysisDetails {
	
	final String analysisId;
	DateTime analysisDate;
	List<TimeHorizon> forecastErrorsDataList = new ArrayList<TimeHorizon>();
	List<TimeHorizon> forecastErrorsStatisticsList = new ArrayList<TimeHorizon>();
	List<TimeHorizon> forecastErrorsGuiUncertaintiesList = new ArrayList<TimeHorizon>();
	
	public ForecastErrorsAnalysisDetails(String analysisId) {
		this.analysisId = analysisId;
	}
	
	public String getAnalysisId() {
		return analysisId;
	}

	public List<TimeHorizon> getForecastErrorsDataList() {
		return forecastErrorsDataList;
	}

	public List<TimeHorizon> getForecastErrorsStatisticsList() {
		return forecastErrorsStatisticsList;
	}
	
	public List<TimeHorizon> getForecastErrorsGuiUncertaintiesList() {
		return forecastErrorsGuiUncertaintiesList;
	}
	
	public void addFEDataTimeHorizon(TimeHorizon timeHorizon) {
		forecastErrorsDataList.add(timeHorizon);
	}
	
	public void addFEStatisticsTimeHorizon(TimeHorizon timeHorizon) {
		forecastErrorsStatisticsList.add(timeHorizon);
	}
	
	public void addFEGuiUncertaintiesTimeHorizon(TimeHorizon timeHorizon) {
		forecastErrorsGuiUncertaintiesList.add(timeHorizon);
	}
	
	public DateTime getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(DateTime analysisDate) {
		this.analysisDate = analysisDate;
	}

}
