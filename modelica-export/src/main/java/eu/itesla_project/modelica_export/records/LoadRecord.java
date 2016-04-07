/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.ModelTemplate;
import eu.itesla_project.iidm.ddb.model.ModelTemplateContainer;
import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseModDefaultTypes;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a Modelica Load Record from IIDM Load
 * @author Silvia Machado <machados@aia.es>
 */
public class LoadRecord extends ModelicaRecord {

	public LoadRecord(Load load, ConnectBusInfo busInfo, float SNREF) {
		this.load = load;
		this.busInfo = busInfo;
		this.loadId = load.getId();
		this.busConnected = busInfo.isConnected();
		this.p0 = this.load.getP0();
		this.q0 = this.load.getQ0();
		this.busVoltage = Float.NaN;
		this.busAngle = Float.NaN;
	
		if(this.busConnected) {
	        if (load.getTerminal().getBusView().getBus() != null) {
	        	if(!Float.isNaN(load.getTerminal().getBusView().getBus().getV())) 
	        		busVoltage = load.getTerminal().getBusView().getBus().getV() / load.getTerminal().getVoltageLevel().getNominalV();
	        	
	        	if(!Float.isNaN(load.getTerminal().getBusView().getBus().getAngle()))
	        		busAngle = load.getTerminal().getBusView().getBus().getAngle();
	        }
	        	        
	        addLfParameters();
		}
		else {
			_log.warn("Load " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Load " + this.getModelicaName() + " disconnected.");
		}
		
		if(this.busVoltage == 0) {
			_log.info("Voltage 0");
		}
	}
	
	public LoadRecord(String loadId, float p0, float q0, float busVoltage, float busAngle, float SNREF) {
		this.loadId = loadId;
		this.busVoltage = busVoltage;
		this.busAngle = busAngle;
		this.p0 = p0;
		this.q0 = q0;
		this.busConnected= true;
		
		addLfParameters();
	}
	
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName = this.load != null ? parseName(this.load.getId()) : parseName(this.loadId);
		modelicaName = StaticData.PREF_LOAD + modelicaName;
		if(this.load != null) modContext.dictionary.add(this.load.getId(), modelicaName); 
		else modContext.dictionary.add(this.loadId, modelicaName);
		super.setModelicaName(modelicaName);
		
		ModelTemplate model = null;
		String ddbid = StaticData.MTC_PREFIX_NAME + super.mtcMapper.get(DEFAULT_LOAD_PREFIX);
		
		ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(ddbid);
		
		if(mtc == null) {
			_log.info("EUROSTAG Model Template Container does not exist. Searching Default Modelica Model Template Container in DDB.");
			mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + DEFAULT_LOAD_TYPE);
		}

		if(mtc != null) {
			for(ModelTemplate mt : mtc.getModelTemplates()) {
				if(mt.getTypeName().equalsIgnoreCase(DEFAULT_LOAD_TYPE)) model = mt;
			}
			
			if(model != null)
			{
				super.setModelicaType(model.getTypeName());
			}
			else _log.warn("MODELICA Model Template does not exist in DDB");
		}
		else  {
			super.setCorrect(false);
			_log.error("MODELICA Model Template Container does not exist in DDB.");
		}
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		if(!Float.isNaN(this.busVoltage) && this.busConnected) {
			if(super.isCorrect()) {
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				else this.addValue(DEFAULT_LOAD_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!iidmloadParameters.isEmpty()) {
					for(int i=0; i<iidmloadParameters.size()-1; i++) {
						if(iidmloadParameters.get(i).getValue() != null) {
							this.addValue("\t " + iidmloadParameters.get(i).getName() + " = " + iidmloadParameters.get(i).getValue() + ",");
						}
						else {
							this.addValue("\t " + iidmloadParameters.get(i).getName() + ",");
						}
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + iidmloadParameters.get(iidmloadParameters.size()-1).getName() + " = " + iidmloadParameters.get(iidmloadParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				else if(!loadParameters.isEmpty()) {
					for(int i=0; i<loadParameters.size()-1; i++) {
						this.addValue("\t " + loadParameters.get(i).getName() + " = " + loadParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + loadParameters.get(loadParameters.size()-1).getName() + " = " + loadParameters.get(loadParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				this.addValue("\t " + EurostagFixedData.ANNOT);
				
				//Clear data
				iidmloadParameters = null;
				loadParameters = null;
			}
			else _log.error(this.getModelicaName() + " not added to grid model.");
		}
		else {
			_log.warn("Load " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Load " + this.getModelicaName() + " disconnected.");
		}
	}
	
	private void addLfParameters() {
		float modulo = this.busVoltage;
		float angulo = this.busAngle;
		this.iidmloadParameters.add(new IIDMParameter(PsseFixedData.V_0, modulo));
		this.iidmloadParameters.add(new IIDMParameter(PsseFixedData.P_0, this.p0));
		this.iidmloadParameters.add(new IIDMParameter(PsseFixedData.Q_0, this.q0));
		
		this.iidmloadParameters.add(new IIDMParameter(PsseFixedData.PQBRAK, 0.7));
		
		if(DEFAULT_LOAD_TYPE.contains(".")) DEFAULT_LOAD_PREFIX = DEFAULT_LOAD_TYPE.substring(DEFAULT_LOAD_TYPE.lastIndexOf(".") + 1);
		else DEFAULT_LOAD_PREFIX = DEFAULT_LOAD_TYPE;
		
		//TODO Harcoded values because we don't have it anywhere
		//Add these values only when PwLoadVoltageDependence is used
//		if(EurostagModDefaultTypes.LOAD_VOLTAGE_DEP_TYPE.contains(DEFAULT_LOAD_PREFIX)) {
//			this.iidmloadParameters.add(new IIDMParameter(EurostagFixedData.ALPHA, 1));
//			this.iidmloadParameters.add(new IIDMParameter(EurostagFixedData.BETA, 2));
//			angulo = (float) (angle*Math.PI/180);
//		}
		this.iidmloadParameters.add(new IIDMParameter(PsseFixedData.ANGLE_0, angulo));
	}
	
	@Override
    public String parseName(String name) {
		String parsedName = name.trim();
		parsedName = parsedName.replaceAll("\\s", "_");
       	parsedName = parsedName.replaceAll("\\.", "_");
       	parsedName = parsedName.replaceAll("\\-", "_");
       	parsedName = parsedName.replaceAll("/", "_");
       	parsedName = parsedName.replaceAll("\\+", "_");
        return parsedName;
    }
	
	@Override
	public LoadRecord getClassName() {
		return this;
	}
	
	protected Load 				load = null;
	
	private String loadId;
	private float p0;
	private float q0;
	private float busVoltage;
	private float busAngle;
	private boolean busConnected;
	private ConnectBusInfo busInfo;
	
	private String				DEFAULT_LOAD_TYPE	= PsseModDefaultTypes.CONSTANT_LOAD_TYPE;
	private String				DEFAULT_LOAD_PREFIX;
	
	private List<Parameter>		loadParameters		= new ArrayList<Parameter>();
	private List<IIDMParameter>	iidmloadParameters	= new ArrayList<IIDMParameter>();
	
	private static final Logger _log				= LoggerFactory.getLogger(LoadRecord.class);
}
