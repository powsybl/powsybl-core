/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db.debug;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class NetworkData {
	
	private final String networkId;	
	private List<BusData> busesData = new ArrayList<BusData>();
	private List<LineData> linesData = new ArrayList<LineData>();
	private List<Tfo2WData> tfos2WData = new ArrayList<Tfo2WData>();
	private List<Tfo3WData> tfos3WData = new ArrayList<Tfo3WData>();
	private List<GeneratorData> generatorsData = new ArrayList<GeneratorData>();
	private List<LoadData> loadsData = new ArrayList<LoadData>();
	
	public NetworkData(String networkId) {
		this.networkId = networkId;
	}
	
	public String getNetworkId() {
		return networkId;
	}
	
	public List<BusData> getBusesData() {
		return busesData;
	}

	public List<LineData> getLinesData() {
		return linesData;
	}

	public List<Tfo2WData> getTfos2WData() {
		return tfos2WData;
	}

	public List<Tfo3WData> getTfos3WData() {
		return tfos3WData;
	}

	public List<GeneratorData> getGeneratorsData() {
		return generatorsData;
	}
	
	public List<LoadData> getLoadsData() {
		return loadsData;
	}
	
	public void addBusData(BusData busData) {
		busesData.add(busData);
	}
	
	public void addLineData(LineData lineData) {
		linesData.add(lineData);
	}

	public void addTfo2WData(Tfo2WData tfo2WData) {
		tfos2WData.add(tfo2WData);
	}
	
	public void addTfo3WData(Tfo3WData tfo3WData) {
		tfos3WData.add(tfo3WData);
	}
	
	public void addGeneratorData(GeneratorData generatorData) {
		generatorsData.add(generatorData);
	}
	
	public void addLoadData(LoadData loadData) {
		loadsData.add(loadData);
	}

}
