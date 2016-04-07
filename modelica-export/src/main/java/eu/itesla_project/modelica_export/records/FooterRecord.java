/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.records;

import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.modelica_export.ModExportContext;
import eu.itesla_project.modelica_export.ModelicaRecord;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class FooterRecord extends ModelicaRecord {
	
	public FooterRecord(String networkName) {
		this.networkName = networkName;
	}

	@Override
	public void createModelicaName(ModExportContext modContext, DDBManager ddbManager, SimulatorInst modelicaSim) {
		
	
	}

	@Override
	public void createRecord(ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		this.deleteInicialWhiteSpaces(2);
        this.addValue(StaticData.END_MODEL + parseName(this.networkName) + StaticData.SEMICOLON);
        this.newLine();
	}

	@Override
	public String parseName(String name) {
		String parsedName = name.trim();
		if(parsedName.substring(0, 1).matches("[0-9]")) {
			parsedName = "M_" + parsedName;
		}
		parsedName = parsedName.replaceAll("\\s", "_");
       	parsedName = parsedName.replaceAll("\\.", "_");
       	parsedName = parsedName.replaceAll("\\-", "_");
       	parsedName = parsedName.replaceAll("/", "_");
       	parsedName = parsedName.replaceAll("\\+", "_");
        return parsedName;
	}
	
	@Override
	public FooterRecord getClassName() {
		return this;
	}
	
	private String networkName;
}
