/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.SingleTerminalConnectable;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Create a Modelica Connect between Global Variable omegaRef and Generators/Loads
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectGlobalVarRecord extends ModelicaRecord {
	
	public ConnectGlobalVarRecord(SingleTerminalConnectable singleTerCon, GlobalVariable globalVar) {
		this.node1 = singleTerCon;
		this.globalVar = globalVar;
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		String modelicaName;
		modelicaName = modContext.dictionary.getModelicaName(this.node1);
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(modelicaName);
		this.addValue("." + EurostagFixedData.GEN_OMEGAREF_PIN + ", ");
		this.addValue(globalVar.getName());
		this.addValue(");");	
	}
	
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) { 
	}

	@Override
	public String parseName(String name) {
		return name;
	}
	
	@Override
	public ConnectGlobalVarRecord getClassName() {
		return this;
	}
	
	private SingleTerminalConnectable	node1;
	private GlobalVariable				globalVar;
}
