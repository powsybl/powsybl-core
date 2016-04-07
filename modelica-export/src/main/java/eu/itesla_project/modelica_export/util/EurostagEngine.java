/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EurostagEngine extends SourceEngine {
	
	/**
	 * Machines params between Eurostag and Modelica <ModelicaParam>;<EurostagParam>
	 */
	private String pwGeneratorM1S = "iPSL.Electrical.Machines.Eurostag.PwGeneratorM1S";
	private String m1sParams[][] = new String[][] {
															{"rRotIn", "RF"}, 
															{"mD0Pu", "WLMQ"}, 
															{"mq", "RMQ"}, 
															{"HIn", "H"}, 
															{"md", "RMD"}, 
															{"PNALT", "PNALT"}, 
															{"rStatIn", "RA"}, 
															{"mQ0Pu", "WLMD"}, 
															{"DIn", "DAMP"}, 
															{"lRotIn", "WLF"}, 
															{"snq", "RNQ"}, 
															{"rQ1Pu", "RQ1"}, 
															{"rQ2Pu", "RQ2"}, 
															{"SN", "SN"}, 
															{"lQ2Pu", "WLQ2"}, 
															{"lStatIn", "WL"}, 
															{"lQ1Pu", "WLQ1"}, 
															{"PN", "PN"}, 
															{"lDPu", "WLDD"}, 
															{"snd", "RND"}, 
															{"rDPu", "RDD"}, 
															{"transformerIncluded", "transformer.included"}, 
															{"SNtfo", "PNT"}, 
															{"XTfoPu", "XT"}, 
															{"RTfoPu", "RT"}, 
															{"U1N", "UNM"}, 
															{"U2N", "UNN"}, 
															{"V1", "UN"}, 
															{"IWLMDV", "IWLMDV"}, 
															{"Saturated", "saturated"}};
	
	
	private String pwGeneratorM2S = "iPSL.Electrical.Machines.Eurostag.PwGeneratorM2S";
	private String m2sParams[][] = new String[][] {
															{"SNREF", ""}, 
															{"SN", "SN"}, 
															{"PN", "PN"}, 
															{"PNALT", "PNALT"}, 
															{"DIn", "DAMP"}, 
															{"HIn", "H"}, 
															{"rStatIn", "RA"}, 
															{"lStatIn", "WL"}, 
															{"mD0Pu", "WLMQ"}, 
															{"XD", "XD"}, 
															{"XPD", "XPD"}, 
															{"XSD", "XSD"}, 
															{"TPD0", "TPD0"}, 
															{"TSD0", "TSD0"}, 
															{"TX", "TX"}, 
															{"XQ", "XQ"}, 
															{"XPQ", "XPQ"}, 
															{"XSQ", "XSQ"}, 
															{"TPQ0", "TPQ0"}, 
															{"TSQ0", "TSQ0"}, 
															{"md", "RMD"}, 
															{"mq", "RMQ"}, 
															{"snd", "RND"}, 
															{"snq", "RNQ"}, 
															{"rDPu", "RDD"}, 
															{"lDPu", "WLDD"}, 
															{"rRotIn", "RF"}, 
															{"lRotIn", "WLF"}, 
															{"mQ0Pu", "WLMD"}, 
															{"transformerIncluded", "transformer.included"}, 
															{"SNtfo", "PNT"}, 
															{"XTfoPu", "XT"}, 
															{"RTfoPu", "RT"}, 
															{"U1N", "UNM"}, 
															{"U2N", "UNN"}, 
															{"V1", "UN"}, 
															{"IENR", "IENR"}, 
															{"IWLMDV", "IWLMDV"}, 
															{"Saturated", "saturated"}};	  
	
	public EurostagEngine(String name, String version) {
		this.setName(name);
		this.setVersion(version);
	}

	@Override
	public HashMap<String, Map<String, String>> createGenParamsDictionary() {
		HashMap<String, Map<String, String>> paramsDictionary = new HashMap<String, Map<String, String>>();
		Map<String, String> map = null;
		String modelicaPar;
		String sourcePar;
		
		map = new HashMap<String, String>();
		//M1S machines
		for(int i=0; i<this.m1sParams.length; i++) {
			modelicaPar = this.m1sParams[i][0];
			sourcePar = this.m1sParams[i][1];
		
			map.put(modelicaPar, sourcePar);
		}
		paramsDictionary.put(this.pwGeneratorM1S, map);
			
		//M2S machines
		map = new HashMap<String, String>();
		for(int i=0; i<this.m2sParams.length; i++) {
			modelicaPar = this.m2sParams[i][0];
			sourcePar = this.m2sParams[i][1];
		
			map.put(modelicaPar, sourcePar);
		}
		paramsDictionary.put(this.pwGeneratorM2S, map);
		
		return paramsDictionary;
	}
	
}
