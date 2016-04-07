/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.IIDMParameter;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;

/**
 * Create a Modelica Capacitor Record from IIDM Shunt
 * @author Silvia Machado <machados@aia.es>
 */
public class CapacitorRecord extends ModelicaRecord {

	public CapacitorRecord(ShuntCompensator shunt, ConnectBusInfo busInfo) {
		this.capacitor = shunt;
		this.busInfo = busInfo;
		
		double V = shunt.getTerminal().getVoltageLevel().getNominalV();
		int numSteps = shunt.getMaximumSectionCount();
		double stepSize = V * V * shunt.getMaximumB() / numSteps;

		double b = stepSize * shunt.getCurrentSectionCount();
		
		this.iidmcapacitorParameters.add(new IIDMParameter(EurostagFixedData.B, b / StaticData.SNREF_VALUE));
		this.iidmcapacitorParameters.add(new IIDMParameter(EurostagFixedData.NSTEPS, numSteps));
		
		//------------------------------------
		//To obtain currents in shunt they must be mapped from grid_condition. Not yet done.
//		float Ibase = (float) (FixedData.SNREF_VALUE/(shunt.getTerminal().getVoltageLevel().getNominalV()*1000));
//		float modulo = shunt.getTerminal().getI()*Ibase;//[p.u]
//		float angulo = (float) (shunt.getTerminal().getIAngle()*Math.PI/180);
//		this.iidmcapacitorParameters.add(new IIDMParameter(FixedData.p_ii, modulo*Math.cos(angulo)));
//		this.iidmcapacitorParameters.add(new IIDMParameter(FixedData.p_ir, modulo*Math.sin(angulo)));
		//------------------------------------
		// this.iidmcapacitorParameters.add(new IIDMParameter(FixedData.B0,
		// stepSize));
	
		if(DEFAULT_CAPACITOR_TYPE.contains(".")) DEFAULT_CAPACITOR_PREFIX = DEFAULT_CAPACITOR_TYPE.substring(DEFAULT_CAPACITOR_TYPE.lastIndexOf(".") + 1);
		else DEFAULT_CAPACITOR_PREFIX = DEFAULT_CAPACITOR_TYPE;
	}
	
	
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		String modelicaName = DEFAULT_CAPACITOR_PREFIX + "_" + parseName(this.capacitor.getId());
		modelicaName = WordUtils.uncapitalize(modelicaName.substring(0,1)) + modelicaName.substring(1);
		modelicaName = StaticData.PREF_CAP + modelicaName;
		modContext.dictionary.add(capacitor, modelicaName);
		super.setModelicaName(modelicaName);
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
//		if(this.busInfo.isConnected()) {
		if(!Float.isNaN(this.busInfo.getBus().getV()) && this.busInfo.isConnected()) {			
			if(super.isCorrect()) {
				if(!this.busInfo.isConnected()) this.addValue(StaticData.COMMENT);
					
				if (super.getModelicaType() != null) this.addValue(super.getModelicaType() + StaticData.WHITE_SPACE);
				else this.addValue(DEFAULT_CAPACITOR_TYPE + StaticData.WHITE_SPACE);
				this.addValue(super.getModelicaName());
				this.addValue(" (");
				this.addValue(StaticData.NEW_LINE);
				
				if(!iidmcapacitorParameters.isEmpty()) {
					for(int i=0; i<iidmcapacitorParameters.size()-1; i++) {
						if(!this.busInfo.isConnected()) this.addValue(StaticData.COMMENT);
						this.addValue("\t " + iidmcapacitorParameters.get(i).getName() + " = " + iidmcapacitorParameters.get(i).getValue() + ",");
						this.addValue(StaticData.NEW_LINE);
					}
					if(!this.busInfo.isConnected()) this.addValue(StaticData.COMMENT);
					this.addValue("\t " + iidmcapacitorParameters.get(iidmcapacitorParameters.size()-1).getName() + " = " + iidmcapacitorParameters.get(iidmcapacitorParameters.size()-1).getValue());
					this.addValue(StaticData.NEW_LINE);
				}
				if(!this.busInfo.isConnected()) this.addValue(StaticData.COMMENT);
				this.addValue("\t " + EurostagFixedData.ANNOT);
				
				//Clear data
				iidmcapacitorParameters = null;
			}
			else _log.error(this.getModelicaName() + " not added to grid model.");
		}
		else {
			_log.warn("Capacitor " + this.getModelicaName() + " disconnected.");
			this.addValue(StaticData.COMMENT + " Capacitor " + this.getModelicaName() + " disconnected.");
		}
		
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
	public CapacitorRecord getClassName() {
		return this;
	}

	protected ShuntCompensator 	capacitor;
	private ConnectBusInfo		busInfo;
	
	private List<IIDMParameter>	iidmcapacitorParameters	= new ArrayList<IIDMParameter>();
	
	private String				DEFAULT_CAPACITOR_TYPE	= EurostagModDefaultTypes.DEFAULT_CAPACITOR_TYPE;
	private String				DEFAULT_CAPACITOR_PREFIX;
	
	private static final Logger _log					= LoggerFactory.getLogger(CapacitorRecord.class);
}
