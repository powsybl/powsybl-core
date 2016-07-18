/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.Parameter;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Equipments;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;

/**
 * Create a Modelica Line Record from IIDM Line
 * @author Silvia Machado <machados@aia.es>
 */
public class DanglingLineRecord extends ModelicaRecord {
	

    public DanglingLineRecord (DanglingLine danglingLine, String danglingBusName, String danglingLoadName, float SNREF) {
        this.danglingLine = danglingLine;
        this.danglingBusName = danglingBusName;
        this.danglingLoadName = danglingLoadName;
        this.DEFAULT_LINE_PREFIX = StaticData.PREF_LINE;
        		
        this.setParameters(SNREF);
    }
    

    @Override
    public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
    	modContext.dictionary.add(this.danglingLine, this.danglingLine.getId()); 
    	    	
    	//If any bus is disconnected, line is not declared
		Equipments.ConnectionInfo info1 = Equipments.getConnectionInfoInBusBreakerView(this.danglingLine.getTerminal());
		Bus b1 = info1.getConnectionBus();
		
		if((!Float.isNaN(b1.getV()) && info1.isConnected())) {
			if(super.isCorrect()) {
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				else this.addValue(this.DEFAULT_LINE_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!this.iidmbranchParameters.isEmpty()) {
					for(int i=0; i<this.iidmbranchParameters.size()-1; i++) {
						this.addValue("\t " + this.iidmbranchParameters.get(i).getName() + " = " + this.iidmbranchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + this.iidmbranchParameters.get(this.iidmbranchParameters.size()-1).getName() + " = " + this.iidmbranchParameters.get(this.iidmbranchParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				} else if(!this.branchParameters.isEmpty()) {
					for(int i=0; i<this.branchParameters.size()-1; i++) {
						this.addValue("\t " + this.branchParameters.get(i).getName() + " = " + this.branchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + this.branchParameters.get(this.branchParameters.size()-1).getName() + " = " + this.branchParameters.get(this.branchParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				
				this.addValue("\t " + EurostagFixedData.ANNOT);
				
				//Clear data
				iidmbranchParameters = null;
				branchParameters = null;
			}
			else _log.error(this.getModelicaName() + " not added to grid model.");
		}
		else {
			_log.warn("Line " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Line " + this.getModelicaName() + " disconnected.");
		}
    }
    
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName = parseName(this.danglingLine.getId());
		modelicaName = this.DEFAULT_LINE_PREFIX + "dl_" + modelicaName;
		modContext.dictionary.add(this.danglingLine, modelicaName);
		super.setModelicaName(modelicaName);		
	}
    
	@Override
	public String parseName(String name) {
	   	String parsedName = name.trim();

	   	//Lines contains "-"
    	if(parsedName.contains("-")) {
        	if(!parsedName.startsWith("_")) parsedName = "_" + parsedName;
        	parsedName = parsedName.replaceAll("-", "_");
    	}
       	parsedName = parsedName.replaceAll("\\s", "_");
       	
       	if(parsedName.substring(0, 1).matches("[0-9]")) parsedName = "l_" + parsedName;
       	
       	while(parsedName.endsWith("_")) parsedName = parsedName.substring(0, parsedName.length()-1);
       	
       	parsedName = parsedName.replaceAll(StaticData.WHITE_SPACE, "_");
       	parsedName = parsedName.replaceAll(StaticData.DOT, "_");
       	
        return parsedName;
	}
	
	/**
	 * Add IIDM parameters to Dangling Line Modelica Model in p.u
	 */
	private void setParameters(float SNREF) {
		//this.iidmbranchParameters = new ArrayList<IIDMParameter>();
		float tNominalV = this.danglingLine.getTerminal().getVoltageLevel().getNominalV();
		float voltage = Float.isNaN(tNominalV) == false ? tNominalV : 0;
		float Z = (voltage * voltage)/SNREF;

		super.addParameter(this.iidmbranchParameters, StaticData.R, this.danglingLine.getR()/Z);
		super.addParameter(this.iidmbranchParameters, StaticData.X, this.danglingLine.getX()/Z);
		super.addParameter(this.iidmbranchParameters, StaticData.G, this.danglingLine.getG()*Z);
		super.addParameter(this.iidmbranchParameters, StaticData.B, this.danglingLine.getB()*Z);
	}
	
	@Override
	public DanglingLineRecord getClassName() {
		return this;
	}
	
	public DanglingLine getDanglingLine() {
		return danglingLine;
	}

	public void setDanglingLine(DanglingLine danglingLine) {
		this.danglingLine = danglingLine;
	}
	
	public String getDanglingBusName() {
		return danglingBusName;
	}


	public void setDanglingBusName(String danglingBusName) {
		this.danglingBusName = danglingBusName;
	}

	public String getDanglingLoadName() {
		return danglingLoadName;
	}


	public void setDanglingLoadName(String danglingLoadName) {
		this.danglingLoadName = danglingLoadName;
	}

	private DanglingLine		danglingLine;
	private String				danglingBusName;
	private String				danglingLoadName;
	
    protected List<Parameter>			branchParameters			= new ArrayList<Parameter>();
    protected List<IIDMParameter>		iidmbranchParameters		= new ArrayList<IIDMParameter>();
	
	private String				DEFAULT_LINE_TYPE		= EurostagModDefaultTypes.DEFAULT_LINE_TYPE;
	private String				DEFAULT_OPEN_LINE_TYPE	= EurostagModDefaultTypes.DEFAULT_OPEN_LINE_TYPE;
	
	private String				DEFAULT_LINE_PREFIX;
	
	private static final Logger _log					= LoggerFactory.getLogger(DanglingLineRecord.class);

}
