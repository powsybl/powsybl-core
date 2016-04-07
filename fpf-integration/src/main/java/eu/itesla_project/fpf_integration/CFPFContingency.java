/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class defines the contingency data layout necessary to run a power flow
 * 
 * @author Leonel Carvalho - INESC TEC
 */

public class CFPFContingency {
	private int number; // CONTINGENCY NUMBER
	private int numberBranchesOnOutage; // NUMBER OF BRANCHES ON OUTAGE
	private ArrayList< Integer > branchesOnOutage = null; // BRANCHES ON OUTAGE
	
	public CFPFContingency() {
		
	}
	
	public CFPFContingency( int number, ArrayList< Integer > branchesOnOutage ) {
		setNumber( number );
		setNumberBranchesOnOutage( branchesOnOutage.size() );
		Collections.sort( branchesOnOutage );
		setBranchesOnOutage( branchesOnOutage );
	}
	
	public String printCFPFContingency() {
		String ch = ",";
		String out = getNumber() + ch + 
				getNumberBranchesOnOutage();
		for( int i = 0; i < numberBranchesOnOutage; i++ ) {
			out += ch + branchesOnOutage.get( i );
		}
		return out;
	}
	
	public boolean contains( int number ) {
		return branchesOnOutage.contains( number );
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumberBranchesOnOutage() {
		return numberBranchesOnOutage;
	}

	public void setNumberBranchesOnOutage(int numberBranchesOnOutage) {
		this.numberBranchesOnOutage = numberBranchesOnOutage;
	}

	public ArrayList<Integer> getBranchesOnOutage() {
		return branchesOnOutage;
	}

	public void setBranchesOnOutage(ArrayList<Integer> branchesOnOutage) {
		this.branchesOnOutage = branchesOnOutage;
	}
}