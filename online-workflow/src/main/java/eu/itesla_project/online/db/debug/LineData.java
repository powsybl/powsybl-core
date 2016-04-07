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
public class LineData implements EquipmentData {
	
	private final String lineId;
	private final String bus1Id;
	private final String bus2Id;
	private final float current1;
	private final float current2;
	private final float currentLimit1;
	private final float currentLimit2;
	
	public LineData(String lineId, String bus1Id, String bus2Id, float current1, float current2, float currentLimi1, float currentLimi2) {
		this.lineId = lineId;
		this.bus1Id = bus1Id;
		this.bus2Id = bus2Id;
		this.current1 = current1;
		this.current2 = current2;
		this.currentLimit1 = currentLimi1;
		this.currentLimit2 = currentLimi2;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"bus 1 id", 
					"bus 2 id", 
					"current 1",
					"current 2",
					"current limit 1",
					"current limit 2"
				};
	}
	
	@Override
	public String getFieldValue(String fieldName) {
		switch (fieldName) {
		case "id":
			return lineId;
		case "bus 1 id":
			return bus1Id;
		case "bus 2 id":
			return bus2Id;
		case "current 1":
			return Float.toString(current1);
		case "current 2":
			return Float.toString(current2);
		case "current limit 1":
			return Float.toString(currentLimit1);
		case "current limit 2":
			return Float.toString(currentLimit2);
		default:
			throw new RuntimeException("no " + fieldName + " available in line data");
		}
	}
	
}
