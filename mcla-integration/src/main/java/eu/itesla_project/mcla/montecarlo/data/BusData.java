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
public class BusData {
	
	public static final int BUS_TYPE_SLACK = 1;
	public static final int BUS_TYPE_PQ    = 2;
	public static final int BUS_TYPE_PV    = 3;
	
	String busId;
	String busName;
	int busIndex = -1;
	int busType = -1;
	double nominalVoltage = Double.NaN;
	double voltage = Double.NaN;
	double angle = Double.NaN;
	double minVoltage = Double.NaN;
	double maxVoltage = Double.NaN;
	double activePower = Double.NaN;
	double reactivePower = Double.NaN;
	
	public BusData(String busId) {
		Objects.requireNonNull(busId, "bus id is null");
		this.busId = busId;
	}
	
	public String getBusId() {
		return busId;
	}
	public String getBusName() {
		return busName;
	}
	public void setBusName(String busName) {
		this.busName = busName;
	}
	public int getBusIndex() {
		return busIndex;
	}
	public void setBusIndex(int busIndex) {
		this.busIndex = busIndex;
	}
	public int getBusType() {
		return busType;
	}
	public void setBusType(int busType) {
		this.busType = busType;
	}
	public double getNominalVoltage() {
		return nominalVoltage;
	}
	public void setNominalVoltage(double nominalVoltage) {
		this.nominalVoltage = nominalVoltage;
	}
	public double getVoltage() {
		return voltage;
	}
	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	public double getMinVoltage() {
		return minVoltage;
	}
	public void setMinVoltage(double minVoltage) {
		this.minVoltage = minVoltage;
	}
	public double getMaxVoltage() {
		return maxVoltage;
	}
	public void setMaxVoltage(double maxVoltage) {
		this.maxVoltage = maxVoltage;
	}
	public double getActivePower() {
		return activePower;
	}
	public void setActivePower(double activePower) {
		this.activePower = activePower;
	}
	public double getReactivePower() {
		return reactivePower;
	}
	public void setReactivePower(double reactivePower) {
		this.reactivePower = reactivePower;
	}

	@Override
	public String toString() {
		String busData = "bus[id=" + this.getBusId();
		busData += "; name=" + this.getBusName();
		busData += "; type=" + this.getBusType();
		busData += "; index=" + this.getBusIndex();
		busData += "; Vnom=" + this.getNominalVoltage();
		busData += "; V=" + this.getVoltage();
		busData += "; angle=" + this.getVoltage();
		busData += "; Vmin=" + this.getVoltage();
		busData += "; Vmax=" + this.getMaxVoltage();
		busData += "; P=" + this.getMaxVoltage();
		busData += "; Q=" + this.getMaxVoltage();
		busData += "]";
		return busData;
	}
	
	
}
