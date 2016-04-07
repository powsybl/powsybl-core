/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.psse_imp_exp;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import eu.itesla_project.iidm.ddb.model.*;
import eu.itesla_project.iidm.ddb.service.DDBManager;
import eu.itesla_project.iidm.ejbclient.EjbClientCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;



/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class DdbDyrLoader {

    static Logger log = LoggerFactory.getLogger(DdbDyrLoader.class);
    
    private FileWriter writer;

    private static final String MTC_PREFIX_NAME = "MTC_";
    private static final String PC_PREFIX_NAME = "PC_";

    //DDBManager jndi name
    private static final String DDBMANAGERJNDINAME = "ejb:iidm-ddb-ear/iidm-ddb-ejb-0.0.1-SNAPSHOT/DDBManagerBean!eu.itesla_project.iidm.ddb.service.DDBManager";


    public EjbClientCtx newEjbClientEcx(DdbConfig config) throws NamingException {
        return new EjbClientCtx(config.getJbossHost(), Integer.parseInt(config.getJbossPort()), config.getJbossUser(), config.getJbossPassword());
    }


    /*
    * read mapping psseId,rdfId from a csv file
    */
    private Map<String, String> readWithCsvMapReader(Path mappingFile) throws IOException {
        Map<String, String> retMap = new HashMap<>();
        try (ICsvMapReader mapReader = new CsvMapReader(Files.newBufferedReader(mappingFile, StandardCharsets.UTF_8), CsvPreference.STANDARD_PREFERENCE)) {
            final String[] header = mapReader.getHeader(true);
            log.info(" cvsheader length: " + header.length);
            final CellProcessor[] rowProcessors = new CellProcessor[header.length];
            for (int i = 0; i < rowProcessors.length; i++) {
                if (i == 0) {
                    rowProcessors[i] = new NotNull();
                } else {
                    rowProcessors[i] = new NotNull();
                }
            }

            Map<String, Object> componentMap;
            while ((componentMap = mapReader.read(header, rowProcessors)) != null) {
                String psseId = (String) componentMap.get(header[0]);
                String rdfId = (String) componentMap.get(header[1]);
                if (psseId == null) {
                    log.warn("psseId=" + psseId + ", rdfId=" + rdfId);
                } else {
                    if (retMap.containsKey(psseId)) {
                        log.warn("psseId=" + psseId + " already in the map");
                    }
                    retMap.put(psseId, rdfId);
                }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("ids map: " + retMap);
        }
        log.info("ids map: " + retMap);
        return retMap;
    }


    public SimulatorInst getOrCreatePsseSimulatorInst(DDBManager ddbmanager, String version) {
        SimulatorInst simulator = ddbmanager.findSimulator(Simulator.PSSE, version);
        if (simulator == null) {
            log.debug("* Creating simulator PSSE, version " + version);
            simulator = new SimulatorInst(Simulator.PSSE, version);
            simulator = ddbmanager.save(simulator);
        }
        return simulator;
    }

    public static byte[] stringAsByteArrayUTF8(String par) {
        try {
            return par.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return new byte[] {};
        }
    }


    /**
     * @param dyrPath
     * @param psseCimMappingPath
     * @param psseVersion
     * @param config
     * @param deleteData
     * @throws Exception
     */
    public void load(Path dyrPath, Path psseCimMappingPath, String psseVersion, DdbConfig config, boolean deleteData) throws IOException {
        Objects.requireNonNull(psseCimMappingPath, "path must be not null");
        Map<String, String> psseCimMapping = readWithCsvMapReader(psseCimMappingPath);
        load(dyrPath, psseCimMapping, psseVersion, config, deleteData);
    }

    /**
     * @param dyrPath
     * @param psseCimMappingPath
     * @param psseVersion
     * @param config
     * @throws Exception
     */
    public void load(Path dyrPath, Path psseCimMappingPath, String psseVersion, DdbConfig config) throws IOException {
        Objects.requireNonNull(psseCimMappingPath, "path must be not null");
        Map<String, String> psseCimMapping = readWithCsvMapReader(psseCimMappingPath);
        load(dyrPath, psseCimMapping, psseVersion, config, false);
    }


    /**
     * @param dyrPath
     * @param cimPsseIdsMap
     * @param psseVersion
     * @param config
     * @param deleteData
     * @throws IOException
     */
    public void load(Path dyrPath, Map<String, String> cimPsseIdsMap, String psseVersion, DdbConfig config, boolean deleteData) throws IOException {
    	writer = new FileWriter(new File("DdbDyrLoader.log"));
    	
    	
        log.info("Loading {}", dyrPath);
        List<PsseRegister> aList = null;
        aList = PsseDyrParser.parseFile(dyrPath.toFile());

        Set<String> mtcSet=new LinkedHashSet<>();
        ArrayList<String> ptcList=new ArrayList<>();
        ArrayList<String> internalsList=new ArrayList<>();
        ArrayList<String> equipmentsList=new ArrayList<>();


        //partitions the list of PsseRegisters by  dyrId=r.getBusNum() + "_" + r.getId()
        //NOTE: this dyrId is the default internal id used in resolving mappings iidm ID - psse ID
        Multimap<String, PsseRegister> groupsByBus = Multimaps.index(aList, new Function<PsseRegister, String>() {
            @Override
            public String apply(final PsseRegister s) {
                return s.getBusNum() + "_" + s.getId();
            }
        });


        try (EjbClientCtx cx = newEjbClientEcx(config)) {
            DDBManager ddbmanager = cx.connectEjb(DDBMANAGERJNDINAME);
            SimulatorInst simulator = getOrCreatePsseSimulatorInst(ddbmanager, psseVersion);

            //iterate over the (dyrId=r.getBusNum() + "_" + r.getId()) indexed collection
            int counter=0;
            for (String dyrId : groupsByBus.keySet()) {
                counter=counter+1;
                //get iddmId from mapping (read from file)
                String iidmId = cimPsseIdsMap.get(dyrId);
                if (iidmId == null) {
                    //throw new RuntimeException("  No mapping found for ", dyrId);
                    log.warn("No mapping found for bus_id {}", dyrId);
                    continue;
                }

                //for a given dyrId, partitions the sublist of related registers in equipment and internals
                ListMultimap<String, PsseRegister> groupByBusByType = Multimaps.index(groupsByBus.get(dyrId), new Function<PsseRegister, String>() {
                    @Override
                    public String apply(final PsseRegister s) {
                        return ((PsseRegisterType.isEquipment(s) == true) ? "equipment" : "internal");
                    }
                });
                // check the assumption that for a given dyrId there must be just one machine: if not , ERROR
                if (groupByBusByType.get("equipment").size() != 1) {
                    throw new RuntimeException("must be 1 machine in " + dyrId);
                }

                //get the machine register and the other components registers
                PsseRegister equipmentReg = (PsseRegister) groupByBusByType.get("equipment").get(0);
                List<PsseRegister> internalsRegs = groupByBusByType.get("internal");

                //logs some info about this group machine-regulators
                List<String> modelsNamesList = FluentIterable.from(internalsRegs).transform(new Function<PsseRegister, String>() {
                    public String apply(PsseRegister f) {
                        String rName = f.getModel();
                        return rName;
                    }
                }).toList();
                log.info("bus_id:{}, generator model: {}; regulators models: {}", dyrId, equipmentReg.getModel(), modelsNamesList);


                //create the ddb data
                String equipmentId = iidmId;
                String equipmentMtcId = MTC_PREFIX_NAME + equipmentReg.getModel();
                String equipmentPcId = dyrId;

                //step a:  process Equipment
//                log.info("Equipment: iidmId {}", equipmentId);
//                log.info("  MTC id: {}", equipmentMtcId);
//                log.info("  PC id: {}", equipmentPcId);
//                for (String parName : equipmentReg.parameters.keySet()) {
//                    log.info("    {}={}", parName, equipmentReg.parameters.get(parName));
//                }

                ModelTemplateContainer eqMtc = ddbmanager.findModelTemplateContainer(equipmentMtcId);
                if (eqMtc == null) {
                    log.debug("-- creating MTC " + equipmentMtcId);
                    eqMtc = new ModelTemplateContainer(equipmentMtcId, "");
                    ModelTemplate mt = new ModelTemplate(simulator, equipmentReg.getModel(), "");
                    // must be decided what to put in data attribute; here we put
                    // again the typename, as in the original example circulated
                    mt.setData("data", DdbDyrLoader.stringAsByteArrayUTF8(equipmentReg.getModel()));
                    eqMtc.getModelTemplates().add(mt);
                    eqMtc = ddbmanager.save(eqMtc);

                } else {
                    log.info("-- MTC " + equipmentMtcId + " already defined, id: " + eqMtc.getId());
                }
                mtcSet.add(equipmentMtcId);
                ParametersContainer eqPc = ddbmanager.findParametersContainer(equipmentPcId);
                if (eqPc == null) {
                    log.info("-- creating PC " + equipmentPcId + " plus parameters.");
                    eqPc = new ParametersContainer(equipmentPcId);
                    Parameters pars = new Parameters(simulator);
                    for (String parName : equipmentReg.parameters.keySet()) {
                        //tokenize parname (separator, comma char) to handle cases where one psse var leads to a couple of parameters (with the same value)
                        //eg. "Xppd,Xppq" in GENROU
                        List<String> splitParamNames = Arrays.asList(parName.trim().split("\\s*,\\s*"));
                        for(String spName: splitParamNames){
                            log.info("    {}={}", spName, equipmentReg.parameters.get(parName));
                            pars.addParameter(new ParameterFloat(spName, equipmentReg.parameters.get(parName)));
                        }
                    }
                    eqPc.getParameters().add(pars);
                    eqPc = ddbmanager.save(eqPc);
                }else {
                    log.info("-- PC " + equipmentPcId + " already defined, id: " + eqPc.getId());
                }
                ptcList.add(equipmentPcId);

                Equipment equipment = ddbmanager.findEquipment(equipmentId);
                if (equipment == null) {
                    log.info("-- creating Equipment " + equipmentId + "; psse name is: " + dyrId);
                    equipment = new Equipment(equipmentId);
                    equipment.setModelContainer(eqMtc);
                    equipment.setParametersContainer(eqPc);
                    equipment = ddbmanager.save(equipment);
                } else {
                    log.info("-- Equipment  " + equipmentId + " already defined, id: " + equipment.getId());
                }
                equipmentReg.setId(equipmentId);
                equipmentsList.add(equipmentId);

                //create a connection schema, if needed
                ConnectionSchema cs = ddbmanager.findConnectionSchema(equipmentId, null);
                if (cs == null) {
                    log.info("- creating conn. schema with cimId " + equipmentId);
                    cs = new ConnectionSchema(equipmentId, null);
                    cs = ddbmanager.save(cs);
                } else {
                    log.info("- conn. schema with cimId "+equipmentId+", already exists! currently defined connections: " + cs.getConnections());
                }

                writer.write("Equipment: " + equipmentReg.getModel() + "\n");
                
                //step b:  process Internals
                boolean hasHygov = false;
                boolean hasPss2a = false;
                boolean hasTgov1 = false;
                boolean hasIeesgo = false;
                for (PsseRegister regulatorRegister : internalsRegs) {
                  	if(regulatorRegister.getModel().endsWith("HYGOV")) {
                		hasHygov = true;
                	}
                	if(regulatorRegister.getModel().endsWith("PSS2A")) {
                		hasPss2a = true;
                	}
                	if(regulatorRegister.getModel().endsWith("TGOV1")) {
                		hasTgov1 = true;
                	}
                	if(regulatorRegister.getModel().endsWith("IEESGO")) {
                		hasIeesgo = true;
                	}
                	
                    String internalId = iidmId + "_" + regulatorRegister.getModel();
                    String internalMtcId = MTC_PREFIX_NAME + regulatorRegister.getModel();
                    String internalPcId = PC_PREFIX_NAME + iidmId + "_" + regulatorRegister.getModel();

//                    log.info(" Internal: id {}", internalId);
//                    log.info("   MTC id: {}", internalMtcId);
//                    log.info("   PC id: {}", internalPcId);
//                    for (String parName : regulatorRegister.parameters.keySet()) {
//                        log.info("     {}={}", parName, regulatorRegister.parameters.get(parName));
//                    }

                    ModelTemplateContainer internalMtc = ddbmanager.findModelTemplateContainer(internalMtcId);
                    if (internalMtc == null) {
                        log.info("-- creating MTC " + internalMtcId);
                        internalMtc = new ModelTemplateContainer(internalMtcId, "");
                        ModelTemplate mt = new ModelTemplate(simulator, regulatorRegister.getModel(), "");
                        // must be decided what to put in data attribute; here we put
                        // again the typename, as in the original example circulated
                        mt.setData("data", DdbDyrLoader.stringAsByteArrayUTF8(regulatorRegister.getModel()));
                        internalMtc.getModelTemplates().add(mt);
                        internalMtc = ddbmanager.save(internalMtc);

                    } else {
                        log.info("-- MTC " + internalMtcId + " already defined, id: " + internalMtc.getId());
                    }
                    mtcSet.add(internalMtcId);
                    ParametersContainer intPc = ddbmanager.findParametersContainer(internalPcId);
                    if (intPc == null) {
                        log.info("-- creating PC " + internalPcId + " plus parameters.");
                        intPc = new ParametersContainer(internalPcId);
                        Parameters pars = new Parameters(simulator);
                        for (String parName : regulatorRegister.parameters.keySet()) {
                            List<String> splitParamNames = Arrays.asList(parName.trim().split("\\s*,\\s*"));
                            for(String spName: splitParamNames){
                                log.info("    {}={}", spName, regulatorRegister.parameters.get(parName));
                                pars.addParameter(new ParameterFloat(spName, regulatorRegister.parameters.get(parName)));
                            }
                        }
                        intPc.getParameters().add(pars);
                        intPc = ddbmanager.save(intPc);
                    } else {
                        log.info("-- PC " + internalPcId + " already defined, id: " + intPc.getId());
                    }
                    ptcList.add(internalPcId);
                    Internal internal = ddbmanager.findInternal(internalId);
                    if (internal == null) {
                        log.info("-- Creating Internal " + internalId);
                        internal = new Internal(internalId);
                        internal.setModelContainer(internalMtc);
                        internal.setParametersContainer(intPc);
                        internal = ddbmanager.save(internal);
                    } else {
                        log.info("-- Internal  " + internalId + " already defined, id: " + internal.getId());
                    }
                    regulatorRegister.setId(internalId);
                    internalsList.add(internalId);
                }
                
                //Connection schemas
                List<Connection> clist=cs.getConnections();
                // first create equipment-regulators connections
                for(PsseRegister regulatorRegister: internalsRegs) {
                    LinkedHashSet<String> sharedPins=PsseRegisterFactory.intersectPinsSets(equipmentReg, regulatorRegister);
                    if (sharedPins.size()>0) {
                        log.info("-- connection eq-int {}-{}  on pins {}", equipmentReg.getModel(), regulatorRegister.getModel(), sharedPins);
                        for (String pinName: sharedPins) {
                            log.info("----  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, pinName, pinName, "internal", regulatorRegister.getId());
                            Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, pinName,
                                    pinName, Connection.INSIDE_CONNECTION);
                            if (!(clist.contains(newC))) {
                                log.info("----- creating connection " + newC);
                                log.info("------- clist size" + clist.size());
                                clist.add(newC);
                            } else {
                                log.info("----- connection already exist: " + newC);
                            }
                        }
                    } else {
                        log.info("-- connection eq-int {}-{} does not exist: no shared pins names", equipmentReg.getModel(), regulatorRegister.getModel());
                    }
                    //possibly create connections  equipment-regulator when pin names are different
                    //here is hardcoded the one and only case known, so far: ETERM(equipment), ECOMP(all regulators thaqt have this pin)
                    //There is also the case ETERM (equipment) and V_CT (IEEEST)
                    String eqPin="ETERM";
                    String regPin="ECOMP";
                    if ((equipmentReg.pins.contains(eqPin) && (regulatorRegister.pins.contains(regPin)))) {
                        log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                        Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
                    }
                    
                    regPin="V_CT";
                    if ((equipmentReg.pins.contains(eqPin) && (regulatorRegister.getModel().equalsIgnoreCase("IEEEST")))) {
                        log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                        Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
                    }
 
                    regPin="VT";
                    if (equipmentReg.pins.contains(eqPin) && regulatorRegister.Model.equalsIgnoreCase("ESST1A")) {
                        log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                        Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
                    }
                    
                    if (equipmentReg.pins.contains(eqPin) && regulatorRegister.Model.equalsIgnoreCase("ESDC2A")) {
                        log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                        Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
                    }
                    
                    //This is a rule from PSS/E about how to connect IEEEST.V_S
                    regPin = "V_S";
                    if(regulatorRegister.Model.equalsIgnoreCase("IEEEST")) {
                    	if((regulatorRegister.parameters.get("IM") == 3) && regulatorRegister.parameters.get("IM1") == 0) {
                    		eqPin = "PELEC";
                            log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                            log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                            Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                            if (!(clist.contains(newC))) {
                                log.info("----- creating connection " + newC);
                                log.info("------- clist size" + clist.size());
                                clist.add(newC);
                            } else {
                                log.info("----- connection already exist: " + newC);
                            }
                    	}
                    }
                    
                    if(equipmentReg.Model.equals("GENSAL") && hasPss2a && hasHygov) {
                    	if(regulatorRegister.getModel().equals("PSS2A")) {
                    		eqPin = "PELEC";
                    		regPin = "V_S2";
                            log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                            log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                            Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                            if (!(clist.contains(newC))) {
                                log.info("----- creating connection " + newC);
                                log.info("------- clist size" + clist.size());
                                clist.add(newC);
                            } else {
                                log.info("----- connection already exist: " + newC);
                            }
                    	}
                    }
                    if(equipmentReg.Model.equals("GENROU") && hasPss2a && hasTgov1) {
                    	if(regulatorRegister.getModel().equals("PSS2A")) {
                    		eqPin = "PELEC";
                    		regPin = "V_S2";
                            log.info("--* connection eq-int {}-{}  on pins {},{}", equipmentReg.getModel(), regulatorRegister.getModel(), eqPin, regPin);
                            log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin, regPin, "internal", regulatorRegister.getId());
                            Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, regulatorRegister.getId(), Connection.INTERNAL_TYPE, eqPin, regPin, Connection.INSIDE_CONNECTION);
                            if (!(clist.contains(newC))) {
                                log.info("----- creating connection " + newC);
                                log.info("------- clist size" + clist.size());
                                clist.add(newC);
                            } else {
                                log.info("----- connection already exist: " + newC);
                            }
                    	}
                    }
                }
                
                // second create regulator-regulators connections
                for (int i = 0; i < internalsRegs.size(); i++) {
                    for (int j = i+1; j < internalsRegs.size(); j++) {
                    	log.info("-- for iteration {} regulator(i) {} regulator(j) {}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel());
                    	
                        LinkedHashSet<String> sharedPins=PsseRegisterFactory.intersectPinsSets(internalsRegs.get(i), internalsRegs.get(j));
                        if (sharedPins.size()>0) {
                            log.info("-- connection int-int {}-{}  on pins {}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel(), sharedPins);
                            for (String pinName: sharedPins) {
                                log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),pinName,pinName, "internal", internalsRegs.get(j).getId());
                                Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(j).getId(), Connection.INTERNAL_TYPE, pinName, pinName, Connection.INSIDE_CONNECTION);
                                if (!(clist.contains(newC))) {
                                    log.info("---- creating connection: "+ newC);
                                    clist.add(newC);
                                } else {
                                    log.info("---- connection already exists: "+ newC);
                                }
                            }
                        } else {
                            log.info("-- connection int-int {}-{}  does not exist: no shared pins names", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel());
                        }
                        
                        
                        //TODO SMF Specific connections in a specific configuration
                        String regPin1 = "V_S1";
                        String regPin2 = "SPEED";
                        
                        writer.write("Internal 1: " + internalsRegs.get(i).getModel() + ". Internal 2: " +internalsRegs.get(j).getModel()  + "\n");
                        
                        if(equipmentReg.Model.equals("GENSAL") && hasPss2a && hasHygov) {
                        	if(internalsRegs.get(i).Model.equals("PSS2A") && internalsRegs.get(j).Model.equals("HYGOV")) { 
         	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel(), regPin1, regPin2);
         	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin1,regPin2, "internal", internalsRegs.get(j).getId());
         	                    Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(j).getId(), Connection.INTERNAL_TYPE, regPin1,regPin2, Connection.INSIDE_CONNECTION);
         	                    if (!(clist.contains(newC))) {
         	                        log.info("---- creating connection: "+ newC);
         	                        clist.add(newC);
         	                    } else {
         	                        log.info("---- connection already exists: "+ newC);
         	                    }
                        	}
                        	else if (internalsRegs.get(i).Model.equals("HYGOV") && internalsRegs.get(j).Model.equals("PSS2A")){
         	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel(), regPin2, regPin1);
         	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin2,regPin1, "internal", internalsRegs.get(j).getId());
         	                    Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(j).getId(), Connection.INTERNAL_TYPE, regPin2,regPin1, Connection.INSIDE_CONNECTION);
         	                    if (!(clist.contains(newC))) {
         	                        log.info("---- creating connection: "+ newC);
         	                        clist.add(newC);
         	                    } else {
         	                        log.info("---- connection already exists: "+ newC);
         	                    }
                        	}
                        } 
                        if(equipmentReg.Model.equals("GENROU") && hasPss2a && hasTgov1) {
                        	if(internalsRegs.get(i).Model.equals("PSS2A") && internalsRegs.get(j).Model.equals("TGOV1")) { 
                        		
         	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel(), regPin1, regPin2);
         	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin1,regPin2, "internal", internalsRegs.get(j).getId());
         	                    Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(j).getId(), Connection.INTERNAL_TYPE, regPin1,regPin2, Connection.INSIDE_CONNECTION);
         	                    if (!(clist.contains(newC))) {
         	                        log.info("---- creating connection: "+ newC);
         	                        clist.add(newC);
         	                    } else {
         	                        log.info("---- connection already exists: "+ newC);
         	                    }
                        	}
                        	else if(internalsRegs.get(i).Model.equals("TGOV1") && internalsRegs.get(j).Model.equals("PSS2A")) {
         	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(j).getModel(), regPin2, regPin1);
         	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin2,regPin1, "internal", internalsRegs.get(j).getId());
         	                    Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(j).getId(), Connection.INTERNAL_TYPE, regPin2,regPin1, Connection.INSIDE_CONNECTION);
         	                    if (!(clist.contains(newC))) {
         	                        log.info("---- creating connection: "+ newC);
         	                        clist.add(newC);
         	                    } else {
         	                        log.info("---- connection already exists: "+ newC);
         	                    }
                        	}
                        }
                    }
                    
                    //TODO SMF Perhaps this will happen in several cases as if regpins.contains(VOTHSG VUEL and VOEL) ==> connect these pins
                    String regPin1;
					String regPin2;
                    if (internalsRegs.get(i).Model.equals("IEEEX1")) {
						regPin1 = "VOTHSG";
						regPin2 = "VOEL";
	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(i).getModel(), regPin1, regPin2);
	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin1,regPin2, "internal", internalsRegs.get(i).getId());
	                    Connection newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, regPin1,regPin2, Connection.INSIDE_CONNECTION);
	                    if (!(clist.contains(newC))) {
	                        log.info("---- creating connection: "+ newC);
	                        clist.add(newC);
	                    } else {
	                        log.info("---- connection already exists: "+ newC);
	                    }
	                    
	                    regPin2 = "VUEL";
	                    log.info("-- connection int-int {}-{}  on pins {},{}", internalsRegs.get(i).getModel(), internalsRegs.get(i).getModel(), regPin1, regPin2);
	                    log.info("----  {} {} {} {} {} {} {}", "inside","internal",internalsRegs.get(i).getId(),regPin1,regPin2, "internal", internalsRegs.get(i).getId());
	                    newC = new Connection(internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, internalsRegs.get(i).getId(), Connection.INTERNAL_TYPE, regPin1,regPin2, Connection.INSIDE_CONNECTION);
	                    if (!(clist.contains(newC))) {
	                        log.info("---- creating connection: "+ newC);
	                        clist.add(newC);
	                    } else {
	                        log.info("---- connection already exists: "+ newC);
	                    }
                    }
                }
                
                //third create equipment-equipment connections
                String eqPin1="PMECH";
                String eqPin2="PMECH0";
                if(equipmentReg.Model.equals("GENROU") && (!hasTgov1 && !hasIeesgo)) {
                    log.info("--* connection eq-eq {}-{}  on pins {},{}", equipmentReg.getModel(), equipmentReg.getModel(), eqPin1, eqPin2);
                    log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin1, eqPin2, "equipment", equipmentId);
                    Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, equipmentId, Connection.EQUIPMENT_TYPE, eqPin1, eqPin2, Connection.INSIDE_CONNECTION);
                    if (!(clist.contains(newC))) {
                        log.info("----- creating connection " + newC);
                        log.info("------- clist size" + clist.size());
                        clist.add(newC);
                    } else {
                        log.info("----- connection already exist: " + newC);
                    }
                }                
                
                if(equipmentReg.Model.equals("GENSAL") && !hasHygov) {
                    log.info("--* connection eq-eq {}-{}  on pins {},{}", equipmentReg.getModel(), equipmentReg.getModel(), eqPin1, eqPin2);
                    log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin1, eqPin2, "equipment", equipmentId);
                    Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, equipmentId, Connection.EQUIPMENT_TYPE, eqPin1, eqPin2, Connection.INSIDE_CONNECTION);
                    if (!(clist.contains(newC))) {
                        log.info("----- creating connection " + newC);
                        log.info("------- clist size" + clist.size());
                        clist.add(newC);
                    } else {
                        log.info("----- connection already exist: " + newC);
                    }
                }
                
                if (equipmentReg.Model.equals("WT4G1")) {
                		eqPin1 = "I_qcmd";
            			eqPin2 = "I_qcmd0";
                        log.info("--* connection eq-eq {}-{}  on pins {},{}", equipmentReg.getModel(), equipmentReg.getModel(), eqPin1, eqPin2);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin1, eqPin2, "equipment", equipmentId);
                        Connection newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, equipmentId, Connection.EQUIPMENT_TYPE, eqPin1, eqPin2, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
	                    
                        eqPin1 = "I_pcmd";
                        eqPin2 = "I_pcmd0";
                        log.info("--* connection eq-eq {}-{}  on pins {},{}", equipmentReg.getModel(), equipmentReg.getModel(), eqPin1, eqPin2);
                        log.info("----*  {} {} {} {} {} {} {}", "inside", "equipment", equipmentId, eqPin1, eqPin2, "equipment", equipmentId);
                        newC = new Connection(equipmentId, Connection.EQUIPMENT_TYPE, equipmentId, Connection.EQUIPMENT_TYPE, eqPin1, eqPin2, Connection.INSIDE_CONNECTION);
                        if (!(clist.contains(newC))) {
                            log.info("----- creating connection " + newC);
                            log.info("------- clist size" + clist.size());
                            clist.add(newC);
                        } else {
                            log.info("----- connection already exist: " + newC);
                        }
                 }
                
                // fourth persist the connection schema
                if(clist.size()>0) {
                    cs.setConnections(clist);
                    cs = ddbmanager.save(cs);
                }

            }
            //TODO loads?

            if (deleteData) {
                for (String csId : equipmentsList) {
                    ConnectionSchema cs = ddbmanager.findConnectionSchema(csId, null);
                    log.info("- removing  cs {} {}", csId, cs.getId());
                    ddbmanager.delete(cs);
                }
                for (String eqId : equipmentsList) {
                    Equipment eq = ddbmanager.findEquipment(eqId);
                    log.info("- removing  eq {} {}", eqId, eq.getId());
                    ddbmanager.delete(eq);
                }
                for (String intId : internalsList) {
                    Internal internal = ddbmanager.findInternal(intId);
                    log.info("- removing  int {} {}", intId, internal.getId());
                    ddbmanager.delete(internal);
                }
                for (String pcId : ptcList) {
                    ParametersContainer pc = ddbmanager.findParametersContainer(pcId);
                    log.info("- removing  pc {} - {}", pcId, pc.getId());
                    ddbmanager.delete(pc);
                }
                for (String mtcId : mtcSet) {
                    ModelTemplateContainer mtc = ddbmanager.findModelTemplateContainer(mtcId);
                    try {
                        if (mtc != null) {
                            log.info("- removing  mtc {} {}", mtcId, mtc.getId());
                            ddbmanager.delete(mtc);
                        }
                    } catch (RuntimeException ex) {
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


}