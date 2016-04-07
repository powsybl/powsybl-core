/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.Connectable;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Equipments;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Create a Modelica Line Record from IIDM Line
 * @author Silvia Machado <machados@aia.es>
 */
public class LineRecord extends BranchRecord {
	
    public LineRecord (Line line, float SNREF) {
        super(line);
        this.line = line;
        
		boolean isSendingOpen = line.getTerminal1().isConnected() ? true : false;
		boolean isReceivingOpen = line.getTerminal2().isConnected() ? true : false;
  
		if(!isSendingOpen && isReceivingOpen) { //Opening Sending
			super.setDEFAULT_BRANCH_TYPE(DEFAULT_OPEN_LINE_TYPE);
			super.addParameter(this.iidmbranchParameters, EurostagFixedData.OPENR, false);			
		}
		else if(isSendingOpen && !isReceivingOpen) { //Opening receiving
			super.setDEFAULT_BRANCH_TYPE(DEFAULT_OPEN_LINE_TYPE);
			super.addParameter(this.iidmbranchParameters, EurostagFixedData.OPENR, true);
		}
		else {
			super.setDEFAULT_BRANCH_TYPE(DEFAULT_LINE_TYPE);
		}
        super.setDEFAULT_BRANCH_PREFIX(StaticData.PREF_LINE);
		this.setParameters(SNREF);
    }
    
    @Override
    public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {

    	modContext.dictionary.add(this.line, this.line.getId()); 
    	
    	//If any bus is disconnected, line is not declared
		Equipments.ConnectionInfo info1 = Equipments.getConnectionInfoInBusBreakerView(this.line.getTerminal1());
		Bus b1 = info1.getConnectionBus();
		Equipments.ConnectionInfo info2 = Equipments.getConnectionInfoInBusBreakerView(this.line.getTerminal2());
		Bus b2 = info2.getConnectionBus();
		
		if((!Float.isNaN(b1.getV()) && info1.isConnected()) || (!Float.isNaN(b2.getV()) && info2.isConnected())) {    	
			if(super.isCorrect()) {
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				else this.addValue(super.DEFAULT_BRANCH_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!super.iidmbranchParameters.isEmpty()) {
					for(int i=0; i<super.iidmbranchParameters.size()-1; i++) {
						this.addValue("\t " + super.iidmbranchParameters.get(i).getName() + " = " + super.iidmbranchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + super.iidmbranchParameters.get(super.iidmbranchParameters.size()-1).getName() + " = " + super.iidmbranchParameters.get(super.iidmbranchParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				} else if(!super.branchParameters.isEmpty()) {
					for(int i=0; i<super.branchParameters.size()-1; i++) {
						this.addValue("\t " + super.branchParameters.get(i).getName() + " = " + super.branchParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					this.addValue("\t " + super.branchParameters.get(super.branchParameters.size()-1).getName() + " = " + super.branchParameters.get(super.branchParameters.size()-1).getValue());
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
    
	/**
	 * Add IIDM parameters to Line Modelica Model in p.u
	 */
	@Override
	public void setParameters(float SNREF) {		
		super.iidmbranchParameters = new ArrayList<IIDMParameter>();
		float tNominalV = ((Line) this.line).getTerminal2().getVoltageLevel().getNominalV(); 
		float voltage = Float.isNaN(tNominalV) == false ? tNominalV : 0;
		float Z = (voltage * voltage)/SNREF;

		super.addParameter(this.iidmbranchParameters, StaticData.R, this.line.getR()/Z);
		super.addParameter(this.iidmbranchParameters, StaticData.X, this.line.getX()/Z);
		super.addParameter(this.iidmbranchParameters, StaticData.G, this.line.getG1()*Z);
		super.addParameter(this.iidmbranchParameters, StaticData.B, this.line.getB1()*Z);
	}
	
	@Override
	public LineRecord getClassName() {
		return this;
	}
	
	
	
	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
	}



	private Line				line;
	
	private String				DEFAULT_LINE_TYPE		= EurostagModDefaultTypes.DEFAULT_LINE_TYPE;
	private String				DEFAULT_OPEN_LINE_TYPE	= EurostagModDefaultTypes.DEFAULT_OPEN_LINE_TYPE;
	private String				DEFAULT_LINE_PREFIX;
	
	private static final Logger _log					= LoggerFactory.getLogger(LineRecord.class);
}
