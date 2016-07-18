/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.initialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.modelica_export.records.RegulatorRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.Utils;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class RegulatorInitData {
	
	public RegulatorInitData(Internal reg, String regulatorName, String regulatorModel, String regulatorModelData, RegulatorRecord regulatorRecord) {
		this.regulator = reg;
		this.regName = regulatorName;
		this.regModel = regulatorModel;
		this.regModelData = regulatorModelData;
		this.regRecord = regulatorRecord;
		
		try {
			fillDataList();
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}
	
	private void fillDataList() throws IOException {
		if(this.regModelData != null) {
			BufferedReader buffer = new BufferedReader(new StringReader(this.regModelData));
			  
			this.pinsList = Utils.parseModelPins(buffer); //paramsAndPins.get(1);
			
			//Cogemos las init variables
			this.initVars = Utils.getOtherRegVars(buffer);
			Map<String, String> params = this.regRecord.getRegParamsMap();
			if((params != null) && (!params.isEmpty())) {
				for(String par : params.keySet()) {
					this.paramsMap.put(par, params.get(par));
				}
			}
		}
	}
	

	public void addRegRecordParameters(Map<String, String> regInitValues) {
		IIDMParameter parameter;
		BufferedReader buffer = new BufferedReader(new StringReader(this.regRecord.getModelData()));
		try {
			List<String> initList = Utils.parseRegInitVariables(buffer);
			
			if(regInitValues != null && !regInitValues.isEmpty()) {
				for(String param : regInitValues.keySet()) {
					String paramValue = regInitValues.get(param);
//					param = param.trim().replace(StaticData.PIN, "pin_");
					if(initList.contains(param)) {
						parameter = new IIDMParameter(param, paramValue.trim());
						this.regRecord.getIidmregParameters().add(parameter);
					}
				}
			}
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}

	public String getRegModel() {
		return regModel;
	}

	public void setRegModel(String regModel) {
		this.regModel = regModel;
	}

	public Internal getRegulator() {
		return regulator;
	}
	public void setRegulator(Internal regulator) {
		this.regulator = regulator;
	}
	public String getRegName() {
		return regName;
	}
	public void setRegName(String regName) {
		this.regName = regName;
	}
	public List<String> getPinsList() {
		return pinsList;
	}
	public void setPinsList(List<String> pinsList) {
		this.pinsList = pinsList;
	}

	public String getRegModelData() {
		return regModelData;
	}

	public void setRegModelData(String regModelData) {
		this.regModelData = regModelData;
	}

	public RegulatorRecord getRegRecord() {
		return regRecord;
	}

	public void setRegRecord(RegulatorRecord regRecord) {
		this.regRecord = regRecord;
	}

	public Map<String, String> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public List<String> getInitVars() {
		return initVars;
	}

	public void setInitVars(List<String> initVars) {
		this.initVars = initVars;
	}

	private Internal		regulator;
	private String			regName;
	private String			regModel;
	private String			regModelData;
	private RegulatorRecord	regRecord;
	private List<String>	pinsList		= new ArrayList<String>();
	private List<String>	initVars		= new ArrayList<String>();
	private Map<String, String> paramsMap	= new HashMap<String, String>();
	
	private static final Logger _log	= LoggerFactory.getLogger(RegulatorInitData.class);
}
