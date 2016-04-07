/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

/**
 * This class defines the bus data layout
 * 
 * @author Leonel Carvalho - INESC TEC
 */
public class CFPFBus {
	
	private int number; // BUS NUMBER
	private String name = null; // NAME
	private int loadFlowAreaNumber; // LOAD FLOW AREA NUMBER
	private int type; // BUS TYPE
	// 0 - UNREGULATED (PQ)
    // 1 - GENERATION BUS (PV)
	// 2 - REFERENCE BUS (REF)
	private double desiredVoltage; // FINAL VOLTAGE (p.u.)
	private double desiredAngle; // FINAL ANGLE (degrees)
	private double loadMW; // LOAD MW
	private double loadMVAR; // LOAD MVAR
	private double generationMW; // GENERATION MW
	private double generationMVAR; // GENERATION MVAR
	private double minimumGenerationMW; // MINIMUM MW
	private double maximumGenerationMW; // MAXIMUM MW
	private double minimumGenerationMVAR; // MINIMUM MVAR
	private double maximumGenerationMVAR; // MAXIMUM MVAR
	private double baseKV; // BASE KV
	private double minimumVoltage; // MINIMUM VOLTAGE (p.u.)
	private double maximumVoltage; // MAXIMUM VOLTAGE (p.u.)
	private double shuntConductanceG; // SHUNT CONDUCTANCE G (p.u.)
	private double shuntSusceptanceB; // SHUNT SUSCEPTANCE B (p.u.)
	private CFPFFuzzyNumber fuzzyLoadMW = null; // FUZZY LOAD MW
	private CFPFFuzzyNumber fuzzyLoadMVAR = null; // FUZZY LOAD MVAR
	private CFPFFuzzyNumber fuzzyGenerationMW = null; // FUZZY GENERATION MW
	private CFPFFuzzyNumber fuzzyGenerationMVAR = null; // FUZZY GENERATION MVAR
		
	public CFPFBus() {
	}
	
	public CFPFBus( int number, String name, int loadFlowAreaNumber, int type, double desiredVoltage, double desiredAngle, 
			double loadMW, double loadMVAR, 
			double generationMW, double generationMVAR, 
			double minimumGenerationMW, double maximumGenerationMW,
			double minimumGenerationMVAR, double maximumGenerationMVAR,
			double baseKV, double minimumVoltage, double maximumVoltage, 
			double shuntConductanceG, double shuntSusceptanceB, 
			double minimumFuzzyLoadMW, double maximumFuzzyLoadMW, double minimumFuzzyLoadMVAR, double maximumFuzzyLoadMVAR,
			double minimumFuzzyGenerationMW, double maximumFuzzyGenerationMW, double minimumFuzzyGenerationMVAR, double maximumFuzzyGenerationMVAR ) {
		
		setNumber( number );
		setName( name );
		setLoadFlowAreaNumber( loadFlowAreaNumber );
		setType( type );
		setDesiredVoltage( desiredVoltage );
		setDesiredAngle( desiredAngle );
		setLoadMW( loadMW );
		setLoadMVAR( loadMVAR );
		setGenerationMW( generationMW );
		setGenerationMVAR( generationMVAR );
		setMinimumGenerationMW( minimumGenerationMW );
		setMaximumGenerationMW( maximumGenerationMW );
		setMinimumGenerationMVAR( minimumGenerationMVAR );
		setMaximumGenerationMVAR( maximumGenerationMVAR );
		setBaseKV( baseKV );
		setMinimumVoltage( minimumVoltage );
		setMaximumVoltage( maximumVoltage );
		setShuntConductanceG( shuntConductanceG );
		setShuntSusceptanceB( shuntSusceptanceB );
		
		int numVerticesFuzzyNumber = 3; 
		double[] fuzzyNumberGenerationMW = new double[ numVerticesFuzzyNumber ]; 
		double[] fuzzyNumberGenerationMVAR = new double[ numVerticesFuzzyNumber ]; 
		double[] fuzzyNumberLoadMW = new double[ numVerticesFuzzyNumber ]; 
		double[] fuzzyNumberLoadMVAR = new double[ numVerticesFuzzyNumber ]; 

		if( getType() == 0 ) {
			fuzzyNumberLoadMW[ 0 ] = minimumFuzzyLoadMW;
			fuzzyNumberLoadMW[ 1 ] = loadMW;
			fuzzyNumberLoadMW[ 2 ] = maximumFuzzyLoadMW;
			fuzzyNumberLoadMVAR[ 0 ] = minimumFuzzyLoadMVAR;
			fuzzyNumberLoadMVAR[ 1 ] = loadMVAR;
			fuzzyNumberLoadMVAR[ 2 ] = maximumFuzzyLoadMVAR;
			fuzzyNumberGenerationMW[ 0 ] = minimumFuzzyGenerationMW;
			fuzzyNumberGenerationMW[ 1 ] = generationMW;
			fuzzyNumberGenerationMW[ 2 ] = maximumFuzzyGenerationMW;
			fuzzyNumberGenerationMVAR[ 0 ] = minimumFuzzyGenerationMVAR;
			fuzzyNumberGenerationMVAR[ 1 ] = generationMVAR;
			fuzzyNumberGenerationMVAR[ 2 ] = maximumFuzzyGenerationMVAR;
		} else {
			fuzzyNumberLoadMW[ 0 ] = minimumFuzzyLoadMW;
			fuzzyNumberLoadMW[ 1 ] = loadMW;
			fuzzyNumberLoadMW[ 2 ] = maximumFuzzyLoadMW;
			fuzzyNumberLoadMVAR[ 0 ] = minimumFuzzyLoadMVAR;
			fuzzyNumberLoadMVAR[ 1 ] = loadMVAR;
			fuzzyNumberLoadMVAR[ 2 ] = maximumFuzzyLoadMVAR;	
		}
		
		this.fuzzyLoadMW = new CFPFFuzzyNumber( fuzzyNumberLoadMW );
		this.fuzzyLoadMVAR = new CFPFFuzzyNumber( fuzzyNumberLoadMVAR );
		this.fuzzyGenerationMW = new CFPFFuzzyNumber( fuzzyNumberGenerationMW );
		this.fuzzyGenerationMVAR = new CFPFFuzzyNumber( fuzzyNumberGenerationMVAR );
	}
	
