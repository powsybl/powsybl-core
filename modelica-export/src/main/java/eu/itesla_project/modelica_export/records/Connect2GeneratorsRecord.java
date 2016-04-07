/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class Connect2GeneratorsRecord extends ModelicaRecord {
	
	/**
	 * Connect between two regulators
	 * @param node1
	 * @param node2
	 * @param pinName1
	 * @param pinName2
	 */
	public Connect2GeneratorsRecord(Generator gen, String pinName1, String pinName2) {
        this.gen = gen;
        this.node1Pin = pinName1;
        this.node2Pin = pinName2;
    }
	
    @Override
    public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {  	
    	nodeName = modContext.dictionary.getModelicaName(gen);
  
        String modelicaName = EurostagFixedData.CONNECT + nodeName + ", " + nodeName + EurostagFixedData.ANNOT_CONNECT;
        
        modContext.dictionary.add(gen, nodeName);

        super.setModelicaName(modelicaName);
    }
    
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(nodeName);
		
		this.addValue(".");
		this.addValue(node1Pin);
		
		this.addValue(", ");
		
		this.addValue(nodeName);
		
		this.addValue(".");
		this.addValue(node2Pin);
		
		this.addValue(EurostagFixedData.ANNOT_CONNECT);
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
	public Connect2GeneratorsRecord getClassName() {
		return this;
	}
    
    protected String		nodeName;
	protected Generator		gen			= null;
	
	private String			node1Pin;
	private String			node2Pin;
}