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
public class PsseModDefaultTypes {
	//Machines
	public final static String GENROU							= "iPSL.Electrical.Machines.PSSE.GENROU.GENROU";
	public final static String GENSAL							= "iPSL.Electrical.Machines.PSSE.GENSAL.GENSAL";
	public final static String GENCLS							= "iPSL.Electrical.Machines.PSSE.GENCLS.GENCLS";
	
	//Regulators
	public final static String HYGOV							= "iPSL.Electrical.Controls.PSSE.TG.HYGOV";
	public final static String IEEET2							= "iPSL.Electrical.Controls.PSSE.ES.IEEET2.IEEET2";
	public final static String IEEEX1							= "iPSL.Electrical.Controls.PSSE.ES.IEEEX1.IEEEX1";
	public final static String IEESGO							= "iPSL.Electrical.Controls.PSSE.TG.IEESGO";
	public final static String SCRX								= "iPSL.Electrical.Controls.PSSE.ES.SCRX.SCRX";
	public final static String SEXS								= "iPSL.Electrical.Controls.PSSE.ES.SEXS.SEXS";
	public final static String STAB2A							= "iPSL.Electrical.Controls.PSSE.PSS.STAB2A.STAB2A";
	public final static String PSS2A							= "iPSL.Electrical.Controls.PSSE.PSS.PSS2A.PSS2A";
	public final static String TGOV1							= "iPSL.Electrical.Controls.PSSE.TG.TGOV1";
	public final static String IEEEST							= "iPSL.Electrical.Controls.PSSE.PSS.IEEEST.IEEEST";
	public final static String ESST1A							= "iPSL.Electrical.Controls.PSSE.ES.ESST1A.ESST1A";
	public final static String ESAC1A							= "iPSL.Electrical.Controls.PSSE.ES.ESAC1A.ESAC1A";
	public final static String ESDC1A							= "iPSL.Electrical.Controls.PSSE.ES.ESDC1A.ESDC1A";
	public final static String ESDC2A							= "iPSL.Electrical.Controls.PSSE.ES.ESDC2A.ESDC2A";
	public final static String WT4G1							= "iPSL.Electrical.Wind.PSSE.WT4G.WT4G1";
		
	//Regulators with constant
	public final static List<String> REGS_WITH_CONST			= Arrays.asList(new String[]{PsseModDefaultTypes.SCRX, 
																							PsseModDefaultTypes.SEXS, 
																							PsseModDefaultTypes.IEEET2});
	
	
	
	//Regulators with constant: en el caso de REN los reguladores tienes pines VUEL1, VUEL2, VUEL3, VOTHSG, VOTHSG2
	public final static List<String> REGS_WITH_CONST_REN			= Arrays.asList(new String[]{PsseModDefaultTypes.IEEEST,
																							PsseModDefaultTypes.ESST1A,
																							PsseModDefaultTypes.ESAC1A});
	
	
	
	public final static String PIN_TYPE							= "iPSL.Connectors.ImPin";
	
	public final static String DEFAULT_PIN_TYPE					= "iPSL.Connectors.ImPin";

	public final static String DEFAULT_BUS_TYPE 				= "iPSL.Electrical.Buses.Bus";
	
	public final static String DEFAULT_DETAILED_TRAFO_TYPE		= "iPSL.Electrical.Branches.Eurostag.PwPhaseTransformer";
	
	public final static String DEFAULT_FIXED_TRAFO_TYPE			= "iPSL.Electrical.Branches.Eurostag.PwTransformer_2";
	
	public final static String DEFAULT_GENROU_TYPE				= "iPSL.Electrical.Machines.PSSE.GENROU.GENROU";
	public final static String DEFAULT_GENSAL_TYPE				= "iPSL.Electrical.Machines.PSSE.GENSAL.GENSAL";
	public final static String DEFAULT_GENCLS_TYPE				= "iPSL.Electrical.Machines.PSSE.GENCLS.GENCLS";
	
	public final static String DEFAULT_GEN_LOAD_TYPE			= "iPSL.Electrical.Loads.PSAT.LOADPQ";
	
	public final static String DEFAULT_LINE_TYPE				= "iPSL.Electrical.Branches.PwLine_2";
	
	public final static String DEFAULT_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwOpenLine";
	
	public final static String DEF_SEN_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwLinewithOpeningSending";
	
	public final static String DEF_REC_OPEN_LINE_TYPE			= "iPSL.Electrical.Branches.PwLinewithOpening";
	
	public final static String DEFAULT_LOAD_TYPE				= "iPSL.Electrical.Loads.PSSE.Load";
	
	public final static String LOAD_VOLTAGE_DEP_TYPE			= "iPSL.Electrical.Loads.PwLoadVoltageDependence";
	
	public final static String CONSTANT_LOAD_TYPE				= "iPSL.Electrical.Loads.PSSE.Load";
	
	public final static String LOAD_FREQ_DEP					= "iPSL.Electrical.Loads.PwLoadFrequencyDependence";
	
	public final static String DEFAULT_CAPACITOR_TYPE			= "iPSL.Electrical.Banks.PwCapacitorBank";
	
	public final static String CONSTANT_TYPE					= "Modelica.Blocks.Sources.Constant";
	

}
