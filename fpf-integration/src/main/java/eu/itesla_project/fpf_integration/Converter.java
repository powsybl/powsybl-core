/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.ThreeWindingsTransformer;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.ContingencyElementType;
import eu.itesla_project.modules.mcla.ForecastErrorsStatistics;
import eu.itesla_project.modules.test.AutomaticContingenciesAndActionsDatabaseClient;

/**
 * This class reads a CIM file and creates a file with the input data for the FPF module
 * 
 * @author Leonel Carvalho - INESC TEC
 */

public class Converter {
	
	// Power System object
	private static CFPFPowerSystem powerSystem = new CFPFPowerSystem();
		
	// Hastable that maps the buses number with the id
	private static Hashtable< Integer, String > numberBusIdMap = new Hashtable< Integer, String >();

	// Hashtable that maps the id of the buses with the corresponding number
	private static Hashtable< String, Integer > idBusNumberMap = new Hashtable< String, Integer >();
	
	// Hastable that maps the branches number with the id
	private static Hashtable< Integer, String > numberBranchIdMap = new Hashtable< Integer, String >();

	// Hashtable that maps the id of the branches with the corresponding number
	private static Hashtable< String, Integer > idBranchNumberMap = new Hashtable< String, Integer >();
	
	// Hashtable that maps the id of the contingency with the corresponding number
	private static Hashtable< String, Integer > idContingencyNumberMap = new Hashtable< String, Integer >();
	
	// Default load flow area
	private static int defaultLoadFlowArea = 1;
	
	// Default power base (MVA)
	private static double defaultBaseMVA = 100;
	
	// Default minimum voltage (p.u.)
	private static double defaultMinimumVoltage = 0.95;
	
	// Default maximum voltage (p.u.)
	private static double defaultMaximumVoltage = 1.15;
	
	// Default reactance (p.u.)
	private static double defaultReactance = 0.001;
	
	// Sting line
	private static String idL = "L_";
	
	// Sting dangling line
	private static String idDL = "DL_";
	
	// Sting two winding transformer
	private static String idT2W = "T2W_";
	
	// Sting three winding transformer
	private static String idT3W = "T3W_";
		
	// Seasons
	private static final String seasons[] = {
		"Winter", "Winter", "Spring", "Spring", "Summer", "Summer", 
		"Summer", "Summer", "Fall", "Fall", "Winter", "Winter"
	};
	
	// Maximum tolerance for the mismatch between load and generation
	private static double tol = 1e-3;
	
	// Path for the CIM input file
	static String inputFilePath = "";
	
	// Path for the FPF input file
	//static String outputFilePath = "";
	
