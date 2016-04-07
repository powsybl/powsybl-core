/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export;

import eu.itesla_project.iidm.ddb.model.Internal;
import eu.itesla_project.iidm.ddb.model.SimulatorInst;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modelica_export.records.*;
import eu.itesla_project.modelica_export.util.SourceEngine;

import java.util.Map;

/**
 * Class factory to create Modelica Records from IIDM components
 * @author Silvia Machado <machados@aia.es>
 */
public final class ModelConverter {
	
	/**Convert PSSE regulator-constant connection to Modelica connect regulator
	 * 
	 */
	public static ConnectConstantRecord getModelicaRecord(String regName, String constantName, String regPin, String constantPin) {
		ConnectConstantRecord connectConstantRecord = new ConnectConstantRecord(regName, constantName, regPin, constantPin);
		connectConstantRecord.createModelicaName(null, null, null);
		
		return connectConstantRecord;
	}
	
	/**
	 * Convert IDDM bus-load connection to Modelica connect loads
	 * @param bus
	 * @param load
	 * @param modContext
	 * @return
	 */
	public static ConnectLoadRecord getModelicaRecord(ConnectBusInfo busInfo, Load load, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLoadRecord connectLoadRecord = new ConnectLoadRecord(busInfo, load);
		connectLoadRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectLoadRecord;
	}
	
	/**
	 * Convert IDDM dummy bus-dummy load connection to Modelica connect loads
	 * @param bus
	 * @param load
	 * @param modContext
	 * @return
	 */
	public static ConnectLoadRecord getModelicaRecord(String busName, String loadName, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLoadRecord connectLoadRecord = new ConnectLoadRecord(busName, loadName);
		connectLoadRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectLoadRecord;
	}
	
	/**
	 * Convert IDDM bus-shunt connection to Modelica connect capacitors
	 * @param bus
	 * @param shunt
	 * @param modContext
	 * @return
	 */
	public static ConnectCapacitorRecord getModelicaRecord(ConnectBusInfo busInfo, ShuntCompensator shunt, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectCapacitorRecord connectCapacitorRecord = new ConnectCapacitorRecord(busInfo, shunt);
		connectCapacitorRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectCapacitorRecord;
	}
	
	/**
	 * Convert IIDM bus-generator connection to Modelica connect generators
	 * @param bus
	 * @param gen
	 * @param modContext
	 * @return
	 */
	public static ConnectGeneratorRecord getModelicaRecord(ConnectBusInfo busInfo, Generator gen, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, boolean isInjection, SourceEngine sourceEngine) {
		ConnectGeneratorRecord connectGeneratorRecord = new ConnectGeneratorRecord(busInfo, gen, isInjection, sourceEngine);
		connectGeneratorRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectGeneratorRecord;
	}
	
	/**
	 * Convert IIDM generator-generator connection to Modelica connect generators
	 * @param gen
	 * @param modContext
	 * @return
	 */
	public static Connect2GeneratorsRecord getModelicaRecord(Generator gen, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, String pinName1, String pinName2) {
		Connect2GeneratorsRecord connect2GeneratorsRecord = new Connect2GeneratorsRecord(gen, pinName1, pinName2);
		connect2GeneratorsRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connect2GeneratorsRecord;
	}
	
	/**TODO On progress
	 * Convert generator-regulator connection to Modelica connect regulators
	 * @param gen
	 * @param reg
	 * @param modContext
	 * @return
	 */
	public static ConnectRegulatorRecord getModelicaRecord(Internal reg, Generator gen, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, String pinName1, String pinName2) {
		ConnectRegulatorRecord connectRegulatorRecord = new ConnectRegulatorRecord(reg, gen, pinName1, pinName2);
		connectRegulatorRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectRegulatorRecord;
	}
	
	/**
	 * Convert regulator-regulator connection to modelica connect between 2 regulators
	 * @param reg1
	 * @param reg2
	 * @param modContext
	 * @param ddbManager
	 * @param simulator
	 * @param pinName1
	 * @param pinName2
	 * @return
	 */
	public static Connect2RegulatorsRecord getModelicaRecord(Internal reg1, Internal reg2, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, String pinName1, String pinName2) {
		Connect2RegulatorsRecord connectRegulatorsRecord = new Connect2RegulatorsRecord(reg1, reg2, pinName1, pinName2);
		connectRegulatorsRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return connectRegulatorsRecord;
	}
	
	/**
	 * Creates Modelica generator-omegaRef connect
	 * @param bus
	 * @param gen
	 * @param modContext
	 * @return
	 */
	public static ConnectGlobalVarRecord getModelicaRecord(SingleTerminalConnectable ident, GlobalVariable globalVar, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectGlobalVarRecord connectGlobalVarRecord = new ConnectGlobalVarRecord(ident, globalVar);
		connectGlobalVarRecord.createModelicaName(modContext, ddbManager, simulator);
		return connectGlobalVarRecord;
	}
	
