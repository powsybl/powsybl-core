/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

import java.util.Hashtable;

/**
 * This class contains power system data.
 * 
 * @author Leonel Carvalho - INESC TEC
 */
public class CFPFPowerSystem {
	
	// Case Data
	private String date = null; // DATE (DD/MM/YY)
	private String originatorName = null; // NAME OF THE ORIGINATOR
	private double baseMVA; // MVA BASE
	private int year; // YEAR
	private String season = null; // SEASON
	private String caseId = null; // CASE IDENTIFICATION
	// Database
	private Hashtable< Integer, CFPFBus > hashBuses = new Hashtable< Integer, CFPFBus >(); // LIST BUSES
	private Hashtable< Integer, CFPFBranch > hashBranches = new Hashtable< Integer, CFPFBranch >(); // LIST BRANCHES
	private Hashtable< Integer, CFPFContingency > hashContingencies = new Hashtable< Integer, CFPFContingency >(); // LIST CONTINGENCIES
		
	public CFPFPowerSystem() {
	}

	public CFPFPowerSystem( String date, String originatorName, double baseMVA, int year,
			String season, String caseId ) {
		this.date = date;
		this.originatorName = originatorName;
		this.baseMVA = baseMVA;
		this.year = year;
		this.season = season;
		this.caseId = caseId;
	}
	
	public String printCFPFPowerSystem() {
		String ch = ",";
		String out = String.format( "%s", getDate() ) + ch + 
				String.format( "%s", getOriginatorName() )  + ch +  
				String.format( "%.0f", getBaseMVA() ) + ch + 
				String.format( "%d", getYear() ) + ch + 
				String.format( "%s", getSeason() ) + ch + 
				String.format( "%s", getCaseId() );
		return out;
	}	
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getOriginatorName() {
		return originatorName;
	}

	public void setOriginatorName(String originatorName) {
		this.originatorName = originatorName;
	}

	public double getBaseMVA() {
		return baseMVA;
	}
	
	public void setBaseMVA(double baseMVA) {
		this.baseMVA = baseMVA;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public String getSeason() {
		return season;
	}
	
	public void setSeason(String season) {
		this.season = season;
	}
	
	public String getCaseId() {
		return caseId;
	}
	
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
		
	public int getNumBuses() {
		return hashBuses.size();
	}
	
	public int getNumBranches() {
		return hashBranches.size();
	}
	
	public int getNumContingencies() {
		return hashContingencies.size();
	}

	public Hashtable<Integer, CFPFBus> getBuses() {
		return hashBuses;
	}

	public Hashtable<Integer, CFPFBranch> getBranches() {
		return hashBranches;
	}

	public Hashtable<Integer, CFPFContingency> getContingencies() {
		return hashContingencies;
	}
}