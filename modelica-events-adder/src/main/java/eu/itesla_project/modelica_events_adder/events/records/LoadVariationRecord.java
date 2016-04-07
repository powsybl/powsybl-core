/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.records;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.itesla_project.modelica_events_adder.events.EventsStaticData;
import eu.itesla_project.modelica_events_adder.events.utils.IIDMParameter;
import eu.itesla_project.modelica_events_adder.events.utils.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class LoadVariationRecord extends EventRecord {

	
	public LoadVariationRecord(Record loadRecord, Event event, Map<String, Record> recordsMap) {
		super(event.getType(), event.getCIMDevice(), event.getParams());
		this.event = event;
		this.loadRecord = loadRecord;
		this.recordsMap = recordsMap;
		super.setModelicaType(EventsStaticData.LINE_MODEL);
		
		for(String par : event.getParams()) {
			String name = par.split("=")[0];
			String value = par.split("=")[1];
			addParameter(name, value);
		}
	}

	@Override
	public void createModelicaName() {
		String modelicaName = this.loadRecord.getModelicaName();
		super.setModelicaName(modelicaName);
		
		getLoadParameters();
	}

	@Override
	public void createRecord() {
		this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE); 
		this.addValue(super.getModelicaName());
		this.addValue(" (");
		this.addValue(StaticData.NEW_LINE);
		
		if(!iidmParameters.isEmpty()) {
			for(int i=0; i<iidmParameters.size()-1; i++) {
				this.addValue("\t " + iidmParameters.get(i).getName() + " = " + iidmParameters.get(i).getValue() + ",");
				this.addValue(StaticData.NEW_LINE);
			}
			this.addValue("\t " + iidmParameters.get(iidmParameters.size()-1).getName() + " = " + iidmParameters.get(iidmParameters.size()-1).getValue());
			this.addValue(StaticData.NEW_LINE);
		}

		this.addValue("\t " + StaticData.ANNOT);
		
		//Clear data
		iidmParameters = null;		
		
	}

	@Override
	public String parseName(String name) {
		return null;
	}

	@Override
	public LoadVariationRecord getClassName() {
		return this;
	}

	/**
	 * Gets parameters from the original load to set them in the load variation event.
	 */
	private void getLoadParameters() {
		for(String parName : this.loadRecord.getParamsMap().keySet()) {
			addParameter(parName, this.loadRecord.getParamsMap().get(parName));
		}
	}
	
	private void addParameter(String name, Object value) {
		this.iidmParameters.add(new IIDMParameter(name, value));
	}
	
	private Record loadRecord;
	private Event event;
	private List<IIDMParameter>	iidmParameters	= new ArrayList<IIDMParameter>();
	
	private Map<String, Record> recordsMap = new HashMap<String, Record>();
	
	private String busFrom;
	private String busTo;
}
