/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util;

import eu.itesla_project.modelica_export.util.psse.PsseModDefaultTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class PsseEngine extends SourceEngine {

	/**
	 * Machines params between PSSE and Modelica <ModelicaParam>;<PSSEParam>
	 */
	private String GENROU = PsseModDefaultTypes.GENROU;
	private String genrouParams[][] = new String[][] {
															{"Tpd0", "Tpd0"}, 
															{"Tppd0", "Tppd0"}, 
															{"Tpq0", "Tpq0"}, 
															{"T2q0", "T2q0"}, 
															{"H", "H"}, 
															{"D", "D"}, 
															{"Xd", "Xd"}, 
															{"Xq", "Xq"}, 
															{"Xpd", "Xpd"}, 
															{"Xpq", "Xpq"}, 
															{"Xppd", "Xppd"}, 
															{"Xppq", "Xppq"}, 
															{"Xl", "Xl"}, 
															{"S10", "S10"}, 
															{"S20", "S20"}};
	
	private String GENSAL = PsseModDefaultTypes.GENSAL;
	private String gensalParams[][] = new String[][] {
															{"Tpd0", "Tpd0"}, 
															{"Tppd0", "Tppd0"}, 
															{"Tppq0", "Tppq0"}, 
															{"H", "H"}, 
															{"D", "D"}, 
															{"Xd", "Xd"}, 
															{"Xq", "Xq"}, 
															{"Xpd", "Xpd"}, 
															{"Xppd", "Xppd"}, 
															{"Xppq", "Xppq"}, 
															{"Xl", "Xl"}, 
															{"S10", "S10"}, 
															{"S20", "S20"}};
	
	private String GENCLS = PsseModDefaultTypes.GENCLS;
	private String genclsParams[][] = new String[][] {
												{"Tpd0", "Tpd0"}, 
												{"Tppd0", "Tppd0"}, 
												{"Tppq0", "Tppq0"}, 
												{"H", "H"}, 
												{"D", "D"}, 
												{"Xd", "Xd"}, 
												{"Xq", "Xq"}, 
												{"Xpd", "Xpd"}, 
												{"Xppd", "Xppd"}, 
												{"Xppq", "Xppq"}, 
												{"Xl", "Xl"}, 
												{"S10", "S10"}, 
												{"S20", "S20"}};
	
	private String WT4G1 = PsseModDefaultTypes.WT4G1;
	private String wt4g1Params[][] = new String[][] {			
												{"T_IQCmd", "T_IQCmd"}, 
												{"T_IPCmd", "T_IPCmd"}, 
												{"V_LVPL1", "V_LVPL1"}, 
												{"V_LVPL2", "V_LVPL2"}, 
												{"G_LVPL", "G_LVPL"}, 
												{"V_HVRCR", "V_HVRCR"}, 
												{"CUR_HVRCR", "CUR_HVRCR"}, 
												{"RIp_LVPL", "RIp_LVPL"}, 
												{"T_LVPL", "T_LVPL"}};
	
	public PsseEngine(String name, String version) {
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
		//GENROU MACHINES
		for(int i=0; i<this.genrouParams.length; i++) {
			modelicaPar = this.genrouParams[i][0];
			sourcePar = this.genrouParams[i][1];
		
			map.put(modelicaPar, sourcePar);
		}
		paramsDictionary.put(PsseModDefaultTypes.GENROU, map);
		
		//GENSAL MACHINES
		for(int i=0; i<this.gensalParams.length; i++) {
			modelicaPar = this.gensalParams[i][0];
			sourcePar = this.gensalParams[i][1];
		
			map.put(modelicaPar, sourcePar);
		}
		paramsDictionary.put(this.GENSAL, map);
		
		//WT4G1 MACHINES
		for(int i=0; i<this.wt4g1Params.length; i++) {
			modelicaPar = this.wt4g1Params[i][0];
			sourcePar = this.wt4g1Params[i][1];
		
			map.put(modelicaPar, sourcePar);
		}
		paramsDictionary.put(this.WT4G1, map);
		
		return paramsDictionary;
	}
	
	
}