	/**
	 * Convert IIDM bus-line connection to Modelica connect lines
	 * @param bus
	 * @param line
	 * @param modContext
	 * @return
	 */
	public static ConnectLineRecord getModelicaRecord(Bus bus, Line line, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLineRecord connectLineRecord = new ConnectLineRecord(bus, line);
		connectLineRecord.createModelicaName(modContext, ddbManager, simulator);
        
	    return connectLineRecord;
	}
	
	public static ConnectLineRecord getModelicaRecord(Line line, Bus bus, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLineRecord connectLineRecord = new ConnectLineRecord(line, bus);
		connectLineRecord.createModelicaName(modContext, ddbManager, simulator);
        
	    return connectLineRecord;
	}
	
	/**
	 * Convert IIDM bus-dangling line connection to Modelica connect lines
	 * @param bus
	 * @param line
	 * @param modContext
	 * @return
	 */
	public static ConnectLineRecord getModelicaRecord(Bus bus, DanglingLine line, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLineRecord connectLineRecord = new ConnectLineRecord(bus, line);
		connectLineRecord.createModelicaName(modContext, ddbManager, simulator);
        
	    return connectLineRecord;
	}
	
	public static ConnectLineRecord getModelicaRecord(DanglingLine line, String busId, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectLineRecord connectLineRecord = new ConnectLineRecord(line, busId);
		connectLineRecord.createModelicaName(modContext, ddbManager, simulator);
        
	    return connectLineRecord;
	}
	
	
	/**
	 * Convert IIDM switch to Modelica (connect) coupling device
	 * @param switchNode
	 * @param bus1
	 * @param bus2
	 * @param modContext
	 * @return
	 */
	public static ConnectCouplingDevicesRecord getModelicaRecord(Switch switchNode, Bus bus1, Bus bus2, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		ConnectCouplingDevicesRecord couplingDeviceRecord = new ConnectCouplingDevicesRecord(switchNode, bus1, bus2);
		couplingDeviceRecord.createModelicaName(modContext, ddbManager, simulator);
        
	    return couplingDeviceRecord;
	}
	
	/**
	 * Convert IIDM bus-transformer connection to Modelica connect transformers
	 * @param transformer
	 * @param modContext
	 * @param fixed
	 * @return
	 */
	public static ConnectRecord getModelicaRecord(Bus bus, TwoWindingsTransformer transformer, ModExportContext modContext, boolean fixed, DDBManager ddbManager, SimulatorInst simulator) {
		if(fixed) {
			ConnectFixedTransformerRecord connectFixedTrafoRecord = new ConnectFixedTransformerRecord(bus, transformer);
			connectFixedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return connectFixedTrafoRecord;
		}
		else {
			ConnectDetailedTransformerRecord connectDetailedTrafoRecord = new ConnectDetailedTransformerRecord(bus, transformer);
			connectDetailedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return connectDetailedTrafoRecord;
		}
	}
	
	//TODO ELIMINAR en cuanto se obtenga la informaci?n de los PINS de la DDB (usar el m?todo anterior)
	public static ConnectRecord getModelicaRecord(TwoWindingsTransformer transformer, Bus bus, ModExportContext modContext, boolean fixed, DDBManager ddbManager, SimulatorInst simulator) {
		if(fixed) {
			ConnectFixedTransformerRecord connectFixedTrafoRecord = new ConnectFixedTransformerRecord(transformer, bus);
			connectFixedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return connectFixedTrafoRecord;
		}
		else {
			ConnectDetailedTransformerRecord connectDetailedTrafoRecord = new ConnectDetailedTransformerRecord(transformer, bus);
			connectDetailedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return connectDetailedTrafoRecord;
		}
	}
	
	/**
	 * Convert IIDM bus to Modelica bus
	 * @param bus
	 * @param modContext
	 * @return
	 */
	public static BusRecord getModelicaRecord(Bus bus, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		BusRecord busRecord = new BusRecord(bus);
		busRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return busRecord;
	}
	
	/**
	 * Create a Modelica bus correspondig to a "dangling"/"dummy" bus data 
	 * @param bus
	 * @param modContext
	 * @return
	 */
	public static BusRecord getModelicaRecord(String busId, float busVoltage, float busAngle, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		BusRecord busRecord = new BusRecord(busId, busVoltage, busAngle);
		busRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return busRecord;
	}
	
	/**
	 * Convert IIDM line to Modelica line
	 * @param line
	 * @param modContext
	 * @return
	 */
	public static LineRecord getModelicaRecord(Line line, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, float snref) {
		LineRecord lineRecord = new LineRecord(line, snref);
		lineRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return lineRecord;
	}
	
