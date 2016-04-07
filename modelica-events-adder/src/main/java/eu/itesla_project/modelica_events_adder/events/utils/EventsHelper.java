/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.modelica_events_adder.events.records.ConnectRecord;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EventsHelper {
	
	public static String parseBusName() {
		return null;
	}

	public static void	addLine(Writer writer, String line) {
		try {
			writer.append(line);
			writer.append(StaticData.NEW_LINE);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	/*
	 * Converts Modelica Name to CIM ID 
	 */
	public static String parseModelicaToCIM(String modelicaName, String modelicaType) {
//		String prefixType = modelicaType.substring(modelicaType.lastIndexOf(".")+1);
		String cimid = null;
		
		if(modelicaName.startsWith(StaticData.PREF_BUS)) {
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_GEN)) { //gen_gENROU__78bb0400_86f5_11e4_824f_c8f73332c8f4(
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_LINE)) {
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_TRAFO)) { //trafo_PwPhaseTransformer__78bb01eb_86f5_11e4_824f_c8f73332c8f4__78bb01e4_86f5_11e4_824f_c8f73332c8f4_1(
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_LOAD)) { //load_pwLoadVoltageDependence__78bb02fa_86f5_11e4_824f_c8f73332c8f4
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_CAP)) {
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		else if(modelicaName.startsWith(StaticData.PREF_REG)) {
			cimid = "_" + (modelicaName.substring(modelicaName.indexOf("__")+2)).replaceAll("_", "-");
		}
		return cimid;		
	}
	
	
	private static final Logger log = LoggerFactory.getLogger(EventsHelper.class);
}
