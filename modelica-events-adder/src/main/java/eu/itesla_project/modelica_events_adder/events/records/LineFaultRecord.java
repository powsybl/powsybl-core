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
public class LineFaultRecord extends EventRecord {

	
	public LineFaultRecord(Record lineRecord, Event event, Map<String, Record> recordsMap, List<ConnectRecord> conRecList) {
		super(event.getType(), event.getCIMDevice(), event.getParams());
		this.event = event;
		this.lineRecord = lineRecord;
		this.recordsMap = recordsMap;
		this.conRecList = conRecList;
		super.setModelicaType(EventsStaticData.LINE_MODEL);
		
		for(String par : event.getParams()) {
			String name = par.split("=")[0];
			String value = par.split("=")[1];
			addParameter(name, value);
		}
	}

	@Override
	public void createModelicaName() {
//		String modelicaName = this.event.getCIMDevice() + FAULT;
		String modelicaName = this.lineRecord.getModelicaName() + FAULT;
		super.setModelicaName(modelicaName);
		
		getBuses();
		
		System.out.println("Bus from = " + this.busFrom + " - Bus to = " + this.busTo);
		
		getBusesVoltages();
		
		getLineParameters();
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
		String parsedName;

		//Remove the bus prefix = "bus_"
		name = name.substring(5);
		parsedName = "_" + name.replaceAll("_", "-");
		
		return parsedName;
	}

	@Override
	public LineFaultRecord getClassName() {
		return this;
	}
	
	/**
	 * Gets V0_real1, V0_img1, V0_real2, V0_img2 from each side bus in order to set them in the Line Fault.
	 * 
	 */
	private void getBusesVoltages() {
		//Vo_real = 0.9999380512793338,
		 //Vo_img = 0.011130750365022713
		//Search in the list of buses records the bus from and the bus to and get their voltages
		
		//Get bus from voltages
		Record busFromRecord = this.recordsMap.get(parseName(this.busFrom));
		
		String voltage = busFromRecord.getParamsMap().get(StaticData.VOLTAGE);
		String angle = busFromRecord.getParamsMap().get(StaticData.ANGLE);
		
		double vreal = Double.parseDouble(voltage)*Math.cos(Double.parseDouble(angle));
		double vimag = Double.parseDouble(voltage)*Math.sin(Double.parseDouble(angle));
		
//		String vReal = busFromRecord.getParamsMap().get(StaticData.VO_REAL);
//		String vImg = busFromRecord.getParamsMap().get(StaticData.VO_IMG);
		addParameter(EventsStaticData.VO_REAL1, vreal);
		addParameter(EventsStaticData.VO_IMG1, vimag);
		
		//Get bus to voltages
		Record busToRecord = this.recordsMap.get(parseName(this.busTo));
		
		voltage = busToRecord.getParamsMap().get(StaticData.VOLTAGE);
		angle = busToRecord.getParamsMap().get(StaticData.ANGLE);
		
		vreal = Double.parseDouble(voltage)*Math.cos(Double.parseDouble(angle));
		vimag = Double.parseDouble(voltage)*Math.sin(Double.parseDouble(angle));
		
//		vReal = busToRecord.getParamsMap().get(StaticData.VO_REAL);
//		vImg = busToRecord.getParamsMap().get(StaticData.VO_IMG);
		addParameter(EventsStaticData.VO_REAL2, vreal);
		addParameter(EventsStaticData.VO_IMG2, vimag);
	}

	
	/**
	 * Gets R1, X1, G1, B1 from the original line to set them in the line fault.
	 */
	private void getLineParameters() {
		//TOD search in the record text the R1, X1, G1 and B1 values
		
		//Get bus from voltages
		String R1 = this.lineRecord.getParamsMap().get(StaticData.R);
		String X1 = this.lineRecord.getParamsMap().get(StaticData.X);
		String G1 = this.lineRecord.getParamsMap().get(StaticData.G);
		String B1 = this.lineRecord.getParamsMap().get(StaticData.B);
		
		addParameter(EventsStaticData.R1, R1);
		addParameter(EventsStaticData.X1, X1);
		addParameter(EventsStaticData.G1, G1);
		addParameter(EventsStaticData.B1, B1);
	}
	
	private void getBuses() {
		List<ConnectRecord> busesList = new ArrayList<ConnectRecord>();
		
		int i = 0;
		for(ConnectRecord conRec : this.conRecList) 
		{			
			if(conRec.containsElement(this.lineRecord.getModelicaName())) 
			{
				System.out.println("Connect con la linea: " + this.lineRecord.getModelicaName());
				if(conRec.getConnectedElement(this.lineRecord.getModelicaName()).startsWith(StaticData.PREF_BUS)) 
				{
					busesList.add(conRec);
					i++;
				}
			}
			if(i == 2) { 
				break;
			} 
		}
		
		this.busFrom = busesList.get(0).getNodeF().equalsIgnoreCase(this.lineRecord.getModelicaName()) ? busesList.get(1).getNodeF() : busesList.get(0).getNodeF();
		this.busTo = busesList.get(0).getNodeT().equalsIgnoreCase(this.lineRecord.getModelicaName()) ? busesList.get(1).getNodeT() : busesList.get(0).getNodeT();
	}
	
	private void addParameter(String name, Object value) {
		this.iidmParameters.add(new IIDMParameter(name, value));
	}
	
	private Record lineRecord;
	private Event event;
	private List<IIDMParameter>	iidmParameters	= new ArrayList<IIDMParameter>();
	private String FAULT = "_Fault";
	
	private Map<String, Record> recordsMap = new HashMap<String, Record>();
	private List<ConnectRecord> conRecList = new ArrayList<ConnectRecord>();
	
	private String busFrom;
	private String busTo;
}
