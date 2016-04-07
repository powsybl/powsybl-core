/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectConstantRecord extends ModelicaRecord {

	public ConnectConstantRecord(String regName, String constantName, String pinName1, String constantPin) {
        this.regName = regName;
        this.constantName = constantName;
        this.node1Pin = pinName1;
        this.constantPin = constantPin;
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
	   	  
        String modelicaName = StaticData.CONNECT + regName + ", " + constantName + StaticData.ANNOT_CONNECT;

        super.setModelicaName(modelicaName);
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.addValue(StaticData.CONNECT);
		this.addValue(regName);
		
		this.addValue(".");
		this.addValue(node1Pin);
		
		this.addValue(", ");
		
		this.addValue(constantName);
		
		this.addValue(".");
		this.addValue(constantPin);
		
		this.addValue(StaticData.ANNOT_CONNECT);
	}



	@Override
	public String parseName(String name) {
		return null;
	}
	
	@Override
	public ConnectConstantRecord getClassName() {
		return this;
	}
	
    protected String		regName;
    protected String		constantName;
	protected Internal		node			= null;
	
	private String			node1Pin;
	private String			constantPin;

}
