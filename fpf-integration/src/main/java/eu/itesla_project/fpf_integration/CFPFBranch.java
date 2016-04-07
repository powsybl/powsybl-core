/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

/**
 * This class defines the branch data layout
 * 
 * @author Leonel Carvalho - INESC TEC
 */
public class CFPFBranch {
	
	private int number; // BRANCH NUMBER
	private String name = null; // NAME
	private int tapBusNumber; // TAP BUS NUMBER
	private int zBusNumber; // Z BUS NUMBER
	private int loadFlowArea; // LOAD FLOW AREA
	private int type; // TYPE: 0 - TRANSMISSION LINE ; 1 - FIXED TAP
	private double resistanceR; // BRANCH RESISTANCE R (p.u.)
	private double reactanceX; // BRANCH REACTANCE X (p.u.)
	private double lineChargingG; // LINE CHARGING G (p.u.)
	private double lineChargingB; // LINE CHARGING B (p.u.)
	private double ratingA; // LINE RATING A
	private double ratingB; // LINE RATING B
	private double ratingC; // LINE RATING C
	private double transformerTurnsRatio; // TRANSFORMER FINAL TURNS RATIO
	private double transformerAngle; // TRANSFORMER FINAL ANGLE (degrees)
		
	public CFPFBranch() {
	}
	
	public CFPFBranch( int number, String name, int tapBusNumber, int zBusNumber, int loadFlowArea, int type, double resistanceR, 
			double reactanceX, double lineChargingG, double lineChargingB, double ratingA, double ratingB, double ratingC, 
			double transformerTurnsRatio, double transformerAngle ) {
		setNumber( number );
		setName( name );
		setTapBusNumber( tapBusNumber ); 
		setZBusNumber( zBusNumber ); 
		setLoadFlowArea( loadFlowArea ); 
		setType( type );
		setResistanceR( resistanceR ); 
		setReactanceX( reactanceX ); 
		setLineChargingG( lineChargingG ); 
		setLineChargingB( lineChargingB ); 
		setRatingA( ratingA ); 
		setRatingB( ratingB ); 
		setRatingC( ratingC ); 
		setTransformerTurnsRatio( transformerTurnsRatio ); 
		setTransformerAngle( transformerAngle );
	}
	
	public String printCFPFBranch() {
		String ch = ",";
		String out = String.format( "%d", getNumber() ) + ch + 
				String.format( "%s", getName() ) + ch + 
				String.format( "%d", getTapBusNumber() ) + ch + 
				String.format( "%d", getZBusNumber() ) + ch + 
				String.format( "%d", getLoadFlowArea() ) + ch + 
				String.format( "%d", getType() ) + ch + 
				String.format( "%.8f", getResistanceR() + 0.0f ) + ch + 
				String.format( "%.8f", getReactanceX() + 0.0f ) + ch + 
				String.format( "%.8f", getLineChargingG() + 0.0f ) + ch + 
				String.format( "%.8f", getLineChargingB() + 0.0f ) + ch + 
				String.format( "%.2f", getRatingA() + 0.0f ) + ch + 
				String.format( "%.2f", getRatingB() + 0.0f  ) + ch + 
				String.format( "%.2f", getRatingC() + 0.0f ) + ch + 
				String.format( "%.3f", getTransformerTurnsRatio() + 0.0f ) + ch + 
				String.format( "%.3f", getTransformerAngle() + 0.0f );
		return out;
	}

	public int getTapBusNumber() {
		return tapBusNumber;
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

	public void setTapBusNumber(int tapBusNumber) {
		this.tapBusNumber = tapBusNumber;
	}

	public int getZBusNumber() {
		return zBusNumber;
	}

	public void setZBusNumber(int zBusNumber) {
		this.zBusNumber = zBusNumber;
	}

	public int getLoadFlowArea() {
		return loadFlowArea;
	}

	public void setLoadFlowArea(int loadFlowArea) {
		this.loadFlowArea = loadFlowArea;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public double getResistanceR() {
		return resistanceR;
	}

	public void setResistanceR(double resistanceR) {
		this.resistanceR = resistanceR;
	}

	public double getReactanceX() {
		return reactanceX;
	}

	public void setReactanceX(double reactanceX) {
		this.reactanceX = reactanceX;
	}

	public int getzBusNumber() {
		return zBusNumber;
	}

	public void setzBusNumber(int zBusNumber) {
		this.zBusNumber = zBusNumber;
	}

	public double getLineChargingG() {
		return lineChargingG;
	}

	public void setLineChargingG(double lineChargingG) {
		this.lineChargingG = lineChargingG;
	}

	public double getLineChargingB() {
		return lineChargingB;
	}

	public void setLineChargingB(double lineChargingB) {
		this.lineChargingB = lineChargingB;
	}

	public double getRatingA() {
		return ratingA;
	}

	public void setRatingA(double ratingA) {
		this.ratingA = ratingA;
	}

	public double getRatingB() {
		return ratingB;
	}

	public void setRatingB(double ratingB) {
		this.ratingB = ratingB;
	}

	public double getRatingC() {
		return ratingC;
	}

	public void setRatingC(double ratingC) {
		this.ratingC = ratingC;
	}

	public double getTransformerTurnsRatio() {
		return transformerTurnsRatio;
	}

	public void setTransformerTurnsRatio(double transformerTurnsRatio) {
		this.transformerTurnsRatio = transformerTurnsRatio;
	}

	public double getTransformerAngle() {
		return transformerAngle;
	}

	public void setTransformerAngle(double transformerAngle) {
		this.transformerAngle = transformerAngle;
	}
}