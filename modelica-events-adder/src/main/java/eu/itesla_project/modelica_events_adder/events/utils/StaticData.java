/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events.utils;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class StaticData {
	
	//SOURCE ENGINES
	public final static String		EUROSTAG			= "eurostag";
	public final static String		PSSE				= "psse";
	
	//NAMES PREFIXES
	public final static String		PREF_BUS			= "bus_";
	public final static String		PREF_GEN			= "gen_";
	public final static String		PREF_LINE			= "line_";
	public final static String		PREF_TRAFO			= "trafo_";
	public final static String		PREF_LOAD			= "load_";
	public final static String		PREF_CAP			= "cap_";
	public final static String		PREF_REG			= "reg_";
	
	public final static String		IPSL		= "iPSL";
	///Según la API de IIDM dan por supuesto que la potencia nominal es 100
	public final static float		SNREF_VALUE			= 100;
	public final static String		SNREF				= "SNREF";
	public final static String		VO_REAL				= "Vo_real";
	public final static String		VO_IMG				= "Vo_img";
	public final static String		R					= "R";
	public final static String		X					= "X";
	public final static String		G					= "G";
	public final static String		B					= "B";
	public final static String		VOLTAGE				= "V_0";
	public final static String		ANGLE				= "angle_0";
	
	public final static String 		PARAM_TYPE			= "parameter Real";
	public final static String		PARAMETER			= "parameter";
	
	public final static String 		OMEGAREF_NAME		= "omegaRef";
	
	public final static String 		ANNOT				= ") annotation (Placement(transformation()));";
	public final static String 		ANNOT_CONNECT		= ") annotation (Line());";
	public final static String 		CONNECT				= "connect(";
	
	public final static String		PIN					= "pin_";
	public final static String		INIT_VAR			= "init_";
	
	public final static String 		POSITIVE_PIN		= "p";
	public final static String 		NEGATIVE_PIN		= "n";
	public final static String 		BREAKER_S_PIN		= "s";
	public final static String 		BREAKER_R_PIN		= "r";
	public final static String		HIN_PIN				= "SN";
	public final static String		SN_PIN				= "HIn";
	public final static String		OMEGA_PIN			= "omega";
	
	public final static String 		GEN_SORTIE_PIN		= "sortie";
	public final static String		GEN_OMEGAREF_PIN	= "omegaRef";
	
	public final static String		MO					= "mo";
	public final static String		MO_INIT				= "init_mo";
	public final static String		MO_EXTENSION		= ".mo";
	public final static String		MO_LIB_EXTENSION	= "_Lib.mo";
	public final static String		MO_INIT_EXTENSION	= "_Init.mo";
	
	public final static String		NEW_LINE			= System.getProperty("line.separator");
	public final static String		WHITE_SPACE			= " ";
	public final static String		DOT					= "\\.";
	
	public final static String		MODEL				= "model ";
	public final static String		WITHIN				= "within ;";
	public final static String		EQUATION			= "equation";
	public final static String		END_MODEL			= "end ";
	public final static String		INITIALIZATION		= "_Initialization";
	public final static String		INIT				= "_init";
	public final static	String		SEMICOLON			= ";";
	public final static String		COMMENT				= "//";
	
	public final static String		MTC_PREFIX_NAME		= "MTC_"; 
	
	
	public final static String		CON_OTHERS			= "// Connecting OTHERS";
}
