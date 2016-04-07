/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.mcla.montecarlo.data;

import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LoadData {
	
	String loadId;
	String busId;
	int busIndex = -1;
	double actvePower = Double.NaN;
	double reactvePower = Double.NaN;
	double voltage = Double.NaN;
	boolean connected;
	
	public LoadData(String loadId) {
		Objects.requireNonNull(loadId, "load id is null");
		this.loadId = loadId;
	}
	
	public String getLoadId() {
		return loadId;
	}
	public String getBusId() {
		return busId;
	}
	public void setBusId(String busId) {
		this.busId = busId;
	}
	public int getBusIndex() {
		return busIndex;
	}
	public void setBusIndex(int busIndex) {
		this.busIndex = busIndex;
	}
	public double getActvePower() {
		return actvePower;
	}
	public void setActvePower(double actvePower) {
		this.actvePower = actvePower;
	}
	public double getReactvePower() {
		return reactvePower;
	}
	public void setReactvePower(double reactvePower) {
		this.reactvePower = reactvePower;
	}
	public double getVoltage() {
		return voltage;
	}
	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	@Override
	public String toString() {
		String loadData = "load[id=" + this.getLoadId();
		loadData += "; busId=" + this.getBusId();
		loadData += "; busIndex=" + this.getBusIndex();
		loadData += "; connected=" + this.isConnected();
		loadData += "; P=" + this.getActvePower();
		loadData += "; Q=" + this.getReactvePower();
		loadData += "; V=" + this.getVoltage();
		loadData += "]";
		return loadData;
	}

}
