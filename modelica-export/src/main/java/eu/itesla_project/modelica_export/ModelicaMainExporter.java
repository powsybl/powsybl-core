/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modelica_export;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtx;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modelica_export.util.EurostagEngine;
import eu.itesla_project.modelica_export.util.PsseEngine;
import eu.itesla_project.modelica_export.util.SourceEngine;
import eu.itesla_project.modelica_export.util.StaticData;
import eu.itesla_project.loadflow.api.LoadFlowResult;
//import eu.itesla_project.helmflow.HELMLoadFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Silvia Machado <machados@aia.es>
 */
public class ModelicaMainExporter {
	
	private static final boolean READ_SOLUTION_FROM_FILE = false;
	public static final boolean RATIOS_TO_1 = false;

	private static final String FILENAME_LOADFLOW_SOLUTION_VOLTAGES = "loadflow_solution_v.csv";
	// Fields: iidm Id, external Id, voltage (pu), angle(deg), angle(rad)
	// angle(deg) is used,  angle(rad) is checked to be consistent with angle(deg)
	private static final String FILENAME_LOADFLOW_SOLUTION_Q_GENERATORS = "loadflow_solution_q.csv";
	// Fields: external Id, Q (MVAr)
	// carefully dealing with sign of Q 
	private static final String FILENAME_LOADFLOW_SOLUTION_ID_MAPPINGS = "mapping.csv";
	// Fields: external Id, iidm Id
	public ModelicaMainExporter(Network network, String slackId, String jbossHost, String jbossPort, String jbossUser, String jbossPassword, String modelicaVersion, String sourceEngine, String sourceVersion, Path modelicaLibtPath, LoadFlowFactory loadFlowFactory) {
		this._network = network;
		this._slackId = slackId;
		this._jbossHost = jbossHost;
		this._jbossPort = jbossPort;
		this._jbossUser = jbossUser;
		this._jbossPassword = jbossPassword;
		this._modelicaVersion = modelicaVersion;
		this._modelicaLibPath = modelicaLibtPath;
		this.loadFlowFactory = loadFlowFactory;

		if(sourceEngine.toLowerCase().compareTo(StaticData.EUROSTAG) == 0) {
			this._sourceEngine = new EurostagEngine(sourceEngine, sourceVersion);
		} else if (sourceEngine.toLowerCase().compareTo(StaticData.PSSE) == 0) {
			this._sourceEngine = new PsseEngine(sourceEngine, sourceVersion);
		}
		else {
			_log.error("This source engine doesn't exist.");
		}
		_log.info("Modelica main exporter created, slackId = " + this._slackId);
		
		paramsDictionary = this._sourceEngine.createGenParamsDictionary();
	}
	
	public ModelicaMainExporter(Network network, String slackId, Path modelicaLibtPath, String sourceEngine, String sourceVersion, LoadFlowFactory loadFlowFactory) {
		this._network = network;
		this._slackId = slackId;
		this._jbossHost = DEFAULT_HOST;
		this._jbossPort = DEFAULT_PORT;
		this._jbossUser = DEFAULT_USER;
		this._jbossPassword = DEFAULT_PASSWORD;
		this._modelicaVersion = DEFAULT_MODELICA_VERSION;
		this._modelicaLibPath = modelicaLibtPath;
		this.loadFlowFactory = loadFlowFactory;

		if(sourceEngine.toLowerCase().compareTo(StaticData.EUROSTAG) == 0) {
			this._sourceEngine = new EurostagEngine(sourceEngine, sourceVersion);
		} else if (sourceEngine.toLowerCase().compareTo(StaticData.PSSE) == 0) {
			this._sourceEngine = new PsseEngine(sourceEngine, sourceVersion);
		}
		else {
			_log.error("This source engine doesn't exist.");
		}
	}
	
