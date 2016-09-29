/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag_imp_exp;

import com.google.common.collect.Sets;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.utils.Utils;
import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.model.Equipment;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtx;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Identifiables;
import itesla.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DdbDtaImpExp implements DynamicDatabaseClient {
	private static final String PAR_MACROBLOCK__NAME = "macroblock.name";

	private static final String MTC_PREFIX_NAME = "MTC_";

	//DDBManager jndi name
	private static final String DDBMANAGERJNDINAME = "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";
	private static final String DICT_GENS_CSV = "dict_gens.csv";
	private static final String SKIPPED_GENS_CSV = "skipped_gens.csv";

	static Logger log = LoggerFactory.getLogger(DdbDtaImpExp.class);


	String estg[][]=new String[][] {
			{"M1U", "Unsaturated generator defined by its internal parameters - full model",},
			{"M1DU", "Unsaturated generator defined by its internal parameters - full model - type Fortescue"},
			{"M2U", "Unsaturated generator defined by its external parameters - full model"},
			{"M2DU", "Unsaturated generator defined by its external parameters - full model - type Fortescue"},
			{"M1S", "Saturated generator defined by its internal parameters - full model"},
			{"M1DS", "Saturated generator defined by its internal parameters - full model - type Fortescue"},
			{"M2S", "Saturated generator defined by its external parameters - full model"},
			{"M2DS", "Saturated generator defined by its external parameters - full model - type Fortescue"},
			//			  {"M5 U", "Unsaturated generator defined by its internal parameters - simplified model"},
			//			  {"M5DU", "Unsaturated generator defined by its internal parameters - simplified model - type Fortescue"},
			//			  {"M6 U", "Unsaturated generator defined by its external parameters - simplified model"},
			//			  {"M6DU", "Unsaturated generator defined by its external parameters - simplified model - type Fortescue"},
			//			  {"M5 S", "Saturated generator defined by its internal parameters - simplified model"},
			//			  {"M5DS", "Saturated generator defined by its internal parameters - simplified model - type Fortescue"},
			//			  {"M6 S", "Saturated generator defined by its external parameters - simplified model"},
			//			  {"M6DS", "Saturated generator defined by its external parameters - simplified model - type Fortescue"},
			//			  {"M10",  "Induction motor - full model"},
			//			  {"M10D", "Induction motor - full model - type Fortescue"},
			//			  {"M11",  "Induction motor - simplified model"},
			//			  {"M11D", "Induction motor - simplified model - type Fortescue"},
			//			  {"M13",  "Induction motor - full model with torque by macrolanguage"},
			//			  {"M13D", "Induction motor - full model with torque by macrolanguage - type Fortescue"},
			//			  {"M14",  "Induction motor - simplified model with torque by macrolanguage"},
			//			  {"M14D", "Induction motor - simplified model with torque by macrolanguage - type Fortescue"},
			//			  {"M15",  "Double Fed Induction machines"},
			//			  {"M15D", "Double Fed Induction machines - type Fortescue"},
			//			  {"M20",  "PLOAD-QLOAD injector"},
			//			  {"M20D", "PLOAD-QLOAD injector - type Fortescue"},
			{"M21",  "B-G injector"},
			//			  {"M21D", "B-G injector - type Fortescue"},
			//			  {"M22",  "I-PHI injector"},
			//			  {"M22D", "I-PHI injector - type Fortescue"},
			//			  {"M23",  "IR-II injector"},
			//			  {"M23D", "IR-II injector - type Fortescue"},
			//			  {"M50",  "Converter"},
			//			  {"M50D", "Converter - type Fortescue"},
			{"MA",  "Macro-Automaton"},
			////          {"R",    "Macroblock"}
	};

	public String eurostagVersion="5.1.1";
	private final DdbConfig config;
	private final DdExportConfig configExport;
	private String jndiName;
	private Network network;
	private Map<String, Character> parallelIndexes;
	private boolean updateFlag=false;

	Hashtable<String, Set<String>> macroblocksPinNames= new Hashtable<>();
	Hashtable<String, List<String>> EquipmentsInternalsMap= new Hashtable<>();
	Hashtable<String, String> equipmentsTypeMap= new Hashtable<>();
	//structure tracking regs names
	Set<String> uniqueRegNamesSet=new TreeSet<>();
	//structure tracking RST zones
	HashMap<String, ArrayList<String>> geneRst = new HashMap<String, ArrayList<String>>();
	HashMap<String, String> pilotPointRst = new HashMap<String, String>();
	HashMap<String, String> pilotGeneratorsRst = new HashMap<String, String>();
	//structure tracking ACMC equipments
	HashMap<String, ArrayList<String>> acmcs = new HashMap<String, ArrayList<String>>();

	public DdbConfig getConfig() {
		return config;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public void setUpdateFlag(boolean updateFlag) {
		this.updateFlag = updateFlag;
	}

	@Override
	public String getName() {
		return "Quinary DDB";
	}

	@Override
	public String getVersion() {
		return null;
	}

	public EjbClientCtx newEjbClientEcx() throws NamingException{
		return new  EjbClientCtx(config.getJbossHost(), Integer.parseInt(config.getJbossPort()), config.getJbossUser(), config.getJbossPassword());
	}

	public DdbDtaImpExp(DdbConfig config, String jndiName) {
		this.config = config;
		this.jndiName = jndiName;
		this.configExport = DdExportConfig.load();
	}

	public DdbDtaImpExp(DdbConfig config) {
		this(config, DDBMANAGERJNDINAME);
	}

	public DdbDtaImpExp() {
		this(new DdbConfig(), DDBMANAGERJNDINAME);
	}


	/* ********************************
	 * Import Functions
	 ********************************* */
	public void loadEurostagData(Path dtaFile, Path mappingFile, String eurostagVersion, Path regsFolder) {
		try {
			log.info("Reading mapping cimid-eurostagid file: " + mappingFile.toAbsolutePath());
			Map<String, String> mapping = readWithCsvMapReader(mappingFile);
			log.info("Loading data from: " + dtaFile.toAbsolutePath() +", Eurostag version: " + eurostagVersion + ", regulators directory: " + regsFolder.toAbsolutePath());
			loadEurostagData(dtaFile, mapping, eurostagVersion, regsFolder);
		} catch (Throwable e) {
			log.error(e.getMessage(),e);
		}
	}
	public void loadEurostagData(Path dtaFile, Map<String, String> dicoMap, String eurostagVersion, Path regsFolder) {
		try  (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
			SimulatorInst eurostagSim=getOrCreateEurostagSimulatorInst(ddbmanager, eurostagVersion);

			//make sure eurostag models catalog is loaded
			loadEurostagTemplatesCatalog(ddbmanager);

			final Map<String,Path> regsMapping=buildRegsFilesMapping(regsFolder);

			if (!Files.exists(dtaFile)) {
				log.error(dtaFile +" does not exist");
			} else {

				if (!Files.isDirectory(dtaFile)) {
					// process single file
					try {
						feedDDBWithEurostagData(dtaFile, dicoMap, eurostagSim , ddbmanager,regsMapping);
					} catch (Throwable t) {
						log.error(t.getMessage()+ "; file " + dtaFile, t);
					}
				} else {
					// process a directory, recursively
					final Map<String, String> dicoMapF=dicoMap;
					final SimulatorInst eurostagSimF = eurostagSim;
					final DDBManager ddbmanagerF=ddbmanager;

					Files.walkFileTree(dtaFile,
							new SimpleFileVisitor<Path> (){
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
							if (attr.isRegularFile()) {
								String aFileName=file.toString();
								if ((aFileName.endsWith(".dd")) || (aFileName.endsWith(".dta"))) {
									try {
										feedDDBWithEurostagData(file, dicoMapF, eurostagSimF , ddbmanagerF,regsMapping);
									} catch (Throwable t) {
										log.error(t.getMessage()+ "; file " + file, t);
									}
								} else {
									log.warn("file " + file +" not recognized (not .dd, nor .dta): skipped!");
								}
							} else {
								log.warn("file %s not a regular file: skipped!", file);
							}
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							return FileVisitResult.CONTINUE;
						};
						// If this method is not overridden and an error occurs, an IOException is thrown.
						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) {
							log.error(exc.getMessage(),exc);
							return FileVisitResult.CONTINUE;
						}
					});
				}
			}

			processConnections(ddbmanager);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		} 

	}
	//load ddb with eurostag templates
	public void loadEurostagTemplatesCatalog(DDBManager ddbmanager) {
		try {
			SimulatorInst eurostagSim=getOrCreateEurostagSimulatorInst(ddbmanager, eurostagVersion);
			feedDDBWithEurostagModelTemplates(estg, eurostagSim, ddbmanager);
		} catch (Throwable e) {
			log.error(e.getMessage());
		} finally {
		}
	}
	private void feedDDBWithEurostagModelTemplates(
			String keyNamesAndDescriptions[][],
			SimulatorInst eurostagSimulator, DDBManager ddbmanager) {
		ModelTemplateContainer mtc = null;
		ModelTemplate mt = null;
		String eurostagKey = "";
		String eurostagComment = "";
		String ddbId = "";

		for (int i = 0; i < keyNamesAndDescriptions.length; i++) {
			eurostagKey = keyNamesAndDescriptions[i][0];
			eurostagComment = keyNamesAndDescriptions[i][1];
			ddbId = MTC_PREFIX_NAME + eurostagKey;
			mtc = ddbmanager.findModelTemplateContainer(ddbId);
			if (mtc == null) {
				log.debug("-- creating Model Template Container" + ddbId);
				mtc = new ModelTemplateContainer(ddbId, "");
				mt = new ModelTemplate(eurostagSimulator, eurostagKey,
						eurostagComment);
				// must be decided what to put in data attribute; here we put
				// again the typename, as in the original example circulated
				mt.setData("data", Utils.stringAsByteArrayUTF8(eurostagKey));
				mtc.getModelTemplates().add(mt);
				mtc = ddbmanager.save(mtc);
			} else {
				log.debug("-- Modeltemplate container " + ddbId
						+ " already defined, id: " + mtc.getId());
			}
		}
	}
	public void feedDDBWithEurostagData(Path inputFile,
			Map<String, String> dicoMap, SimulatorInst eurostagSim,
			DDBManager ddbmanager, Map<String,Path> regsMapping) throws Exception {
		log.info("Processing file: " + inputFile.toAbsolutePath());
		// first parse the .dta file
		ArrayList<EurostagRecord> retZones = DtaParser.parseZones(inputFile);
		// second, populate DDB with parsed data
		for (EurostagRecord zone : retZones) {
			log.info(" processing:  {} ", zone);
			String dbId = "";
			switch (zone.getKeyName()) {
			case "M1U":
			case "M1DU":
			case "M1S":
			case "M1DS":
			case "M2U":
			case "M2DU":
			case "M2S":
			case "M2DS":
				dbId = populateDDB_M1M2(zone, dicoMap, ddbmanager, eurostagSim);
				break;
			case "R":
				dbId = populateDDB_R(zone, dicoMap, ddbmanager, eurostagSim, regsMapping);
				break;
			default:
				dbId = populateDDB_Default(zone, dicoMap, ddbmanager, eurostagSim);
				break;
			}
			log.trace(dbId);
		}
	}
	public String populateDDB_M1M2(EurostagRecord zone, Map<String, String> amap,
			DDBManager ddbmanager, SimulatorInst eurostagSim) {
		// here we assume that the simulator with the required version already
		// exists
		if (eurostagSim == null) {
			throw new RuntimeException("Eurostag simulator could not be null");
		}

		if (!(("M1".equals(zone.getTypeName())) || ("M2".equals(zone.getTypeName())))) {
			throw new RuntimeException("not expected type (not M1 nor M2): " + zone);
		}

		log.debug("-Creating DDB component {} ", zone.getTypeName());
		String mName = (String) zone.getData().get("machine.name");
		String cimId = amap.get(mName);
		if ((cimId == null) || ("".equals(cimId))) {
			throw new RuntimeException(mName + ": cimId not found in mapping.");
		}

		if (!equipmentsTypeMap.contains(cimId)){
			equipmentsTypeMap.put(cimId, zone.getTypeName());
		}

		Equipment eq1 = ddbmanager.findEquipment(cimId);
		if ((eq1!=null) && (updateFlag==true)) {
			Set<String> connectedInternals=getConnectedInternals(cimId,ddbmanager);

			//remove this equipment graph
			log.info("- removing existing equipment {}", cimId);
			removeEquipment(cimId,ddbmanager);
			eq1=null;

			for (String internalId: connectedInternals) {
				log.info("- removing existing connected internal {}", internalId);
				removeInternal(internalId,ddbmanager);
			}

		}

		String mtc_ddbid = MTC_PREFIX_NAME + zone.getKeyName();
		ModelTemplateContainer mtc1 = ddbmanager
				.findModelTemplateContainer(mtc_ddbid);
		if (mtc1 == null) {
			throw new RuntimeException(" template container " + mtc_ddbid
					+ " not defined! ");
		}

		ParametersContainer pc1 = ddbmanager.findParametersContainer(mName);
		if (pc1 == null) {
			log.debug("-- creating Parameters Container " + mName +" plus parameters.");
			pc1 = new ParametersContainer(mName);
			Parameters pars = new Parameters(eurostagSim);
			for (String varName : zone.getData().keySet()) {
				String varFType = DtaParser.getVarFType(zone.typeName, varName);
				Object varValue = zone.getData().get(varName);
				if (log.isDebugEnabled()) {
					log.trace("----" + varName + " = " + varValue + ", " + varFType);
				}
				if (varFType.startsWith("F")) {
					if (varValue != null) {
						pars.addParameter(new ParameterFloat(varName,
								new Float((Double) varValue)));
					} else {
						pars.addParameter(new ParameterFloat(varName, null));
					}
				} else if (varFType.startsWith("A")) {
					pars.addParameter(new ParameterString(varName,
							(String) varValue));
				} else {
					log.error(varFType + " not handled");
				}
			}
			pc1.getParameters().add(pars);
			pc1 = ddbmanager.save(pc1);
		} else {
			log.debug("-- Parameters Container container " + mName
					+ " already defined, id: " + pc1.getId());
			// ddbmanager.delete(pc1);
		}

		if (eq1 == null) {
			log.info("-- creating Equipment " + cimId + "; eurostag name is: "
					+ mName);
			eq1 = new Equipment(cimId);
			eq1.setModelContainer(mtc1);
			eq1.setParametersContainer(pc1);
			eq1 = ddbmanager.save(eq1);
		} else {
			log.warn("-- Equipment  " + cimId + " already defined, id: "
					+ eq1.getId());
		}

		return cimId;
	}
	private String populateDDB_R(EurostagRecord zone, Map<String, String> amap,
			DDBManager ddbmanager, SimulatorInst eurostagSim, Map<String,Path> regsMapping) {


		log.debug("-Creating DDB component (R) " + zone);

		String macroblockName = (String) zone.getData().get(
				PAR_MACROBLOCK__NAME);
		int paramSetNum = (int) zone.getData().get("psetnum");
		String machineName = (String) zone.getData().get("machine.name");
		String nativeId = amap.get(machineName);

		if ((nativeId == null) || ("".equals(nativeId))) {
			throw new RuntimeException(
					"native id cannot be null; machine name is " + machineName);
		}

		addToEquipmentsInternalMap(nativeId,macroblockName);
		addPinNames(nativeId,macroblockName,retrieveActualRegPath(macroblockName+"."+"frm", regsMapping));

		//MTC and MT handling
		String mtc_ddbid = MTC_PREFIX_NAME + macroblockName;
		ModelTemplateContainer mtc1 = ddbmanager.findModelTemplateContainer(mtc_ddbid);
		if (mtc1 != null) {
			log.info("-- Using existing Model Template Container: " + mtc1.getDdbId());
		} else {
			log.info("-- Creating Model Template Container: " + mtc_ddbid);
			mtc1 = new ModelTemplateContainer(mtc_ddbid, "");
		}

		//find a mt for this mtc and simulator
		ModelTemplate mt1=null;
		for(ModelTemplate mt: mtc1.getModelTemplates()) {
			if ((mt.getSimulator().equals(eurostagSim)) && ("R".equals(mt.getTypeName())) && (mt.getComment().equals(macroblockName))) {
				mt1=mt;
				break;
			}
		}
		//if it does exist, and the update flag is set, delete it
		if (mt1 != null) {
			if (updateFlag == true) {
				log.info("--- removing existing Model Template: {}, {}, {}", mt1.getSimulator(), mt1.getTypeName(), mt1.getComment());
				mtc1.getModelTemplates().remove(mt1);
				mtc1 = ddbmanager.save(mtc1);
				mtc1 = ddbmanager.findModelTemplateContainer(mtc_ddbid);
				mt1=null;
				removePinNames(nativeId,macroblockName);
			} else {
				log.info("--- using existing Model Template: {}, {}, {}", mt1.getSimulator(), mt1.getTypeName(), mt1.getComment());
			}
		}

		if (mt1 == null) {
			mt1 = new ModelTemplate(eurostagSim, "R", macroblockName);
			log.info("--- creating model template: {}, {}, {}", mt1.getSimulator(), mt1.getTypeName(), mt1.getComment());

			// populate DDB with regs files
			// assumption:  regulators files' names are always in lower case
			// if a regulator file does not exist, it does not throw any exception but logs a warning
			// ... some regulators, e.g. pcp or rcp, could not be available at this time
			// to be resolved externally before a computation ....
			for (String extension : Arrays.asList( "fri","frm","par","pcp","rcp")) {
				Path friPath=retrieveActualRegPath(macroblockName+"."+extension, regsMapping);
				if ((friPath == null) || (Files.notExists(friPath))) {
					log.warn("--- Regulator file " + macroblockName + "." + extension
							+ " does not exist");
				} else {
					log.debug("--- Loading regulator file " + macroblockName + "." + extension + " in the database.");
					try {
						mt1.setData(extension, Files.readAllBytes(friPath));
					} catch (IOException e) {
						log.error("Could not read regulator file "+friPath+", " + e.getMessage(), e);
					}
				}
			}

			mtc1.getModelTemplates().add(mt1);
			mtc1 = ddbmanager.save(mtc1);

			addPinNames(nativeId,macroblockName,retrieveActualRegPath(macroblockName+"."+"frm", regsMapping));
		}


		String pcontainerID = "PC_" + nativeId + "_" + macroblockName;
		ParametersContainer pc1 = ddbmanager
				.findParametersContainer(pcontainerID);
		if (pc1 == null) {
			log.debug("-- Creating Parameters Container " + pcontainerID);
			log.debug("--- Parameter set number: " + paramSetNum);

			pc1 = new ParametersContainer(pcontainerID);
			Parameters pars = new Parameters(eurostagSim);
			pars.setDefParamSetNum(paramSetNum);
			for (String varName : zone.getData().keySet()) {
				String varFType = DtaParser.getVarFType(zone.typeName, varName);
				Object varValue = zone.getData().get(varName);
				if (log.isDebugEnabled()) {
					log.trace("----" + varName + " = " + varValue + ", " + varFType);
				}
				if (varFType.startsWith("F")) {
					if (varValue != null) {
						pars.addParameter(new ParameterFloat(varName,
								new Float((Double) varValue)));
					} else {
						pars.addParameter(new ParameterFloat(varName, null));
					}
				} else if (varFType.startsWith("A")) {
					pars.addParameter(new ParameterString(varName,
							(String) varValue));
				} else if (varFType.startsWith("I")) {
					pars.addParameter(new ParameterInteger(varName,
							(Integer) varValue));
				} else {
					log.error(varFType + " not handled");
				}
			}
			pc1.getParameters().add(pars);
			pc1 = ddbmanager.save(pc1);
		} else {
			log.debug("-- ParametersContainer container " + pcontainerID
					+ " already defined, id: " + pc1.getId());
		}

		String internalID = nativeId + "_" + macroblockName;

		Internal int1 = ddbmanager.findInternal(internalID);
		if (int1 == null) {
			log.info("-- Creating Internal " + internalID);
			int1 = new Internal(internalID);
			int1.setModelContainer(mtc1);
			int1.setParametersContainer(pc1);
			int1 = ddbmanager.save(int1);


		} else {
			log.warn("-- Internal  " + internalID + " already defined, id: "
					+ int1.getId());
			// ddbmanager.delete(int1);

		}

		return int1.getNativeId();
	}
	private String populateDDB_Default(EurostagRecord zone, Map<String, String> amap,
			DDBManager ddbmanager, SimulatorInst eurostagSim) {
		log.warn("- Not supported, skipping:  {}" , zone);
		return null;
	}

	private void processConnections(DDBManager ddbmanager) {
		log.debug("processing connections");
		//nativeId being the equipment's cimId
		for (String  nativeId : EquipmentsInternalsMap.keySet()) {

			//create a connection schema, if needed
			log.info("processing connections for Equipment: " + nativeId);
			ConnectionSchema cs = ddbmanager.findConnectionSchema(nativeId, null);
			if (cs == null) {
				log.info("- creating conn. schema with cimId " + nativeId);
				cs = new ConnectionSchema(nativeId, null);
				cs = ddbmanager.save(cs);
			} else {
				log.warn("- conn. schema with cimId " + nativeId + ", already exists! currently defined connections: " + cs.getConnections());
			}
			//machineInterfaceVariables is the set of this machine type variables names
			Set<String> machineInterfaceVariables=DtaParser.getInterfaceVariablesNamesByComponentTypeName(equipmentsTypeMap.get(nativeId));
			log.debug("-- equipment " + nativeId + ", variable names: " + machineInterfaceVariables);

			//create a connection machine-macroblock, for each macroblock
			log.debug("-- equipment " + nativeId + ", connected regulators: " + EquipmentsInternalsMap.get(nativeId));
			log.debug("--- processing machine-regulators connections, for equipment " + nativeId);
			//for each regulator/macroblock; mName is the eurostag name of the regulator
			for (String mName : EquipmentsInternalsMap.get(nativeId)) {
				String internalID=nativeId+"_"+mName; // internaID name of the regulator
				List<Connection> definedConnections = cs.getConnections();
				//create a direct machine-macroblock connection iff machine and macroblock share -at least- one variable
				Set<String> machineRegulatorIntSet=Sets.intersection(machineInterfaceVariables, Sets.newHashSet(macroblocksPinNames.get(mName)));
				log.debug("---- regulator: " + mName + " (internalId: " + internalID + "); reg. variable names: " + macroblocksPinNames.get(mName) + ", equip. regul. common vars: " + machineRegulatorIntSet);
				if (!machineRegulatorIntSet.isEmpty()) {
					for (String pinName : machineRegulatorIntSet) {
						Connection newC = new Connection(nativeId, Connection.EQUIPMENT_TYPE, internalID, Connection.INTERNAL_TYPE, pinName,
								pinName, Connection.INSIDE_CONNECTION);
						if (!definedConnections.contains(newC)) {
							log.debug("----- creating connection " + newC);
							cs.getConnections().add(newC);
						} else {
							log.debug("----- connection already exist: " + newC);
						}
					}
				} else {
					log.debug("----- no common variable names, so no connections are created.");
				}
			}

			//create connections macroblock-macroblock
			log.debug("--- processing regulator-regulator connections, for equipment " + nativeId);
			for (int i = 0; i < EquipmentsInternalsMap.get(nativeId).size(); i++) {
				for (int j = i+1; j < EquipmentsInternalsMap.get(nativeId).size(); j++) {
					String id1 = EquipmentsInternalsMap.get(nativeId).get( i);
					String id2 = EquipmentsInternalsMap.get(nativeId).get( j);
					Set<String> intSet=Sets.intersection(Sets.newHashSet(macroblocksPinNames.get(id1)), Sets.newHashSet(macroblocksPinNames.get(id2)));
					log.debug("--- reg: " + id1 + ", reg: " + id2 + ", common variable names:" + intSet);
					boolean toBeProcessed=(intSet.size()>0);
					if (toBeProcessed) {
						for (String pinName : intSet) {
							String longId1=nativeId+"_"+id1;
							String longId2=nativeId+"_"+id2;
							Connection newC = new Connection(longId1, Connection.INTERNAL_TYPE, longId2, Connection.INTERNAL_TYPE, pinName, pinName, Connection.INSIDE_CONNECTION);
							List<Connection> definedConnections = cs.getConnections();
							if (!definedConnections.contains(newC)) {
								log.debug("---- creating connection: " + newC);

								cs.getConnections().add(newC);
							} else {
								log.debug("---- connection already exists: " + newC);
							}
						}
					} else {
						log.debug("---- no common variable names,, so no connections are created.");
					}
				}

			}
			log.info("- connection schema for cimId " + nativeId + ": " + cs.getConnections());
			cs = ddbmanager.save(cs);
		}
	}
	/*
	//  regs file names are inferred implicitly from .dta data
	// the problem is that names are always uppercase whilst actual filenames are not
	// Apparently eurostag is case unsensitive, so regs file names are to be matched case-insensitively
	// Here we assume that no (f1,f2) , where f1 != f2 and f1.equalsIgnoreCase(f2) actually exist
	 *
	 */
	public Map<String, Path> buildRegsFilesMapping(Path regsFolderPath) {
		Map<String, Path> regsMap=new HashMap<>();
		regsMap=new HashMap<>();
		try (DirectoryStream<Path> flow = Files.newDirectoryStream(
				regsFolderPath, "*")) {
			for (Path item : flow) {
				//System.out.println("Reg file: " + item.toString());
				String fileName = item.getFileName().toString();
				regsMap.put(fileName.toLowerCase(), regsFolderPath.resolve(fileName));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return regsMap;
	}

	public Path retrieveActualRegPath(String tentativeFileName, Map<String,Path> regsMap) {
		if (!regsMap.containsKey(tentativeFileName.toLowerCase())) {
			return null;
		} else {
			return regsMap.get(tentativeFileName.toLowerCase());
		}

	}





	private void addToEquipmentsInternalMap(String nativeId, String macroblockName) {
		List<String> macroblocks=null;
		if (!EquipmentsInternalsMap.containsKey(nativeId)) {
			macroblocks=new ArrayList<String>();
		} else {
			macroblocks=EquipmentsInternalsMap.get(nativeId);
		}
		if (!macroblocks.contains(macroblockName)){
			macroblocks.add(macroblockName);
		}
		EquipmentsInternalsMap.put(nativeId,macroblocks);
	}

	private void addPinNames(String nativeId, String macroblockName, Path friPath) {
		log.debug("addPinNames: NativeId " + nativeId + ", Macroblock " + macroblockName + ", file " + friPath);
		if (!macroblocksPinNames.containsKey(macroblockName)) {
			log.debug(" ---------- Reading pin names from file {}" , macroblockName, friPath);
			Converter converter = new Converter(friPath.toAbsolutePath().toString(), "/tmp", false);

			List<String> pinNames=converter.getConnections();
			for (String pinName : pinNames) {
				log.debug(" --------------  {}", pinName);
			}
			macroblocksPinNames.put(macroblockName, new HashSet<String>(pinNames));
		}
	}

	private void removePinNames(String nativeId, String macroblockName) {
		log.debug("removePinNames: NativeId " + nativeId + ", Macroblock " + macroblockName);
		if (!macroblocksPinNames.containsKey(macroblockName)) {
			log.debug(" ----------  macroblock does not exist in the macroblock map", macroblockName);
		} else {
			macroblocksPinNames.remove(macroblockName);
		}
	}



	public Map<String, String> readWithCsvMapReader(Path dicoFile) throws Exception {
		return DtaParser.readWithCsvMapReader(dicoFile);
	}

	/* ********************************
	 * Export Functions
	 ********************************* */
	/**
	 * in the workingDir, write eurostag dynamic data (fileName) for a given list of cimIDs, and regulator files (.fri, .frm, .par, .pcp, rcp)
	 * 	 *
	 * @param workingDir
	 * @param fileName
	 * @param network
	 * @param parallelIndexes
	 * @param eurostagVersion
	 * @param iidm2eurostagId
	 */
	@Override
	public void dumpDtaFile(Path workingDir, String fileName,
			Network network, Map<String, Character> parallelIndexes, String eurostagVersion,
			Map<String, String> iidm2eurostagId) {
		try (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
			dumpDtaFile(workingDir, fileName, network, parallelIndexes, eurostagVersion, iidm2eurostagId, ddbmanager);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} 

	}
	
	private boolean filteredGenerator(Generator g) {
		if (configExport.getGensPQfilter() == true) {
	  		if  ( !Float.isNaN(g.getTerminal().getP()) && ((-g.getTerminal().getP() > g.getMaxP()) || (-g.getTerminal().getP() < g.getMinP())) ) {
	  			return true;
	  		}
		}
		return false;
	}

	/**
	 * in the workingDir, write eurostag dynamic data (fileName) for a given list of cimIDs, and regulator files (.fri, .frm, .par, .pcp, rcp)
	 * 	 *
	 * @param workingDir
	 * @param fileName
	 * @param network
	 * @param parallelIndexes
	 * @param eurostagVersion
	 * @param iidm2eurostagId
	 * @param ddbmanager
	 */
	public void dumpDtaFile(Path workingDir, String fileName,
			Network network, Map<String, Character> parallelIndexes, String eurostagVersion,
			Map<String, String> iidm2eurostagId, DDBManager ddbmanager) {


		if (workingDir == null) {
			throw new IllegalArgumentException("working directory is null");
		}
		if (!Files.isDirectory(workingDir)) {
			throw new IllegalArgumentException(
					"working directory path is not a directory");
		}
		if (fileName == null) {
			throw new IllegalArgumentException("fileName is null");
		}

		if (ddbmanager == null) {
			throw new IllegalArgumentException("ddbmanager is null");
		}
		this.network = network;
		this.parallelIndexes = parallelIndexes;

		if (configExport.getGensPQfilter() == true) {
			log.warn("Dta export configured to skip generators when their P is outside (MinP, MaxP)");
		}

		Path destFile = workingDir.resolve(fileName);
		log.info("dump dta file to : " + destFile.toAbsolutePath());

		try (PrintStream dtaOutStream = new PrintStream(Files.newOutputStream(destFile)); PrintStream genListOutStream = new PrintStream(Files.newOutputStream(workingDir.resolve(DICT_GENS_CSV))); PrintStream skippedGensOutStream = new PrintStream(Files.newOutputStream(workingDir.resolve(SKIPPED_GENS_CSV)))) {

			SimulatorInst eurostagSim=ddbmanager.findSimulator(Simulator.EUROSTAG, eurostagVersion);
			if (eurostagSim == null) {
				throw new RuntimeException("Could not find Eurostag simulator version " + eurostagVersion);
			}

			List<String> cimIds = new ArrayList<>(network.getGeneratorCount());
			// collect generators cim ids for the DTA exporter
			log.warn("Skipping generators: network, generator Id, min P, P, max P");
			for (Generator g : Identifiables.sort(network.getGenerators())) {
				if (!filteredGenerator(g))
					cimIds.add(g.getId());
				else {
					log.warn("skipped generator: {};{};{};{};{}", network.getId(),g.getId(), g.getMinP(), -g.getTerminal().getP(), g.getMaxP());
					skippedGensOutStream.println(network.getId()+";"+g.getId()+";"+g.getMinP()+";"+ (-g.getTerminal().getP()) +";"+g.getMaxP());
				}
			}

			// writing a .dta
			DtaParser.dumpHeader(new Date(), eurostagVersion, dtaOutStream);

			for (String cimId : cimIds) {
				log.info("Processing equipment: " + cimId);
				Equipment eq = ddbmanager.findEquipment(cimId);
				if (eq == null) {
					log.warn("- equipment: " + cimId + " not found !");
					continue;
				}
				dumpData(eq, eurostagSim,
						ddbmanager, dtaOutStream, genListOutStream, iidm2eurostagId);

				try {
					dumpDataInternals(workingDir, cimId, eq, eurostagSim, iidm2eurostagId, ddbmanager, dtaOutStream, network);
				} catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
			}

			dumpDataRST(eurostagSim, ddbmanager, dtaOutStream, iidm2eurostagId, workingDir);

			if(configExport.getAutomatonA14()) {
				HashMap<String, Object> zm = new HashMap<String, Object>();
				EurostagRecord eRecord=new EurostagRecord("TRF", zm);
				zm.put("keyword","TRF");
				zm.put("type.zone","PTA");
				zm.put("zone.name","*");
				zm.put("device.name","PRTRF1");
				zm.put("time.down.change","5");
				zm.put("max.taps.down","1");
				zm.put("time.up.change","5");
				zm.put("max.taps.up","1");
				DtaParser.dumpZone(eRecord,dtaOutStream);
			}

			if(configExport.getExportACMC()) {
				HashMap<String, Object> zm = new HashMap<String, Object>();
				EurostagRecord eRecord=new EurostagRecord("BAT", zm);
				zm.put("keyword","BAT");
				zm.put("type.zone","PBA");
				zm.put("zone.name","*");
				zm.put("bank.name","");
				zm.put("model.name","PRBAT1");
				zm.put("min.time1","0.5");
				zm.put("min.time2","2.5");
				zm.put("min.time3","0.2");
				zm.put("min.time4","0.2");
				DtaParser.dumpZone(eRecord,dtaOutStream);
			}

			dumpDataLoadPatternAndBehaviour(cimIds, eurostagSim, ddbmanager,dtaOutStream);

			dumpDataAutomatons(eurostagSim, ddbmanager, dtaOutStream, iidm2eurostagId);

			dumpDataACMC(eurostagSim, ddbmanager, dtaOutStream, iidm2eurostagId, workingDir);

			// does eurostag pcp and rcp compilers comply, in case macro.lis is empty?
			// it seems not (windows version)
			try (BufferedWriter macrolisWriter = Files.newBufferedWriter(workingDir.resolve("macro.lis"), StandardCharsets.UTF_8)) {
				for (String regName : uniqueRegNamesSet) {
					macrolisWriter.append(regName);
					macrolisWriter.newLine();
				}
			} catch (Exception e) {
				log.error("could not write macro.lis file, due to " + e.getMessage());
			}


		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		}

	}
	public void dumpDataLoadPatternAndBehaviour(List<String> cimIds, SimulatorInst simInst, DDBManager ddbmanager, PrintStream out) throws IOException {
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (ddbmanager == null) {
			throw new RuntimeException("DDBManager must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}

		if (log.isDebugEnabled()) {
			log.trace("Dumping load pattern and behaviour data");
		}

		//TBD: hardcoded, for now, both the load pattern and the behaviour
		HashMap<String, Object> zmLoadPattern=new HashMap<String,Object>();
		zmLoadPattern.put("keyword", "LOADP");
		zmLoadPattern.put("pattern.identifier", "CH");
		zmLoadPattern.put("reg_subload.name","VOLTA1");
		zmLoadPattern.put("REG_RPPROPA",new Float(1));
		zmLoadPattern.put("REG_RQPROPA",new Float(1));
		zmLoadPattern.put("REG_RALFAPA", configExport.getLoadPatternAlpha());
		zmLoadPattern.put("REG_RBETAPA", configExport.getLoadPatternBeta());
		zmLoadPattern.put("REG_RGAMMAPA",new Float(0));
		zmLoadPattern.put("REG_RDELTAPA",new Float(0));
		EurostagRecord eRecord=new EurostagRecord("LOADP", zmLoadPattern);
		try {
			DtaParser.dumpZone(eRecord,out);
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}


		// hardcoded; definition: assuming 'whole network W'. ....
		HashMap<String, Object> zmLoadBehaviour=new HashMap<String,Object>();
		zmLoadBehaviour.put("keyword", "CH");
		zmLoadBehaviour.put("identifier", "CH");
		zmLoadBehaviour.put("definition","W");
		EurostagRecord eBRecord=new EurostagRecord("CH", zmLoadBehaviour);
		try {
			DtaParser.dumpZone(eBRecord,out);
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}

	}
	public void dumpDataInternals(Path workingDir, String cimId, Equipment eq, SimulatorInst eurostagSim,
			Map<String, String> iidm2eurostagId, DDBManager ddbmanager,
			PrintStream dtaOutStream, Network network) {
		//retrieve directly and not directly connected internals
		Set<String> internalIds = getConnectedInternals(cimId, ddbmanager);
		log.info("- connected internals: " + cimId+": "+ internalIds);
		for (String nativeId : internalIds) {
			Internal internal = ddbmanager
					.findInternal(nativeId);
			if (internal == null) {
				log.warn("- internal with nativeId: " + nativeId
						+ " not found !");
			} else {
				String substMachineName=null;
				if (iidm2eurostagId!=null) {
					substMachineName=iidm2eurostagId.get(eq.getCimId());
				}
				try {
					dumpData(internal, eurostagSim, ddbmanager, dtaOutStream, substMachineName, iidm2eurostagId, network);
				} catch (Exception e) {
					log.error("could not write macro.lis file, due to " + e.getMessage());
				}
				// dump regulator files start
				String macroblockName=ddbmanager.getStringParameter(
						internal, eurostagSim,
						PAR_MACROBLOCK__NAME);
				if (macroblockName == null) {
					throw new RuntimeException("null macroblock.name for internal " + internal);
				}
				//since this will drive regs filenames, normalize to lower case (pro case sensitive filesystems)
				String intName = macroblockName.toLowerCase();


				ModelTemplateContainer mtc = internal
						.getModelContainer();

				boolean foundFRI=false;
				boolean foundFRM=false;
				boolean foundPAR=false;
				boolean foundPCP=false;
				boolean foundRCP=false;
				boolean foundRegFile=false;
				for (ModelTemplate mt : mtc.getModelTemplates()) {
					if ((mt.getSimulator().getSimulator() == Simulator.EUROSTAG)
							&& (mt.getSimulator().getVersion()
									.equals(eurostagVersion))) {

						for (String dataKey : mt.modelDataMap()
								.keySet()) {
							switch (dataKey) {
							case "pcp":
								foundRegFile=true;
								foundPCP=true;
								break;
							case "rcp":
								foundRegFile=true;
								foundRCP=true;
								break;
							case "fri":
								foundRegFile=true;
								foundFRI=true;
								break;
							case "frm":
								foundRegFile=true;
								foundFRM=true;
								break;
							case "par":
								foundRegFile=true;
								foundPAR=true;
								break;
							default:
								log.warn("- regfile extension not recognized: " + dataKey);
								break;
							}

							if (foundRegFile) {
								Path path = workingDir.resolve(intName + "." + dataKey);
								//avoiding dups
								if (!Files.exists(path)) {
									try {
										Files.write(path, mt.getData(dataKey));
									} catch (Throwable e) {
										log.error(e.getMessage(), e);
									}
								}
							}

						}

					}
				}

				//keeps track of reg name, pro .pcp, .rcp compiling
				//skipping those regs that do not have both .rcp and .pcp files
				if (((foundFRI==true) && (foundFRM==true) && (foundPAR==true))
						&& ((foundPCP==false) || (foundRCP==false))) {
					uniqueRegNamesSet.add(intName);
				}
				//keeps track of generators in RST zones
				if(configExport.getExportRST()) {
					Parameters params = ddbmanager.findParameters(internal, eurostagSim);
					HashMap<String, Object> parsList=Utils.getHashMapFromParameters(params);
					if(parsList.containsKey("macroblock.name")
							&& parsList.containsKey("machine.name")
							&& parsList.containsKey("coupling.par1")
							&& ((String) parsList.get("macroblock.name")).equals(configExport.getRSTRegulGenerator())) {

						String zoneRstName = ((String) parsList.get("coupling.par1")).substring(3); // "M  ZoneName"
						if(!geneRst.containsKey(zoneRstName)) {
							geneRst.put(zoneRstName, new ArrayList<String>());
						}						
						geneRst.get(zoneRstName).add(cimId);
					}
				}
			}
		}
	}
	public void dumpData(Equipment inst, SimulatorInst simInst, DDBManager ddbmanager, PrintStream out, PrintStream gensOut, Map<String, String> iidm2eurostagId) throws IOException {
		if (inst == null) {
			throw new RuntimeException("Equipment must be not null");
		}
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (ddbmanager == null) {
			throw new RuntimeException("DDBManager must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}

		if (log.isDebugEnabled()) {
			log.trace("Dumping data for: " +inst.getCimId());
		}
		ModelTemplate mtc=ddbmanager.findModelTemplate(inst, simInst);
		if (mtc==null) {
			throw new RuntimeException("Could not find any model templates for simulator: " + simInst.toString());
		}

		//get type name from model template data, normalizing result to common set of components (e.g. both M2 U and M2DU are in fact
		// represented in eurostag with the same set of parameters
		String zoneTypeName=null;
		switch (mtc.getTypeName()) {
		case "M1U":
		case "M1DU":
		case "M1S":
		case "M1DS":  zoneTypeName="M1";
		break;
		case "M2U":
		case "M2DU":
		case "M2S":
		case "M2DS":  zoneTypeName="M2";
		break;
		case "R":  zoneTypeName="R";
		break;

		default:
			log.error("not supported keyword " + mtc.getTypeName());
			break;

		}
		if (zoneTypeName==null) {
			throw new RuntimeException(" not supported typeName " + mtc.getTypeName() );
		}

		log.trace(" model template typename: " + mtc.getTypeName());
		Parameters pars=ddbmanager.findParameters(inst, simInst);
		HashMap<String, Object> zm=Utils.getHashMapFromParameters(pars);

		//change machine.name, according to the mapping  iidm2eurostagId<cimid, eurostagid>
		switch (zoneTypeName) {
		case "M2": case "M1":
			if (iidm2eurostagId!=null) {
				String substMachineName=iidm2eurostagId.get(inst.getCimId());
				if ((substMachineName!=null) && (!"".equals(substMachineName))) {
					zm.put("machine.name",substMachineName);
				}

				if (log.isDebugEnabled()) {
					log.trace(" machine.name mapped to new eurostag id: " +substMachineName);
				}
				
				String nodeName=iidm2eurostagId.get(network.getGenerator(inst.getCimId()).getTerminal().getBusBreakerView().getConnectableBus().getId());
				zm.put("connection.node.name",nodeName);
				//Connection node name needed for RST couplings
				//zm.put("connection.node.name",null);
				//if (log.isDebugEnabled()) {
				//	log.trace(" connection.node.name mapped to empty string" );
				//}

				//systematically, do not export fortescue data, more complex than what we need (Jun 20, 2013)
				if ("D".equals(zm.get("type.fortescue"))) {
					zm.put("type.fortescue",null);
					//TBD (Sept 04, 2013) eurostag complains if transformer.included data is missing and the machine is not fortescue
					// since it happens frequently in french data, I'm forcing it to 'N'  but ... is it correct?
					// Moreover, is it still needed to skip fortescue ?
					if ("".equals(zm.get("transformer.included"))) {
						zm.put("transformer.included","N");
					}
				}
				//
				//				String substTerminalName=null;
				//				if ((substTerminalName!=null) && (!"".equals(substTerminalName))) {
				//					zm.put("connection.node.name",substTerminalName);
				//					if (log.isDebugEnabled()) {
				//						log.trace(" connection.node.name mapped to new eurostag id: " +substTerminalName);
				//					}
				//				}
			}
			break;
		default:
			break;
		}

		EurostagRecord eRecord=new EurostagRecord(zoneTypeName, zm);

		try {
			DtaParser.dumpZone(eRecord,out);
			DtaParser.dumpGensInertia(inst.getCimId(), eRecord, gensOut);

		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
	}

	public void dumpData(Internal inst, SimulatorInst simInst, DDBManager ddbmanager, PrintStream out, String machineName, Map<String, String> iidm2eurostagId, Network network) throws IOException {
		if (inst == null) {
			throw new RuntimeException("Internal must be not null");
		}
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (ddbmanager == null) {
			throw new RuntimeException("DDBManager must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}

		if (log.isDebugEnabled()) {
			log.trace("Dumping data for: " +inst.getNativeId());
		}

		//ModelTemplate mtc=ddbmanager.findModelTemplate(inst, simInst);
		ModelTemplate mtc=null;
		for (ModelTemplate mt : inst.getModelContainer().getModelTemplates()) {
			if (mt.getSimulator().equals(simInst)){
				mtc=mt;
				break;
			}
		}
		if (mtc==null) {
			throw new RuntimeException("Could not find any model templates for simulator: " + simInst.toString());
		}

		//get type name from model template data, normalizing result to common set of components (e.g. both M2 U and M2DU are in fact
		// represented in eurostag with the same set of parameters
		String zoneTypeName=null;
		switch (mtc.getTypeName()) {
		case "R":  zoneTypeName="R";
		break;
		case "RMA":  zoneTypeName="RMA";
		break;
		default:
			break;

		}
		if (zoneTypeName==null) {
			throw new RuntimeException(" not supported typeName " + mtc.getTypeName() );
		}

		log.trace(" model template typename: " + mtc.getTypeName());
		//Parameters pars=ddbmanager.findParameters(inst, simInst);
		Parameters pars=null;
		for (Parameters ps : inst.getParametersContainer().getParameters()) {
			if (ps.getSimulator().equals(simInst)){
				pars=ps;
				break;
			}
		}
		HashMap<String, Object> zm=Utils.getHashMapFromParameters(pars);

		//change connection.node.name, according to the mapping  iidm2eurostagId<cimid, eurostagid>
		switch (zoneTypeName) {
		case "R":
			if ((machineName!=null) && !("".equals(machineName))) {
				zm.put("machine.name",machineName);
			}
			break;
		case "RMA":
			if ((machineName!=null) && !("".equals(machineName))) {
				zm.put("machine.name",machineName);
			}
			break;
		default:
			break;
		}

		//[06 sept 2013] This way, no node name is mentioned in the .dta, the correpsondance is only carried by the machine names.
		boolean couplingDataExists=false;
		for (int i = 1; i < 10; i++) {
			String apar=(String) zm.get("coupling.par"+i);
			if ((apar!=null) && (!"".equals(apar.trim()))) {
				couplingDataExists=true;
				break;
			}
		}

		if(((String) zm.get("macroblock.name")).equals(configExport.getRSTRegulInjector())) {
			// RST coupling machine
			String zoneRST = (String) zm.get("machine.name");
			if(!geneRst.containsKey(zoneRST)) {
				log.warn("Reference generator can't be found - RST zone: "+zoneRST);
			} else {
				int coupling = 1;
				// Coupling 1 to 3 : nodes
				String defaultBus = null;
				String stationPilotPoint = pilotPointRst.get(zoneRST);
				for(Bus bus : network.getVoltageLevel(stationPilotPoint).getBusBreakerView().getBuses()) {
					if(coupling < 4) {
						String busEsgName = iidm2eurostagId.get(bus.getId());
						if(busEsgName != null) {
							zm.put("coupling.par"+coupling,"N  "+busEsgName);
							coupling++;
							defaultBus = busEsgName;
						}
					}
				}
				while(coupling<=3) {
					zm.put("coupling.par"+coupling,"N  "+defaultBus); // not used
				    coupling++;
				}
				// Coupling 4 to 9 : generators
				String gene = pilotGeneratorsRst.get(zoneRST);
				for(int j=0;j<6;j++) {
					zm.put("coupling.par"+coupling,"M  "+iidm2eurostagId.get(gene));
					coupling++;
				}
				/*
				// TODO Choose automatically generators for RST
				int currentIndexGen = 0;
				for(String genTemp : geneRst.get(zoneRST)) {
					if(currentIndexGen < 6) {
						if(network.getGenerator(genTemp).getTerminal().isConnected()) {
							generatorNames[currentIndexGen] = iidm2eurostagId.get(genTemp);
							generatorPower[currentIndexGen] = network.getGenerator(genTemp).getMaxP();
							currentIndexGen++;
						}
					} else {
						for(int j=0;j<6;j++) {
							if(network.getGenerator(genTemp).getTerminal().isConnected() || network.getGenerator(genTemp).getMaxP() > generatorPower[j]) {
								generatorNames[j] = iidm2eurostagId.get(genTemp);
								generatorPower[j] = network.getGenerator(genTemp).getMaxP();
								break;
							}
						}
					}
				}
				if(currentIndexGen < 6) {
					for(String genTemp : geneRst.get(zoneRST)) {
						if(currentIndexGen < 6) {
							generatorNames[currentIndexGen] = iidm2eurostagId.get(genTemp);
							generatorPower[currentIndexGen] = network.getGenerator(genTemp).getMaxP();
							currentIndexGen++;
						}
					}
				}
				while(currentIndexGen < 6) {
					String genTemp = geneRst.get(zoneRST).get(0);
					generatorNames[currentIndexGen] = iidm2eurostagId.get(genTemp);
					generatorPower[currentIndexGen] = network.getGenerator(genTemp).getMaxP();
					currentIndexGen++;
				}
				for(int j=0;j<6;j++) {
					zm.put("coupling.par"+coupling,"M  "+generatorNames[j]);
					coupling++;
				}
				*/
				
			}
		} else if(((String) zm.get("macroblock.name")).equals(configExport.getACMCRegul())) {
			// ACMC coupling MA and Pilot station
			String acmcMachineName = (String) zm.get("machine.name");
			String pilotStation = ((String) zm.get("coupling.par2")).substring(3);

			String defaultNode = null;
			int couplingParam = 1;
			for(VoltageLevel station : network.getVoltageLevels()) {
				if(station.getName().equals(pilotStation)) {
					// Looking for connected nodes
					for(Bus bus : station.getBusBreakerView().getBuses()) {
						String pilotNewName = iidm2eurostagId.get(bus.getId());
						if(pilotNewName != null) {
							zm.put("coupling.par"+couplingParam,"N  "+pilotNewName);
							defaultNode = pilotNewName;
							couplingParam++;
						}
						if(couplingParam == 5) {
							break;
						}
					}
					for(int cpl=couplingParam;cpl<5;cpl++) {
						zm.put("coupling.par"+cpl,"N  "+defaultNode); // not used
					}
				}
			}
			if(defaultNode == null || "".equals(defaultNode)) {
				log.error("No pilot station found for shunt : "+acmcMachineName);
			}
			ArrayList<String> bankInAcmc = acmcs.get(acmcMachineName);
			int couplingDevice = 5;
			for(String bank : bankInAcmc) {
				zm.put("coupling.par"+couplingDevice,"S  "+bank);
				couplingDevice++;
			}
			if(couplingDevice > 9) {
				log.warn("Too many shunts for : "+acmcMachineName);
			}
			String defaultBank = bankInAcmc.get(0);
			for(int cpl=couplingDevice;cpl<=9;cpl++) {
				zm.put("coupling.par"+cpl,"S  "+defaultBank); // not used
			}

			zm.put("BAT225T",bankInAcmc.size());

		} else if(((String) zm.get("macroblock.name")).equals(configExport.getRSTRegulGenerator())) { 
			if(configExport.getExportRST()) {
				// First coupling to injector machine
				zm.put("coupling.par2",null);
				zm.put("coupling.par3",null);
				zm.put("coupling.par4",null);
				zm.put("coupling.par5",null);
				zm.put("coupling.par6",null);
				zm.put("coupling.par7",null);
				zm.put("coupling.par8",null);
				zm.put("coupling.par9",null);
			} else { // Write the previously deleted regulation RSTRegulGeneratorDelete
				zm.put("macroblock.name", configExport.getRSTRegulGeneratorDelete());
				zm.put("psetnum", 1);
				zm.put("coupling.par1",null);
				zm.put("coupling.par2",null);
				zm.put("coupling.par3",null);
				zm.put("coupling.par4",null);
				zm.put("coupling.par5",null);
				zm.put("coupling.par6",null);
				zm.put("coupling.par7",null);
				zm.put("coupling.par8",null);
				zm.put("coupling.par9",null);
			}
		} else {
			if (couplingDataExists==true) {
				log.warn("coupling macroblock data in ddb -  macroblock name: "+zm.get("macroblock.name") + "; machine name: " + machineName);
			}
			zm.put("coupling.par1",null);
			zm.put("coupling.par2",null);
			zm.put("coupling.par3",null);
			zm.put("coupling.par4",null);
			zm.put("coupling.par5",null);
			zm.put("coupling.par6",null);
			zm.put("coupling.par7",null);
			zm.put("coupling.par8",null);
			zm.put("coupling.par9",null);
			if (log.isDebugEnabled()) {
				log.trace(" coupling.par(s) mapped to empty string" );
			}
		}

		EurostagRecord eRecord=new EurostagRecord(zoneTypeName, zm);
		try {
			// RST export ?
			if(configExport.getExportRST()
					|| (!((String) zm.get("macroblock.name")).equals(configExport.getRSTRegulGenerator())
							&& !((String) zm.get("macroblock.name")).equals(configExport.getRSTRegulInjector()))) {
				DtaParser.dumpZone(eRecord,out);
			}
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
	}

	// Dump data without DDB parameters
	public void dumpDataAutomatons(SimulatorInst eurostagSim, DDBManager ddbmanager,
			PrintStream dtaOutStream, Map<String, String> iidm2eurostagId) throws ParseException, IOException {

		if(configExport.getAutomatonA11()) {
			// A11
			DtaParser.dumpAutomatonHeader("A11", false, dtaOutStream);
			for (Generator g : network.getGenerators()) {
				Equipment eq = ddbmanager.findEquipment(g.getId());
				if (eq != null) {
					if ( !filteredGenerator(g) )
						dumpDataGeneratorAutomaton(g, eurostagSim, "A11", dtaOutStream, iidm2eurostagId);
					else
						log.warn("Dump automaton A11: skipping generator {} P outside limits MinP:{} P:{} MaxP:{},  ", g.getId(),g.getMinP(), -g.getTerminal().getP(), g.getMaxP());
				}
			}
			DtaParser.dumpAutomatonHeader("A11", true, dtaOutStream);
		}

		if(configExport.getAutomatonA12()) {
			DtaParser.dumpAutomatonHeader("A12", false, dtaOutStream);
			for (Generator g : network.getGenerators()) {
				Equipment eq = ddbmanager.findEquipment(g.getId());
				if (eq != null) {
					if ( !filteredGenerator(g) )
						dumpDataGeneratorAutomaton(g, eurostagSim, "A12", dtaOutStream, iidm2eurostagId);
					else
						log.warn("Dump automaton A12: skipping generator {} P outside limits MinP:{} P:{} MaxP:{},  ", g.getId(),g.getMinP(), -g.getTerminal().getP(), g.getMaxP());
				}
			}
			DtaParser.dumpAutomatonHeader("A12", true, dtaOutStream);
		}

		if(configExport.getAutomatonA14()) {
			DtaParser.dumpAutomatonHeader("A14", false, dtaOutStream);
			for (TwoWindingsTransformer t : network.getTwoWindingsTransformers()) {
				if(t.getPhaseTapChanger() == null // no TD
						&& t.getRatioTapChanger() != null
						&& t.getRatioTapChanger().hasLoadTapChangingCapabilities() // has tap changer
						&& t.getRatioTapChanger().isRegulating()
						&& t.getTerminal1().getVoltageLevel().getNominalV() >= 63.0 // no TG
						&& t.getTerminal2().getVoltageLevel().getNominalV() >= 63.0 // no TG
						) {
					dumpDataTransformerAutomaton(t, eurostagSim, "A14_HT", dtaOutStream, iidm2eurostagId);
				} else if(t.getPhaseTapChanger() != null 
						&& t.getPhaseTapChanger().isRegulating()) { // TD
					//TODO
					//dumpDataTransformerAutomaton(t, eurostagSim, "A14_TD", dtaOutStream, iidm2eurostagId);
				}
			}
			if(configExport.getLVLoadModeling()) {
				for(Load l : network.getLoads()) {
					if(l.getTerminal().getVoltageLevel().getNominalV() <= 100 && l.getTerminal().isConnected()) { // connected HV load
						if(l.getLoadType() != LoadType.AUXILIARY) {
							dumpDataLoadAutomaton(l, eurostagSim, "A14_MT", dtaOutStream, iidm2eurostagId);
						} else {
							if (log.isDebugEnabled())
								log.trace(l.getId() + " is considered as a generator auxiliary alimentation");
						}
					}
				}
			}
			DtaParser.dumpAutomatonHeader("A14", true, dtaOutStream);
		}
	}
	public void dumpDataGeneratorAutomaton(Generator g, SimulatorInst simInst, String typeName, PrintStream out, Map<String, String> iidm2eurostagId) throws IOException {
		if (g == null) {
			throw new RuntimeException("Generator must be not null");
		}
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}

		if (log.isDebugEnabled()) {
			log.trace("Dumping data for: " +g.getId());
		}

		HashMap<String, Object> zm = new HashMap<String, Object>();

		//change machine.name, according to the mapping  iidm2eurostagId<cimid, eurostagid>
		if (iidm2eurostagId!=null) {
			String substMachineName=iidm2eurostagId.get(g.getId());
			if ((substMachineName!=null) && (!"".equals(substMachineName))) {
				zm.put("machine.name",substMachineName);
			}

			if (log.isDebugEnabled()) {
				log.trace(" machine.name mapped to new eurostag id: " +substMachineName);
			}

			String substNodeName=iidm2eurostagId.get(g.getTerminal().getBusBreakerView().getConnectableBus().getId());
			if ((substNodeName!=null) && (!"".equals(substNodeName))) {
				zm.put("connection.node.name",substNodeName);
			}

			if (log.isDebugEnabled()) {
				log.trace(" connection.node.name mapped to:" + substNodeName);
			}
		}

		switch (typeName) {
		case "A11":
			zm.put("USINF","0.7");
			zm.put("USRINF","0.707");
			zm.put("TINF","0.7");
			zm.put("TRINF","0.");
			zm.put("DELINF","0.");
			zm.put("USSUP","1.2");
			zm.put("USRSUP","1.188");
			zm.put("TSUP","3.");
			zm.put("TRSUP","0.");
			zm.put("DELSUP","0.");
			break;
		case "A12":
			zm.put("VIMIN","47.5");
			zm.put("TMIN","1.");
			zm.put("VIMAX","55.");
			zm.put("TMAX","1.");
			zm.put("TDEL","0.");
			break;
		default:
			log.error("not supported keyword " + typeName);
			break;

		}

		log.trace(" typename: " + typeName);

		EurostagRecord eRecord=new EurostagRecord(typeName, zm);

		try {
			DtaParser.dumpZone(eRecord,out);
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
	}
	public void dumpDataTransformerAutomaton(TwoWindingsTransformer t, SimulatorInst simInst, String typeName, PrintStream out, Map<String, String> iidm2eurostagId) throws IOException {
		if (t == null) {
			throw new RuntimeException("Transformer must be not null");
		}
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}
		if (log.isDebugEnabled()) {
			log.trace("Dumping data for: " +t.getId());
		}

		HashMap<String, Object> zm = new HashMap<String, Object>();

		//change node.name, according to the mapping  iidm2eurostagId<cimid, eurostagid>
		if (iidm2eurostagId!=null) {
			String substNodeName1=iidm2eurostagId.get(t.getTerminal1().getBusBreakerView().getConnectableBus().getId());
			String substNodeName2=iidm2eurostagId.get(t.getTerminal2().getBusBreakerView().getConnectableBus().getId());
			if ((substNodeName1!=null) && (!"".equals(substNodeName1)) && (substNodeName2!=null) && (!"".equals(substNodeName2))) {
				zm.put("sending.node",substNodeName2);
				zm.put("receiving.node",substNodeName1);
			}

			if (log.isDebugEnabled()) {
				log.trace(" sending.node mapped to:" + substNodeName2);
				log.trace(" receiving.node mapped to:" + substNodeName1);
			}
		}

		zm.put("index",parallelIndexes.get(t.getId()));

		String newTypeName = "";
		switch (typeName) {
		case "A14_HT":
			zm.put("R","0");
			zm.put("E1","0.015");
			zm.put("E2","0.01");
			zm.put("T1","30.0");
			zm.put("TINT","10.0");
			zm.put("setpoint","2");
			zm.put("time.margin","3.0");
			zm.put("tap.direction","-1");
			zm.put("TMAN","10.0");
			zm.put("V1","0.5");
			zm.put("V2","0.6");
			zm.put("TV1","30.0");
			zm.put("TDEL","10.0");
			newTypeName = "A14";
			break;
		case "A14_TD":
			zm.put("T1","23.0");
			zm.put("TINT","6.0");
			zm.put("setpoint","1");
			zm.put("VC","0.98");
			zm.put("time.margin","0.");
			zm.put("control.type",1);
			zm.put("tap.direction","+1");
			zm.put("transfo.side","S");
			zm.put("TMAN","0.0");
			newTypeName = "A14";
			break;
		default:
			log.error("not supported keyword " + typeName);
			break;
		}

		log.trace(" typename: " + typeName);

		EurostagRecord eRecord=new EurostagRecord(newTypeName, zm);

		try {
			DtaParser.dumpZone(eRecord,out);
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
	}
	public void dumpDataLoadAutomaton(Load l, SimulatorInst simInst, String typeName, PrintStream out, Map<String, String> iidm2eurostagId) throws IOException {
		if (l == null) {
			throw new RuntimeException("Load must be not null");
		}
		if (simInst == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (simInst.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}
		if (log.isDebugEnabled()) {
			log.trace("Dumping data for: " +l.getId());
		}

		HashMap<String, Object> zm = new HashMap<String, Object>();

		//change node.name, according to the mapping  iidm2eurostagId<cimid, eurostagid>
		if (iidm2eurostagId!=null) {
			String substNodeName1=iidm2eurostagId.get(l.getId());
			String substNodeName2=iidm2eurostagId.get(l.getTerminal().getBusBreakerView().getConnectableBus().getId());
			if ((substNodeName1!=null) && (!"".equals(substNodeName1)) && (substNodeName2!=null) && (!"".equals(substNodeName2))) {
				zm.put("sending.node",substNodeName2);
				zm.put("receiving.node",substNodeName1);
			}

			if (log.isDebugEnabled()) {
				log.trace(" sending.node mapped to:" + substNodeName2);
				log.trace(" receiving.node mapped to:" + substNodeName1);
			}
		}
		String newTypeName = "";
		switch (typeName) {
		case "A14_MT":
			zm.put("index","1");
			zm.put("R","0");
			zm.put("E1","0.015");
			zm.put("E2","0.01");
			zm.put("T1","60.0");
			zm.put("TINT","10.0");
			zm.put("setpoint","2");
			zm.put("time.margin","3.0");
			zm.put("tap.direction","-1");
			zm.put("TMAN","10.0");
			zm.put("V1","0.5");
			zm.put("V2","0.6");
			zm.put("TV1","60.0");
			zm.put("TDEL","10.0");
			newTypeName = "A14";
			break;
		default:
			log.error("not supported keyword " + typeName);
			break;
		}

		log.trace(" typename: " + typeName);

		EurostagRecord eRecord=new EurostagRecord(newTypeName, zm);

		try {
			DtaParser.dumpZone(eRecord,out);
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
	}	
	public void dumpDataRST(SimulatorInst eurostagSim, DDBManager ddbmanager,
			PrintStream dtaOutStream, Map<String, String> iidm2eurostagId, Path workingDir) throws ParseException, IOException {

		if (network == null) {
			throw new RuntimeException("Network must be not null");
		}
		if (eurostagSim == null) {
			throw new RuntimeException("SimulatorInst must be not null");
		}
		if (ddbmanager == null) {
			throw new RuntimeException("DDBManager must be not null");
		}
		if (eurostagSim.getSimulator() != Simulator.EUROSTAG) {
			throw new RuntimeException("Simulator must be EUROSTAG");
		}
		if (iidm2eurostagId==null) {
			throw new RuntimeException("Iidm2Eurostag dictionary must not be null");
		}
		if(configExport.getExportRST()) {
			//TODO : automatic choose of RST generators
			// Pilot generators configuration : parsing
			String generatorsRaw = configExport.getRSTPilotGenerators();
			String[] zonesRaw = generatorsRaw.split(";");
			for(String zoneRaw : zonesRaw) {
				String[] zoneGenerator = zoneRaw.split(":");
				pilotGeneratorsRst.put(zoneGenerator[0], zoneGenerator[1]);
			}
			
			// M21
			List<Equipment> equipsAll = ddbmanager.findEquipmentsAll();
			for(Equipment eq : equipsAll) {
				ModelTemplate mt=ddbmanager.findModelTemplate(eq, eurostagSim);
				if (mt==null) {
					throw new RuntimeException("Could not find any model templates for simulator: " + eurostagSim.toString());
				}

				if(eq.getModelContainer().getDdbId().equals(MTC_PREFIX_NAME + "M21")) {
					if (log.isDebugEnabled()) {
						log.trace("Dumping data for: " +eq.getCimId());
					}
					String zoneTypeName = "M21";
					log.trace(" model template typename: " + mt.getTypeName());
					Parameters pars=ddbmanager.findParameters(eq, eurostagSim);
					HashMap<String, Object> zm=Utils.getHashMapFromParameters(pars);

					String zoneRST = (String) zm.get("machine.name");
					// Modification pilot point
					String pilotPoint = (String) zm.get("connection.node.name");
					String substpilotPoint = null;
					for(VoltageLevel station : network.getVoltageLevels()) {
						if(station.getName().equals(pilotPoint)) {
							// Looking for a connected bus
							for(Bus bus : station.getBusBreakerView().getBuses()) {
								if(bus.getConnectedComponent().getNum() == ConnectedComponent.MAIN_CC_NUM || substpilotPoint == null) {  
									substpilotPoint = iidm2eurostagId.get(bus.getId());
								}
							}
							pilotPointRst.put(zoneRST, station.getId());
						}
					}
					if(substpilotPoint == null) {
						log.warn("Pilot point "+pilotPoint+" isn't in the network - Broken RST zone: "+zoneRST);
					}
					zm.put("connection.node.name",substpilotPoint);

					EurostagRecord eRecord=new EurostagRecord(zoneTypeName, zm);
					try {
						DtaParser.dumpZone(eRecord, dtaOutStream);

					} catch (ParseException e) {
						log.error(e.getMessage(),e);
					}
					// Regulator
					dumpDataInternals(workingDir, zoneRST, eq, eurostagSim, iidm2eurostagId, ddbmanager, dtaOutStream, network);
					// Regulator files
					String intName = configExport.getRSTRegulInjector().toLowerCase();
					boolean foundFRI=false;
					boolean foundFRM=false;
					boolean foundPAR=false;
					boolean foundPCP=false;
					boolean foundRCP=false;
					boolean foundRegFile=false;
					if ((mt.getSimulator().getSimulator() == Simulator.EUROSTAG)
							&& (mt.getSimulator().getVersion()
									.equals(eurostagVersion))) {

						for (String dataKey : mt.modelDataMap()
								.keySet()) {
							switch (dataKey) {
							case "pcp":
								foundRegFile=true;
								foundPCP=true;
								break;
							case "rcp":
								foundRegFile=true;
								foundRCP=true;
								break;
							case "fri":
								foundRegFile=true;
								foundFRI=true;
								break;
							case "frm":
								foundRegFile=true;
								foundFRM=true;
								break;
							case "par":
								foundRegFile=true;
								foundPAR=true;
								break;
							default:
								log.warn("- regfile extension not recognized: " + dataKey);
								break;
							}

							if (foundRegFile) {
								Path path = workingDir.resolve(intName + "." + dataKey);
								//avoiding dups
								if (!Files.exists(path)) {
									Files.write(path, mt.getData(dataKey));
								}
							}
							//keeps track of reg name, pro .pcp, .rcp compiling
							//skipping those regs that do not have both .rcp and .pcp files
							if (((foundFRI==true) && (foundFRM==true) && (foundPAR==true))
									&& ((foundPCP==false) || (foundRCP==false))) {
								uniqueRegNamesSet.add(intName);
							}
						}
					}
				}
			}
		}
	}
	public void dumpDataACMC(SimulatorInst eurostagSim, DDBManager ddbmanager,
			PrintStream dtaOutStream, Map<String, String> iidm2eurostagId, Path workingDir) throws ParseException, IOException {

		if(configExport.getExportACMC()) {

			// MA
			List<Equipment> equipsAll = ddbmanager.findEquipmentsAll();
			for(Equipment eq : equipsAll) {
				ModelTemplate mt=ddbmanager.findModelTemplate(eq, eurostagSim);
				if (mt==null) {
					throw new RuntimeException("Could not find any model templates for simulator: " + eurostagSim.toString());
				}

				if(eq.getModelContainer().getDdbId().equals(MTC_PREFIX_NAME + "MA")) {
					if (log.isDebugEnabled()) {
						log.trace("Dumping data for: " +eq.getCimId());
					}
					String zoneTypeName = "MA";
					log.trace(" model template typename: " + mt.getTypeName());
					Parameters pars=ddbmanager.findParameters(eq, eurostagSim);
					HashMap<String, Object> zm=Utils.getHashMapFromParameters(pars);

					ArrayList<String> bankInAcmc = new ArrayList<String>();
					String acmcName = (String) zm.get("ma.name");
					// Modification shunt nameS
					String connectingStation = (String) zm.get("equipment.name");
					for(VoltageLevel station : network.getVoltageLevels()) {
						if(station.getName().equals(connectingStation)) {
							// Looking for shunts at the connecting station
							for(ShuntCompensator shunt : station.getShunts()) {
								bankInAcmc.add(iidm2eurostagId.get(shunt.getId()));
							}
						}
					}
					if(bankInAcmc.isEmpty()) {
						log.warn("No shunt/station found for station name :"+connectingStation);
					} else {
						acmcs.put(acmcName, bankInAcmc);
						zm.put("equipment.name",bankInAcmc.get(0)); // Any of the banks = not used

						EurostagRecord eRecord=new EurostagRecord(zoneTypeName, zm);
						try {
							DtaParser.dumpZone(eRecord, dtaOutStream);
						} catch (ParseException e) {
							log.error(e.getMessage(),e);
						}

						// Regulator RMA
						Set<String> internalIds = getConnectedInternals(acmcName, ddbmanager);
						log.info("- connected internals: " + acmcName+": "+ internalIds);
						for (String nativeId : internalIds) {
							Internal internal = ddbmanager.findInternal(nativeId);
							if (internal == null) {
								log.warn("- internal with nativeId: " + nativeId + " not found !");
							} else {
								try {
									dumpData(internal, eurostagSim, ddbmanager, dtaOutStream, acmcName, iidm2eurostagId, network);
								} catch (Exception e) {
									log.error("could not write macro.lis file, due to " + e.getMessage());
								}
								// dump regulator files start
								String macroblockName=ddbmanager.getStringParameter(internal, eurostagSim, PAR_MACROBLOCK__NAME);
								if (macroblockName == null) {
									throw new RuntimeException("null macroblock.name for internal " + internal);
								}
								//since this will drive regs filenames, normalize to lower case (pro case sensitive filesystems)
								String intName = macroblockName.toLowerCase();
								ModelTemplateContainer mtc = internal.getModelContainer();

								boolean foundFRI=false;
								boolean foundFRM=false;
								boolean foundPAR=false;
								boolean foundPCP=false;
								boolean foundRCP=false;
								boolean foundRegFile=false;
								for (ModelTemplate mt2 : mtc.getModelTemplates()) {
									if ((mt2.getSimulator().getSimulator() == Simulator.EUROSTAG)
											&& (mt2.getSimulator().getVersion().equals(eurostagVersion))) {

										for (String dataKey : mt2.modelDataMap()
												.keySet()) {
											switch (dataKey) {
											case "pcp":
												foundRegFile=true;
												foundPCP=true;
												break;
											case "rcp":
												foundRegFile=true;
												foundRCP=true;
												break;
											case "fri":
												foundRegFile=true;
												foundFRI=true;
												break;
											case "frm":
												foundRegFile=true;
												foundFRM=true;
												break;
											case "par":
												foundRegFile=true;
												foundPAR=true;
												break;
											default:
												log.warn("- regfile extension not recognized: " + dataKey);
												break;
											}

											if (foundRegFile) {
												Path path = workingDir.resolve(intName + "." + dataKey);
												//avoiding dups
												if (!Files.exists(path)) {
													try {
														Files.write(path, mt2.getData(dataKey));
													} catch (Throwable e) {
														log.error(e.getMessage(), e);
													}
												}
											}

										}

									}
								}
								//keeps track of reg name, pro .pcp, .rcp compiling
								//skipping those regs that do not have both .rcp and .pcp files
								if (((foundFRI==true) && (foundFRM==true) && (foundPAR==true))
										&& ((foundPCP==false) || (foundRCP==false))) {
									uniqueRegNamesSet.add(intName);
								}
							}
						}
					}
				}
			}
			for(String acmc : acmcs.keySet()) {
				ArrayList<String> bankInAcmc = acmcs.get(acmc);
				char internalVariableCount = 'A';
				for(String bank : bankInAcmc) {
					// A33 + EVT N225
					HashMap<String, Object> zm2 = new HashMap<String, Object>();
					zm2.put("keyword","A33");
					zm2.put("ma.name",acmc);
					zm2.put("interface.name","N225"+internalVariableCount);
					zm2.put("block.number","---");
					zm2.put("c.type",1);
					zm2.put("S1","0.7");
					zm2.put("S2","0.5");
					zm2.put("S3","2.0");
					zm2.put("T1","0.0");
					zm2.put("TDEL","0.0");
					zm2.put("ev.keyword","EV");
					zm2.put("ev.type","CAP BANK");
					zm2.put("equipment.name",bank);
					zm2.put("seq.params","1   1  0                                          0.");

					EurostagRecord eRecord=new EurostagRecord("A33_ACMC", zm2);

					try {
						DtaParser.dumpZone(eRecord,dtaOutStream);
					} catch (ParseException e) {
						log.error(e.getMessage(),e);
					}

					// A33 + EVT D225
					zm2 = new HashMap<String, Object>();
					zm2.put("keyword","A33");
					zm2.put("ma.name",acmc);
					zm2.put("interface.name","D225"+internalVariableCount);
					zm2.put("block.number","---");
					zm2.put("c.type",1);
					zm2.put("S1","0.7");
					zm2.put("S2","0.5");
					zm2.put("S3","2.0");
					zm2.put("T1","0.0");
					zm2.put("TDEL","0.0");
					zm2.put("ev.keyword","EV");
					zm2.put("ev.type","CAP BANK");
					zm2.put("equipment.name",bank);
					zm2.put("seq.params","-1  1  0                                          0.");

					eRecord=new EurostagRecord("A33_ACMC", zm2);

					try {
						DtaParser.dumpZone(eRecord,dtaOutStream);
					} catch (ParseException e) {
						log.error(e.getMessage(),e);
					}
					internalVariableCount++;
				}
			}
		}
	}

	public SimulatorInst getOrCreateEurostagSimulatorInst(DDBManager ddbmanager, String eurostagVersion) {
		SimulatorInst eurostagSim=ddbmanager.findSimulator(Simulator.EUROSTAG, eurostagVersion);
		if (eurostagSim==null) {
			log.debug("* Creating simulator Eurostag, version " + eurostagVersion);
			eurostagSim=new SimulatorInst(Simulator.EUROSTAG,eurostagVersion);
			eurostagSim=ddbmanager.save(eurostagSim);
		}
		return eurostagSim;
	}

	private Set<String> getConnectedInternals(String cimId, DDBManager ddbmanager) {
		ConnectionSchema cs = ddbmanager.findConnectionSchema(
				cimId, null);
		Set<String> internalIds = new HashSet<String>();
		if (cs != null) {
			//retrieve internal ids from equipment connection schema
			for (Connection connection : cs.getConnections()) {
				if ((connection.getConType() == 0) && (connection.getId2Type() == 1)) {
					// log.trace(connection);
					internalIds.add(connection.getId2());
				} else 	if ((connection.getConType() == 1) && (connection.getId2Type() == 1)) {
					internalIds.add(connection.getId1());
					internalIds.add(connection.getId2());
				} 
			} 
		}
		return internalIds;
	}

	/* ********************************
	 * Clean up Functions
	 ********************************* */
	public void unloadEurostagData() {
		try  (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);

			for(Equipment eq : ddbmanager.findEquipmentsAll()) {
				removeEquipment(eq.getCimId(), ddbmanager);
			}
			for(Internal in : ddbmanager.findInternalsAll()) {
				removeInternal(in.getNativeId(), ddbmanager);
			}
			for(ConnectionSchema cs : ddbmanager.findConnectionSchemasAll()) {
				ddbmanager.delete(cs);
			}
			for(ModelTemplateContainer mtc : ddbmanager.findModelTemplateContainerAll()) {
				removeModelTemplateContainer(mtc.getDdbId(), ddbmanager);
			}			
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	public void removeEquipment(String cimId, DDBManager ddbmanager) {
		log.debug("Removing equipment: " + cimId);

		Equipment eq1 = ddbmanager.findEquipment(cimId);
		if (eq1 != null) {
			// remove its paramcontainer, as well
			ParametersContainer pc1 = eq1.getParametersContainer();
			// this unlink the PC from the EQ, otherwise JPA would not be able
			// to delete the entity (throws an exc)
			eq1.setParametersContainer(null);
			ddbmanager.save(eq1);
			ddbmanager.delete(pc1);
			ddbmanager.delete(eq1);

			// final step, remove the connection schema
			ConnectionSchema cs = ddbmanager.findConnectionSchema(cimId, null);
			if (cs != null) {
				ddbmanager.delete(cs);
			}
		} else {
			log.warn("* Equipment  " + cimId + " does not exist.");
		}
	}
	public void removeEquipment(String cimId) {
		try (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
			removeEquipment(cimId, ddbmanager);
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
	public void removeInternal(String nativeId, DDBManager ddbmanager) {
		log.debug("Removing internal: " + nativeId);

		Internal eq1 = ddbmanager.findInternal(nativeId);
		if (eq1 != null) {
			// remove its parametercontainer, as well
			ParametersContainer pc1 = eq1.getParametersContainer();
			// this unlink the PC from the EQ, otherwise JPA would not be able
			// to delete the entity (throws an exc)
			eq1.setParametersContainer(null);
			ddbmanager.save(eq1);
			ddbmanager.delete(pc1);
			ddbmanager.delete(eq1);
		} else {
			log.warn("* Internal  " + nativeId + " does not exist.");
		}
	}
	public void removeInternal(String nativeId) {
		try (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
			removeInternal(nativeId, ddbmanager);
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
	}
	public void removeModelTemplateContainer(String mtcId, DDBManager ddbmanager) {
		log.debug("Removing modeltemplatecontainer: " + mtcId);

		ModelTemplateContainer eq1 = ddbmanager
				.findModelTemplateContainer(mtcId);
		if (eq1 != null) {
			ddbmanager.delete(eq1);
		} else {
			log.warn("* modeltemplatecontainer  " + mtcId + " does not exist.");
		}
	}
	public void removeModelTemplateContainer(String mtcId) {
		try (EjbClientCtx cx=newEjbClientEcx()){
			DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
			removeModelTemplateContainer(mtcId, ddbmanager);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
	public void unloadEurostagTemplatesCatalog(DDBManager ddbmanager, String eurostagVersion) {
		try {
			SimulatorInst eurostagSim=getOrCreateEurostagSimulatorInst(ddbmanager, eurostagVersion);
			removeFromDDBEurostagModelTemplates(estg, eurostagSim, ddbmanager);

		} catch (Throwable e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			//Utils.closeContext(context);
		}
	}
	private void removeFromDDBEurostagModelTemplates(
			String keyNamesAndDescriptions[][],
			SimulatorInst eurostagSimulator, DDBManager ddbmanager) {
		log.debug("Removing eurostag catalog (Eurostag 5.1.1)");

		for (int i = 0; i < keyNamesAndDescriptions.length; i++) {
			String key = keyNamesAndDescriptions[i][0];
			ModelTemplateContainer mtc1 = ddbmanager
					.findModelTemplateContainer(MTC_PREFIX_NAME + key);
			if (mtc1 != null) {
				ddbmanager.delete(mtc1);
			}
		}
	}

}
