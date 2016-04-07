/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class InitializationRecord extends ModelicaRecord {
	
	public InitializationRecord(GlobalVariable var) {
		this.globalVar = var;		
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		if(globalVar != null)
			this.addValue(globalVar.getName() + " = " + globalVar.getValue() + StaticData.SEMICOLON);
	}

	@Override
	public String parseName(String name) {
		return null;
	}
	
	@Override
	public InitializationRecord getClassName() {
		return this;
	}
	
	private GlobalVariable globalVar;
}
