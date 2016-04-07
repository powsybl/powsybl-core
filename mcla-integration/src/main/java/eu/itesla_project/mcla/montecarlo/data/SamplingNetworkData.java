/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo.data;

import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class SamplingNetworkData {
	
	ArrayList<BusData> busesData = null;
	ArrayList<GeneratorData> generatorsData = null;
	ArrayList<LoadData> loadsData = null;
	
	public SamplingNetworkData(ArrayList<BusData> busesData, ArrayList<GeneratorData> generatorsData, ArrayList<LoadData> loadsData) {
		Objects.requireNonNull(busesData, "buses data is null");
		Objects.requireNonNull(generatorsData, "generators data is null");
		Objects.requireNonNull(loadsData, "loads data is null");
		this.busesData = busesData;
		this.generatorsData = generatorsData;
		this.loadsData = loadsData;
	}
	
	public ArrayList<BusData> getBusesData() {
		return busesData;
	}
	public ArrayList<GeneratorData> getGeneratorsData() {
		return generatorsData;
	}
	public ArrayList<LoadData> getLoadsData() {
		return loadsData;
	}
}
