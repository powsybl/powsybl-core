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
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.util.EurostagEngine;
import eu.itesla_project.modelica_export.util.PsseEngine;
import eu.itesla_project.modelica_export.util.SourceEngine;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * Create a Modelica Connect Generator Record from IIDM Generator connection (between Generator and OmegaRef)
 * @author Silvia Machado <machados@aia.es>
 */
public class ConnectGeneratorRecord extends ConnectRecord {
	
	public ConnectGeneratorRecord(ConnectBusInfo busInfo, Generator gen, boolean isInyection, SourceEngine sourceEngine) {
		super(busInfo.getBus().getId(), gen.getId());
		this.node1 = busInfo.getBus();
		this.node2 = gen;
		this.isConnected = busInfo.isConnected();
		this.isInyection = isInyection;
		this.sourceEngine = sourceEngine;
	}
	
	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		if(!isConnected) this.addValue(StaticData.COMMENT);
		
		this.addValue(StaticData.CONNECT);
		this.addValue(super.nodeName1);
		
		if(this.node1 instanceof Bus) this.addValue("." + StaticData.POSITIVE_PIN + ", ");
		else this.addValue("." + StaticData.NEGATIVE_PIN + ", ");
		
		this.addValue(super.nodeName2);
		
		if(!isInyection) {
			if(this.sourceEngine instanceof EurostagEngine) {
				this.addValue("." + EurostagFixedData.GEN_SORTIE_PIN);
			} else if(this.sourceEngine instanceof PsseEngine) {
				this.addValue("." + StaticData.POSITIVE_PIN);
			}
		}
		else this.addValue("." + StaticData.POSITIVE_PIN);
		
		this.addValue(StaticData.ANNOT_CONNECT);	
	}
	
	@Override
	public ConnectGeneratorRecord getClassName() {
		return this;
	}
	
//	private ConnectBusInfo		busInfo;
	private SourceEngine		sourceEngine;
	
	public boolean				isInyection;
	public boolean				isConnected = true;
	
	private Identifiable		node1 = null;
	private Identifiable		node2 = null;
}
