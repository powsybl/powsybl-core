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
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a Modelica Bus Record from IIDM Bus
 * @author Silvia Machado <machados@aia.es>
 */
public class BusRecord extends ModelicaRecord {

	/**
	 * Constructor to buses that come from the IIDM network.
	 * @param bus
	 */
	public BusRecord(Bus bus) {
		this.bus = bus;
		this.busId = bus.getId(); 
				
		if(!Float.isNaN(this.bus.getV())) {
			this.busVoltage = bus.getV() / bus.getVoltageLevel().getNominalV();
			
			this.busAngle = this.bus.getAngle();
			
			addParameter(StaticData.V_0, this.busVoltage);
			addParameter(StaticData.ANGLE_0, this.busAngle);
		}
		else {
			this.busVoltage = Float.NaN;
		}

	}
	
	/**
	 * Constructor to "Dangling" or dummy buses that do not exist in network.
	 * @param id
	 * @param voltage
	 * @param angle
	 */
	public BusRecord(String id, float voltage, float angle) {
		this.busId = id;
		this.busVoltage = voltage;
		this.busAngle = angle;
		
		if(!Float.isNaN(this.busVoltage)) {
			addParameter(StaticData.V_0, this.busVoltage);
			addParameter(StaticData.ANGLE_0, this.busAngle);
		}
	}
	
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName = parseName(this.busId);
		modelicaName = StaticData.PREF_BUS + modelicaName;
		if(this.bus != null) modContext.dictionary.add(this.bus, modelicaName);
		super.setModelicaName(modelicaName);

		ModelTemplate model = null;
		String ddbid = StaticData.MTC_PREFIX_NAME + super.mtcMapper.get(DEFAULT_BUS_TYPE);
		
		ModelTemplateContainer mtc = ddbManager.findModelTemplateContainer(ddbid);
		if(mtc == null) {
//			_log.warn("EUROSTAG Model Template Container does not exist. Searching Default MODELICA Model Template Container in DDB.");
			mtc = ddbManager.findModelTemplateContainer(StaticData.MTC_PREFIX_NAME + DEFAULT_BUS_TYPE);
		}

		if(mtc != null) {
			for(ModelTemplate mt : mtc.getModelTemplates()) {
				if(mt.getTypeName().equalsIgnoreCase(DEFAULT_BUS_TYPE)) model = mt;
			}
			
			if(model != null)
			{
//				String data = new String(model.getData("mo"));
//				super.setModelData(data);
				super.setModelicaType(model.getTypeName());
				
//				List<DefaultParameters> defSetParams = model.getDefaultParameters();
//				List<Parameter> defParameters = defSetParams.get(0).getParameters();
//				for(Parameter param : defParameters) busParameters.add(param);
			}
			else {
				super.setCorrect(false);
//				_log.warn("MODELICA Model Template does not exist in DDB");
			}
		}
		else {
			super.setCorrect(false);
			_log.error("MODELICA Model Template Container does not exist in DDB.");
		}
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		//We suppose: if V=NaN the bus is disconnected.
		if(!Float.isNaN(this.busVoltage)) {
			if(super.isCorrect()) {
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE); 
				else this.addValue(DEFAULT_BUS_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!iidmbusParameters.isEmpty()) {
					for(int i=0; i<iidmbusParameters.size()-1; i++) {
						this.addValue("\t " + iidmbusParameters.get(i).getName() + " = " + iidmbusParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + iidmbusParameters.get(iidmbusParameters.size()-1).getName() + " = " + iidmbusParameters.get(iidmbusParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				else if(!busParameters.isEmpty()) 
				{
					for(int i=0; i<busParameters.size()-1; i++) {
						this.addValue("\t " + busParameters.get(i).getName() + " = " + busParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + busParameters.get(busParameters.size()-1).getName() + " = " + busParameters.get(busParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				this.addValue("\t " + EurostagFixedData.ANNOT);
				
				//Clear data
				iidmbusParameters = null;
				busParameters = null;
			}
			else _log.error(this.getModelicaName() + " not added to grid model.");
		}
		else {
			_log.warn("Bus " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Bus " + this.getModelicaName() + " disconnected.");
		}
	}
	
	private void addParameter(String name, Object value) {
		this.iidmbusParameters.add(new IIDMParameter(name, value));
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
	public BusRecord getClassName() {
		return this;
	}

	protected Bus 				bus					= null;
	
	private String				busId;
	private float				busVoltage			= 0;
	private float				busAngle;
	
	private String				DEFAULT_BUS_TYPE	= EurostagModDefaultTypes.DEFAULT_BUS_TYPE;
	
	private List<Parameter>		busParameters		= new ArrayList<Parameter>();
	private List<IIDMParameter>	iidmbusParameters	= new ArrayList<IIDMParameter>();
	
	private static final Logger _log				= LoggerFactory.getLogger(BusRecord.class);
}