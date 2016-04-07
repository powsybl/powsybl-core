/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db.debug;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LoadData implements EquipmentData {
	
	private final String loadId;
	private final String busId;
	private final boolean isConnected;
	private final float nominalVoltage;
	private final float activePower;
	private final float reactivePower;
	
	public LoadData(String loadId, String busId, boolean isConnected, float nominalVoltage, float activePower, float reactivePower) {
		this.loadId = loadId;
		this.busId = busId;
		this.isConnected = isConnected;
		this.nominalVoltage = nominalVoltage;
		this.activePower = activePower;
		this.reactivePower = reactivePower;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"bus id", 
					"connected",
					"nominal voltage",
					"active power",
					"reactive power"
				};
	}
	
	@Override
	public String getFieldValue(String fieldName) {
		switch (fieldName) {
		case "id":
			return loadId;
		case "bus id":
			return busId;
		case "connected":
			return Boolean.toString(isConnected);
		case "nominal voltage":
			return Float.toString(nominalVoltage);
		case "active power":
			return Float.toString(activePower);
		case "reactive power":
			return Float.toString(reactivePower);
		default:
			throw new RuntimeException("no " + fieldName + " available in load data");
		}
	}

}