	public String printCFPFBus() {
		String ch  = ",";
		String out = String.format( "%d", getNumber() ) + ch + 
				String.format( "%s", getName() ) + ch +  
				String.format( "%d", getLoadFlowAreaNumber() ) + ch + 
				String.format( "%d", getType() ) + ch + 
				String.format( "%.3f", getDesiredVoltage() + 0.0f ) + ch + 
				String.format( "%.3f", getDesiredAngle() + 0.0f ) + ch + 
				String.format( "%.3f", getLoadMW() + 0.0f ) + ch + 
				String.format( "%.3f", getLoadMVAR() + 0.0f ) + ch + 
				String.format( "%.3f", getGenerationMW() + 0.0f ) + ch + 
				String.format( "%.3f", getGenerationMVAR() + 0.0f ) + ch + 
				String.format( "%.3f", getMinimumGenerationMW() + 0.0f ) + ch + 
				String.format( "%.3f", getMaximumGenerationMW() + 0.0f ) + ch + 
				String.format( "%.3f", getMinimumGenerationMVAR() + 0.0f ) + ch + 
				String.format( "%.3f", getMaximumGenerationMVAR() + 0.0f ) + ch + 
				String.format( "%.2f", getBaseKV() + 0.0f ) + ch + 
				String.format( "%.2f", getMinimumVoltage() + 0.0f ) + ch + 
				String.format( "%.2f", getMaximumVoltage() + 0.0f ) + ch + 
				String.format( "%.2f", getShuntConductanceG() + 0.0f ) + ch + 
				String.format( "%.2f", getShuntSusceptanceB() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyLoadMW().getMinimumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyLoadMW().getMaximumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyLoadMVAR().getMinimumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyLoadMVAR().getMaximumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyGenerationMW().getMinimumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyGenerationMW().getMaximumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyGenerationMVAR().getMinimumValue() + 0.0f ) + ch + 
				String.format( "%.3f", getFuzzyGenerationMVAR().getMaximumValue() + 0.0f );
		return out;
	}
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLoadFlowAreaNumber() {
		return loadFlowAreaNumber;
	}

