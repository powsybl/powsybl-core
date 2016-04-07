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
public class Tfo3WData implements EquipmentData {
	
	private final String tfoId;
	private final String bus1Id;
	private final String bus2Id;
	private final String bus3Id;
	private final float apparentPower1;
	private final float apparentPower2;
	private final float apparentPower3;
	private final float nominalVoltage1;
	private final float nominalVoltage2;
	private final float nominalVoltage3;
	private final float currentLimit1;
	private final float currentLimit2;
	private final float currentLimit3;
	
	public Tfo3WData(String tfoId, String bus1Id, String bus2Id, String bus3Id, float apparentPower1, float apparentPower2, 
					 float apparentPower3, float nominalVoltage1, float nominalVoltage2, float nominalVoltage3, float currentLimit1, 
					 float currentLimit2, float currentLimit3) {
		this.tfoId = tfoId;
		this.bus1Id = bus1Id;
		this.bus2Id = bus2Id;
		this.bus3Id = bus3Id;
		this.apparentPower1 = apparentPower1;
		this.apparentPower2 = apparentPower2;
		this.apparentPower3 = apparentPower3;
		this.nominalVoltage1 = nominalVoltage1;
		this.nominalVoltage2 = nominalVoltage2;
		this.nominalVoltage3 = nominalVoltage3;
		this.currentLimit1 = currentLimit1;
		this.currentLimit2 = currentLimit2;
		this.currentLimit3 = currentLimit3;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"bus 1 id", 
					"bus 2 id",
					"bus 3 id",
					"apparent power 1", 
					"apparent power 2",
					"apparent power 3",
					"nominal voltage 1",
					"nominal voltage 2",
					"nominal voltage 3",
					"current limit 1",
					"current limit 2",
					"current limit 3"
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
		case "bus 3 id":
			return bus3Id;
		case "apparent power 1":
			return Float.toString(apparentPower1);
		case "apparent power 2":
			return Float.toString(apparentPower2);
		case "apparent power 3":
			return Float.toString(apparentPower3);
		case "nominal voltage 1":
			return Float.toString(nominalVoltage1);
		case "nominal voltage 2":
			return Float.toString(nominalVoltage2);
		case "nominal voltage 3":
			return Float.toString(nominalVoltage3);
		case "current limit 1":
			return Float.toString(currentLimit1);
		case "current limit 2":
			return Float.toString(currentLimit2);
		case "current limit 3":
			return Float.toString(currentLimit3);
		default:
			throw new RuntimeException("no " + fieldName + " available in tfo data");
		}
	}

}
