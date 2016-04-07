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
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class Connect2RegulatorsRecord extends ModelicaRecord {
	
	/**
	 * Connect between two regulators
	 * @param node1
	 * @param node2
	 * @param pinName1
	 * @param pinName2
	 */
	public Connect2RegulatorsRecord(Internal node1, Internal node2, String pinName1, String pinName2) {
        this.node1 = node1;
        this.node2 = node2;
        this.node1Pin = pinName1;
        this.node2Pin = pinName2;
    }
	
    @Override
    public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {  	
    	nodeName1 = modContext.dictionary.getModelicaName(node1);
    	nodeName2 = modContext.dictionary.getModelicaName(node2);
  
        String modelicaName = EurostagFixedData.CONNECT + nodeName1 + ", " + nodeName2 + EurostagFixedData.ANNOT_CONNECT;
        
        modContext.dictionary.add(node1, nodeName1);
        modContext.dictionary.add(node2, nodeName2);

        super.setModelicaName(modelicaName);
    }
    
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(nodeName1);
		
		this.addValue(".");
		this.addValue(node1Pin);
		
		this.addValue(", ");
		
		this.addValue(nodeName2);
		
		this.addValue(".");
		this.addValue(node2Pin);
		
		this.addValue(EurostagFixedData.ANNOT_CONNECT);
	}
	
	
	/**
	 * In Regulator's case convert Regulator DDB nativeId to Modelica name
	 */
	@Override
	public String parseName(String name) { //example: flr flr_BLAYAI1
		String parsedName = name.trim();

		if(name.length() > eqName.length()) parsedName = name.substring(eqName.length());
		else if(eqName.length() > name.length()) parsedName = eqName.substring(name.length());
		 
		if(parsedName.trim().startsWith("_")) parsedName = parsedName.trim().replaceFirst("_","").toLowerCase();
		
		if(eqName.startsWith("_")) eqName = eqName.replaceFirst("_", "");
		
		eqName = eqName.substring(0, eqName.indexOf("_"));
		
		parsedName = parsedName + "_" + eqName;

		return parsedName;
	}
	
	@Override
	public Connect2RegulatorsRecord getClassName() {
		return this;
	}
    
    protected String		nodeName1;
    protected String		nodeName2;
    private String			eqName;
	protected Internal		node1			= null;
	protected Internal		node2			= null;
	
	private String			node1Pin;
	private String			node2Pin;
}