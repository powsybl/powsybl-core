/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import java.util.List;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class HeaderRecord extends ModelicaRecord {
	
	public HeaderRecord(String networkName, List<GlobalVariable> globalVars) {
		this.networkName = networkName;
		this.globalVars = globalVars;
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.deleteInicialWhiteSpaces(2);
		this.addValue(StaticData.WITHIN);
        this.newLine();
        this.addValue(StaticData.MODEL + parseName(this.networkName));
        this.newLine();
        
        for(GlobalVariable var : globalVars) { 
        	this.addValue(var.toString());
        	this.newLine();
        }
	}

	@Override
	public String parseName(String name) {
		String parsedName = name.trim();
		if(parsedName.substring(0, 1).matches("[0-9]")) {
			parsedName = "M_" + parsedName;
		}
		parsedName = parsedName.replaceAll("\\s", "_");
       	parsedName = parsedName.replaceAll("\\.", "_");
       	parsedName = parsedName.replaceAll("\\-", "_");
       	parsedName = parsedName.replaceAll("/", "_");
       	parsedName = parsedName.replaceAll("\\+", "_");
        return parsedName;
	}
	
	@Override
	public HeaderRecord getClassName() {
		return this;
	}
	
	private String networkName;
	private List<GlobalVariable> globalVars;
}
