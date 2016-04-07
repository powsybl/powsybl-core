/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util.eurostag;

import java.util.Arrays;
import java.util.List;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EurostagFixedData {
	///Según la API de IIDM dan por supuesto que la potencia nominal es 100	
	public final static String		PARAMETER			= "parameter";
	
	public final static String 		OMEGAREF_NAME		= "omegaRef";
	
	public final static String 		ANNOT				= ") annotation (Placement(transformation()));";
	public final static String 		ANNOT_CONNECT		= ") annotation (Line());";
	public final static String 		CONNECT				= "connect(";
	
	public final static String		HIN_PIN				= "SN";
	public final static String		SN_PIN				= "HIn";
	public final static String		OMEGA_PIN			= "omega";
	
	public final static String 		GEN_SORTIE_PIN		= "sortie";
	public final static String		GEN_OMEGAREF_PIN	= "omegaRef";
	
	/**
	 * Data about generators with transformer included
	 */
	public final static String			TRAFO_INCLUDED		= "T";
	public final static String			TRAFO_NOT_INCLUDED	= "N";
	public final static String			IS_SATURATED		= "S";
	public final static String			IS_UNSATURATED		= "U";
	public final static List<String>	TRAFO_GEN_PARAMS	= Arrays.asList(new String[]{"V1", "V2", "U1N", "U2N", "SNtfo", "RTfoPu", "XTfoPu"});
	public final static List<String>	MACHINE_INIT_PAR	= Arrays.asList(new String[]{"lambdaF0", "lambdaD0", "lambdaAD0", "lambdaAQ0", "lambdaQ10", "lambdaQ20", "iD0", "iQ0", "teta0", "omega_0", "cm0", "efd0", "mDVPu"});
	public final static List<String>	MACHINE_PAR			= Arrays.asList(new String[]{"init_lambdaf", "init_lambdad", "init_lambdaad", "init_lambdaaq", "init_lambdaq1", "init_lambdaq2", "init_id", "init_iq", "init_theta", "init_omega", "init_cm", "init_efd", "WLMDVPu"});
	public final static List<String>	SATURATED_MACHINE	= Arrays.asList(new String[]{"snq", "snd", "mq", "md"});
	
	
	/**
	 * MODELICA PARAMETER NAMES
	 */
	public final static String	UR0			= "ur0";
	public final static String	UI0			= "ui0";
	public final static String	V_0			= "V_0";
	public final static String	ANGLE_0		= "angle_0";
	public final static String	VO_REAL		= "Vo_real";
	public final static String	VO_IMG		= "Vo_img";
	public final static String	P			= "P";
	public final static String	Q			= "Q";
	public final static String	SNOM		= "Snom";
	public final static String	PCU			= "Pcu";
	public final static String	PFE			= "Pfe";
	public final static String	IM			= "IM";
	public final static String	B0			= "B0";
	public final static String	G0			= "G0";
	public final static String	V1			= "V1";
	public final static String	V2			= "V2";
	public final static String	U1N			= "U1N";
	public final static String	U2N			= "U2N";
	public final static String	U1nom		= "U1nom";
	public final static String	U2nom		= "U2nom";
	public final static String	UCC			= "Ucc";
	public final static String	THETA		= "theta";
	public final static String	ESAT		= "ESAT";
	public final static String	R			= "R";
	public final static String	X			= "X";
	public final static String	G			= "G";
	public final static String	B			= "B";
	public final static String	r			= "r";
	public final static String	ALPHA		= "alpha";
	public final static String	BETA		= "beta";
	public final static String	TRAFOINCLUDED	="transformerIncluded";
	public final static String	SATURATED		="Saturated";
	public final static String	INLMDV			= "IWLMDV";
	public final static String	TX				= "TX";
	public final static String	XD				= "XD";
	public final static String	XPD				= "XPD";
	public final static String	XSD				= "XSD";
	public final static String	TPD0			= "TPD0";
	public final static String	TSD0			= "TSD0";
	public final static String	XQ				= "XQ";
	public final static String	XPQ				= "XPQ";
	public final static String	XSQ				= "XSQ";
	public final static String	TPQ0			= "TPQ0";
	public final static String	TSQ0			= "TSQ0";
	public final static String	IENR			= "IENR";
	
	public final static String	SNTFO		= "SNtfo";
	public final static String	SN			= "SN";
	public final static String	RTFOPU		= "RTfoPu";
	public final static String	XTFOPU		= "XTfoPu";
	public final static String	SND			= "snd";
	public final static String	SNQ			= "snq";
	public final static String	MD			= "md";
	public final static String	MQ			= "mq";
	public final static String	RSTATIN		= "rStatIn";
	public final static String	LSTATIN 	= "lStatIn";
	public final static String	MQ0PU 		= "mQ0Pu";
	public final static String	MD0PU		= "mD0Pu";
	public final static String	PN			= "PN";
	public final static String	LDPU		= "lDPu";
	public final static String	RROTIN		= "rRotIn";
	public final static String	LROTIN		= "lRotIn";
	public final static String	RQ1PU		= "rQ1Pu";
	public final static String	LQ1PU		= "lQ1Pu";
	public final static String	RQ2PU		= "rQ2Pu";
	public final static String	LQ2PU		= "lQ2Pu";
	public final static String	MCANPU		= "mCanPu";
	public final static String	PNALT		= "PNALT";
	
	
	//M1S & M2S INIT
	public final static String	INIT_SNREF		= "SNREF";
	public final static String	INIT_SN			= "SN";
	public final static String	INIT_PN			= "PN";
	public final static String	INIT_PNALT		= "PNALT";
	public final static String	INIT_SNTFO		= "sNTfo";
	public final static String	INIT_UR0		= "ur0";
	public final static String	INIT_UI0		= "ui0";
	public final static String	INIT_P0			= "p0";
	public final static String	INIT_Q0			= "q0";
	public final static String	INIT_UNRESTFO	= "uNResTfo";
	public final static String	INIT_UNOMNW		= "uNomNw";
	public final static String	INIT_UNMACTFO	= "uNMacTfo";
	public final static String	INIT_UBMAC		= "uBMac";
	public final static String	INIT_RTFOIN		= "rTfoIn";
	public final static String	INIT_XTFOIN		= "xTfoIn";
	public final static String	INIT_NDSAT		= "nDSat";
	public final static String	INIT_NQSAT		= "nQSat";
	public final static String	INIT_MDSATIN	= "mDSatIn";
	public final static String	INIT_MQSATIN	= "mQSatIn";
	public final static String	INIT_RSTATIN	= "rStatIn";
	public final static String	INIT_LSTATIN	= "lStatIn";
	public final static String	INIT_MD0PU		= "mD0Pu";
	public final static String	INIT_PNOM		= "pNom";
	public final static String	INIT_OMEGA0		= "omega_0";
	public final static String	INIT_PPUWLMDV 	= "pPuWLMDV";
	public final static String	INIT_IENR		= "IENR";
	
	//M1S INIT
	public final static String	INIT_MQ0PU		= "mQ0Pu";
	public final static String	INIT_LDPU		= "lDPu";
	public final static String	INIT_RROTIN		= "rRotIn";
	public final static String	INIT_LROTIN		= "lRotIn";
	public final static String	INIT_RQ1PU		= "rQ1Pu";
	public final static String	INIT_LQ1PU		= "lQ1Pu";
	public final static String	INIT_RQ2PU		= "rQ2Pu";
	public final static String	INIT_LQ2PU		= "lQ2Pu";
	public final static String	INIT_MCANPU		= "mCanPu";
	
	//M2S INIT
	public final static String	INIT_XD 		= "XD";
	public final static String	INIT_XSD 		= "XSD";
	public final static String	INIT_XPD 		= "XPD";
	public final static String	INIT_TPDO 		= "TPD0";
	public final static String	INIT_TSDO 		= "TSD0";
	public final static String	INIT_XQ 		= "XQ";
	public final static String	INIT_XPQ 		= "XPQ";
	public final static String	INIT_XSQ	 	= "XSQ";
	public final static String	INIT_TPQO 		= "TPQ0";
	public final static String	INIT_TSQO 		= "TSQ0";
	public final static String	INIT_TX 		= "TX";
	
	public final static String NSTEPS			= "nsteps";
	public final static String BO				= "Bo";
	
	public final static String OPENR			= "OpenR_end";

}