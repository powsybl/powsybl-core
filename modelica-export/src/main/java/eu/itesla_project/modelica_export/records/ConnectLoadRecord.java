/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.Equipments.ConnectionInfo;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.util.StaticData;

/**
 * Create a Modelica Connect Load Record from IIDM Load connection
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectLoadRecord extends ConnectRecord {
	
	public ConnectLoadRecord(ConnectBusInfo busInfo, Load load) {
		super(busInfo.getBus().getId(), load.getId());
	}

	public ConnectLoadRecord(String busName, String loadName) {
		super(busName, loadName);
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
//		if(!busInfo.isConnected()) this.addValue(StaticData.COMMENT);
		
//		if(Float.isNaN(busInfo.getBus().getV())) this.addValue(StaticData.COMMENT);
//		else if (!busInfo.isConnected()) this.addValue(StaticData.COMMENT);
		
		this.addValue(StaticData.CONNECT);
		this.addValue(super.nodeName1);
		
		this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		
		this.addValue(super.nodeName2);
		
		this.addValue("." + StaticData.POSITIVE_PIN);
		
		this.addValue(StaticData.ANNOT_CONNECT);
	}
	
	@Override
	public ConnectLoadRecord getClassName() {
		return this;
	}
}
