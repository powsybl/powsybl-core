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
public class Event {
	
	public Event(int fileId, int id, String type, String cimDevice, List<String> params) {
		this.fileId = fileId;
		this.id = id;
		this.type = type;
		this.cimDevice = cimDevice;
		this.params = params;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getCIMDevice() {
		return cimDevice;
	}
	
	public void setCIMDevice(String cimDevice) {
		this.cimDevice = cimDevice;
	}
	
	public List<String> getParams() {
		return params;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}



	private int fileId;
	private int id;
	private String type;
	private String cimDevice;
	private List<String> params;

}
