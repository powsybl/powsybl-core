/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.sampling.util;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Wp41HistoData {
	
	//these are the ids for which actually exist both P and Q values in the histo db
	private final List<String> historicalGensIds;
	private final List<String> historicalLoadsIds;
	private final List<String> historicalDanglingLinesIds;

	//joined table of historical data (renewables data , loads, ..)
    private final ArrayTable<Integer,String,Float> hdTable;

	public List<String> getHistoricalGensIds() {
		return historicalGensIds;
	}

	public List<String> getHistoricalLoadsIds() {
		return historicalLoadsIds;
	}

	public List<String> getHistoricalDanglingLinesIds() {
		return historicalDanglingLinesIds;
	}

	public ArrayTable<Integer, String, Float> getHdTable() {
		return hdTable;
	}

	public Wp41HistoData(List<String> historicalGensIds,
			             List<String> historicalLoadsIds,
			             List<String> historicalDanglingLinesIds,
						 ArrayTable<Integer, String, Float> hdTable) {
		this.historicalGensIds = historicalGensIds;
		this.historicalLoadsIds = historicalLoadsIds;
		this.historicalDanglingLinesIds = historicalDanglingLinesIds;
		this.hdTable = hdTable;
	}

}
