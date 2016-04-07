/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_events_adder.events;

import java.util.Arrays;
import java.util.List;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EventsStaticData {
	
	public final static String	LINE_MODEL			= "iPSL.Electrical.Events.PwLineFault";
	public final static String	BUS_MODEL			= "iPSL.Electrical.Events.PwFault";
	public final static String	BREAKER_MODEL		= "iPSL.Electrical.Events.Breaker";
	public final static String	LINE_OPEN_REC_MODEL	= "iPSL.Electrical.Branches.PwLinewithOpeningReceiving";
	public final static String	LINE_2_OPEN_MODEL	= "iPSL.Electrical.Branches.PwLine2Openings";
	public final static String	BANK_MODIF_MODEL	= "iPSL.Electrical.Banks.PwCapacitorBankWithModification";
	public final static String	LOAD_VAR_MODEL		= "iPSL.Electrical.Loads.PwLoadwithVariation";
	
	public final static String		LINE_FAULT			= "LINE_FAULT";
	public final static String		BUS_FAULT			= "BUS_FAULT";
	public final static String		LINE_OPEN_REC		= "LINE_OPEN_REC";
	public final static String		LINE_2_OPEN			= "LINE_2_OPEN";
	public final static	String		BANK_MODIF			= "BANK_MODIF";
	public final static	String		LOAD_VAR			= "LOAD_VAR";
	public final static String		BREAKER				= "BREAKER";
	
	public final static List<String> EVENT_TYPES		=  Arrays.asList(new String[] {BUS_FAULT, LINE_FAULT, LINE_OPEN_REC, BANK_MODIF, LOAD_VAR, BREAKER});

	//Other parameters of the events
	public static String VO_REAL1	= "Vo_real1";
	public static String VO_IMG1	= "Vo_img1";
	public static String VO_REAL2	= "Vo_real2";
	public static String VO_IMG2	= "Vo_img2";
	public static String R1			= "R1";
	public static String X1			= "X1";
	public static String G1			= "G1";
	public static String B1			= "B1";
	public static String P2			= "P2";
	public static String Q2			= "Q2";
	
}
