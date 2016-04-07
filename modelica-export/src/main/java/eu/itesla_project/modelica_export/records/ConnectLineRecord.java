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
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Create a Modelica Connect Line Record from IIDM Line connections
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectLineRecord extends ConnectRecord {

	public ConnectLineRecord(Identifiable node1, Identifiable node2) {
		super(node1.getId(), node2.getId());
		this.node1 = node1;
		this.node2 = node2;
	}
	
	public ConnectLineRecord(Identifiable node1, String node2Id) {
		super(node1.getId(), node2Id);
		this.node1 = node1;
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		
		this.addValue(EurostagFixedData.CONNECT);
		this.addValue(super.nodeName1);
		
		if(this.node1 instanceof Bus) this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		else this.addValue("." + StaticData.NEGATIVE_PIN + ", ");
		
		this.addValue(super.nodeName2);
		this.addValue("." + StaticData.POSITIVE_PIN);
		this.addValue(EurostagFixedData.ANNOT_CONNECT);
	}
	
	@Override
	public ConnectLineRecord getClassName() {
		return this;
	}
	
	private Identifiable node1	=	null;
	private Identifiable node2	= 	null;
}