	public void export(Path outputParentDir) {
		_log.info("Exporting model from " + this._sourceEngine.getName() + ", version " + this._sourceEngine.getVersion() + "...");
		long initTime = System.currentTimeMillis();
		try  (EjbClientCtx ctx=newEjbClientEcx(); ComputationManager _computationManager = new LocalComputationManager()) {
			long endEjb = System.currentTimeMillis();
			_log.debug("Connexion EJB = " + (endEjb - initTime));

			//Executing LF
			long endStep = System.currentTimeMillis();
			_log.debug("ModelicaMainExporter. StepUpTrafos = " + (endStep-endEjb));
			// Even if we are reading a different solution from a file, run a Loadflow 
			// (it seems this allows to setup properly disconnected elements)
			runLoadFlow(_computationManager);
			if (READ_SOLUTION_FROM_FILE) readSolutionFromFile();

			long endLF = System.currentTimeMillis();
			_log.debug("ModelicaMainExporter. HELM LF (ms) = " + (endLF-endStep));
			
			//Exporting model
            DDBManager ddbmanager = ctx.connectEjb(DDBMANAGERJNDINAME);

			ModelicaExport export = null;
			
			if(this._sourceEngine instanceof EurostagEngine) {
				//To have the same representation/results between HELM and PSS/E the sign of P and Q in generators have been changed
				//Moreover, for Eurostag the sign must be the same in IIDM and HELM and as it has been changed in the HELM integration
				//i should be changed after this in the IIDM
				for(Generator gen : _network.getGenerators()) {
					float P = -gen.getTerminal().getP();
					float Q = -gen.getTerminal().getQ();
					
					gen.getTerminal().setP(P);
					gen.getTerminal().setQ(Q);
				}
				
				export = new ModelicaExport(_network, ddbmanager, paramsDictionary, this._modelicaLibPath.toFile(), _sourceEngine);
			}
			else if(this._sourceEngine instanceof PsseEngine) {
				export = new ModelicaExport(_network, ddbmanager, paramsDictionary, _sourceEngine);
			}
			else {
				_log.error("The source engine must be eurostaqg or psse.");
				System.exit(-1);
			}
			long preWrite = System.currentTimeMillis();
			//export.WriteMo("log/" + _network.getName(), _modelicaVersion);
			export.WriteMo(outputParentDir.resolve(_network.getName()).toAbsolutePath().toString(), _modelicaVersion);
			_log.info("Writer time = " + (System.currentTimeMillis() - preWrite));
			ctx.close();
			long endExport = System.currentTimeMillis();
			_log.debug("ModelicaMainExporter. Export (ms) = " + (endExport-endLF));
			
			long duration = endExport - initTime;
			
			_log.debug("ModelicaMainExporter. Duration: " + (duration));
			_log.info("Conversion finished.");
			System.exit(0);
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
		}
	}

	private void runLoadFlow(ComputationManager _computationManager) throws Exception {
		int priority = 1;
		LoadFlow loadflow = loadFlowFactory.create(_network, _computationManager, priority);
		//((HELMLoadFlow) loadflow).setSlack(this._slackId);
		LoadFlowResult lfResults = loadflow.run();

		if(!lfResults.isOk()) {
			System.out.println("LF has not been successfuly completed.");
			_log.info("Loadflow finished. isOk == false");
			System.exit(-1);
		}
	}

	class Voltage {
		String iidmId;
		String externalId;
		float vpu;
		float arad;
		float adeg;
		boolean used;
	}
	
	class Reactive {
		String externalId;
		float q;
		boolean used;
	}

	interface RecordHandler {
		public void processRecord(List<String> fields);
	}
	
	class CsvReader {
		void read(String filename, RecordHandler recordHandler) {
			BufferedReader reader;
			String line;
			try {
				reader = new BufferedReader(new FileReader(filename));
				line = reader.readLine();
				while((line = reader.readLine()) != null) {
					List<String> fields = new ArrayList<String>(Arrays.asList(line.split(",")));
					recordHandler.processRecord(fields);
				}
			} catch (IOException e) {
				_log.error(e.getMessage(), e);
			}
		}
	}
	
