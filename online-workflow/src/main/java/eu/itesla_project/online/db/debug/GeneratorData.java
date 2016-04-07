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
public class GeneratorData implements EquipmentData {
	
	private final String generatorId;
	private final String busId;
	private final boolean isConnected;
	private final float apparentPower;
	private final float activePower;
	private final float reactivePower;
	private final float nominalPower;
	private final float maxReactivePower;
	private final float minReactivePower;
		
	
	public GeneratorData(String generatorId, String busId, boolean isConnected, float apparentPower, float activePower, 
						 float reactivePower, float nominalPower, float maxReactivePower, float minReactivePower) {
		this.generatorId = generatorId;
		this.busId = busId;
		this.isConnected = isConnected;
		this.apparentPower = apparentPower;
		this.activePower = activePower;
		this.reactivePower = reactivePower;
		this.nominalPower = nominalPower;
		this.maxReactivePower = maxReactivePower;
		this.minReactivePower = minReactivePower;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"bus id", 
					"connected",
					"apparent power",
					"active power",
					"reactive power",
					"nominal power",
					"max q",
					"min q"
				};
	}

	@Override
	public String getFieldValue(String fieldName) {
		switch (fieldName) {
		case "id":
			return generatorId;
		case "bus id":
			return busId;
		case "connected":
			return Boolean.toString(isConnected);
		case "apparent power":
			return Float.toString(apparentPower);
		case "active power":
			return Float.toString(activePower);
		case "reactive power":
			return Float.toString(reactivePower);
		case "nominal power":
			return Float.toString(nominalPower);
		case "max q":
			return Float.toString(maxReactivePower);
		case "min q":
			return Float.toString(minReactivePower);
		default:
			throw new RuntimeException("no " + fieldName + " available in generator data");
		}
	}
	

}