	// Main
	public static void convert(Network n, List< Contingency > contingencies, ForecastErrorsStatistics uncertainties, Path outputFile, Path outputMapFile) {
		
		// ********************************** Initializations ********************************** //
		
		// Sets the locale as US
		Locale.setDefault( Locale.US );
		
		System.out.println("CIM model '" + n.getId() + "' loaded");
		
		// Assigns a number to the buses
		int busNumber = 1;
		for( Bus b : n.getBusView().getBuses() ) {
			String str = b.getId();
			numberBusIdMap.put( busNumber, str );
			idBusNumberMap.put( str, busNumber );
			busNumber++;
		}
		
		// Assigns a number to the extra bus for the load of the dangling lines
		for( DanglingLine dl : n.getDanglingLines() ) {
			if( dl.getTerminal().getBusView().getBus() != null ) {
				String str = idDL + dl.getId();
				numberBusIdMap.put( busNumber, str );
				idBusNumberMap.put( str, busNumber );
				busNumber++;
			}
		}
		
		// Assigns a number to the extra bus for the common coupling point of the three winding transformers
		for( ThreeWindingsTransformer t : n.getThreeWindingsTransformers() ) {
			if( t.getLeg1().getTerminal().getBusView().getBus() != null && 
					t.getLeg2().getTerminal().getBusView().getBus() != null && 
					t.getLeg3().getTerminal().getBusView().getBus() != null ) {
				String str = idT3W + t.getId();
				numberBusIdMap.put( busNumber, str );
				idBusNumberMap.put( str, busNumber );
				busNumber++;
			}
		}
		
		// Assigns a number to the branch corresponding to a dangling line
		int branchNumber = 1;
		for( DanglingLine dl : n.getDanglingLines() ) {
			if( dl.getTerminal().getBusView().getBus() != null ) {
				String str = idDL + dl.getId();
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				branchNumber++;
			}
		}
		
		// Assigns a number to the branch corresponding to a transmission line
		for( Line l : n.getLines() ) {
			if( l.getTerminal1().getBusView().getBus() != null && 
					l.getTerminal2().getBusView().getBus() != null ) {
				String str = idL + l.getId();
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				branchNumber++;
			}
		}
		
		// Assigns a number to the branch corresponding to a two winding transformer
		for( TwoWindingsTransformer t : n.getTwoWindingsTransformers() ) {
			if( t.getTerminal1().getBusView().getBus() != null && 
					t.getTerminal2().getBusView().getBus() != null ) {				
				String str = idT2W + t.getId();
				// Update maps between branch numbering and id
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				branchNumber++;
			}
		}
		
		// Assigns a number to the branch corresponding to a three winding transformer
		for( ThreeWindingsTransformer t : n.getThreeWindingsTransformers() ) {
			if( t.getLeg1().getTerminal().getBusView().getBus() != null && 
					t.getLeg2().getTerminal().getBusView().getBus() != null && 
					t.getLeg3().getTerminal().getBusView().getBus() != null ) {
				String str = idT3W + t.getId() + "_1";
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				branchNumber++;
				str = idT3W + t.getId() + "_2";
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				str = idT3W + t.getId() + "_3";
				branchNumber++;
				numberBranchIdMap.put( branchNumber, str );
				idBranchNumberMap.put( str, branchNumber );
				branchNumber++;
			}
		}
		
		// ********************************** Data Reading ********************************** //
				
		// Reads basic power system data
		String date = String.format( "%02d", n.getCaseDate().dayOfMonth().get() ) + "/"
				+ String.format( "%02d", n.getCaseDate().getMonthOfYear() ) + "/"
				+ String.format( "%02d", n.getCaseDate().getYearOfCentury() );
		String originatorName = n.getSourceFormat();
		double baseMVA = defaultBaseMVA;
		int year = n.getCaseDate().getYear();
		String season = getSeason( n.getCaseDate().getMonthOfYear() );
		String caseId = n.getId();
		powerSystem = new CFPFPowerSystem( date, originatorName, baseMVA, year, season, caseId );
		
//		// Reads uncertainty data -> For now, the data is read from an external file
//		TimeHorizon timeHorizon = TimeHorizon.DACF;
//		String analysisId = "France_flagPQ1_ir0_80";
//		Path storageDir = Paths.get("C://Users//Leonel Carvalho//Documents//INESC TEC//Projects//iTESLA//FPF//Integration//Data//ForecastErrors");
//		ForecastErrorsDataStorage feDataStorage = new ForecastErrorsDataStorageImpl( new ForecastErrorsDataStorageConfig( storageDir ) );
//		ForecastErrorsStatistics uncertainties = null;
//				
//		// check if the uncertainties are available
//        if( feDataStorage.areStatisticsAvailable( analysisId, timeHorizon ) ) {
////            // get uncertainties file
////            Path uncertaintiesFile = Paths.get("/itesla_data/uncertainties.csv");
////            try {
////                feDataStorage.getStatisticsFile(analysisId, timeHorizon, uncertaintiesFile);
////            } catch (IOException e) {
////                System.out.println("Error in getting uncertainties file for " + analysisId + " analysis and " + timeHorizon + " time horizon: " + e.getMessage());
////                e.printStackTrace();
////            }
//            // get uncertainties
//        	try {
//        		uncertainties = feDataStorage.getStatistics( analysisId, timeHorizon );
//        		for ( int i = 0; i < uncertainties.getInjectionsIds().length; i++ ) {
//        			System.out.println("injections id = " + uncertainties.getInjectionsIds()[i]
//        					+ ", mean = " + uncertainties.getMeans()[i]
//        							+ ", standard deviation = " + uncertainties.getStandardDeviations()[i] );
//        		}
//        	} catch (IOException e) {
//        		System.out.println("Error in getting uncertainties for " + analysisId + " analysis and " + timeHorizon + " time horizon: " + e.getMessage());
//        		e.printStackTrace();
//        	}
//        } else {
//            System.out.println("No uncertainties avalaibale for " + analysisId + " analysis and " + timeHorizon + " time horizon") ;
//        }
		if (uncertainties!=null) {
			System.out.println("Uncertainties:");
			for (int i = 0; i < uncertainties.getInjectionsIds().length; i++) {
				System.out.println("injections id = " + uncertainties.getInjectionsIds()[i]
						+ ", mean = " + uncertainties.getMeans()[i]
						+ ", standard deviation = " + uncertainties.getStandardDeviations()[i]);
			}
		}




			// Reads bus data
		double accL = 0.0; // load accumulator
		double accNDG = 0.0; // non-dispatchable generation accumulator
		double accDG = 0.0; // dispatchable generation accumulator
		double minDG = 0.0; // minimum dispatchable generation capacity
		double maxDG = 0.0; // maximum dispatchable generation capacity
		
		double sumStd = 0.0;
		
		for( Bus b : n.getBusView().getBuses() ) {
			double zBase = Math.pow( b.getVoltageLevel().getNominalV(), 2 ) / baseMVA;
			double baseKV = b.getVoltageLevel().getNominalV(); 
			String nameBus = b.getId();
			int numberBus = idBusNumberMap.get( nameBus );
			int loadFlowAreaNumber = defaultLoadFlowArea;
			int typeBus = 0; // Default -> PQ bus
			double minimumVoltage = b.getVoltageLevel().getLowVoltageLimit() / baseKV; 
			double maximumVoltage = b.getVoltageLevel().getHighVoltageLimit() / baseKV;
			if( Double.isNaN( minimumVoltage ) )
				minimumVoltage = defaultMinimumVoltage;
			if( Double.isNaN( maximumVoltage ) )
				maximumVoltage = defaultMaximumVoltage;
			double desiredVoltage = 1.0;
			if( !Double.isNaN( b.getV() ) ) {
				desiredVoltage = b.getV() / baseKV;
			}
			double desiredAngle = 0.0;
			if( !Double.isNaN( b.getAngle() ) ) {
				desiredAngle = b.getAngle();
			}
			double generationMW = 0.0;
			double generationMVAR = 0.0;
			double minimumGenerationMW = 0.0;
			double maximumGenerationMW = 0.0;
			double minimumGenerationMVAR = 0.0;
			double maximumGenerationMVAR = 0.0;
			// Update load
			double loadMW = 0.0;
			double loadMVAR = 0.0; 
			double minimumFuzzyLoadMW = 0.0; 
			double maximumFuzzyLoadMW = 0.0;
			double minimumFuzzyLoadMVAR = 0.0;
			double maximumFuzzyLoadMVAR = 0.0;
			Iterator< Load > itrL = b.getLoads().iterator();
			while( itrL.hasNext() ) {
				Load load = itrL.next();
				System.out.println(" Load id: " + load.getId());
				loadMW += load.getP0();
				loadMVAR += load.getQ0();
				if (uncertainties!=null) {
					int idMW = indexArray( uncertainties.getInjectionsIds(), load.getId() + "_P" );
					int idMVAR = indexArray( uncertainties.getInjectionsIds(), load.getId() + "_Q" );
					System.out.println("  "+load.getId() + "_P: "  + idMW + ", "+ load.getId() + "_Q: "  + idMVAR);
					if( idMW != -1 && idMVAR != -1 ) {
						loadMW += uncertainties.getMeans()[ idMW ];
						loadMVAR += uncertainties.getMeans()[ idMVAR ];
						sumStd += uncertainties.getStandardDeviations()[ idMW ];
						minimumFuzzyLoadMW -= uncertainties.getStandardDeviations()[ idMW ];
						maximumFuzzyLoadMW += uncertainties.getStandardDeviations()[ idMW ];
						minimumFuzzyLoadMVAR -= uncertainties.getStandardDeviations()[ idMVAR ];
						maximumFuzzyLoadMVAR += uncertainties.getStandardDeviations()[ idMVAR ];
					}
				}
			}
			
			minimumFuzzyLoadMW -= 2 * loadMW *( 0.01 / 0.67449 ); 
			maximumFuzzyLoadMW += 2 * loadMW *( 0.03 / 0.67449 );
			if( loadMVAR > 0.0 ) {
				minimumFuzzyLoadMVAR -= 2 * loadMVAR *( 0.01 / 0.67449 );  
				maximumFuzzyLoadMVAR += 2 * loadMVAR *( 0.01 / 0.67449 ); 
			} else {
				minimumFuzzyLoadMVAR += 2 * loadMVAR *( 0.01 / 0.67449 );  
				maximumFuzzyLoadMVAR -= 2 * loadMVAR *( 0.01 / 0.67449 ); 
			}
			
			// Correct negative load MW, i.e. injected power into the network
			if( loadMW < 0.0 ) {
				generationMW += -loadMW;
				maximumGenerationMW += -loadMW;
				loadMW = 0.0;
				generationMVAR += -loadMVAR;
				maximumGenerationMVAR += -loadMVAR;
				loadMVAR = 0.0;
				minimumFuzzyLoadMW = loadMW; 
				maximumFuzzyLoadMW = loadMW;
				minimumFuzzyLoadMVAR = loadMVAR;
				maximumFuzzyLoadMVAR = loadMVAR;
			} else {
				minimumFuzzyLoadMW = loadMW + minimumFuzzyLoadMW; 
				maximumFuzzyLoadMW = loadMW + maximumFuzzyLoadMW; 
				minimumFuzzyLoadMVAR = loadMVAR + minimumFuzzyLoadMVAR; 
				maximumFuzzyLoadMVAR = loadMVAR + maximumFuzzyLoadMVAR;
				if( minimumFuzzyLoadMW < 0 ) {
					minimumFuzzyLoadMW = 0;
				}
			}
			// Update generation
			Iterator< Generator > itrG = b.getGenerators().iterator();
			while( itrG.hasNext() ) {
				Generator gen = itrG.next();
				if( gen.getTargetP() > 0.0 ) {
					generationMW += gen.getTargetP();
				} else {
					loadMW += gen.getTargetP();
				}
				generationMVAR += gen.getTargetQ();
				if( gen.getMinP() > 0.0 )
					minimumGenerationMW += gen.getMinP();
				if( gen.getMaxP() > 0.0 )
					maximumGenerationMW += gen.getMaxP();
				minimumGenerationMVAR += gen.getReactiveLimits().getMinQ((float)0);
				maximumGenerationMVAR += gen.getReactiveLimits().getMaxQ((float)0);
				if( gen.isVoltageRegulatorOn() ) {
					desiredVoltage = gen.getTargetV() / baseKV;
					typeBus = 1; // PV bus
					generationMVAR = 0.0;
				}
			}
			// Correct generation limits
			if( generationMW > maximumGenerationMW )
				maximumGenerationMW = generationMW;
			if( generationMW < minimumGenerationMW )
				minimumGenerationMW = generationMW;
			if( generationMVAR > maximumGenerationMVAR )
				maximumGenerationMVAR = generationMVAR;
			if( generationMVAR < minimumGenerationMVAR )
				minimumGenerationMVAR = generationMVAR;
			// Update generation of the PQ bus
			if( typeBus == 0 ) {
				minimumGenerationMW = 0;
				maximumGenerationMW = 0;
				minimumGenerationMVAR = 0;
				maximumGenerationMVAR = 0;
			}
			// Update load MW and generation MW accumulators
			accL += loadMW;
			if( typeBus == 0 ) {
				accNDG += generationMW; 
			} else {
				accDG += generationMW;
				minDG += minimumGenerationMW;
				maxDG += maximumGenerationMW; 	
			}
			// Update shunts
			double shuntConductanceG = 0.0; 
			double shuntSusceptanceB = 0.0; 
			Iterator< ShuntCompensator > itrS = b.getShunts().iterator();
			while( itrS.hasNext() ) {
				ShuntCompensator shunt = itrS.next();
				shuntSusceptanceB += shunt.getCurrentB() * zBase; 
			}
			/* *************
			 * 
			 * Fuzzy Data -> Ask RSE
			 * 
			 * *************/
//			double minimumFuzzyLoadMW = loadMW; 
//			double maximumFuzzyLoadMW = loadMW;
//			double minimumFuzzyLoadMVAR = loadMVAR;
//			double maximumFuzzyLoadMVAR = loadMVAR;
			double minimumFuzzyGenerationMW = 0.0;
			double maximumFuzzyGenerationMW = 0.0;
			double minimumFuzzyGenerationMVAR = 0.0;
			double maximumFuzzyGenerationMVAR = 0.0;
			if( typeBus == 0 ) {
				minimumFuzzyGenerationMW = generationMW;
				maximumFuzzyGenerationMW = generationMW;
				minimumFuzzyGenerationMVAR = generationMVAR;
				maximumFuzzyGenerationMVAR = generationMVAR;	
			}
			/* *************
			 * 
			 * Fuzzy Data -> Ask RSE
			 * 
			 * *************/
			CFPFBus tmpBus = new CFPFBus( numberBus, nameBus, loadFlowAreaNumber, typeBus, desiredVoltage, desiredAngle, 
					loadMW, loadMVAR, 
					generationMW, generationMVAR, 
					minimumGenerationMW, maximumGenerationMW,
					minimumGenerationMVAR, maximumGenerationMVAR,
					baseKV, minimumVoltage, maximumVoltage, 
					shuntConductanceG, shuntSusceptanceB, 
					minimumFuzzyLoadMW, maximumFuzzyLoadMW, minimumFuzzyLoadMVAR, maximumFuzzyLoadMVAR,
					minimumFuzzyGenerationMW, maximumFuzzyGenerationMW, minimumFuzzyGenerationMVAR, maximumFuzzyGenerationMVAR ); 
			powerSystem.getBuses().put( numberBus, tmpBus );	
		}
		
		
		// Reads dangling lines data
		for( DanglingLine dl : n.getDanglingLines() ) {
			if( dl.getTerminal().getBusView().getBus() != null ) {
				/*
				 * First: add a bus at the end of the dangling line with the corresponding load
				 */
				double zBase = Math.pow( dl.getTerminal().getBusView().getBus().getVoltageLevel().getNominalV(), 2 ) / baseMVA;
				double baseKV = dl.getTerminal().getBusView().getBus().getVoltageLevel().getNominalV(); 
				String nameBus = idDL + dl.getId();
				int numberBus = idBusNumberMap.get( nameBus );
				int loadFlowAreaNumber = defaultLoadFlowArea;
				int typeBus = 0; // Default -> PQ bus
				double minimumVoltage = dl.getTerminal().getBusView().getBus().getVoltageLevel().getLowVoltageLimit() / baseKV; 
				double maximumVoltage = dl.getTerminal().getBusView().getBus().getVoltageLevel().getHighVoltageLimit() / baseKV;
				if( Double.isNaN( minimumVoltage ) )
					minimumVoltage = defaultMinimumVoltage;
				if( Double.isNaN( maximumVoltage ) )
					maximumVoltage = defaultMaximumVoltage;
				double desiredVoltage = dl.getTerminal().getBusView().getBus().getV() / baseKV;
				double desiredAngle = dl.getTerminal().getBusView().getBus().getAngle();
				double generationMW = 0.0;
				double generationMVAR = 0.0;
				double minimumGenerationMW = 0.0;
				double maximumGenerationMW = 0.0;
				double minimumGenerationMVAR = 0.0;
				double maximumGenerationMVAR = 0.0;
				double shuntConductanceG = 0.0; 
				double shuntSusceptanceB = 0.0; 
				// Update load
				double loadMW = dl.getP0();
				double loadMVAR = dl.getQ0();
				// Correct negative load MW, i.e. injected power into the network
				if( loadMW < 0.0 ) { 
					generationMW += -loadMW;
					maximumGenerationMW += -loadMW;
					loadMW = 0.0;
				}
				// Update load MW and generation MW accumulators
				accL += loadMW;
				accNDG += generationMW; 
				/* *************
				 * 
				 * Ask RSE 
				 * 
				 * *************/		
				double minimumFuzzyLoadMW = loadMW; 
				double maximumFuzzyLoadMW = loadMW;
				double minimumFuzzyLoadMVAR = loadMVAR;
				double maximumFuzzyLoadMVAR = loadMVAR;
				double minimumFuzzyGenerationMW = generationMW;
				double maximumFuzzyGenerationMW = generationMW;
				double minimumFuzzyGenerationMVAR = generationMVAR;
				double maximumFuzzyGenerationMVAR = generationMVAR;
				/* *************
				 * 
				 * Ask RSE 
				 * 
				 * *************/
				CFPFBus tmpBus = new CFPFBus( numberBus, nameBus, loadFlowAreaNumber, typeBus, desiredVoltage, desiredAngle, 
						loadMW, loadMVAR, 
						generationMW, generationMVAR, 
						minimumGenerationMW, maximumGenerationMW,
						minimumGenerationMVAR, maximumGenerationMVAR,
						baseKV, minimumVoltage, maximumVoltage, 
						shuntConductanceG, shuntSusceptanceB, 
						minimumFuzzyLoadMW, maximumFuzzyLoadMW, minimumFuzzyLoadMVAR, maximumFuzzyLoadMVAR,
						minimumFuzzyGenerationMW, maximumFuzzyGenerationMW, minimumFuzzyGenerationMVAR, maximumFuzzyGenerationMVAR );
				powerSystem.getBuses().put( numberBus, tmpBus );
				/* 
				 * Second: add line to represent the dangling line
				 */
				String nameBranch = idDL + dl.getId();
				int numberBranch = idBranchNumberMap.get( nameBranch );
				int tapBusNumber = idBusNumberMap.get( dl.getTerminal().getBusView().getBus().getId() );
				int zBusNumber = idBusNumberMap.get( idDL + dl.getId() );
				int loadFlowArea = defaultLoadFlowArea;
				int typeBranch = 0;
				double resistanceR = dl.getR() / zBase;
				double reactanceX = dl.getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}	
				double lineChargingG = dl.getG() * zBase;
				double lineChargingB = dl.getB() * zBase;
				// Current is in A and Voltage is in KV
				double ratingA = dl.getCurrentLimits().getPermanentLimit() * dl.getTerminal().getBusView().getBus().getVoltageLevel().getNominalV() / 1e3;
				double ratingB = ratingA;
				double ratingC = ratingA;
				double transformerTurnsRatio = 1.0;
				double transformerAngle = 0.0;
				CFPFBranch branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, 
						zBusNumber, loadFlowArea, typeBranch, 
						resistanceR, reactanceX, 0.0, 0.0, ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );				
				/* 
				 * Third: add the shunt admittance at the beginning of the dangling line
				 */
				tmpBus = powerSystem.getBuses().get( tapBusNumber );
				tmpBus.setShuntConductanceG( tmpBus.getShuntConductanceG() + lineChargingG );
				tmpBus.setShuntSusceptanceB( tmpBus.getShuntSusceptanceB() + lineChargingB );
			}
		}	
		
		// Reads transmission lines data
		for( Line l : n.getLines() ) {
			if( l.getTerminal1().getBusView().getBus() != null && 
					l.getTerminal2().getBusView().getBus() != null ) {				
				double zBase = Math.pow( l.getTerminal1().getBusView().getBus().getVoltageLevel().getNominalV(), 2 ) / baseMVA;
				String nameBranch = idL + l.getId();
				int numberBranch = idBranchNumberMap.get( nameBranch );
				int tapBusNumber = idBusNumberMap.get( l.getTerminal1().getBusView().getBus().getId() );
				int zBusNumber = idBusNumberMap.get( l.getTerminal2().getBusView().getBus().getId() );
				int loadFlowArea = defaultLoadFlowArea;
				int typeBranch = 0;
				double resistanceR = l.getR() / zBase;
				double reactanceX = l.getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}	
				double lineChargingG = ( l.getG1() + l.getG2() ) * zBase;
				double lineChargingB = ( l.getB1() + l.getB2() ) * zBase;
				// Current is in A and Voltage is in KV
				double ratingA = Math.min( ( l.getCurrentLimits1().getPermanentLimit() * l.getTerminal1().getBusView().getBus().getVoltageLevel().getNominalV() ) / 1e3, 
						( l.getCurrentLimits2().getPermanentLimit() * l.getTerminal2().getBusView().getBus().getVoltageLevel().getNominalV() ) / 1e3 );
				double ratingB = ratingA;
				double ratingC = ratingA;
				double transformerTurnsRatio = 1.0;
				double transformerAngle = 0.0;
				CFPFBranch branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, zBusNumber, 
						loadFlowArea, typeBranch, resistanceR, reactanceX, lineChargingG, lineChargingB, 
						ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );				
			}			
		}
		
		// Reads two-windings transformer data
		for( TwoWindingsTransformer t : n.getTwoWindingsTransformers() ) {
			if( t.getTerminal1().getBusView().getBus() != null && 
					t.getTerminal2().getBusView().getBus() != null ) {
				String nameBranch = idT2W + t.getId();

				int numberBranch = idBranchNumberMap.get( nameBranch );
				double zBase = Math.pow( t.getTerminal2().getBusView().getBus().getVoltageLevel().getNominalV(), 2 ) / baseMVA;
				int tapBusNumber = idBusNumberMap.get( t.getTerminal1().getBusView().getBus().getId() );
				int zBusNumber = idBusNumberMap.get( t.getTerminal2().getBusView().getBus().getId() );
				int loadFlowArea = defaultLoadFlowArea;
				int typeBranch = 1;
				double resistanceR = t.getR() / zBase; 
				double reactanceX = t.getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}			
				double lineChargingG = t.getG() * zBase;
				double lineChargingB = t.getB() * zBase;
				// Current is in A and Voltage is in KV

				if (( t.getCurrentLimits1() == null) || ( t.getCurrentLimits2() == null)) {
					System.out.println(" TwoWindingsTransformer: " +  t.getId());
					System.out.println("   - getCurrentLimits1(): " + t.getCurrentLimits1() );
					System.out.println("   - getCurrentLimits2(): " + t.getCurrentLimits2() );
				}
				// TODO : if either currentLimits1() or currentLimits2() or both are null, the execution stops here !!
				double ratingA = Math.min( t.getCurrentLimits1().getPermanentLimit() * t.getRatedU1() / 1e3 , t.getCurrentLimits2().getPermanentLimit() * t.getRatedU2() / 1e3 );
				double ratingB = ratingA;
				double ratingC = ratingA;
				double transformerTurnsRatio = 1.0;
				double transformerAngle = 0.0;
				if( t.getRatioTapChanger() != null ) {
					transformerTurnsRatio = t.getRatioTapChanger().getCurrentStep().getRho();
				} 
				if( t.getPhaseTapChanger() != null ) {
					// Alpha is in degrees
					transformerTurnsRatio = t.getPhaseTapChanger().getCurrentStep().getRho();
					transformerAngle = t.getPhaseTapChanger().getCurrentStep().getAlpha();	 
				}
				CFPFBranch branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, zBusNumber, 
						loadFlowArea, typeBranch, resistanceR, reactanceX, lineChargingG, lineChargingB, 
						ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );				
			}
		}	
		
		// Reads three-windings transformer data
		for( ThreeWindingsTransformer t : n.getThreeWindingsTransformers() ) {
			if( t.getLeg1().getTerminal().getBusView().getBus() != null && 
					t.getLeg2().getTerminal().getBusView().getBus() != null && 
					t.getLeg3().getTerminal().getBusView().getBus() != null ) {
				/* 
				 * First: add a bus at the common coupling point
				 */
				double zBase = Math.pow( t.getLeg1().getTerminal().getBusView().getBus().getVoltageLevel().getNominalV(), 2 ) / baseMVA;
				double baseKV = t.getLeg1().getTerminal().getBusView().getBus().getVoltageLevel().getNominalV(); 
				String nameBus = idT3W + t.getId();
				int numberBus = idBusNumberMap.get( nameBus );
				int loadFlowAreaNumber = defaultLoadFlowArea;
				int typeBus = 0; // Default -> PQ bus
				double minimumVoltage = t.getLeg1().getTerminal().getBusView().getBus().getVoltageLevel().getLowVoltageLimit() / baseKV; 
				double maximumVoltage = t.getLeg1().getTerminal().getBusView().getBus().getVoltageLevel().getHighVoltageLimit() / baseKV;
				if( Double.isNaN( minimumVoltage ) )
					minimumVoltage = defaultMinimumVoltage;
				if( Double.isNaN( maximumVoltage ) )
					maximumVoltage = defaultMaximumVoltage;
				double desiredVoltage = t.getLeg1().getTerminal().getBusView().getBus().getV() / baseKV;
				double desiredAngle = t.getLeg1().getTerminal().getBusView().getBus().getAngle();
				double generationMW = 0.0;
				double generationMVAR = 0.0;
				double minimumGenerationMW = 0.0;
				double maximumGenerationMW = 0.0;
				double minimumGenerationMVAR = 0.0;
				double maximumGenerationMVAR = 0.0;
				double shuntConductanceG = t.getLeg1().getG() * zBase; 
				double shuntSusceptanceB = t.getLeg1().getB() * zBase; 
				double loadMW = 0.0;
				double loadMVAR = 0.0;	
				double minimumFuzzyLoadMW = 0.0; 
				double maximumFuzzyLoadMW = 0.0; 
				double minimumFuzzyLoadMVAR = 0.0;
				double maximumFuzzyLoadMVAR = 0.0; 
				double minimumFuzzyGenerationMW = 0.0; 
				double maximumFuzzyGenerationMW = 0.0; 
				double minimumFuzzyGenerationMVAR = 0.0; 
				double maximumFuzzyGenerationMVAR = 0.0;
				CFPFBus tmpBus = new CFPFBus( numberBus, nameBus, loadFlowAreaNumber, typeBus, desiredVoltage, desiredAngle, 
						loadMW, loadMVAR, 
						generationMW, generationMVAR, 
						minimumGenerationMW, maximumGenerationMW,
						minimumGenerationMVAR, maximumGenerationMVAR,
						baseKV, minimumVoltage, maximumVoltage, 
						shuntConductanceG, shuntSusceptanceB, 
						minimumFuzzyLoadMW, maximumFuzzyLoadMW, minimumFuzzyLoadMVAR, maximumFuzzyLoadMVAR,
						minimumFuzzyGenerationMW, maximumFuzzyGenerationMW, minimumFuzzyGenerationMVAR, maximumFuzzyGenerationMVAR );
				powerSystem.getBuses().put( numberBus, tmpBus );
				/*
				 * Second: insert the first branch
				 */
				String nameBranch = idT3W + t.getId() + "_1";
				int numberBranch = idBranchNumberMap.get( nameBranch );
				int tapBusNumber = idBusNumberMap.get( t.getLeg1().getTerminal().getBusView().getBus().getId() );
				int zBusNumber = idBusNumberMap.get( nameBus );
				int loadFlowArea = defaultLoadFlowArea;
				int typeBranch = 0; // transmission line
				double resistanceR = t.getLeg1().getR() / zBase; 
				double reactanceX = t.getLeg1().getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}			
				double lineChargingG = 0;
				double lineChargingB = 0;
				// Current is in A and Voltage is in KV
				double ratingA = t.getLeg1().getCurrentLimits().getPermanentLimit() * t.getLeg1().getRatedU() / 1e3;
				double ratingB = ratingA; 
				double ratingC = ratingA;
				double transformerTurnsRatio = 1.0;
				double transformerAngle = 0.0;
				CFPFBranch branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, zBusNumber, 
						loadFlowArea, typeBranch, resistanceR, reactanceX, lineChargingG, lineChargingB, 
						ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );				
				/*  
				 * Third: insert the second branch
				 */
				nameBranch = idT3W + t.getId() + "_2";
				numberBranch = idBranchNumberMap.get( nameBranch );
				tapBusNumber = idBusNumberMap.get( nameBus );
				zBusNumber = idBusNumberMap.get( t.getLeg2().getTerminal().getBusView().getBus().getId() );
				loadFlowArea = defaultLoadFlowArea;
				typeBranch = 1; // transformer
				resistanceR = t.getLeg2().getR() / zBase; 
				reactanceX = t.getLeg2().getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}			
				lineChargingG = 0;
				lineChargingB = 0;
				// Current is in A and Voltage is in KV
				ratingA = t.getLeg2().getCurrentLimits().getPermanentLimit() * t.getLeg2().getRatedU() / 1e3;
				ratingB = ratingA; 
				ratingC = ratingA;
				transformerTurnsRatio = 1.0 / t.getLeg2().getRatioTapChanger().getCurrentStep().getRho();
				transformerAngle = 0.0;
				branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, zBusNumber, 
						loadFlowArea, typeBranch, resistanceR, reactanceX, lineChargingG, lineChargingB, 
						ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );				
				/*
				 * Fourth: insert the third branch
				 */
				nameBranch = idT3W + t.getId() + "_3";
				numberBranch = idBranchNumberMap.get( nameBranch );
				tapBusNumber = idBusNumberMap.get( nameBus );
				zBusNumber = idBusNumberMap.get( t.getLeg3().getTerminal().getBusView().getBus().getId() );
				loadFlowArea = defaultLoadFlowArea;
				typeBranch = 1; // transformer
				resistanceR = t.getLeg3().getR() / zBase; 
				reactanceX = t.getLeg3().getX() / zBase;
				if( Math.abs( reactanceX ) < defaultReactance ) {
					if( reactanceX > 0.0 ) {
						reactanceX = defaultReactance;
					} else {
						reactanceX = -defaultReactance;
					}
				}			
				lineChargingG = 0;
				lineChargingB = 0;
				// Current is in A and Voltage is in KV
				ratingA = t.getLeg3().getCurrentLimits().getPermanentLimit() * t.getLeg3().getRatedU() / 1e3;
				ratingB = ratingA; 
				ratingC = ratingA;
				transformerTurnsRatio = 1.0 / t.getLeg3().getRatioTapChanger().getCurrentStep().getRho();
				transformerAngle = 0.0;
				branch = new CFPFBranch( numberBranch, nameBranch, tapBusNumber, zBusNumber, 
						loadFlowArea, typeBranch, resistanceR, reactanceX, lineChargingG, lineChargingB, 
						ratingA, ratingB, ratingC, 
						transformerTurnsRatio, transformerAngle );
				powerSystem.getBranches().put( numberBranch, branch );
			}
		}	

		// Reads contingency data
		// Can only have branch contingencies due to the limitations of the IEEE Common Data Format
		int numContingencies = 0;