	private void readSolutionFromFile() {
		CsvReader csv = new CsvReader();
		HashMap<String,String> ids = readIdMappings(csv, FILENAME_LOADFLOW_SOLUTION_ID_MAPPINGS);
		HashMap<String,Voltage> voltages = readSolutionVoltagesFromFile(csv, FILENAME_LOADFLOW_SOLUTION_VOLTAGES);
		HashMap<String,Reactive> reactives = readSolutionReactivesFromFile(csv, FILENAME_LOADFLOW_SOLUTION_Q_GENERATORS);

		// Check mappings against read mappings in voltages file
		for (Voltage voltage : voltages.values()) {
			String properExternalId = ids.get(voltage.iidmId);
			if (properExternalId == null) {
				_log.error("IIDM id not found in proper mappings, id = [" + voltage.iidmId + "]");
				continue;
			}
			if (!properExternalId.equals(voltage.externalId)) {
				_log.error("External id from proper mappings is not the same as voltage external Id, iidm id [" + voltage.iidmId + "], proper external Id = [" + properExternalId + "], voltage id = [" + voltage.externalId + "]");
				continue;
			}
			_log.info("Proper IIDM mapping for bus [" + voltage.iidmId + "], proper external Id = [" + properExternalId + "], voltage id = [" + voltage.externalId + "]");
		}
		for (Bus bus : _network.getBusBreakerView().getBuses()) {
			Voltage voltage = voltages.get(bus.getId());
			if (voltage == null) {
				_log.error("IIDM Bus not found in solution file, id = [" + bus.getId() + "]");
				continue;
			}
			float v = voltage.vpu;
			if (v == 1.0 && voltage.adeg == 0.0) {
				_log.warn("Ignore setting Voltage V=1.0 and A=0.0 (bus is disconnected, no voltage computed)");
				continue;
			}
			bus.setV(v * bus.getVoltageLevel().getNominalV());
			bus.setAngle(voltage.adeg);
			voltage.used = true;
		}
		for (Generator gen : _network.getGenerators()) {
			String externalId = ids.get(gen.getId());
			if (externalId == null) {
				_log.error("IIDM generator id not found in mapping to external ids, iidm id = [" + gen.getId() + "]");
				continue;
			}
			Reactive reactive = reactives.get(externalId);
			if (reactive == null) {
				_log.error("Reactive not found for external id, id = [" + externalId + "]");
				continue;
			}
			gen.getTerminal().setQ(-reactive.q);
			reactive.used = true;
		}
		for (Voltage voltage : voltages.values()) {
			if (!voltage.used) {
				_log.warn("Voltage from file not used, bus Id = [" + voltage.iidmId + "], num = [" + voltage.externalId + "], V = " + voltage.vpu);
			}
		}
		for (Reactive reactive : reactives.values()) {
			if (!reactive.used) {
				_log.warn("Reactive from file not used, gen Id = [" + reactive.externalId + "]");
			}
		}
	}
	private HashMap<String,String> readIdMappings(CsvReader reader, String filename) {
		_log.info("Reading mapping of identifiers from file [" + filename + "]");
		HashMap<String,String> mappings = new HashMap(100);
		reader.read(filename, new RecordHandler() {
			public void processRecord(List<String> fields) {
				String externalId = fields.get(0);
				String iidmId = fields.get(1);
				mappings.put(iidmId, externalId);
				_log.info("    " + iidmId + ", " + externalId);
			}
		});
		return mappings;
	}
	
	private HashMap<String,Voltage> readSolutionVoltagesFromFile(CsvReader reader, String filename) {
		_log.info("Reading solution voltages from file [" + filename + "]");
		HashMap<String,Voltage> voltages = new HashMap(1000);
		reader.read(filename, new RecordHandler() {
			public void processRecord(List<String> fields) {
				Voltage voltage = new Voltage();
				voltage.iidmId = fields.get(0);
				voltage.externalId = fields.get(1);
				voltage.vpu = Float.parseFloat(fields.get(2));
				voltage.adeg = Float.parseFloat(fields.get(3));
				voltage.arad = Float.parseFloat(fields.get(4));
				voltage.used = false;
				float adegrad = voltage.arad * (float) (180.0 / Math.PI);
				if (Math.abs(voltage.adeg - adegrad) > 1e-3) {
					_log.error("Error in angle at bus [" + voltage.iidmId + "], deg = " + voltage.adeg + ", deg from rad = " + adegrad);
				}
				voltages.put(voltage.iidmId, voltage);
				_log.info("    " + voltage.iidmId + ", " + voltage.externalId + ", " + voltage.vpu);
			}
		});
		return voltages;
	}

