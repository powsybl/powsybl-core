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
public class Tfo2WData implements EquipmentData {
	
	private final String tfoId;
	private final String bus1Id;
	private final String bus2Id;
	private final float apparentPower1;
	private final float apparentPower2;
	private final float nominalVoltage1;
	private final float nominalVoltage2;
	private final float currentLimit1;
	private final float currentLimit2;
	private final boolean isRegulating;
	private final int correntStepPosition;
	
	public Tfo2WData(String tfoId, String bus1Id, String bus2Id, float apparentPower1, float apparentPower2, float nominalVoltage1, 
					 float nominalVoltage2, float currentLimi1, float currentLimi2, boolean isRegulating, int correntStepPosition) {
		this.tfoId = tfoId;
		this.bus1Id = bus1Id;
		this.bus2Id = bus2Id;
		this.apparentPower1 = apparentPower1;
		this.apparentPower2 = apparentPower2;
		this.nominalVoltage1 = nominalVoltage1;
		this.nominalVoltage2 = nominalVoltage2;
		this.currentLimit1 = currentLimi1;
		this.currentLimit2 = currentLimi2;
		this.isRegulating = isRegulating;
		this.correntStepPosition = correntStepPosition;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"bus 1 id", 
					"bus 2 id", 
					"apparent power 1", 
					"apparent power 2",
					"nominal voltage 1",
					"nominal voltage 2",
					"current limit 1",
					"current limit 2",
					"regulating status",
					"step position"
				};
	}

	@Override
	public String getFieldValue(String fieldName) {
		switch (fieldName) {
		case "id":
			return tfoId;
		case "bus 1 id":
			return bus1Id;
		case "bus 2 id":
			return bus2Id;
		case "apparent power 1":
			return Float.toString(apparentPower1);
		case "apparent power 2":
			return Float.toString(apparentPower2);
		case "nominal voltage 1":
			return Float.toString(nominalVoltage1);
		case "nominal voltage 2":
			return Float.toString(nominalVoltage2);
		case "current limit 1":
			return Float.toString(currentLimit1);
		case "current limit 2":
			return Float.toString(currentLimit2);
		case "regulating status":
			return Boolean.toString(isRegulating);
		case "step position":
			return Integer.toString(correntStepPosition);
		default:
			throw new RuntimeException("no " + fieldName + " available in tfo data");
		}
	}
	

}
