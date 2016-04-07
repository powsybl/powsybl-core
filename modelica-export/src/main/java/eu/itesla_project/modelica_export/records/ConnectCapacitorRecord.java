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
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Create a Modelica Connect Capacitor Record from IIDM Shunt connection
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectCapacitorRecord extends ConnectRecord {
	
	public ConnectCapacitorRecord(ConnectBusInfo busInfo, ShuntCompensator shunt) {
		super(busInfo.getBus().getId(), shunt.getId());
		this.isConnected = busInfo.isConnected();
//		this.busInfo = busInfo;
		this.node1 = busInfo.getBus();
		this.node2 = shunt;
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		if(!isConnected) this.addValue(StaticData.COMMENT);
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(super.nodeName1);
		
		if(this.node1 instanceof Bus) this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		else this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		
		this.addValue(super.nodeName2);
		
		this.addValue("." + StaticData.POSITIVE_PIN);
		
		this.addValue(EurostagFixedData.ANNOT_CONNECT);
	}
	
	@Override
	public ConnectCapacitorRecord getClassName() {
		return this;
	}
	
//	private ConnectBusInfo		busInfo;
	
	private Identifiable	node1	=	null;
	private Identifiable	node2	= 	null;
	private boolean			isConnected = true;
}
