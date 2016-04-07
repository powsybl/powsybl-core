/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.contingencies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ActionParameters {
	
	public static String REDISPATCHING_DELTAP_PARAMETER = "deltap";
	
	private Map<String, ActionParameterValue> parameters = new HashMap<String, ActionParameterValue>();
	
	public void addParameter(String name, ActionParameterValue value) {
		parameters.put(name, value);
	}
	
	public Object getValue(String name) {
		return parameters.get(name).getValue();
	}
	
	public Collection<String> getNames() {
		return parameters.keySet();
	}

	@Override
	public String toString() {
		String parametersString = "actionParameters[";
		for(String name : getNames()) {
			if ( getValue(name) instanceof Float ) {
				float value = (Float) getValue(name);
				parametersString += name + "=" + value +"f,";
			}
			if ( getValue(name) instanceof Integer ) {
				int value = (Integer) getValue(name);
				parametersString += name + "=" + value +",";
			}
			if ( getValue(name) instanceof String ) {
				String value = (String) getValue(name);
				parametersString += name + "='" + value +"',";
			}
			if ( getValue(name) instanceof Boolean ) {
				boolean value = (Boolean) getValue(name);
				parametersString += name + "=" + value +",";
			}
		}
		if ( parametersString.length() > 19 )
			parametersString = parametersString.substring(0, parametersString.length()-1);
		parametersString+= "]";
		return parametersString;
	}
	
}
