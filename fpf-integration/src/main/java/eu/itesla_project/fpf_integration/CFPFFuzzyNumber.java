/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

/**
 * This class contains an abstraction of a fuzzy number
 * 
 * @author Leonel Carvalho - INESC TEC
 */
public class CFPFFuzzyNumber {
	
	private int numPoints = 3;
	private double fuzzyNumber[];

	// Empty constructor
	public CFPFFuzzyNumber() {
		this.fuzzyNumber = new double[ numPoints ];
	}

	// Main constructor
	public CFPFFuzzyNumber( double[] fuzzyNumber ) {
		this.fuzzyNumber = fuzzyNumber.clone();
	}
	
	// Visualize triangular Fuzzy number
	public String toString() {
		return super.toString() + "[ " + fuzzyNumber[ 0 ] + ", " + fuzzyNumber[ 1 ] + ", " + fuzzyNumber[ 2 ] + " ]";
	}

	public int getNumPoints() {
		return this.numPoints;	
	}

	public double getFuzzyNumber( int point ){
		return fuzzyNumber[ point ];
	}

	public double[] getFuzzyNumber() {
		return fuzzyNumber;
	}

	public void setFuzzyNumber(double[] fuzzyNumber) {
		this.fuzzyNumber = fuzzyNumber;
	}

	public double getCentralValue() {
		return fuzzyNumber[ 1 ];
	}

	public void setCentralValue( double val ) {
		fuzzyNumber[ 1 ] = val;
	}

	public double getMinimumValue() {
		return fuzzyNumber[ 0 ];
	}

	public void setMinimumValue( double val ) {
		fuzzyNumber[ 0 ] = val;
	}

	public double getMaximumValue() {
		return fuzzyNumber[ numPoints - 1 ];
	}

	public void setMaximumValue( double val ) {
		fuzzyNumber[ numPoints - 1 ] =  val;
	}
}