	private HashMap<String,Reactive> readSolutionReactivesFromFile(CsvReader reader, String filename) {
		_log.info("Reading solution reactives from file [" + filename + "]");
		HashMap<String,Reactive> reactives = new HashMap(100);
		reader.read(filename, new RecordHandler() {
			public void processRecord(List<String> fields) {
				Reactive reactive = new Reactive();
				reactive.externalId = fields.get(0);
				reactive.q = Float.parseFloat(fields.get(1));
				reactive.used = false;
				reactives.put(reactive.externalId, reactive);
				_log.info("    " + reactive.externalId + ", " + reactive.q);
			}
		});
		return reactives;
	}
    public EjbClientCtx newEjbClientEcx() throws NamingException{
        return new  EjbClientCtx(_jbossHost, _jbossPort, _jbossUser,_jbossPassword);
    }
    
	private static void writeNetworkData(Network network, boolean isOld) {
		String text = null;
		String filename = null;
		if(isOld) text = "Old";
		else text = "New";
		filename = network.getName() + "_"  + text;
		
		_log.info(String.format("NETWORK %s", filename));
		_log.info("GENERATORS");
//		_log.info("#        Name          Q (MVAR) ");
		_log.info("#------------  ---------------- ");
		_log.info("#        Name          Q (MVAR)         TargetQ          P (MVAR)         TargetP");
		_log.info("#------------  ----------------  --------------  ----------------  --------------");
		for(Generator gen : network.getGenerators())
		{
			_log.info(String.format("%s %15.8f %15.8f %15.8f %15.8f", gen.getId(), gen.getTerminal().getQ(), gen.getTargetQ(), gen.getTerminal().getP(), gen.getTargetP()));
		}
		_log.info("END GENERATORS\n");

		_log.info("BUSES");
		_log.info("#        Name          V (kV) Angle (Degrees) ");
		_log.info("#------------ --------------- --------------- ");
		for(Bus bus : network.getBusBreakerView().getBuses())
		{
			_log.info(String.format("%s %15.8f %15.8f", bus.getId(), bus.getV(), bus.getAngle()));
		}
		_log.info("END BUSES\n");
		
		_log.info("BRANCHES");
		_log.info("#                                                Q (MVAR)                          P (MW) ");
		_log.info("#                         ------------------------------- ------------------------------- ");
		_log.info("#                    Name            From              To            From              To ");
		_log.info("#------------------------ --------------- --------------- --------------- --------------- ");
		for(Line line : network.getLines())
		{
			_log.info(String.format("%s %15.8f %15.8f %15.8f %15.8f", line.getId(), line.getTerminal1().getQ(), line.getTerminal2().getQ(), line.getTerminal1().getP(), line.getTerminal2().getP()));
		}
		_log.info("END BRANCHES\n");
		
		_log.info("TRANSFORMERS");
		_log.info("#                                                Q (MVAR)                         P (MW) ");
		_log.info("#                         ------------------------------- ------------------------------- ");
		_log.info("#                    Name            From              To            From              To ");
		_log.info("#------------------------ --------------- --------------- --------------- --------------- ");
		for(TwoTerminalsConnectable trafo : network.getTwoWindingsTransformers())
		{
			_log.info(String.format("%s %15.8f %15.8f %15.8f %15.8f", trafo.getId(), trafo.getTerminal1().getQ(), trafo.getTerminal2().getQ(), trafo.getTerminal1().getP(), trafo.getTerminal2().getP()));
		}
		_log.info("END TRANSFORMERS\n");
		_log.info(String.format("END NETWORK %s", filename));
	}
	
	
	//FOR LF
	//private static ComputationManager			_computationManager;
	
	private Network			_network;
	private String          _slackId;
	private String			_jbossHost;
	private String			_jbossPort;
	private String			_jbossUser;
	private String			_jbossPassword;
	private String			_modelicaVersion;
	private Path			_modelicaLibPath;
	private final LoadFlowFactory loadFlowFactory;

	private SourceEngine	_sourceEngine;
	
	private Map<String, Map<String, String>> paramsDictionary; //Map<MOD_Model ,Map<MOD_Name, EUR_Name>>
	
	private final String	DEFAULT_HOST				= "127.0.0.1";
	private final String 	DEFAULT_USER				= "user";
	private final String 	DEFAULT_PASSWORD			= "password";
	private final String 	DEFAULT_PORT				= "8080";
	private final String 	DDBMANAGERJNDINAME			= "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";
	private final String 	DEFAULT_MODELICA_VERSION	= "3.2";
//	private final String 	DEFAULT_EUROSTAG_VERSION	= "5.1.1";
	
	private static final Logger _log					= LoggerFactory.getLogger(ModelicaMainExporter.class);
}

