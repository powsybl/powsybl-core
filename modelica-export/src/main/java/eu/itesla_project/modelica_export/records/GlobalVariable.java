/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class GlobalVariable {
	
	private String	type			= null;
	private String	name			= null;
	private Object	value			= null;

	public GlobalVariable(String type, String name, Object value) {
		this.type	= type;
		this.name	= name;
		this.value	= value;
	}
	
	public GlobalVariable(String type, String name) {
		this(type, name, null);
	}
	
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		if(value != null) return "  " + type + StaticData.WHITE_SPACE + name + " = " + value + StaticData.SEMICOLON;
		else return "  " + type + StaticData.WHITE_SPACE + name + StaticData.SEMICOLON;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