/*
		ContingenciesAndActionsDatabaseClient cadbClient = new AutomaticContingenciesAndActionsDatabaseClient( 5 ); // 5 contingencies
		List< Contingency > contingencies = cadbClient.getContingencies( n );
*/
		for( Contingency contingency : contingencies ) {
			// System.out.println( "contigency id = " + contingency.getId() );
			ArrayList< Integer > idBranch = new ArrayList< Integer >();
			for( ContingencyElement contingencyElement : contingency.getElements() ) {
				if( contingencyElement.getType() == ContingencyElementType.LINE ) {
					// System.out.println( "equipment id = " + contingencyElement.getId() + " - type = " + contingencyElement.getType() );
					String tmpIdL = idL + contingencyElement.getId();
					String tmpIdT2W = idT2W + contingencyElement.getId();
					String tmpIdT3W1 = idT3W + contingencyElement.getId() + "_1";
					String tmpIdT3W2 = idT3W + contingencyElement.getId() + "_2";
					String tmpIdT3W3 = idT3W + contingencyElement.getId() + "_3";
					String tmpIdDL = idDL + contingencyElement.getId();
					int disconnect = 0;
					if( idBranchNumberMap.get( tmpIdL ) != null ) {
						disconnect = 1;
					} else if( idBranchNumberMap.get( tmpIdT2W ) != null ) {
						disconnect = 2;    			
					} else if( idBranchNumberMap.get( tmpIdT3W1 ) != null ) {
						disconnect = 3; 
					} else if( idBranchNumberMap.get( tmpIdDL ) != null ) {
						disconnect = 4; 
					} 
					switch( disconnect ) {
					case 1:
						idBranch.add( idBranchNumberMap.get( tmpIdL ) );
						break;
					case 2:
						idBranch.add( idBranchNumberMap.get( tmpIdT2W ) );
						break;
					case 3:
						idBranch.add( idBranchNumberMap.get( tmpIdT3W1 ) );
						idBranch.add( idBranchNumberMap.get( tmpIdT3W2 ) );
						idBranch.add( idBranchNumberMap.get( tmpIdT3W3 ) );
						break;
					case 4:
						idBranch.add( idBranchNumberMap.get( tmpIdL ) );
						break;
					default:
						// System.out.println( "Contingency element not found." );
						break;
					}
				} else {
					// System.out.println( "Cannot define generators contingencies." );
				}
			}
			if( idBranch.size() > 0 ) {
				numContingencies++;
				CFPFContingency cFPFcont = new CFPFContingency( numContingencies, idBranch );
				powerSystem.getContingencies().put( numContingencies, cFPFcont );
				idContingencyNumberMap.put( contingency.getId(), numContingencies );
			}
		}
		
		System.out.println( accL + "\t" +  ( accDG + accNDG ) );

		// ********************************** Data Handling ********************************** //

		// Re-dispatch generators to meet load
		double maxExcDG = maxDG - accDG;
		double minExcDG = accDG - minDG;
		double excL = accL - ( accDG + accNDG );
		double accG = 0.0;
		if( excL > 0 ) {
			if( Math.abs( excL ) > tol && Math.abs( excL ) < maxExcDG ) {
				Enumeration< Integer > busesKeys = powerSystem.getBuses().keys();
				while( busesKeys.hasMoreElements() ) {
					CFPFBus tmpBus = powerSystem.getBuses().get( busesKeys.nextElement() );
					if( tmpBus.getType() != 0 ) {
						tmpBus.setGenerationMW( tmpBus.getGenerationMW() + ( ( tmpBus.getMaximumGenerationMW() - tmpBus.getGenerationMW() ) / maxExcDG ) * excL );
					}
					accG += tmpBus.getGenerationMW();
				}
			} 
		} else {
			if( Math.abs( excL ) > tol && Math.abs( excL ) < minExcDG ) {
				Enumeration< Integer > busesKeys = powerSystem.getBuses().keys();
				while( busesKeys.hasMoreElements() ) {
					CFPFBus tmpBus = powerSystem.getBuses().get( busesKeys.nextElement() );
					if( tmpBus.getType() != 0 ) {
						tmpBus.setGenerationMW( tmpBus.getGenerationMW() + ( ( tmpBus.getGenerationMW() - tmpBus.getMinimumGenerationMW() ) / minExcDG ) * excL );
					}
					accG += tmpBus.getGenerationMW();
				}
			}
		}
		
		System.out.println( accL + "\t" +  ( accDG + accNDG ) );
		
		// Creates FPF input file
		printPowerSystemDataCFPFFormat(outputFile.toString(), outputMapFile.toString());

		System.out.println("FPF input file of CIM model '" + n.getId() + "' successfully created");
	}

	// Prints input data of the system in the Classic FPF format
	private static void printPowerSystemDataCFPFFormat(String outputFilePath, String outputMapFilePath) {

		// // Redefine output path
		// outputFilePath = "C://Users//Leonel Carvalho//Desktop//" + powerSystem.getCaseId() + "_FPF" + ".txt";
		
		// // Redefine output path
		// String outputFilePath = "C://Users//Leonel Carvalho//workspace//jClassicFuzzyPowerFlow//data//" + powerSystem.getCaseId() + "_FPF" + ".txt";	
		String ch = ",";
		try {	
			PrintWriter out = new PrintWriter( new FileWriter( outputFilePath ) );
			out.println( powerSystem.printCFPFPowerSystem() );
			out.println( "BUS_DATA_FOLLOWS" + ch + powerSystem.getNumBuses() + ch + "ITEMS" );
			List< Integer > busesKeys = Collections.list( powerSystem.getBuses().keys() );
			Collections.sort( busesKeys );
			for( int i = 0; i < busesKeys.size(); i++ ) {
				out.println( powerSystem.getBuses().get( busesKeys.get( i ) ).printCFPFBus() ) ;
			}
			out.println( "-999" );
			out.println( "BRANCH_DATA_FOLLOWS" + ch + powerSystem.getNumBranches() + ch + "ITEMS" );
			List< Integer > branchesKeys = Collections.list( powerSystem.getBranches().keys() );
			Collections.sort( branchesKeys );
			for( int i = 0; i < branchesKeys.size(); i++ ) {
				out.println( powerSystem.getBranches().get( branchesKeys.get( i ) ).printCFPFBranch() ) ;
			}
			out.println( "-999" );
			out.println( "CONTINCENCY_DATA_FOLLOWS" + ch + powerSystem.getNumContingencies() + ch + "ITEMS" );
			List< Integer > contingenciesKeys = Collections.list( powerSystem.getContingencies().keys() );
			Collections.sort( contingenciesKeys );
			for( int i = 0; i < contingenciesKeys.size(); i++ ) {
				out.println( powerSystem.getContingencies().get( contingenciesKeys.get( i ) ).printCFPFContingency() ) ;
			}
			out.println( "-999" );
			out.print("END_OF_DATA");
			out.close();
		} catch( NumberFormatException e ) {
			e.printStackTrace();	
		} catch( MalformedURLException e ) {
			e.printStackTrace();
		} catch( IOException e ) {
			e.printStackTrace();
		}

		try (PrintWriter out = new PrintWriter( new FileWriter( outputMapFilePath) )) {
			out.println( "CONTINGENCIES:" );
			out.println( "IIDM_ID, FPF_ID" );
			for (String cid:idContingencyNumberMap.keySet()){
				out.println(cid + "," + "Contingency "+ idContingencyNumberMap.get(cid));
			}
			out.println();
			out.println( "BUSES:" );
			out.println("IIDM_ID, FPF_ID");
			for (String cid:idBusNumberMap.keySet()){
				out.println(cid + "," + idBusNumberMap.get(cid));
			}
			out.println();
			out.println( "BRANCHES:" );
			out.println("IIDM_ID, FPF_ID");
			for (String cid:idBranchNumberMap.keySet()){
				out.println(cid + "," + idBranchNumberMap.get(cid));
			}
			out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}


	// Main
	public static void main( String[] args ) {

		// ********************************** Initializations ********************************** //

		// Sets the locale as US
		Locale.setDefault( Locale.US );

		// Redefines input data path
		args = new String[ 3 ];
		args[ 0 ] = "CIM1";
		// Input file format is defined in args[ 1 ]
		args[ 1 ] = "C://Users//Leonel Carvalho//Documents//INESC TEC//Projects//iTESLA//FPF//Integration//Data//20130115_1845_SN2_FR0";
		args[ 2 ] = "20130115_1845_SN2_FR0";
		inputFilePath = args[ 1 ];

		// Imports network in CIM format
		Network n = Importers.import_( args[ 0 ], args[ 1 ], args[ 2 ], null );
		ContingenciesAndActionsDatabaseClient cadbClient = new AutomaticContingenciesAndActionsDatabaseClient( 5 ); // 5 contingencies
		List< Contingency > contingencies = cadbClient.getContingencies(n);
		Converter.convert(n, contingencies, null, Paths.get(".").resolve(powerSystem.getCaseId() + "_FPF" + ".txt"),Paths.get(".").resolve(powerSystem.getCaseId() + "_FPF_MAPPING" + ".txt"));
	}

	// Returns the season of the corresponding month
	public static String getSeason( int month ) {
		return seasons[ month - 1 ];
	}
	
	// Returns the index of the position of the string in the array. If the string is not in the array, then the function returns -1
	public static int indexArray( String[] array, String str ) {
		return Arrays.asList( array ).indexOf( str );
	}
}