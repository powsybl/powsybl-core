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
public class GeneratorData {
	
	public static final int GENERATOR_TYPE_CONVENTIONAL = 0;
	public static final int GENERATOR_TYPE_WIND         = 1;
	public static final int GENERATOR_TYPE_SOLAR        = 2;
	
	 // fuel type: 1 = hydro, 2 = gas, 3 = oil, 4 = nuclear, 5 = coal
	public static final int FUEL_TYPE_RES     = 0;
	public static final int FUEL_TYPE_HYDRO   = 1;
	public static final int FUEL_TYPE_GAS     = 2;
	public static final int FUEL_TYPE_OIL     = 3;
	public static final int FUEL_TYPE_NUCLEAR = 4;
	public static final int FUEL_TYPE_COAL    = 5;

	String generatorId;
	String busId;
	int busIndex = -1;
	boolean connected;
	double actvePower = Double.NaN;
	double reactvePower = Double.NaN;
	double minActivePower = Double.NaN;
	double maxActivePower = Double.NaN;
	double minReactivePower = Double.NaN;
	double maxReactivePower = Double.NaN;
	double nominalPower = Double.NaN;
	int renewableEnergySource = -1;
	int fuelType = -1;
	boolean dispatchable;
	
	public GeneratorData(String generatorId) {
		Objects.requireNonNull(generatorId, "generator id is null");
		this.generatorId = generatorId;
	}
	
	public String getGeneratorId() {
		return generatorId;
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
	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
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
	public double getMinActivePower() {
		return minActivePower;
	}
	public void setMinActivePower(double minActivePower) {
		this.minActivePower = minActivePower;
	}
	public double getMaxActivePower() {
		return maxActivePower;
	}
	public void setMaxActivePower(double maxActivePower) {
		this.maxActivePower = maxActivePower;
	}
	public double getMinReactivePower() {
		return minReactivePower;
	}
	public void setMinReactivePower(double minReactivePower) {
		this.minReactivePower = minReactivePower;
	}
	public double getMaxReactivePower() {
		return maxReactivePower;
	}
	public void setMaxReactivePower(double maxReactivePower) {
		this.maxReactivePower = maxReactivePower;
	}
	public double getNominalPower() {
		return nominalPower;
	}
	public void setNominalPower(double nominalPower) {
		this.nominalPower = nominalPower;
	}
	public int getRenewableEnergySource() {
		return renewableEnergySource;
	}
	public void setRenewableEnergySource(int renewableEnergySource) {
		this.renewableEnergySource = renewableEnergySource;
	}
	public int getFuelType() {
		return fuelType;
	}
	public void setFuelType(int fuelType) {
		this.fuelType = fuelType;
	}
	public boolean isDispatchable() {
		return dispatchable;
	}
	public void setDispatchable(boolean dispatchable) {
		this.dispatchable = dispatchable;
	}

	@Override
	public String toString() {
		String generatorData = "generator[id=" + this.getGeneratorId();
		generatorData += "; busId=" + this.getBusId();
		generatorData += "; busIndex=" + this.getBusIndex();
		generatorData += "; connected=" + this.isConnected();
		generatorData += "; P=" + this.getActvePower();
		generatorData += "; Q=" + this.getReactvePower();
		generatorData += "; Pmin=" + this.getMinActivePower();
		generatorData += "; Pmax=" + this.getMaxActivePower();
		generatorData += "; Qmin=" + this.getMinReactivePower();
		generatorData += "; Qmax=" + this.getMaxReactivePower();
		generatorData += "; Anom=" + this.getNominalPower();
		generatorData += "; RES=" + this.getRenewableEnergySource();
		generatorData += "; Fuel=" + this.getFuelType();
		generatorData += "; dispch=" + this.isDispatchable();
		generatorData += "]";
		return generatorData;
	}
	
	
}
