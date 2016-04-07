/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util.psse;

import java.util.Arrays;
import java.util.List;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class PsseFixedData {
	///Segun la API de IIDM dan por supuesto que la potencia nominal es 100
	public final static float	SNREF_VALUE			= 100;
	public final static String	CONSTANT		= "const(k=0)";
	public final static String	CONST_NAME		= "const";
	public final static String	CONSTANT1		= "const1(k=-9999)";
	public final static String	CONST_NAME1		= "const1";
	public final static String	CONSTANT2		= "const2(k=9999)";
	public final static String	CONST_NAME2		= "const2";
	
	public final static String	ETERM			= "eterm";
	public final static String	V_0				= "V_0";
	public final static String	V_c0			= "V_c0";
	public final static String	EC0				= "Ec0";
	public final static String	ET0				= "Et0";
	public final static String	Mbase			= "Mbase";
	public final static String	M_b				= "M_b"; //Mbase in machines
	public final static String	ANGLEV0			= "anglev0";
	public final static String	ANGLE_0			= "angle_0";
	public final static String	PELEC			= "pelec";
	public final static String	QELEC			= "qelec";
	public final static String	P_0				= "P_0";
	public final static String	Q_0				= "Q_0";
	public final static String	PMECH			= "pmech";
	public final static String	p0				= "p0";
	public final static String	P0				= "P0";
	public final static String	V0				= "V0";
	public final static String	v0				= "v0";
	public final static String	S_p				= "S_p";
	public final static String	S_i				= "S_i";
	public final static String	S_y				= "S_y";
	public final static String	a				= "a";
	public final static String	b				= "b";
	public final static String	PQBRAK				= "PQBRAK";
	
	public final static String	VOEL_PIN		= "VOEL";
	public final static String	VUEL_PIN		= "VUEL";
	public final static String	VUEL1_PIN		= "VUEL1";
	public final static String	VUEL2_PIN		= "VUEL2";
	public final static String	VUEL3_PIN		= "VUEL3";
	public final static String	VOTHSG_PIN		= "VOTHSG";
	public final static String	VOTHSG2_PIN		= "VOTHSG2";
	public final static String	VT_PIN			= "VT";
	public final static String	ECOMP_PIN		= "ECOMP";
	public final static String	SIGNAL_PIN		= "Signal";
	public final static String	VCT_PIN			= "VCT";
	public final static String	Y_PIN			= "y";
	public final static String	PMECH_PIN			= "PMECH";
	public final static String	PMECH0_PIN			= "PMECH0";
	
		
	public final static List<String> SPECIAL_REGS = Arrays.asList(new String[] {PsseModDefaultTypes.SCRX,
																				PsseModDefaultTypes.SEXS,
																				PsseModDefaultTypes.IEEET2});
	
}