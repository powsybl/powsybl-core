/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export.util.eurostag;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class EurostagModDefaultTypes {
	public final static String PIN_TYPE							= "iPSL.Connectors.ImPin";
	
	public final static String DEFAULT_PIN_TYPE					= "iPSL.Connectors.ImPin"; // iPSL.Connectors.PwPin 20140515

	public final static String DEFAULT_BUS_TYPE 				= "iPSL.Electrical.Buses.Bus";
	
	public final static String DEFAULT_DETAILED_TRAFO_TYPE		= "iPSL.Electrical.Branches.Eurostag.PwPhaseTransformer";
	
	public final static String DEFAULT_FIXED_TRAFO_TYPE			= "iPSL.Electrical.Branches.Eurostag.PwTransformer_2";
	
	public final static String DEFAULT_GEN_TYPE					= "iPSL.Electrical.Machines.Eurostag.PwGeneratorM2S";
	
	public final static String DEFAULT_GEN_LOAD_TYPE			= "iPSL.Electrical.Loads.PwLoadPQ";
	
	public final static String DEFAULT_LINE_TYPE				= "iPSL.Electrical.Branches.PwLine_2";
	
	public final static String DEFAULT_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwOpenLine";
	
	public final static String DEF_SEN_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwLinewithOpeningSending";
	
	public final static String DEF_REC_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwLinewithOpeningReceiving";
	
	public final static String DEFAULT_LOAD_TYPE				= "iPSL.Electrical.Loads.PwLoadPQ";
	
	public final static String LOAD_VOLTAGE_DEP_TYPE			= "iPSL.Electrical.Loads.PwLoadVoltageDependence";
	
	public final static String LOAD_FREQ_DEP					= "iPSL.Electrical.Loads.PwLoadFrequencyDependence";
	
	public final static String DEFAULT_CAPACITOR_TYPE			= "iPSL.Electrical.Banks.PwCapacitorBank";
	
	public final static String M1S_INIT_MODEL					= "iPSL.Electrical.Machines.Eurostag.DYNModelM1S_INIT";
	public final static String M2S_INIT_MODEL					= "iPSL.Electrical.Machines.Eurostag.DYNModelM2S_INIT";
	
	public final static String M1S_MACHINES						= "iPSL.Electrical.Machines.Eurostag.PwGeneratorM1S";
	public final static String M2S_MACHINES						= "iPSL.Electrical.Machines.Eurostag.PwGeneratorM2S";
}