	/**
	/**
	 * Convert IIDM dangling line to Modelica dangling line
	 * @param line
	 * @param modContext
	 * @return
	 */
	public static DanglingLineRecord getModelicaRecord(DanglingLine dline, String danglingBusName, String danglingLoadName, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, float snref) {
		DanglingLineRecord dlineRecord = new DanglingLineRecord(dline, danglingBusName, danglingLoadName, snref);
		dlineRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return dlineRecord;
	}
	
	/**
	 * Convert IIDM transformer to Modelica transformer
	 * @param transformer
	 * @param modContext
	 * @param fixed
	 * @return
	 */
	public static BranchRecord getModelicaRecord(TwoWindingsTransformer transformer, ModExportContext modContext, boolean fixed, DDBManager ddbManager, SimulatorInst simulator, float snref) {
//		TransformerForTest trafoForTest = new TransformerForTest(transformer, snref);
//		trafoForTest.createModelicaName(modContext, ddbManager, simulator);
//		return trafoForTest;
		if(fixed) {
			FixedTransformerRecord fixedTrafoRecord = new FixedTransformerRecord(transformer, snref);
			fixedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return fixedTrafoRecord;
		}
		else {
			DetailedTransformerRecord detailedTrafoRecord = new DetailedTransformerRecord(transformer, snref);
			detailedTrafoRecord.createModelicaName(modContext, ddbManager, simulator);
			
			return detailedTrafoRecord;
		}
	}
	
	/**
	 * Convert IIDM load to Modelica load
	 * @param load
	 * @param modContext
	 * @return
	 */
	public static LoadRecord getModelicaRecord(Load load, ConnectBusInfo busInfo, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, float snref) {
		LoadRecord loadRecord = new LoadRecord(load, busInfo, snref);
		loadRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return loadRecord;
	}
	
	/**
	 * Create a Modelica load correspondig to a "dangling"/"dummy" load data
	 * @param load
	 * @param modContext
	 * @return
	 */
//	public static FixedInjectionRecord getModelicaRecord(String loadId, float p0, float q0, float busVoltage, float busAngle, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, float snref, SourceEngine sourceEngine) {
//		FixedInjectionRecord fixInjRecord = new FixedInjectionRecord(loadId, p0, q0, busVoltage, busAngle, snref, sourceEngine);
//		fixInjRecord.createModelicaName(modContext, ddbManager, simulator);
//		
//		return fixInjRecord;
//	}
	public static LoadRecord getModelicaRecord(String loadId, float p0, float q0, float busVoltage, float busAngle, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, float snref) {
		LoadRecord loadRecord = new LoadRecord(loadId, p0, q0, busVoltage, busAngle, snref);
		loadRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return loadRecord;
	}
	
	/**
	 * Convert IIDM shunt to Modelica capacitor
	 * 
	 */
	public static CapacitorRecord getModelicaRecord(ShuntCompensator shunt, ConnectBusInfo busInfo, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) {
		CapacitorRecord capacitorRecord = new CapacitorRecord(shunt, busInfo);
		capacitorRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return capacitorRecord;
	}
	
	/**
	 * Convert IIDM generator to Modelica generator
	 * @param generator
	 * @param busInfo
	 * @param modContext
	 * @param ddbManager
	 * @param modSim
	 * @param eurSim
	 * @param isInjection
	 * @param snref
	 * @param paramsDictionary
	 * @return
	 */
	public static GeneratorRecord getModelicaRecord(Generator generator, ConnectBusInfo busInfo, ModExportContext modContext, DDBManager ddbManager, SimulatorInst modSim, SimulatorInst eurSim, boolean isInjection, float snref, Map<String, Map<String, String>> paramsDictionary, SourceEngine sourceEngine) {
		GeneratorRecord generatorRecord = new GeneratorRecord(generator, busInfo, isInjection, snref, eurSim, paramsDictionary, sourceEngine); 		
		generatorRecord.createModelicaName(modContext, ddbManager, modSim);
		
		return generatorRecord;
	}
	
	/**
	 * Create Modelica Regulator
	 * @param regulator
	 * @param modContext
	 * @param ddbManager
	 * @param simulator
	 * @return
	 */
	public static RegulatorRecord getModelicaRecord(GeneratorRecord genRecord, Internal regulator, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator, String eqName, SimulatorInst sourceSimulator, SourceEngine sourceEngine) {
		RegulatorRecord regulatorRecord = new RegulatorRecord(genRecord, regulator, eqName, sourceSimulator, sourceEngine);
		regulatorRecord.createModelicaName(modContext, ddbManager, simulator);
		
		return regulatorRecord; 
	}
	
	
	public static float getNominalVoltage(TwoTerminalsConnectable twoTermCon, TwoTerminalsConnectable.Side side) {

		return 0; 
    }
}
