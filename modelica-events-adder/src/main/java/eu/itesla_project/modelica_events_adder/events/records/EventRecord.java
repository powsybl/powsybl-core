/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.records;

import java.util.List;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public abstract class EventRecord extends ModelicaRecord {

	public EventRecord(String eventType, String device, List<String> params) {
		this.type = eventType;
		this.device = device;
		this.parameters = params;
	}
	
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDevice() {
		return device;
	}
	
	public void setDevice(String device) {
		this.device = device;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}
	
	public void  setModelicaType(String modelicaType) {
		this.modelicaType = modelicaType;
	}

	public String getModelicaType()
	{
		return modelicaType;
	}
	

	private String type;
	private String device;
	private List<String> parameters;
	
	private String				modelicaType	= null;
	
}
