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
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Create a Modelica (Connect) Coupling Device Record from IIDM Switch
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectCouplingDevicesRecord extends ModelicaRecord {
	
	public ConnectCouplingDevicesRecord(Switch switchNode, Bus bus1, Bus bus2) {
		this.switchNode = switchNode;
		this.bus1 = bus1;
		this.bus2 = bus2;
	}
	
	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
        nodeName1 = modContext.dictionary.getModelicaName(bus1);
        nodeName2 = modContext.dictionary.getModelicaName(bus2);

        String modelicaname = nodeName1 + "-" + nodeName2;
        modContext.dictionary.add(switchNode, modelicaname);
        super.setModelicaName(modelicaname);
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(this.nodeName1);
		this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		this.addValue(this.nodeName2);
		this.addValue("." + StaticData.POSITIVE_PIN);
		this.addValue(EurostagFixedData.ANNOT_CONNECT);
	}

	@Override
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
        return parsedName;
	}
	
	@Override
	public ConnectCouplingDevicesRecord getClassName() {
		return this;
	}
	
	private Switch		switchNode;
	protected Bus		bus1 = null;
	protected Bus		bus2 = null;
	protected String	nodeName1;	
	protected String	nodeName2;
}
