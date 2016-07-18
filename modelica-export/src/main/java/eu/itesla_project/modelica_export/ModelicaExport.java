/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export;

import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Identifiables;
import eu.itesla_project.iidm.network.util.SV;
import eu.itesla_project.modelica_export.initialization.Initialization;
import eu.itesla_project.modelica_export.initialization.InitializationData;
import eu.itesla_project.modelica_export.records.*;
import eu.itesla_project.modelica_export.util.EurostagEngine;
import eu.itesla_project.modelica_export.util.PsseEngine;
import eu.itesla_project.modelica_export.util.SourceEngine;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagFixedData;
import eu.itesla_project.modelica_export.util.eurostag.EurostagModDefaultTypes;
import eu.itesla_project.modelica_export.util.psse.PsseFixedData;
import eu.itesla_project.modelica_export.util.psse.PsseModDefaultTypes;
import org.openmodelica.javaomc.JavaOMCAPI;
import org.openmodelica.javaomc.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaExport {

	/**
	 * 
	 * @param network
	 * @param ddbManager
	 * @param paramsDictionary
	 * @param modelicaLibFile
	 * @param sourceEngine
	 */
	public ModelicaExport(Network network, DDBManager ddbManager, Map<String, Map<String, String>> paramsDictionary, File modelicaLibFile, SourceEngine sourceEngine) {
		this(network, ddbManager, new TreeMap<String, String>(), paramsDictionary, modelicaLibFile, sourceEngine);
	}

	/**
	 * Constructor for the EUROSTAG to MODELICA conversion because it's
	 * necessary to create the JavaOMCAPI object and load the Power Systems
	 * Library.
	 * 
	 * @param net
	 * @param ddbManager
	 * @param iidm2modelicaId
	 * @param paramsDictionary
	 * @param modelicaLibFile
	 * @param sourceEngine
	 */
	public ModelicaExport(Network net, DDBManager ddbManager, Map<String, String> iidm2modelicaId, Map<String, Map<String, String>> paramsDictionary, File modelicaLibFile, SourceEngine sourceEngine) {
		_network = net;
		_ddbManager = ddbManager;
		_sourceEngine = sourceEngine;
		dictionary = new ModelicaDictionary(iidm2modelicaId);
		this.paramsDictionary = paramsDictionary;
		this.modelicaLibFile = modelicaLibFile;

		omc = new JavaOMCAPI();
		try {
			if (this._sourceEngine instanceof EurostagEngine) {
				// Load PowerSystems library
				omc.loadFile(this.modelicaLibFile.getPath());
				//omc.loadFile("/home/machados/sources/data/ipsl_Raul/iPSL.mo");
				//omc.loadFile("/home/machados/sources/data/IPSL/PowerSystems.mo"); // Temporal mientras se arregla el macroblock converter
				omc.getStandardLibrary();
			}
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param network
	 * @param ddbManager
	 * @param paramsDictionary
	 * @param sourceEngine
	 */
	public ModelicaExport(Network network, DDBManager ddbManager, Map<String, Map<String, String>> paramsDictionary, SourceEngine sourceEngine) {
		this(network, ddbManager, new TreeMap<String, String>(), paramsDictionary, sourceEngine);
	}

	/**
	 * Constructor for the PSSE to MODELICA conversion because it is not
	 * necessary to initialize anything related to the Open Modelica
	 * initialization.
	 * 
	 * @param net
	 * @param ddbManager
	 * @param iidm2modelicaId
	 * @param paramsDictionary
	 * @param sourceEngine
	 */
	public ModelicaExport(Network net, DDBManager ddbManager, Map<String, String> iidm2modelicaId, Map<String, Map<String, String>> paramsDictionary, SourceEngine sourceEngine) {
		_network = net;
		_ddbManager = ddbManager;
		_sourceEngine = sourceEngine;
		dictionary = new ModelicaDictionary(iidm2modelicaId);
		this.paramsDictionary = paramsDictionary;
	}

	/**
	 * 
	 * @param moFile
	 * @param modelicaVersion
	 * @throws IOException
	 */
	public void WriteMo(String moFile, String modelicaVersion) throws IOException {
		String moFileName = moFile + StaticData.MO_EXTENSION;

		numberOfElements();
		// Just for testing
		// countIIDMElements(moFileName);
		// Just for testing

		FileWriter outputMoFile = new FileWriter(moFileName);
		StringWriter outputStringMo = new StringWriter();
		DoConvertion(outputStringMo, modelicaVersion);

		outputMoFile.write(outputStringMo.toString());
		outputMoFile.close();
	}

	/**
	 * Convert IIDM Data to Modelica data.
	 * 
	 * @param writerMo
	 * @param modelicaVersion
	 * @throws IOException
	 */
	private void DoConvertion(Writer writerMo, String modelicaVersion) throws IOException {
		ModExportContext modContext = new ModExportContext(dictionary);

		long initTime = System.currentTimeMillis();
		SimulatorInst modelicaSim = _ddbManager.findSimulator(Simulator.MODELICA, modelicaVersion);
		SimulatorInst sourceSim = null;
		if (this._sourceEngine instanceof EurostagEngine) {
			sourceSim = _ddbManager.findSimulator(Simulator.EUROSTAG, this._sourceEngine.getVersion());
		} else if (this._sourceEngine instanceof PsseEngine) {
			sourceSim = _ddbManager.findSimulator(Simulator.PSSE, this._sourceEngine.getVersion());
		}

		if (modelicaSim == null) {
			_log.error("Simulator with version = " + modelicaVersion + " does not exist.");
			return;
		}
		List<String> modelicaModelsList = null;

		// File header
		List<GlobalVariable> globalVars = new ArrayList<GlobalVariable>();
		GlobalVariable OMEGAREF_Var = null;
		SNREF = StaticData.SNREF_VALUE;

		GlobalVariable SNREF_Var = new GlobalVariable(StaticData.PARAM_TYPE, StaticData.SNREF, SNREF);
		globalVars.add(SNREF_Var);
		if (this._sourceEngine instanceof EurostagEngine) {
			OMEGAREF_Var = new GlobalVariable(EurostagModDefaultTypes.OUTPUT_PIN_TYPE, EurostagFixedData.OMEGAREF_NAME);
			globalVars.add(OMEGAREF_Var);
		}

		HeaderRecord headerRecord = new HeaderRecord(_network.getName(), globalVars);
		this.addRecord(headerRecord, writerMo, modContext, _ddbManager, modelicaSim);
			
		/**
		 * Export buses
		 */
		busesList = Identifiables.sort(_network.getBusBreakerView().getBuses());
		exportBuses(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Read Dangling lines data to create a BUS at the dangling side
		 */
		dangLinesList = Identifiables.sort(_network.getDanglingLines());
		exportDanglingBuses(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export loads
		 * 			1. Loads
		 * 			2. Dummy loads at the Dangling lines "dangling" side
		 */
		loadsList = Identifiables.sort(_network.getLoads());
		exportLoads(writerMo, modContext, modelicaModelsList, modelicaSim);
		exportDanglingLoads(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export all transformers
		 */
		trafosList = Identifiables.sort(_network.getTwoWindingsTransformers());
		exportTransformers(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export lines
		 */
		linesList = Identifiables.sort(_network.getLines());
		exportLines(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export Dangling lines
		 */
		exportDanglingLines(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export shunts
		 */
		shuntsList = Identifiables.sort(_network.getShunts());
		exportCapacitors(writerMo, modContext, modelicaModelsList, modelicaSim);


		/**
		 * Classifying generators & fixed injections
		 */
		genList = Identifiables.sort(_network.getGenerators());
		for (Generator gen : genList) {
			Equipment eq = _ddbManager.findEquipment(gen.getId());
			if (eq != null)
				generators.add(gen);
			else
				generatorsInyections.add(gen);
		}
		
		/**
		 * Export fixed injections
		 * 		2. export generators as fixed injections
		 */
		this.addRecord(writerMo, null);
		this.addRecord(writerMo, "// FIXED INJECTIONS");
		exportGeneratorsAsFixedInjections(writerMo, modContext, modelicaModelsList, modelicaSim, sourceSim);
		
		/**
		 * Export generators
		 */
		exportGeneratorsAndRegulators(writerMo, modContext, modelicaModelsList, modelicaSim, sourceSim);
		
		if ((this._sourceEngine instanceof PsseEngine)) {
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "\t" + PsseModDefaultTypes.CONSTANT_TYPE + " " + PsseFixedData.CONSTANT + ";");

			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "\t" + PsseModDefaultTypes.CONSTANT_TYPE + " " + PsseFixedData.CONSTANT1 + ";");

			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "\t" + PsseModDefaultTypes.CONSTANT_TYPE + " " + PsseFixedData.CONSTANT2 + ";");
		}

		this.addRecord(writerMo, null);
		this.addRecord(writerMo, StaticData.EQUATION);

		if (this._sourceEngine instanceof EurostagEngine) {
			List<GlobalVariable> globalInitVars = new ArrayList<GlobalVariable>();
			String omegaRefVal = null;
			if ((generators.size() > 0) && (!generators.isEmpty()))
				omegaRefVal = calculateOmegaRef(generators);
			OMEGAREF_Var.setValue(omegaRefVal);
			globalInitVars.add(OMEGAREF_Var);

			for (GlobalVariable var : globalInitVars) {
				if (var.getValue() != null) {
					InitializationRecord initRecord = new InitializationRecord(var);
					this.addRecord(initRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}

			if (OMEGAREF_Var.getValue() != null) {
				// Export Connect between Generators and OmegaRef
				exportConnectGlobalVar(writerMo, modContext, new ArrayList<SingleTerminalConnectable>(generators), OMEGAREF_Var, modelicaSim);
			}
		}

		/**
		 * Export Connect Regulators (MACHINE-REGULATOR or REGULATOR-REGULATOR)
		 */
		exportConnectRegulators(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Lines
		 */
		connectLinesList = Identifiables.sort(_network.getLines());
		exportConnectLines(writerMo, modContext, modelicaModelsList, modelicaSim);
		
		/**
		 * Export Connect Dangling Lines
		 */
		connectDanglingLinesList = Identifiables.sort(_network.getDanglingLines());
		exportConnectDanglingLines(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Coupling devices
		 */
		connectCouplingList = Identifiables.sort(_network.getVoltageLevels());
		exportConnectCouplingDevices(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Loads
		 */
		connectLoadsList = Identifiables.sort(_network.getLoads());
		exportConnectLoads(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Dangling Loads
		 */
		exportConnectDanglingLoads(writerMo, modContext, modelicaModelsList, modelicaSim);

		
		/**
		 * Export Connect Shunts
		 */
		connectCapacitorsList = Identifiables.sort(_network.getShunts());
		exportConnectCapacitors(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Generators
		 */
		exportConnectGenerators(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Connect Transformers
		 */
		exportConnectTransformers(writerMo, modContext, modelicaModelsList, modelicaSim);

		/**
		 * Export Other connections
		 */
		this.addRecord(writerMo, null);
		this.addRecord(writerMo, "// Connecting OTHERS");

		// File footer
		FooterRecord footerRecord = new FooterRecord(_network.getName());
		this.addRecord(footerRecord, writerMo, modContext, _ddbManager, modelicaSim);
	}

	private String calculateOmegaRef(List<Generator> genList) {
		String omegaRef = "", name = "";

		StringBuffer abuff = new StringBuffer();
		StringBuffer bbuff = new StringBuffer();

		for (int i = 0; i < genList.size() - 1; i++) {
			name = dictionary.getModelicaName(genList.get(i));
			abuff.append(name + "." + EurostagFixedData.OMEGA_PIN + "*" + name + "." + EurostagFixedData.HIN_PIN + "*" + name + "." + EurostagFixedData.SN_PIN + " + ");
			bbuff.append(name + "." + EurostagFixedData.HIN_PIN + "*" + name + "." + EurostagFixedData.SN_PIN + " + ");
		}
		name = dictionary.getModelicaName(genList.get(genList.size() - 1));
		abuff.append(name + "." + EurostagFixedData.OMEGA_PIN + "*" + name + "." + EurostagFixedData.HIN_PIN + "*" + name + "." + EurostagFixedData.SN_PIN);
		bbuff.append(name + "." + EurostagFixedData.HIN_PIN + "*" + name + "." + EurostagFixedData.SN_PIN);

		omegaRef = "(" + abuff.toString() + ") / (" + bbuff.toString() + ")";

		return omegaRef;
	}
	
	
	/**
	 * Create a Dummy  Bus (corresponding to a dangling line)
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportDanglingBuses(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if((dangLinesList.size() != 0) && (!dangLinesList.isEmpty())){			
			for(DanglingLine dl : dangLinesList) {
				Bus knownBus = dl.getTerminal().getBusBreakerView().getBus();
				SV sv = new SV(0, 0,  knownBus.getV(), knownBus.getAngle());
				SV svDangling = sv.otherSide(dl);
				
				float voltage = svDangling.getU() / knownBus.getVoltageLevel().getNominalV();
				float angle = svDangling.getA();
				String name = "ext_" + dl.getId();
				
				BusRecord busRecord = ModelConverter.getModelicaRecord(name, voltage, angle, modContext, _ddbManager, modelicaSim);
				this.danglingBuses.add(busRecord);
				this.addRecord(busRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}
	
	/**
	 * Create a Dummy Load (corresponding to a dangling line)
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportDanglingLoads(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if((dangLinesList.size() != 0) && (!dangLinesList.isEmpty())){
			for(DanglingLine dl : dangLinesList) {
				Bus knownBus = dl.getTerminal().getBusBreakerView().getBus();
				SV sv = new SV(0, 0,  knownBus.getV(), knownBus.getAngle());
				SV svDangling = sv.otherSide(dl);
				float busVoltage = svDangling.getU() / knownBus.getVoltageLevel().getNominalV();
				float busAngle = svDangling.getA();
				float p0 = dl.getP0(); 
				float q0 = dl.getQ0();  
				String loadId = "ext_" + dl.getId();
				LoadRecord loadRecord = ModelConverter.getModelicaRecord(loadId, p0, q0, busVoltage, busAngle, modContext, _ddbManager, modelicaSim, SNREF, this._sourceEngine);
//				FixedInjectionRecord fixInjRecord = ModelConverter.getModelicaRecord(loadId, p0, q0, busVoltage, busAngle,modContext, _ddbManager, modelicaSim, SNREF, this._sourceEngine);
				this.danglingLoads.add(loadRecord);
				this.addRecord(loadRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}

	/**
	 * Export IIDM Dangling lines to Modelica lines
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportDanglingLines(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((dangLinesList.size() != 0) && (!dangLinesList.isEmpty())) {
			_log.info("EXPORTING DANGLING LINES");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// DANGLING LINES");
			for (DanglingLine dl : dangLinesList) {
				_log.info("Exporting dangling line " + dl.getId());

				if(dl.getTerminal().getBusBreakerView().getBus() == null) {
					_log.info("DANGLING LINE " + dl.getId() + " OUT OF SERVICE.");
				}
				else {
					Bus knownBus = dl.getTerminal().getBusBreakerView().getBus();
					SV sv = new SV(0, 0,  knownBus.getV(), knownBus.getAngle());
					SV svDangling = sv.otherSide(dl);
					
					float busVoltage = svDangling.getU() / knownBus.getVoltageLevel().getNominalV();
					float busAngle = svDangling.getA();
					String busName = "ext_" + dl.getId();
					float p0 = dl.getP0();
					float q0 = dl.getQ0();
					String loadId = "ext_" + dl.getId();
					
					BusRecord busRecord = ModelConverter.getModelicaRecord(busName, busVoltage, busAngle, modContext, _ddbManager, modelicaSim);
					LoadRecord loadRecord = ModelConverter.getModelicaRecord(loadId, p0, q0, busVoltage, busAngle, modContext, _ddbManager, modelicaSim, SNREF, this._sourceEngine);
					
					DanglingLineRecord dlineRecord = ModelConverter.getModelicaRecord(dl, busRecord.getModelicaName(), loadRecord.getModelicaName(), modContext, _ddbManager, modelicaSim, SNREF);
					danglingLines.add(dlineRecord);
					this.addRecord(dlineRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
		}
	}
	
	/**
	 * Export IIDM buses to Modelica buses
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportBuses(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((busesList.size() != 0) && (!busesList.isEmpty())) {
			_log.info("EXPORTING BUSES");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// BUSES");
			for (Bus bus : busesList) {
				_log.info("Exporting bus " + bus.getId());

				BusRecord busRecord = ModelConverter.getModelicaRecord(bus, modContext, _ddbManager, modelicaSim);
				this.addRecord(busRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
			busesList = null;
		}
	}

	/**
	 * Export IIDM lines to Modelica lines
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportLines(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((linesList.size() != 0) && (!linesList.isEmpty())) {
			_log.info("EXPORTING LINES");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// LINES");
			for (Line line : linesList) {
				_log.info("Exporting line " + line.getId());

				if (line.getTerminal1().getBusBreakerView().getBus() == null && line.getTerminal2().getBusBreakerView().getBus() == null) {
					_log.info("LINE " + line.getId() + " OUT OF SERVICE.");
				}

				if ((line.getB1() == line.getB2()) && (line.getG1() == line.getG2())) {
					LineRecord lineRecord = ModelConverter.getModelicaRecord(line, modContext, _ddbManager, modelicaSim, SNREF);
					this.addRecord(lineRecord, writerMo, modContext, _ddbManager, modelicaSim);
				} else {
					// TODO Por ahora tomamos las lineas asimetricas como
					// simetricas.
					_log.warn("The model has an asymmetric line: {}.", line.getId());
					LineRecord lineRecord = ModelConverter.getModelicaRecord(line, modContext, _ddbManager, modelicaSim, SNREF);
					this.addRecord(lineRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
			linesList = null;
		}
	}

	/**
	 * Export IIDM trafos to Modelica trafos
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportTransformers(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((trafosList.size() != 0) && (!trafosList.isEmpty())) {
			for (TwoWindingsTransformer trafo : trafosList) {
				if ((trafo.getRatioTapChanger() == null) && (trafo.getPhaseTapChanger() == null)) {
					fixedTranformers.add(trafo);
				} else {
					detailedTranformers.add(trafo);
				}
			}

			// Export Fixed Transformers
			if ((fixedTranformers.size() != 0) && (!fixedTranformers.isEmpty())) {
				_log.info("EXPORTING FIXED TRANSFORMERS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// FIXED TRANSFORMERS");
				for (TwoWindingsTransformer trafo : fixedTranformers) {
					_log.info("Exporting fixed trafo " + trafo.getId());
					FixedTransformerRecord fixedTrafoRecord = (FixedTransformerRecord) ModelConverter.getModelicaRecord(trafo, modContext, true, _ddbManager, modelicaSim, SNREF);
					this.addRecord(fixedTrafoRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}

			// Export Detailed Transformers
			if ((detailedTranformers.size() != 0) && (!detailedTranformers.isEmpty())) {
				_log.info("EXPORTING TAP CHANGER TRANSFORMERS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// TAP CHANGER TRANSFORMERS");
				for (TwoWindingsTransformer trafo : detailedTranformers) {
					_log.info("Exporting detailed trafo " + trafo.getId());
					DetailedTransformerRecord detailedTrafoRecord = (DetailedTransformerRecord) ModelConverter.getModelicaRecord(trafo, modContext, false, _ddbManager, modelicaSim, SNREF);
					this.addRecord(detailedTrafoRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
		}
	}

	/**
	 * Export IIDM loads to Modelica loads
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportLoads(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((loadsList.size() != 0) && (!loadsList.isEmpty())) {
			_log.info("EXPORTING LOADS");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// LOADS");
			for (Load load : loadsList) {
				_log.info("Exporting load " + load.getId());
				ConnectBusInfo busInfo = findBus(load.getTerminal(), load.getId());
				// If load's disconnected we remove it from list in order to
				// didn't corresponding connects
				LoadRecord loadRecord = ModelConverter.getModelicaRecord(load, busInfo, modContext, _ddbManager, modelicaSim, SNREF, this._sourceEngine);
				this.addRecord(loadRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
			loadsList = null;
		}
	}

	/**
	 * Export IIDM shunts to Modelica capacitors
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportCapacitors(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((shuntsList.size() != 0) && (!shuntsList.isEmpty())) {
			_log.info("EXPORTING SHUNTS");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// CAPACITORS");
			for (ShuntCompensator shunt : shuntsList) {
				_log.info("Exporting shunt " + shunt.getId());
				ConnectBusInfo busInfo = findBus(shunt.getTerminal(), shunt.getId());
				CapacitorRecord shuntRecord = ModelConverter.getModelicaRecord(shunt, busInfo, modContext, _ddbManager, modelicaSim);
				this.addRecord(shuntRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
			shuntsList = null;
		}
	}

	/**
	 * 
	 */
	private void exportGeneratorsAsFixedInjections(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim, SimulatorInst sourceSim) throws IOException {
		// Export Generators as Fixed Inyections
		if ((generatorsInyections.size() != 0) && (!generatorsInyections.isEmpty())) {
			_log.info("EXPORTING GENERATORS AS FIXED INYECTIONS");
			for (Generator generator : generatorsInyections) {
				_log.info("\t Exporting generator inyection " + generator.getId());

				ConnectBusInfo busInfo = findBus(generator.getTerminal(), generator.getId());
				GeneratorRecord generatorRecord = ModelConverter.getModelicaRecord(generator, busInfo, modContext, _ddbManager, modelicaSim, sourceSim, true, SNREF, this.paramsDictionary, this._sourceEngine);
				this.addRecord(generatorRecord, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}
	
	/**
	 * Export IIDM generators to Modelica generators
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @param sourceSim
	 * @throws IOException
	 */
	private void exportGeneratorsAndRegulators(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim, SimulatorInst sourceSim) throws IOException {
		Initialization initialization;
		List<Internal> genRegulators;
		Map<Internal, RegulatorRecord> genRegRecords;
		List<InitializationData> initializationDataList = new ArrayList<InitializationData>();
		List<GeneratorRecord> generatorsRecords = new ArrayList<GeneratorRecord>();

		if ((genList.size() != 0) && (!genList.isEmpty())) {
		
			Map<GeneratorRecord, List<RegulatorRecord>> genRegsRecordMap = new HashMap<GeneratorRecord, List<RegulatorRecord>>();
			List<RegulatorRecord> genRegsRecordList;

			// Export Generators
			InitializationData initializationData;
			tmpDir = Files.createTempDirectory(Paths.get(new File(".").getCanonicalPath()), "itesla_tmp");
			if ((generators.size() != 0) && (!generators.isEmpty())) {
				_log.info("EXPORTING GENERATORS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// GENERATORS");
				for (Generator generator : generators) {
					genRegsRecordList = new ArrayList<RegulatorRecord>();
					genRegulators = new ArrayList<Internal>();
					genRegRecords = new HashMap<Internal, RegulatorRecord>();
					_log.info("\t Exporting generator " + generator.getId());

					ConnectBusInfo busInfo = findBus(generator.getTerminal(), generator.getId());
					GeneratorRecord generatorRecord = ModelConverter.getModelicaRecord(generator, busInfo, modContext, _ddbManager, modelicaSim, sourceSim, false, SNREF, this.paramsDictionary, this._sourceEngine);
					generatorsRecords.add(generatorRecord);
					
					// If generator is disconnected then we write a message and we don't do anything  about connected regulators
					boolean hasEsst1a = false;
					boolean hasPss2a = false;
					boolean hasIeeest = false;
					boolean hasEsdc2a = false;
					RegulatorRecord esst1aRecord = null;
					RegulatorRecord esdc2aRecord = null;
					if(!Float.isNaN(busInfo.getBus().getV()) && busInfo.isConnected()) {
						Equipment eq = _ddbManager.findEquipment(generator.getId());
						if (eq != null) {
							ConnectionSchema connectionSchema = _ddbManager.findConnectionSchema(eq.getCimId(), null);

							if (connectionSchema != null) {
								List<Connection> connections = connectionSchema.getConnections();
								if ((connections != null) && (!connections.isEmpty())) {
									List<String> regulatorsAdded = new ArrayList<String>();
									String equipName = null;
									RegulatorRecord regulatorRecord;
									for (Connection con : connectionSchema.getConnections()) {
										Internal regulator1 = null;
										Internal regulator2 = null;

										String pinName1 = null;
										String pinName2 = null;
										if (this._sourceEngine instanceof EurostagEngine) {
											pinName1 = StaticData.PIN + con.getConPointName1();
											pinName2 = StaticData.PIN + con.getConPointName2();
										} else if (this._sourceEngine instanceof PsseEngine) {
											pinName1 = con.getConPointName1();
											pinName2 = con.getConPointName2();
										}
										equipName = eq.getCimId();

										// Conexiones entre generadores
										if (con.getId1Type() == 0 && con.getId2Type() == 0) {
											if (this._sourceEngine instanceof PsseEngine) {
												Connect2GeneratorsRecord connect2GensRecord = ModelConverter.getModelicaRecord(generator, modContext, _ddbManager, modelicaSim, con.getConPointName1(), con.getConPointName2());
												connect2GensList.add(connect2GensRecord);
											}
										}

										if (con.getId1Type() == 1) {
											regulator1 = _ddbManager.findInternal(con.getId1());
										}
										if (con.getId2Type() == 1) {
											regulator2 = _ddbManager.findInternal(con.getId2());
										}
										if ((regulator1 == null) && (regulator2 == null))
											continue;

										if (regulator1 != null) {
											if (!regulatorsAdded.contains(regulator1.getNativeId())) {
												regulatorsAdded.add(regulator1.getNativeId());
												regulatorRecord = ModelConverter.getModelicaRecord(generatorRecord, regulator1, modContext, _ddbManager, modelicaSim, equipName, sourceSim, this._sourceEngine);
												regulatorsList.add(regulatorRecord);
												genRegsRecordList.add(regulatorRecord);
												genRegulators.add(regulator1);
												genRegRecords.put(regulator1, regulatorRecord);

												// Harcoded connects (PSSE)
												if ((this._sourceEngine instanceof PsseEngine) && (PsseModDefaultTypes.REGS_WITH_CONST.contains(regulatorRecord.getModelicaType()))) {
													includeconstant = true;

													ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
													connectConstList.add(connectConstRecord);

													connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
													connectConstList.add(connectConstRecord);
												}

												if (this._sourceEngine instanceof PsseEngine) {
													if (PsseModDefaultTypes.ESST1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														includeconstant = true;
														hasEsst1a = true;
														esst1aRecord = regulatorRecord;
														
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG2_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VUEL1_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL2_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL3_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);

														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME2, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} 
													else if(PsseModDefaultTypes.PSS2A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasPss2a = true;
													} else if (PsseModDefaultTypes.ESAC1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME2, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.ESDC2A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasEsdc2a = true;
														esdc2aRecord = regulatorRecord;
														
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.ESDC1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.IEEEX1.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if(PsseModDefaultTypes.IEEEST.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasIeeest = true;
													}
												}
											}
										}

										if (regulator2 != null) {
											if (!regulatorsAdded.contains(regulator2.getNativeId())) {
												regulatorsAdded.add(regulator2.getNativeId());
												regulatorRecord = ModelConverter.getModelicaRecord(generatorRecord, regulator2, modContext, _ddbManager, modelicaSim, equipName, sourceSim, this._sourceEngine);
												regulatorsList.add(regulatorRecord);
												genRegsRecordList.add(regulatorRecord);
												genRegulators.add(regulator2);
												genRegRecords.put(regulator2, regulatorRecord);

												// Harcoded connects (PSSE)
												if ((this._sourceEngine instanceof PsseEngine) && (PsseModDefaultTypes.REGS_WITH_CONST.contains(regulatorRecord.getModelicaType()))) {
													includeconstant = true;

													ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
													connectConstList.add(connectConstRecord);

													connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
													connectConstList.add(connectConstRecord);
												}

												if (this._sourceEngine instanceof PsseEngine) {
													if (PsseModDefaultTypes.ESST1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														includeconstant = true;
														hasEsst1a = true;
														esst1aRecord = regulatorRecord;
														
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG2_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);

														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VUEL1_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL2_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL3_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);

														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME2, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} 
													else if(PsseModDefaultTypes.PSS2A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasPss2a = true;
													} else if (PsseModDefaultTypes.ESAC1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME2, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.ESDC2A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasEsdc2a = true;
														esdc2aRecord = regulatorRecord;
														
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.ESDC1A.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
														
														connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME1, PsseFixedData.VUEL_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if (PsseModDefaultTypes.IEEEX1.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(regulatorRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
														connectConstList.add(connectConstRecord);
													} else if(PsseModDefaultTypes.IEEEST.equalsIgnoreCase(regulatorRecord.getModelicaType())) {
														hasIeeest = true;
													}
												}
											}
										}

										if ((regulator1 != null) && (regulator2 != null)) {
											Connect2RegulatorsRecord connect2RegRecord = ModelConverter.getModelicaRecord(regulator1, regulator2, modContext, _ddbManager, modelicaSim, pinName1, pinName2);
											if (connect2RegRecord != null)
												connect2RegsList.add(connect2RegRecord);
										} else {
											ConnectRegulatorRecord connectRegRecord = null;
											if (regulator1 != null) {
												connectRegRecord = ModelConverter.getModelicaRecord(regulator1, generator, modContext, _ddbManager, modelicaSim, pinName1, pinName2);
											} else if (regulator2 != null) {
												connectRegRecord = ModelConverter.getModelicaRecord(regulator2, generator, modContext, _ddbManager, modelicaSim, pinName2, pinName1);
											}
											if (connectRegRecord != null)
												connectRegList.add(connectRegRecord);
										}
									}

									// Con el generador y la lista de todos sus
									// reguladores se crea el fichero Mi_init.mo
									// para la inicializacion. (Only in the case
									// that source = EUROSTAG
									_log.info("GENERATOR_= " + generator.getId());
									if (this._sourceEngine instanceof EurostagEngine) {

										initializationData = new InitializationData(generator, generatorRecord, genRegRecords);
										initializationDataList.add(initializationData);

									} else if(this._sourceEngine instanceof PsseEngine) {
										if(hasEsst1a && !hasPss2a && !hasIeeest) {
											ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(esst1aRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
											connectConstList.add(connectConstRecord);
										}
										if(hasEsdc2a && !hasPss2a && !hasIeeest) {
											ConnectConstantRecord connectConstRecord = ModelConverter.getModelicaRecord(esdc2aRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
											connectConstList.add(connectConstRecord);
										}
									}
								} else {
									_log.info("GENERATOR HAS NOT CONNECTIONS");

									// Si el generador no tiene reguladores se
									// inicializa el generador unicamente
									if (this._sourceEngine instanceof EurostagEngine) {
										initializationData = new InitializationData(generator, generatorRecord, genRegRecords);
										initializationDataList.add(initializationData);
									}
								}
							} else
								_log.info("CONNECTION SCHEMA IS NULL");
						}

						if (!genRegsRecordMap.containsKey(generator)) {
							genRegsRecordMap.put(generatorRecord, genRegsRecordList);
						} else {
							genRegsRecordMap.get(generator).addAll(genRegsRecordList);
						}
					}
				}

				// The initialization is only performed if the source engine is
				// EUROSTAG
				if (this._sourceEngine instanceof EurostagEngine) {
					System.out.println("Initializing generators");
					initialization = new Initialization(omc, _ddbManager, tmpDir, initializationDataList);
					//initialization.init();
					try {
						initialization.init();
					} catch (ConnectException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
								
				for (GeneratorRecord genRecord : generatorsRecords) {
					// El generatorRecord y los regulatorsRecords se anaden
					// despues de la inicializacion para poder
					// poner ya los valores inicializados
					this.addRecord(genRecord, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
			
			// Export Regulators
			if ((regulatorsList.size() != 0) && (!regulatorsList.isEmpty())) {
				_log.info("EXPORTING REGULATORS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// REGULATORS");
				for (RegulatorRecord reg : regulatorsList) {
					_log.info("\t Exporting regulator " + reg.getModelicaName());
					this.addRecord(reg, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
			// Add special connection between Reg.VOTHSG and Constant.y if
			// needed for conversion from PSSE
			if (this._sourceEngine instanceof PsseEngine) {
				for (GeneratorRecord genRec : genRegsRecordMap.keySet()) {
					List<RegulatorRecord> regs = genRegsRecordMap.get(genRec);

					ConnectConstantRecord connectConstRecord;
					boolean hasStab = isThereAStab(regs);
					List<RegulatorRecord> specialRegs = searchSpecialRegs(regs);

					if (!hasStab && !specialRegs.isEmpty()) {
						for (RegulatorRecord regRecord : specialRegs) {
							connectConstRecord = ModelConverter.getModelicaRecord(regRecord.getModelicaName(), PsseFixedData.CONST_NAME, PsseFixedData.VOTHSG_PIN, PsseFixedData.Y_PIN);
							connectConstList.add(connectConstRecord);
						}
					} else if (!specialRegs.isEmpty()) {

					}
				}
			}
		}
	}

	/**
	 * Searchs in a list of regulators (for a generator) if there a regulator in
	 * (SCRX, SEXS, IEEET2) AND if there isn't a STAB2A and returns true in this
	 * case
	 * 
	 * @param regulators
	 *            : list of regulators for an specific generator
	 */
	private boolean isThereAStab(List<RegulatorRecord> regulators) {
		boolean hasStab = false;

		for (RegulatorRecord reg : regulators) {
			if (reg.getModelicaType().equals(PsseModDefaultTypes.STAB2A)) {
				hasStab = true;
			}
		}
		return hasStab;
	}

	/**
	 * Searchs in a list of regulators (for a generator) if there is a regulator
	 * in (SCRX, SEXS, IEEET2) and returns it/them. Returns en empty list if
	 * there isn't.
	 * 
	 * @param regulators
	 * @return List<RegulatorRecord> list of special regulators contained in the
	 *         list of regulators for a generator.
	 */
	private List<RegulatorRecord> searchSpecialRegs(List<RegulatorRecord> regulators) {
		List<RegulatorRecord> specialRegs = new ArrayList<RegulatorRecord>();

		for (RegulatorRecord reg : regulators) {
			if (PsseFixedData.SPECIAL_REGS.contains(reg.getModelicaType())) {
				specialRegs.add(reg);
			}
		}

		return specialRegs;
	}

	/**
	 * Export IIDM Generators to Modelica Generators-OmegaRef connect
	 */
	private void exportConnectGlobalVar(Writer writerMo, ModExportContext modContext, List<SingleTerminalConnectable> identList, GlobalVariable globalVar, SimulatorInst modelicaSim) throws IOException {
		if ((identList.size() != 0) && (!identList.isEmpty())) {
			this.addRecord(writerMo, null);
			for (SingleTerminalConnectable singleTerCon : identList) {
				ConnectBusInfo busInfo = findBus(singleTerCon.getTerminal(), singleTerCon.getId());
				if (!Float.isNaN(busInfo.getBus().getV()) && busInfo.isConnected()) {
					ConnectGlobalVarRecord record = ModelConverter.getModelicaRecord(singleTerCon, globalVar, modContext, _ddbManager, modelicaSim);
					if (record != null)
						this.addRecord(record, writerMo, modContext, _ddbManager, modelicaSim);
				}
			}
		}
	}

	/**
	 * Export IIDM regulators connect to Modelica regulators connect
	 * 
	 * @throws IOException
	 */
	private void exportConnectRegulators(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		_log.info("EXPORTING CONNECT REGULATORS");
		if ((connectRegList.size() != 0) && (!connectRegList.isEmpty())) {
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting REGULATORS and MACHINES");
			for (ConnectRegulatorRecord connectReg : connectRegList) {
				_log.info("\t Exporting regulator connect " + connectReg.getModelicaName());
				this.addRecord(connectReg, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}

		_log.info("EXPORTING CONNECT BETWEEN 2 REGULATORS");
		if ((connect2RegsList.size() != 0) && (!connect2RegsList.isEmpty())) {
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting REGULATORS and REGULATORS");
			for (Connect2RegulatorsRecord connectReg : connect2RegsList) {
				_log.info("\t Exporting regulator connect " + connectReg.getModelicaName());
				this.addRecord(connectReg, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}

		_log.info("EXPORTING CONNECT BETWEEN 2 EQUIPMENTS");
		if ((connect2GensList.size() != 0) && (!connect2GensList.isEmpty())) {
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting EQUIPMENTS and EQUIPMENTS");
			for (Connect2GeneratorsRecord connectGen : connect2GensList) {
				_log.info("\t Exporting equipment connect " + connectGen.getModelicaName());
				this.addRecord(connectGen, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}

		// Si el regulador es SCRX|SEXS|IEEET2 anadir connect(const.y, Reg.VOEL)
		// y connect(const.y, Reg.VOEL)
		_log.info("EXPORTING CONNECT BETWEEN REGULATOR AND CONSTANT");
		if ((connectConstList.size() != 0) && (!connectConstList.isEmpty())) {
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting REGULATORS and CONSTANTS");
			for (ConnectConstantRecord connectReg : connectConstList) {
				_log.info("\t Exporting regulator connect " + connectReg.getModelicaName());
				this.addRecord(connectReg, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}

	/**
	 * Export IIDM lines connect to Modelica lines connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectLines(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((connectLinesList.size() != 0) && (!connectLinesList.isEmpty())) {
			_log.info("EXPORTING CONNECT LINES");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting LINES");
			for (Line line : connectLinesList) {

				_log.info("\t Exporting line connect " + line.getId());
				Equipments.ConnectionInfo info1 = Equipments.getConnectionInfoInBusBreakerView(line.getTerminal1());
				Bus b = info1.getConnectionBus();
				if (!Float.isNaN(b.getV())) {
					if (info1.isConnected()) {
						ConnectLineRecord lineT1Connect = ModelConverter.getModelicaRecord(b, line, modContext, _ddbManager, modelicaSim);
						this.addRecord(lineT1Connect, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}

				Equipments.ConnectionInfo info2 = Equipments.getConnectionInfoInBusBreakerView(line.getTerminal2());
				b = info2.getConnectionBus();
				if (!Float.isNaN(b.getV())) {
					if (info2.isConnected()) {
						ConnectLineRecord lineT2Connect = ModelConverter.getModelicaRecord(line, b, modContext, _ddbManager, modelicaSim);
						this.addRecord(lineT2Connect, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}
		}
	}
	
	/**
	 * Export IIDM dangling connect to Modelica lines connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectDanglingLines(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((danglingLines.size() != 0) && (!danglingLines.isEmpty())) {
			_log.info("EXPORTING CONNECT DANGLING LINES");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting DANGLING LINES");
			for (DanglingLineRecord dline : danglingLines) {
				_log.info("\t Exporting dangling line connect " + dline.getDanglingLine().getId());
				Equipments.ConnectionInfo info1 = Equipments.getConnectionInfoInBusBreakerView(dline.getDanglingLine().getTerminal());
				Bus b = info1.getConnectionBus();
				if (!Float.isNaN(b.getV())) {
					if (info1.isConnected()) {
						ConnectLineRecord lineT1Connect = ModelConverter.getModelicaRecord(b, dline.getDanglingLine(), modContext, _ddbManager, modelicaSim);
						this.addRecord(lineT1Connect, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
				
				ConnectLineRecord lineT2Connect = ModelConverter.getModelicaRecord(dline.getDanglingLine(), dline.getDanglingBusName(), modContext, _ddbManager, modelicaSim);
				this.addRecord(lineT2Connect, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}


	/**
	 * Export IIDM coupling devices connect to Modelica coupling devices connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectCouplingDevices(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((connectCouplingList.size() != 0) && (!connectCouplingList.isEmpty())) {
			_log.info("EXPORTING CONNECT COUPLING DEVICES " + connectCouplingList.size());
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// COUPLING DEVICES");

			for (VoltageLevel voltageLevel : connectCouplingList) {
				for (Switch sw : voltageLevel.getBusBreakerView().getSwitches()) {
					_log.info("\t Exporting coupling device connect " + sw.getId());
					Bus bus1 = voltageLevel.getBusBreakerView().getBus1(sw.getId());
					Bus bus2 = voltageLevel.getBusBreakerView().getBus2(sw.getId());
					if (!Float.isNaN(bus1.getV()) && !Float.isNaN(bus2.getV())) {
						ConnectCouplingDevicesRecord couplingDeviceRecord = ModelConverter.getModelicaRecord(sw, bus1, bus2, modContext, _ddbManager, modelicaSim);
						this.addRecord(couplingDeviceRecord, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}
		}
	}

	/**
	 * Export IIDM loads connect to Modelica loads connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectLoads(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((connectLoadsList.size() != 0) && (!connectLoadsList.isEmpty())) {
			_log.info("EXPORTING CONNECT LOADS");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting LOADS");
			for (Load load : connectLoadsList) {
				_log.info("\t Exporting load connect " + load.getId());
				ConnectBusInfo busInfo = findBus(load.getTerminal(), load.getId());
				if (!Float.isNaN(busInfo.getBus().getV())) {
					if (busInfo.isConnected()) {
						ConnectRecord loadConnect = ModelConverter.getModelicaRecord(busInfo, load, modContext, _ddbManager, modelicaSim);
						this.addRecord(loadConnect, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}
		}
	}
	
	/**
	 * Export IIDM loads connect (for the dangling lines) to Modelica loads connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectDanglingLoads(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((danglingLines.size() != 0) && (!danglingLines.isEmpty())) {
			_log.info("EXPORTING CONNECT LOADS");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting LOADS");
			for (DanglingLineRecord dline : danglingLines) {
				_log.info("\t Exporting load connect " + dline.getDanglingLine().getId());
				
				ConnectRecord loadConnect = ModelConverter.getModelicaRecord(dline.getDanglingBusName(), dline.getDanglingLoadName(), modContext, _ddbManager, modelicaSim);
				this.addRecord(loadConnect, writerMo, modContext, _ddbManager, modelicaSim);
			}
		}
	}

	/**
	 * Export IIDM shunts connect to Modelica capacitors connect
	 */
	private void exportConnectCapacitors(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((connectCapacitorsList.size() != 0) && (!connectCapacitorsList.isEmpty())) {
			_log.info("EXPORTING CONNECT CAPACITORS");
			this.addRecord(writerMo, null);
			this.addRecord(writerMo, "// Connecting Capacitors");
			for (ShuntCompensator capacitor : connectCapacitorsList) {
				_log.info("\t Exporting capacitor connect " + capacitor.getId());
				ConnectBusInfo busInfo = findBus(capacitor.getTerminal(), capacitor.getId());
				if (!Float.isNaN(busInfo.getBus().getV())) {
					if (busInfo.isConnected()) {
						ConnectRecord capacitorConnect = ModelConverter.getModelicaRecord(busInfo, capacitor, modContext, _ddbManager, modelicaSim);
						this.addRecord(capacitorConnect, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}
		}
	}

	/**
	 * Export IIDM generators connect to Modelica generators connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectGenerators(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((genList.size() != 0) && (!genList.isEmpty())) {
			if ((generators.size() != 0) && (!generators.isEmpty())) {
				_log.info("EXPORTING CONNECT GENERATORS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// Connecting GENERATORS");
				for (Generator gen : generators) {
					_log.info("\t Exporting generator connect " + gen.getId());
					ConnectBusInfo busInfo = findBus(gen.getTerminal(), gen.getId());
					if (!Float.isNaN(busInfo.getBus().getV())) {
						if (busInfo.isConnected()) {
							ConnectRecord genConnect = ModelConverter.getModelicaRecord(busInfo, gen, modContext, _ddbManager, modelicaSim, false, this._sourceEngine);
							this.addRecord(genConnect, writerMo, modContext, _ddbManager, modelicaSim);
						}
					}
				}
			}

			if ((generatorsInyections.size() != 0) && (!generatorsInyections.isEmpty())) {
				_log.info("EXPORTING CONNECT GENERATORS AS FIXED INYECTIONS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// Connecting GENERATORS AS FIXED INYECTIONS");
				for (Generator gen : generatorsInyections) {
					_log.info("\t Exporting generator connect " + gen.getId());
					ConnectBusInfo busInfo = findBus(gen.getTerminal(), gen.getId());
					if (!Float.isNaN(busInfo.getBus().getV())) {
						if (busInfo.isConnected()) {
							ConnectRecord genConnect = ModelConverter.getModelicaRecord(busInfo, gen, modContext, _ddbManager, modelicaSim, true, this._sourceEngine);
							this.addRecord(genConnect, writerMo, modContext, _ddbManager, modelicaSim);
						}
					}
				}
			}
		}
	}

	/**
	 * Export IIDM transformers connect to Modelica transformers connect
	 * 
	 * @param writerMo
	 * @param modContext
	 * @param modelicaModelsList
	 * @param modelicaSim
	 * @throws IOException
	 */
	private void exportConnectTransformers(Writer writerMo, ModExportContext modContext, List<String> modelicaModelsList, SimulatorInst modelicaSim) throws IOException {
		if ((trafosList.size() != 0) && (!trafosList.isEmpty())) {
			if ((fixedTranformers.size() != 0) && (!fixedTranformers.isEmpty())) {
				_log.info("EXPORTING CONNECT FIXED TRANSFORMERS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// Connecting FIXED TRANSFORMERS");
				for (TwoWindingsTransformer trafo : fixedTranformers) {
					_log.info("\t Exporting fixed trafo connect " + trafo.getId());
					Equipments.ConnectionInfo trafoT1Info = Equipments.getConnectionInfoInBusBreakerView(trafo.getTerminal1());
					if (!Float.isNaN(trafoT1Info.getConnectionBus().getV()) && trafoT1Info.isConnected()) {
						ConnectFixedTransformerRecord connectFixedTrafoT1Record = (ConnectFixedTransformerRecord) ModelConverter.getModelicaRecord(trafoT1Info.getConnectionBus(), trafo, modContext, true, _ddbManager, modelicaSim);
						this.addRecord(connectFixedTrafoT1Record, writerMo, modContext, _ddbManager, modelicaSim);
					}

					Equipments.ConnectionInfo trafoT2Info = Equipments.getConnectionInfoInBusBreakerView(trafo.getTerminal2());
					if (!Float.isNaN(trafoT2Info.getConnectionBus().getV()) && trafoT2Info.isConnected()) {
						ConnectFixedTransformerRecord connectFixedTrafoT2Record = (ConnectFixedTransformerRecord) ModelConverter.getModelicaRecord(trafo, trafoT2Info.getConnectionBus(), modContext, true, _ddbManager, modelicaSim);
						this.addRecord(connectFixedTrafoT2Record, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}

			if ((detailedTranformers.size() != 0) && (!detailedTranformers.isEmpty())) {
				_log.info("EXPORTING CONNECT DETAILED TRANSFORMERS");
				this.addRecord(writerMo, null);
				this.addRecord(writerMo, "// Connecting DETAILED TRANSFORMERS");
				for (TwoWindingsTransformer trafo : detailedTranformers) {
					_log.info("\t Exporting detailed trafo connect " + trafo.getId());
					Equipments.ConnectionInfo trafoT1Info = Equipments.getConnectionInfoInBusBreakerView(trafo.getTerminal1());
					if (!Float.isNaN(trafoT1Info.getConnectionBus().getV()) && trafoT1Info.isConnected()) {
						ConnectDetailedTransformerRecord connectDetailedTrafoT1Record = (ConnectDetailedTransformerRecord) ModelConverter.getModelicaRecord(trafoT1Info.getConnectionBus(), trafo, modContext, false, _ddbManager, modelicaSim);
						this.addRecord(connectDetailedTrafoT1Record, writerMo, modContext, _ddbManager, modelicaSim);
					}

					Equipments.ConnectionInfo trafoT2Info = Equipments.getConnectionInfoInBusBreakerView(trafo.getTerminal2());
					if (!Float.isNaN(trafoT2Info.getConnectionBus().getV()) && trafoT2Info.isConnected()) {
						ConnectDetailedTransformerRecord connectDetailedTrafoT2Record = (ConnectDetailedTransformerRecord) ModelConverter.getModelicaRecord(trafo, trafoT2Info.getConnectionBus(), modContext, false, _ddbManager, modelicaSim);
						this.addRecord(connectDetailedTrafoT2Record, writerMo, modContext, _ddbManager, modelicaSim);
					}
				}
			}
		}
	}

	private ConnectBusInfo findBus(Terminal terminal, String context) {
		ConnectBusInfo busInfo;
		Bus bus = null;
		boolean connected = true;

		bus = terminal.getBusBreakerView().getBus();

		if (bus == null) {
			connected = false;
			bus = terminal.getBusBreakerView().getConnectableBus();

			if (bus == null) {
				throw new RuntimeException("Cannot find connection bus");
			}
		}

		busInfo = new ConnectBusInfo(bus, connected);

		return busInfo;
	}

	/**
	 * add new Modelica record
	 * 
	 * @param modRecord
	 * @throws Exception
	 */
	private void addRecord(ModelicaRecord modRecord, Writer writer, ModExportContext modContext, DDBManager ddbManager, SimulatorInst simulator) throws IOException {
		if (modRecord == null) {
			writer.append(StaticData.NEW_LINE);
			return;
		}
		modRecord.createRecord(modContext, ddbManager, simulator);
		writer.append(modRecord.toString());
		writer.append(StaticData.NEW_LINE);
	}

	private void addRecord(Writer writer, String data) throws IOException {
		if (data == null) {
			writer.append(StaticData.NEW_LINE);
			return;
		}
		writer.append(data);
		writer.append(StaticData.NEW_LINE);
	}

	private void numberOfElements() {
		// BUSES
		System.out.println("Buses = " + Identifiables.sort(_network.getBusBreakerView().getBuses()).size());

		// LINES
		System.out.println("Lines = " + Identifiables.sort(_network.getLines()).size());

		// TRANSFORMERS
		System.out.println("Trafos = " + Identifiables.sort(_network.getTwoWindingsTransformers()).size());

		List<TwoWindingsTransformer> fixedTranformers = new ArrayList<TwoWindingsTransformer>();
		List<TwoWindingsTransformer> detailedTranformers = new ArrayList<TwoWindingsTransformer>();
		for (TwoWindingsTransformer trafo : Identifiables.sort(_network.getTwoWindingsTransformers())) {
			if ((trafo.getRatioTapChanger() == null) && (trafo.getPhaseTapChanger() == null)) {
				fixedTranformers.add(trafo);
			} else {
				detailedTranformers.add(trafo);
			}
		}
		System.out.println("Fixed Trafos = " + fixedTranformers.size());
		System.out.println("Detailed Trafos = " + detailedTranformers.size());

		// LOADS
		System.out.println("Loads = " + Identifiables.sort(_network.getLoads()).size());

		// SHUNTS
		System.out.println("Shutns = " + Identifiables.sort(_network.getShunts()).size());

		// MACHINES
		System.out.println("Machines = " + Identifiables.sort(_network.getGenerators()).size());

		int numMachines = Identifiables.sort(_network.getGenerators()).size();
		int numGens = _ddbManager.findEquipmentAllCount();
		int numFixInyec = numMachines - numGens;

		System.out.println("Generators = " + numGens);
		System.out.println("Fixed inyections = " + numFixInyec);
		System.out.println("Regulators = " + _ddbManager.findInternalsAllCount());
	}

	private void countIIDMElements(String moFile) {
		int count = 0;
		String listOfElemName = moFile + "Elem.csv";
		FileWriter elements;
		try {
			elements = new FileWriter(listOfElemName);
			elements.append("Id;Name");
			elements.append(StaticData.NEW_LINE);
			elements.append("BUSES");
			elements.append(StaticData.NEW_LINE);

			for (Bus bus : Identifiables.sort(_network.getBusBreakerView().getBuses())) {
				elements.append(bus.getId() + ";" + bus.getName());
				elements.append(StaticData.NEW_LINE);
				count++;
			}
			_log.info("Buses = " + count);
			count = 0;

			elements.append("LINES");
			elements.append(StaticData.NEW_LINE);

			_log.info("Lines = " + count);
			count = 0;

			elements.append("TRAFOS");
			elements.append(StaticData.NEW_LINE);
			List<TwoWindingsTransformer> fixedTranformers = new ArrayList<TwoWindingsTransformer>();
			List<TwoWindingsTransformer> detailedTranformers = new ArrayList<TwoWindingsTransformer>();
			for (TwoWindingsTransformer trafo : Identifiables.sort(_network.getTwoWindingsTransformers())) {
				if ((trafo.getRatioTapChanger() == null) && (trafo.getPhaseTapChanger() == null)) {
					fixedTranformers.add(trafo);
				} else {
					detailedTranformers.add(trafo);
				}
				// elements.append(trafo.getId() + ";" + trafo.getName());
				elements.append(trafo.getTerminal1().getBusBreakerView().getBus().getId() + ";" + trafo.getTerminal2().getBusBreakerView().getBus().getId());
				elements.append(StaticData.NEW_LINE);
				count++;
			}

			_log.info("Trafos = " + count);
			_log.info("Fixed Trafos = " + fixedTranformers.size());
			_log.info("Detailed Trafos = " + detailedTranformers.size());
			count = 0;

			elements.append("LOAD");
			elements.append(StaticData.NEW_LINE);
			for (Load load : Identifiables.sort(_network.getLoads())) {
				// _log.info(load.getId());
				elements.append(load.getId() + ";" + load.getName());
				elements.append(StaticData.NEW_LINE);
				count++;
			}
			_log.info("Loads = " + count);
			count = 0;

			elements.append("SHUNTS");
			elements.append(StaticData.NEW_LINE);
			for (ShuntCompensator shunt : Identifiables.sort(_network.getShunts())) {
				elements.append(shunt.getId() + ";" + shunt.getName());
				elements.append(StaticData.NEW_LINE);
				count++;
			}
			_log.info("Shunts = " + count);

			elements.append("GENERATOR");
			elements.append(StaticData.NEW_LINE);
			count = 0;
			int countEq = 0;
			for (Generator gen : Identifiables.sort(_network.getGenerators())) {
				elements.append(gen.getId() + ";" + gen.getName());
				elements.append(StaticData.NEW_LINE);
				count++;
				Equipment eq = _ddbManager.findEquipment(gen.getId().substring(1));
				if (eq != null) {
					countEq++;
					// TODO Si existiera un schema distinto por simulador, el
					// parametro NULL debera cambiarse.
					ConnectionSchema connectionSchema = _ddbManager.findConnectionSchema(eq.getCimId(), null);

					if (connectionSchema != null) {
						elements.append("REGULATORS");
						elements.append(StaticData.NEW_LINE);
						List<Connection> connections = connectionSchema.getConnections();
						if ((connections != null) && (!connections.isEmpty())) {
							for (Connection con : connectionSchema.getConnections()) {
								if (con.getId1Type() == 1) {
									elements.append("\t" + con.getId1());
									elements.append(StaticData.NEW_LINE);
								}
								if (con.getId2Type() == 1) {
									elements.append("\t" + con.getId2());
									elements.append(StaticData.NEW_LINE);
								}
							}
						}
					}
				}
			}
			_log.info("Gens = " + count);
			_log.info("Machines = " + countEq);
			_log.info("Inyections = " + (count - countEq));
			count = 0;
			elements.close();
		} catch (IOException e) {
			e.printStackTrace();
			_log.error("Error counting elements.");
		}
	}

	private final Network _network;
	private final SourceEngine _sourceEngine;
	private DDBManager _ddbManager;
	private JavaOMCAPI omc;
	private Path tmpDir;
	private final ModelicaDictionary dictionary;
	private File modelicaLibFile = null;
	private Map<String, Map<String, String>> paramsDictionary; // Map<MOD_Model
																// ,Map<MOD_Name,
																// EUR_Name>>
	private float SNREF;
	private boolean includeconstant = false;

	private Collection<Bus> busesList;
	private Collection<Line> linesList;
	private Collection<DanglingLine> dangLinesList;
	private Collection<TwoWindingsTransformer> trafosList;
	private Collection<Load> loadsList;
	private Collection<ShuntCompensator> shuntsList;
	private Collection<Generator> genList;
	private Collection<Line> connectLinesList;
	private Collection<DanglingLine> connectDanglingLinesList;
	private Collection<VoltageLevel> connectCouplingList;
	private Collection<Load> connectLoadsList;
	private Collection<ShuntCompensator> connectCapacitorsList;

	private List<BusRecord> danglingBuses = new ArrayList<BusRecord>();
	private List<LoadRecord> danglingLoads = new ArrayList<LoadRecord>();
	private List<DanglingLineRecord> danglingLines = new ArrayList<DanglingLineRecord>();
	private List<TwoWindingsTransformer> fixedTranformers = new ArrayList<TwoWindingsTransformer>();
	private List<TwoWindingsTransformer> detailedTranformers = new ArrayList<TwoWindingsTransformer>();
	private List<Generator> generators = new ArrayList<Generator>();
	private List<Generator> generatorsInyections = new ArrayList<Generator>();
	private List<RegulatorRecord> regulatorsList = new ArrayList<RegulatorRecord>();
	private List<ConnectRegulatorRecord> connectRegList = new ArrayList<ConnectRegulatorRecord>();
	private List<Connect2RegulatorsRecord> connect2RegsList = new ArrayList<Connect2RegulatorsRecord>();
	private List<Connect2GeneratorsRecord> connect2GensList = new ArrayList<Connect2GeneratorsRecord>();
	private List<ConnectConstantRecord> connectConstList = new ArrayList<ConnectConstantRecord>();

	private static final Logger _log = LoggerFactory.getLogger(ModelicaExport.class);
}
