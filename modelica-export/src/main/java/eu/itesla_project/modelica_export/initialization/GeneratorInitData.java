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

import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.records.GeneratorRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.Utils;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class GeneratorInitData {
	
	public GeneratorInitData(Generator gen, GeneratorRecord genRecord, String generatorName, String generatorModel, String generatorModelData) {
		this.generator = gen;
		this.genRecord = genRecord;
		this.genModelData = generatorModelData;
		this.genModel = generatorModel; 
		
		try {
			fillData();
		} catch (IOException e) {
			_log.error(e.getMessage(), e);
		}
	}
	
	private void fillData() throws IOException {
		BufferedReader buffer = new BufferedReader(new StringReader(this.genModelData));
		this.pinsList = Utils.parseModelPins(buffer);
		
		this.paramsMap = fillParameters();
	}
	
	private Map<String, String> fillParameters() {
		Map<String, String> params = new HashMap<String, String>();
		
		String trafoIncluded = this.genRecord.getGenParamsMap().get(EurostagFixedData.TRAFOINCLUDED.toUpperCase());
		
		//M1S Y M2S INIT
		params.put(EurostagFixedData.INIT_SNREF, String.valueOf(StaticData.SNREF_VALUE));
		params.put(EurostagFixedData.INIT_SN, this.genRecord.getGenParamsMap().get(EurostagFixedData.SN.toUpperCase()));
		params.put(EurostagFixedData.INIT_PN, this.genRecord.getGenParamsMap().get(EurostagFixedData.PN.toUpperCase()));
		params.put(EurostagFixedData.INIT_PNALT, this.genRecord.getGenParamsMap().get(EurostagFixedData.PNALT.toUpperCase()));

		
		float voltage = 0;
		float angle = 0;

		if (generator.getTerminal().getBusView().getBus() != null) {
			if (!Float.isNaN(generator.getTerminal().getBusView().getBus().getV()))
				voltage = generator.getTerminal().getBusView().getBus().getV();
			if (!Float.isNaN(generator.getTerminal().getBusView().getBus().getAngle()))
				angle = generator.getTerminal().getBusView().getBus().getAngle();
		}

		float modulo = voltage / this.generator.getTerminal().getVoltageLevel().getNominalV();
		float angulo =  (float) (angle*Math.PI/180);
		
		double ur0 = modulo * Math.cos(angulo);
		double ui0 = modulo * Math.sin(angulo);
		
		params.put(EurostagFixedData.INIT_UR0, String.valueOf(ur0));
		params.put(EurostagFixedData.INIT_UI0, String.valueOf(ui0));
		params.put(EurostagFixedData.INIT_P0, String.valueOf(this.generator.getTerminal().getP()/StaticData.SNREF_VALUE));
		params.put(EurostagFixedData.INIT_Q0, String.valueOf(this.generator.getTerminal().getQ()/StaticData.SNREF_VALUE));
		params.put(EurostagFixedData.INIT_NDSAT, this.genRecord.getGenParamsMap().get(EurostagFixedData.SND.toUpperCase()));
		params.put(EurostagFixedData.INIT_NQSAT, this.genRecord.getGenParamsMap().get(EurostagFixedData.SNQ.toUpperCase()));
		params.put(EurostagFixedData.INIT_MDSATIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.MD.toUpperCase()));
		params.put(EurostagFixedData.INIT_MQSATIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.MQ.toUpperCase()));
		params.put(EurostagFixedData.INIT_RSTATIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.RSTATIN.toUpperCase()));
		params.put(EurostagFixedData.INIT_LSTATIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.LSTATIN.toUpperCase()));
		if(this.genRecord.getGenParamsMap().get(EurostagFixedData.MD0PU.toUpperCase()) != null)
			params.put(EurostagFixedData.INIT_MD0PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.MD0PU.toUpperCase()));
		params.put(EurostagFixedData.INIT_OMEGA0, "1");
		params.put(EurostagFixedData.INIT_PPUWLMDV, this.genRecord.getGenParamsMap().get(EurostagFixedData.PNALT.toUpperCase()));
		params.put(EurostagFixedData.SATURATED, this.genRecord.getGenParamsMap().get(EurostagFixedData.SATURATED.toUpperCase()));
		params.put(EurostagFixedData.INLMDV, this.genRecord.getGenParamsMap().get(EurostagFixedData.INLMDV.toUpperCase()));
		
		//M1S & M2S IF TRAFO INCLUDED
		if(trafoIncluded.equals(Boolean.TRUE.toString())) {
			params.put(EurostagFixedData.INIT_SNTFO, this.genRecord.getGenParamsMap().get(EurostagFixedData.SNTFO.toUpperCase()));
			params.put(EurostagFixedData.INIT_UNRESTFO, this.genRecord.getGenParamsMap().get(EurostagFixedData.U2N.toUpperCase()));
			params.put(EurostagFixedData.INIT_UNOMNW, this.genRecord.getGenParamsMap().get(EurostagFixedData.V2.toUpperCase()));
			params.put(EurostagFixedData.INIT_UNMACTFO, this.genRecord.getGenParamsMap().get(EurostagFixedData.U1N.toUpperCase()));
			params.put(EurostagFixedData.INIT_UBMAC, this.genRecord.getGenParamsMap().get(EurostagFixedData.V1.toUpperCase()));
			params.put(EurostagFixedData.INIT_RTFOIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.RTFOPU.toUpperCase()));
			params.put(EurostagFixedData.INIT_XTFOIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.XTFOPU.toUpperCase()));
		}		
		
		//M1S INIT
		if(this.genModel.equals(EurostagModDefaultTypes.M1S_INIT_MODEL)) {
			params.put(EurostagFixedData.INIT_MQ0PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.MQ0PU.toUpperCase()));
			params.put(EurostagFixedData.INIT_LDPU, this.genRecord.getGenParamsMap().get(EurostagFixedData.LDPU.toUpperCase()));
			params.put(EurostagFixedData.INIT_RROTIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.RROTIN.toUpperCase()));
			params.put(EurostagFixedData.INIT_LROTIN, this.genRecord.getGenParamsMap().get(EurostagFixedData.LROTIN.toUpperCase()));
			params.put(EurostagFixedData.INIT_RQ1PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.RQ1PU.toUpperCase()));
			params.put(EurostagFixedData.INIT_LQ1PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.LQ1PU.toUpperCase()));
			params.put(EurostagFixedData.INIT_RQ2PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.RQ2PU.toUpperCase()));
			params.put(EurostagFixedData.INIT_LQ2PU, this.genRecord.getGenParamsMap().get(EurostagFixedData.LQ2PU.toUpperCase()));
			if(this.genRecord.getGenParamsMap().get(EurostagFixedData.MCANPU.toUpperCase()) != null) //TODO Just for testing
				params.put(EurostagFixedData.INIT_MCANPU, this.genRecord.getGenParamsMap().get(EurostagFixedData.MCANPU.toUpperCase()));
		}
		//M2S INIT
		if(this.genModel.equals(EurostagModDefaultTypes.M2S_INIT_MODEL)) {
			params.put(EurostagFixedData.INIT_XPD, this.genRecord.getGenParamsMap().get(EurostagFixedData.XPD.toUpperCase()));
			params.put(EurostagFixedData.INIT_XD, this.genRecord.getGenParamsMap().get(EurostagFixedData.XD.toUpperCase()));
			params.put(EurostagFixedData.INIT_XSD, this.genRecord.getGenParamsMap().get(EurostagFixedData.XSD.toUpperCase()));
			params.put(EurostagFixedData.INIT_TPDO, this.genRecord.getGenParamsMap().get(EurostagFixedData.TPD0.toUpperCase()));
			params.put(EurostagFixedData.INIT_TSDO, this.genRecord.getGenParamsMap().get(EurostagFixedData.TSD0.toUpperCase()));
			params.put(EurostagFixedData.INIT_XQ, this.genRecord.getGenParamsMap().get(EurostagFixedData.XQ.toUpperCase()));
			params.put(EurostagFixedData.INIT_XPQ, this.genRecord.getGenParamsMap().get(EurostagFixedData.XPQ.toUpperCase()));
			params.put(EurostagFixedData.INIT_XSQ, this.genRecord.getGenParamsMap().get(EurostagFixedData.XSQ.toUpperCase()));
			params.put(EurostagFixedData.INIT_TPQO, this.genRecord.getGenParamsMap().get(EurostagFixedData.TPQ0.toUpperCase()));
			params.put(EurostagFixedData.INIT_TSQO, this.genRecord.getGenParamsMap().get(EurostagFixedData.TSQ0.toUpperCase()));
			params.put(EurostagFixedData.INIT_TX, this.genRecord.getGenParamsMap().get(EurostagFixedData.TX.toUpperCase()));
			
			params.put(EurostagFixedData.INIT_IENR, this.genRecord.getGenParamsMap().get(EurostagFixedData.IENR.toUpperCase()));
		}
		return params;
	}
	
	public void addGenRecordParameters(Map<String, String> initValues) {
		IIDMParameter parameter;
		
		if(initValues != null && !initValues.isEmpty()) {
			for(String param : initValues.keySet()) {
				String machinePar = EurostagFixedData.MACHINE_PAR.get(EurostagFixedData.MACHINE_INIT_PAR.indexOf(param));
				parameter = new IIDMParameter(machinePar, initValues.get(param));
				this.genRecord.getIidmgenParameters().add(parameter);
			}
		}
	}
	
	public Generator getGenerator() {
		return generator;
	}

	public void setGenerator(Generator generator) {
		this.generator = generator;
	}

	public String getGenModelData() {
		return genModelData;
	}

	public void setGenModelData(String genModelData) {
		this.genModelData = genModelData;
	}

	public List<String> getPinsList() {
		return pinsList;
	}

	public void setPinsList(List<String> pinsList) {
		this.pinsList = pinsList;
	}
	
	public GeneratorRecord getGenRecord() {
		return genRecord;
	}

	public Map<String, String> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, String> paramsMap) {
		this.paramsMap = paramsMap;
	}

	public String getGenModel() {
		return genModel;
	}

	public void setGenModel(String genModel) {
		this.genModel = genModel;
	}

	private Generator		generator;
	private GeneratorRecord	genRecord;
	private String			genModel;

	private String			genModelData;
	private List<String>	pinsList		= new ArrayList<String>();
	private Map<String, String>	paramsMap		= new HashMap<String, String>();
	
	private static final Logger _log	= LoggerFactory.getLogger(RegulatorInitData.class);
}