	public void setLoadFlowAreaNumber(int loadFlowAreaNumber) {
		this.loadFlowAreaNumber = loadFlowAreaNumber;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getDesiredVoltage() {
		return desiredVoltage;
	}

	public void setDesiredVoltage(double desiredVoltage) {
		this.desiredVoltage = desiredVoltage;
	}

	public double getDesiredAngle() {
		return desiredAngle;
	}

	public void setDesiredAngle(double desiredAngle) {
		this.desiredAngle = desiredAngle;
	}

	public double getLoadMW() {
		return loadMW;
	}

	public void setLoadMW(double loadMW) {
		this.loadMW = loadMW;
	}

	public double getLoadMVAR() {
		return loadMVAR;
	}

	public void setLoadMVAR(double loadMVAR) {
		this.loadMVAR = loadMVAR;
	}

	public double getGenerationMW() {
		return generationMW;
	}

	public void setGenerationMW(double generationMW) {
		this.generationMW = generationMW;
	}

	public double getGenerationMVAR() {
		return generationMVAR;
	}

	public void setGenerationMVAR(double generationMVAR) {
		this.generationMVAR = generationMVAR;
	}
	
	public double getMinimumGenerationMW() {
		return minimumGenerationMW;
	}

	public void setMinimumGenerationMW(double minimumGenerationMW) {
		this.minimumGenerationMW = minimumGenerationMW;
	}

	public double getMaximumGenerationMW() {
		return maximumGenerationMW;
	}

	public void setMaximumGenerationMW(double maximumGenerationMW) {
		this.maximumGenerationMW = maximumGenerationMW;
	}

	public double getMinimumGenerationMVAR() {
		return minimumGenerationMVAR;
	}

	public void setMinimumGenerationMVAR(double minimumGenerationMVAR) {
		this.minimumGenerationMVAR = minimumGenerationMVAR;
	}

	public double getMaximumGenerationMVAR() {
		return maximumGenerationMVAR;
	}

	public void setMaximumGenerationMVAR(double maximumGenerationMVAR) {
		this.maximumGenerationMVAR = maximumGenerationMVAR;
	}

	public double getBaseKV() {
		return baseKV;
	}

	public void setBaseKV(double baseKV) {
		this.baseKV = baseKV;
	}

	public double getMinimumVoltage() {
		return minimumVoltage;
	}

	public void setMinimumVoltage(double minimumVoltage) {
		this.minimumVoltage = minimumVoltage;
	}

	public double getMaximumVoltage() {
		return maximumVoltage;
	}

	public void setMaximumVoltage(double maximumVoltage) {
		this.maximumVoltage = maximumVoltage;
	}
	
	public double getShuntConductanceG() {
		return shuntConductanceG;
	}

	public void setShuntConductanceG(double shuntConductanceG) {
		this.shuntConductanceG = shuntConductanceG;
	}

	public double getShuntSusceptanceB() {
		return shuntSusceptanceB;
	}

	public void setShuntSusceptanceB(double shuntSusceptanceB) {
		this.shuntSusceptanceB = shuntSusceptanceB;
	}

	public CFPFFuzzyNumber getFuzzyGenerationMW() {
		return fuzzyGenerationMW;
	}

	public void setFuzzyGenerationMW(CFPFFuzzyNumber fuzzyGenerationMW) {
		this.fuzzyGenerationMW = fuzzyGenerationMW;
	}

	public CFPFFuzzyNumber getFuzzyGenerationMVAR() {
		return fuzzyGenerationMVAR;
	}

	public void setFuzzyGenerationMVAR(CFPFFuzzyNumber fuzzyGenerationMVAR) {
		this.fuzzyGenerationMVAR = fuzzyGenerationMVAR;
	}

	public CFPFFuzzyNumber getFuzzyLoadMW() {
		return fuzzyLoadMW;
	}

	public void setFuzzyLoadMW(CFPFFuzzyNumber fuzzyLoadMW) {
		this.fuzzyLoadMW = fuzzyLoadMW;
	}

	public CFPFFuzzyNumber getFuzzyLoadMVAR() {
		return fuzzyLoadMVAR;
	}

	public void setFuzzyLoadMVAR(CFPFFuzzyNumber fuzzyLoadMVAR) {
		this.fuzzyLoadMVAR = fuzzyLoadMVAR;
	}
}