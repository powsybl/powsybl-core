/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import eu.itesla_project.iidm.ddb.model.Parameters;
import eu.itesla_project.iidm.ddb.model.Simulator;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Sorter {

	public static Comparator<Parameters> compareParamSetNum = new Comparator<Parameters>()
		    {
		    // This is where the sorting happens.
		        public int compare(Parameters p1, Parameters p2)
		        {   System.out.println(" compare "+p1.getDefParamSetNum() + " with "+p2.getDefParamSetNum());
		        	int diff= p1.getDefParamSetNum() - p2.getDefParamSetNum() ;
		        	return (diff==0 && p1.getSimulator().equals(p2.getSimulator()))? 0 :diff; 
		            
		        }
		    };
		     
		    public static void main(String[] args)
		    {
		    	SimulatorInst simulEurostag1 = new SimulatorInst(Simulator.EUROSTAG, "rel 1.0.0");
		    	SimulatorInst simulEurostag2 = new SimulatorInst(Simulator.EUROSTAG, "rel 2.0.0");
		    	SimulatorInst simulEurostag3 = new SimulatorInst(Simulator.EUROSTAG, "rel 3.0.0");
		    	SimulatorInst simulModelica1 = new SimulatorInst(Simulator.MODELICA, "rel 1.1.1");
		    	SimulatorInst simulModelica2 = new SimulatorInst(Simulator.MODELICA, "rel 2.2.2");
		    	SimulatorInst simulModelica3 = new SimulatorInst(Simulator.MODELICA, "rel 3.3.3");
		    	
		    	Parameters pe1 = new Parameters(simulEurostag1);
		    	pe1.setDefParamSetNum(3);
		    	Parameters pe2 = new Parameters(simulEurostag2);
		    	pe2.setDefParamSetNum(0);
		    	Parameters pe3 = new Parameters(simulEurostag3);
		    	pe2.setDefParamSetNum(1);
		    	Parameters pm1 = new Parameters(simulModelica1);
		    	pm1.setDefParamSetNum(2);
		    	Parameters pm2 = new Parameters(simulModelica2);
		    	pm2.setDefParamSetNum(0);
		    	Parameters pm3 = new Parameters(simulModelica3);
		    	pm3.setDefParamSetNum(1);
		    	
		    	List<Parameters> params = new ArrayList<Parameters>(6);
		    	params.add(pe1);
		    	params.add(pe2);
		    	params.add(pe3);
		    	params.add(pm1);
		    	params.add(pm2);
		    	params.add(pm3);
		   
		     
		    for (Parameters par : params)
		        System.out.println("Before sorting on defParamSetNum: " + par.getDefParamSetNum() + par.getSimulator().toString());
		    
		    System.out.println("........................................................ "); 
		    Collections.sort(params, compareParamSetNum);
		     
		    for (Parameters par : params)
		        System.out.println("After sorting on defParamSetNum: " + par.getDefParamSetNum()+ par.getSimulator().toString());
		}
		 
}
