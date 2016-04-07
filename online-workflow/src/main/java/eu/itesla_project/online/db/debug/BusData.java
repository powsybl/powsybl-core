/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db.debug;

import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class BusData implements EquipmentData {

	private final String busId;
	private final float ratedVoltage;
	private final float voltageMagnitude;
	private final float voltageAngle;
	private final boolean connectedToGenerator;
	private final List<String> generators;
	private final float activeInjection;
	private final float reactiveInjection;
	private final boolean connectedToLoad;
	private final List<String> loads;
	private final float activeAbsorption;
	private final float reactiveAbsorption;
	private final float activePower;
	private final float reactivePower;
	private boolean slack;
	
	
	public BusData(String busId, float ratedVoltage, float voltageMagnitude, float voltageAngle, boolean connectedToGenerator, List<String> generators, 
				   float activeInjection, float reactiveInjection, boolean connectedToLoad, List<String> loads, float activeAbsorption, float reactiveAbsorption, 
				   float activePower, float reactivePower, boolean slack) {
		this.busId = busId;
		this.ratedVoltage = ratedVoltage;
		this.voltageMagnitude = voltageMagnitude;
		this.voltageAngle = voltageAngle;
		this.connectedToGenerator = connectedToGenerator;
		this.generators = generators;
		this.activeInjection = activeInjection;
		this.reactiveInjection = reactiveInjection;
		this.connectedToLoad = connectedToLoad;
		this.loads = loads;
		this.activeAbsorption = activeAbsorption;
		this.reactiveAbsorption = reactiveAbsorption;
		this.activePower = activePower;
		this.reactivePower = reactivePower;
		this.slack = slack;
	}
	
	public void setSlack(boolean slack) {
		this.slack = slack;
	}
	
	public static String[] getFields() {
		return new String[]{
					"id", 
					"rated voltage", 
					"voltage magnitude", 
					"voltage angle", 
					"connected to generator",
					"generators",
					"Pg",
					"Qg",
					"connected to load",
					"loads",
					"Pc",
					"Qc",
					"active power", 
					"reactive power",
					"slack"
				};
	}
	
	@Override
	public String getFieldValue(String fieldName) {
		switch (fieldName) {
		case "id":
			return busId;
		case "rated voltage":
			return Float.toString(ratedVoltage);
		case "voltage magnitude":
			return Float.toString(voltageMagnitude);
		case "voltage angle":
			return Float.toString(voltageAngle);
		case "connected to generator":
			return Boolean.toString(connectedToGenerator);
		case "generators":
			return listToString(generators);
		case "Pg":
			return Float.toString(activeInjection);
		case "Qg":
			return Float.toString(reactiveInjection);
		case "connected to load":
			return Boolean.toString(connectedToLoad);
		case "loads":
			return listToString(loads);
		case "Pc":
			return Float.toString(activeAbsorption);
		case "Qc":
			return Float.toString(reactiveAbsorption);
		case "active power":
			return Float.toString(activePower);
		case "reactive power":
			return Float.toString(reactivePower);
		case "slack":
			return Boolean.toString(slack);
		default:
			throw new RuntimeException("no " + fieldName + " available in bus data");
		}
	}
	
	private String listToString(List<String> list) {
		String listString = "";
		for (String string : list)
			listString += string + ";";
		if ( ! listString.isEmpty() )
				listString = listString.substring(0, listString.length()-1);
		return listString;
	}
	
}
