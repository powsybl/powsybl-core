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
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;

/**
 * Abstract class of Connect record
 * @author Silvia Machado <machados@aia.es>
 */
public abstract class ConnectRecord extends ModelicaRecord {

	public ConnectRecord(String node1, String node2) {
		this.node1 = node1;
		this.node2 = node2;
	}

    @Override
    public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
    	nodeName1 = modContext.dictionary.getModelicaName(node1);
    	nodeName2 = modContext.dictionary.getModelicaName(node2);
 	
        String modelicaName = StaticData.CONNECT + nodeName1 + ", " + nodeName2 + StaticData.ANNOT_CONNECT;
        modContext.dictionary.add(node1, nodeName1);
        modContext.dictionary.add(node2, nodeName2);

        super.setModelicaName(modelicaName);
    }
    
    public String parseName(String name) {
    	String parsedName = name.trim();

    	if(parsedName.contains("-")) {
        	if(!parsedName.startsWith("_")) {
            	parsedName = "_" + parsedName;    		
        	}
        	parsedName = parsedName.replaceAll("-", "_");
    	}

        int posi = parsedName.indexOf('_');
        int pose = parsedName.lastIndexOf('_');
        if (pose > posi) {
        	parsedName = parsedName.substring(posi+1, pose);
        }
       	parsedName = parsedName.replaceAll("\\s", "_");
       	
       	if(parsedName.substring(0, 1).matches("[0-9]")) {
        	parsedName = "b_" + parsedName;
       	}
       	
       	while(parsedName.endsWith("_")) parsedName = parsedName.substring(0, parsedName.length()-1);
       	
        return parsedName;
    }
    
    protected String			nodeName1;
    protected String			nodeName2;
    protected String			node1;
    protected String			node2;
}
