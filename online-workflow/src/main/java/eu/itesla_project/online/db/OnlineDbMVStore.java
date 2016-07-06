/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.db;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVMapConcurrent;
import org.h2.mvstore.MVStore;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.csvreader.CsvWriter;

import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.FileDataSource;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.modules.contingencies.ActionParameters;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineStep;
import eu.itesla_project.modules.online.OnlineWorkflowDetails;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.OnlineWorkflowResults;
import eu.itesla_project.modules.online.OnlineWorkflowRulesResults;
import eu.itesla_project.modules.online.OnlineWorkflowWcaResults;
import eu.itesla_project.modules.online.StateProcessingStatus;
import eu.itesla_project.modules.online.StateStatus;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.optimizer.CCOFinalStatus;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import eu.itesla_project.online.db.debug.NetworkData;
import eu.itesla_project.online.db.debug.NetworkDataExporter;
import eu.itesla_project.online.db.debug.NetworkDataExtractor;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineDbMVStore implements OnlineDb {

	private static final String STORED_WORKFLOW_PREFIX = "wf-";
	private static final String STORED_METRICS_STEPS_MAP_NAME = "storedSteps";
	private static final String STORED_METRICS_STATES_MAP_SUFFIX = "_states";
	private static final String STORED_METRICS_PARAMS_MAP_SUFFIX = "_params";
	private static final String STORED_RESULTS_MAP_NAME = "wfResults";
	private static final String STORED_RESULTS_ACTIONS_MAP_SUFFIX = "_actions";
	private static final String STORED_RESULTS_ACTIONINFO_MAP_SUFFIX = "_actionplans"; // i do not change this, for backward compatibility
	private static final String STORED_RESULTS_ACTIONINFO_ACTIONSFOUND_KEY_SUFFIX = "_actionsfound";
	private static final String STORED_RESULTS_ACTIONINFO_STATUS_KEY_SUFFIX = "_status";
	private static final String STORED_RESULTS_ACTIONINFO_CAUSE_KEY_SUFFIX = "_cause";
	private static final String STORED_RESULTS_ACTIONINFO_ACTIONPLAN_KEY_SUFFIX = "_actionplan";
	private static final String STORED_RESULTS_ACTIONS_EQUIPMENTS_MAP_SUFFIX = "_actionequipments";
	private static final String STORED_RESULTS_ACTIONS_PARAMETERS_MAP_SUFFIX = "_actionparameters";
	private static final String STORED_RESULTS_INDEXES_MAP_SUFFIX = "_indexes";
	private static final String STORED_RESULTS_TIMEHORIZON_KEY = "time_orizon";
	private static final String STORED_RESULTS_CONTINGENCIES_WITH_ACTIONS_KEY = "contingiencies_with_actions";
	private static final String STORED_RESULTS_UNSAFE_CONTINGENCIES_KEY = "unsafe_contingiencies";
	private static final String STORED_RULES_RESULTS_MAP_NAME = "wfRulesResults";
	private static final String STORED_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX = "_rulesresults";
	private static final String STORED_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX = "_rulesstatus";
	private static final String STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY = "contingiencies_with_rules";
	private static final String STORED_WCA_RESULTS_MAP_NAME = "wfWcaResults";
	private static final String STORED_WCA_RESULTS_CLUSTERS_MAP_NAME = "contingencies_wcaclusters";
	private static final String STORED_WCA_RESULTS_CAUSES_MAP_NAME = "contingencies_wcacause";
	private static final String STORED_PARAMETERS_MAP_NAME = "wfParameters";
	private static final String STORED_PARAMETERS_BASECASE_KEY = "basecase";
	private static final String STORED_PARAMETERS_STATE_NUMBER_KEY = "state_number";
	private static final String STORED_PARAMETERS_HISTO_INTERVAL_KEY = "histo_interval";
	private static final String STORED_PARAMETERS_OFFLINE_WF_ID_KEY = "offline_wf";
	private static final String STORED_PARAMETERS_FEA_ID_KEY = "fe_analysis";
	private static final String STORED_PARAMETERS_RULES_PURITY_KEY = "rules_purity";
	private static final String STORED_PARAMETERS_STORE_STATES_KEY = "store_states";
	private static final String STORED_PARAMETERS_ANALYSE_BASECASE_KEY = "analyse_basecase";
	private static final String STORED_PARAMETERS_VALIDATION_KEY = "validation";
	private static final String STORED_PARAMETERS_SECURITY_INDEXES_KEY = "security_indexes";
	private static final String STORED_PARAMETERS_CASE_TYPE_KEY = "case_type";
	private static final String STORED_PARAMETERS_COUNTRIES_KEY = "countries";
	private static final String STORED_PARAMETERS_MERGE_OPTIMIZED_KEY = "merge_optimized";
	private static final String STORED_PARAMETERS_LIMIT_REDUCTION_KEY = "limit_reduction";
	private static final String STORED_STATES_PROCESSING_STATUS_MAP_NAME = "statesProcessingStatus";
	private static final String STORED_STATES_LIST_KEY = "states";
	private static final String STORED_STATES_STATE_DETAILS_KEY = "stateStatusDetails";
	private static final String STORED_STATE_PROCESSING_STATUS_MAP_SUFFIX = "_processingstatus";
	private static final String STORED_WORKFLOW_STATES_FOLDER_PREFIX = "states-wf-";
	private static final String STORED_STATE_PREFIX = "state-";
	private static final String STORED_VIOLATIONS_STEPS_MAP_NAME = "storedViolationsSteps";
	private static final String STORED_VIOLATIONS_STATES_MAP_SUFFIX = "_violationsstates";
	private static final String STORED_VIOLATIONS_STATES_MAP_NAME = "storedViolationsStates";
	private static final String STORED_VIOLATIONS_STEPS_MAP_SUFFIX = "_violationssteps";
	private static final String STORED_VIOLATIONS_MAP_PREFIX = "violations_";
	private static final String STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_NAME = "storedPCViolationsContingencies";
	private static final String STORED_PC_VIOLATIONS_STATES_MAP_SUFFIX = "_pcviolationsstates";
	private static final String STORED_PC_VIOLATIONS_STATES_MAP_NAME = "storedPCViolationsStates";
	private static final String STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_SUFFIX = "_pcviolationscontigencies";
	private static final String STORED_PC_VIOLATIONS_MAP_PREFIX = "pcviolations_";
	private static final String STORED_PC_LOADFLOW_CONTINGENCIES_MAP_NAME = "storedPCLoadflowContingencies";
	private static final String STORED_PC_LOADFLOW_STATES_MAP_SUFFIX = "_pcloadflowstates";;
	private static final String STORED_PC_LOADFLOW_STATES_MAP_NAME = "storedPCLoadflowStates";
	private static final String STORED_PC_LOADFLOW_CONTINGENCIES_MAP_SUFFIX = "_pcloadflowcontigencies";
	private static final String STORED_WCA_RULES_RESULTS_MAP_NAME = "wfWcaRulesResults";
	private static final String STORED_WCA_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX = "_wcarulesresults";
	private static final String STORED_WCA_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX = "_wcarulesstatus";
	private static final String SERIALIZED_STATES_FILENAME = "network-states.csv";	
	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OnlineDbMVStore.class);

	
	private OnlineDbMVStoreConfig config = null;
	
	HashMap<String, MVStore> storedWFMetrics = new HashMap<String, MVStore>();
	ConcurrentHashMap<String, ConcurrentHashMap<Integer, Map<HistoDbAttributeId, Object>>> workflowsStates = new ConcurrentHashMap<String, ConcurrentHashMap<Integer,Map<HistoDbAttributeId,Object>>>();
	
	MVMapConcurrent.Builder<String, String> mapBuilder;


	public OnlineDbMVStore(OnlineDbMVStoreConfig config) {
		this.config = config;
		LOGGER.info(config.toString());
		Path storageFolder = config.getOnlineDbDir();
		if ( !Files.exists(storageFolder) ) {
			try {
				Files.createDirectories(storageFolder);
			} catch (IOException e) {
				String errorMessage = "online db folder " + storageFolder + " does not exist and cannot be created: " + e.getMessage();
				LOGGER.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		mapBuilder = new MVMapConcurrent.Builder<String, String>();
	}

	public OnlineDbMVStore() {
		this(OnlineDbMVStoreConfig.load());
	}

	private synchronized void closeStores() {
		ArrayList<String> workflowIds = new ArrayList<String>();
		for (String storedWorkflowId : storedWFMetrics.keySet()) {
			MVStore wfMVStore = storedWFMetrics.get(storedWorkflowId);
			wfMVStore.close();
			workflowIds.add(storedWorkflowId);
		}
		for (String workflowId : workflowIds) {
			storedWFMetrics.remove(workflowId);
		}
	}

	private synchronized MVStore getStore(String workflowId) {
		MVStore wfMVStore;
		if ( storedWFMetrics.containsKey(workflowId))
			wfMVStore = storedWFMetrics.get(workflowId);
		else {
			LOGGER.debug("Opening file for workflow {}", workflowId);
			wfMVStore = MVStore.open(config.getOnlineDbDir().toString() + File.separator + STORED_WORKFLOW_PREFIX + workflowId);
			storedWFMetrics.put(workflowId, wfMVStore);
		}
		return wfMVStore;
	}

	@Override
	public void storeMetrics(String workflowId, OnlineStep step, Map<String, String> metrics) {
		LOGGER.info("Storing metrics for wf {} and step {}", workflowId, step.name());
		storeMetrics(workflowId, step.name() + "__", metrics);
		LOGGER.info("Storing metadata for wf {} and step {}", workflowId, step.name());
		storeStepMetadata(workflowId, "_", step, metrics);

	}

	@Override
	public void storeMetrics(String workflowId, Integer stateId, OnlineStep step, Map<String, String> metrics) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Storing metrics for wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		storeMetrics(workflowId, step.name() + "_" + stateIdStr, metrics);
		LOGGER.info("Storing metadata for wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		storeStepMetadata(workflowId, stateIdStr, step, metrics);
	}

	private void storeMetrics(String workflowId, String mapName, Map<String, String> metrics) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			Map<String, String> metricsMap = wfMVStore.openMap(mapName, mapBuilder);
			for ( String parameter : metrics.keySet() ) {
				metricsMap.put(parameter, metrics.get(parameter));
			}
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing metrics for wf " +  workflowId + " in map " + mapName + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
    	}
	}

	private void storeStepMetadata(String workflowId, String stateId, OnlineStep step, Map<String, String> metrics) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			// save info about stored wf steps
			MVMap<String, String> storedStepsMap = wfMVStore.openMap(STORED_METRICS_STEPS_MAP_NAME, mapBuilder);
			storedStepsMap.putIfAbsent(step.name(), "1");
			// save info about stored states per step
			MVMap<String, String> stepStatesMap = wfMVStore.openMap(step.name() + STORED_METRICS_STATES_MAP_SUFFIX, mapBuilder);
			stepStatesMap.putIfAbsent(stateId, "");
			// save info about stored params per step
			MVMap<String, String> stepParamsMap = wfMVStore.openMap(step.name() + STORED_METRICS_PARAMS_MAP_SUFFIX, mapBuilder);
			for ( String parameter : metrics.keySet() ) {
				stepParamsMap.putIfAbsent(parameter, "");
			}
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing metadata for wf " +  workflowId + ", step "+ step.name() + ", state " + stateId + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
    	}
	}

	@Override
	public Map<String, String> getMetrics(String workflowId, OnlineStep step) {
		LOGGER.info("Getting metrics from wf {} and step {}", workflowId, step.name());
		return getMetrics(workflowId, step.name() + "__");
	}

	@Override
	public Map<String, String> getMetrics(String workflowId, Integer stateId, OnlineStep step) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Getting metrics from wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		return getMetrics(workflowId, step.name() + "_" + stateIdStr);
	}

	private Map<String, String> getMetrics(String workflowId, String mapName) {
		if ( isWorkflowStored(workflowId) ) {
			HashMap<String, String> metrics = new HashMap<String, String>();
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.getMapNames().contains(mapName) ) {
				Map<String, String> storedMap = wfMVStore.openMap(mapName, mapBuilder);
				for ( String parameter : storedMap.keySet() ) {
					metrics.put(parameter, storedMap.get(parameter));
				}
			}
			return metrics;
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	@Override
	public String getCsvMetrics(String workflowId, OnlineStep step) {
		LOGGER.info("Preparing CSV data for wf {} and step {}", workflowId, step.name());
		if ( isWorkflowStored(workflowId) ) {
			StringWriter content = new StringWriter();
			CsvWriter cvsWriter = new CsvWriter(content, ',');
			try {
				MVStore wfMVStore = getStore(workflowId);
				// check if there are stored metrics
				Map<String, String> storedStepsMap = wfMVStore.openMap(STORED_METRICS_STEPS_MAP_NAME, mapBuilder);
				if ( storedStepsMap.containsKey(step.name()) ) {
					MVMap<String, String> stepParamsMap = wfMVStore.openMap(step.name() + STORED_METRICS_PARAMS_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> stepStatesMap = wfMVStore.openMap(step.name() + STORED_METRICS_STATES_MAP_SUFFIX, mapBuilder);
					// write headers
					//LOGGER.debug("Preparing CSV headers for wf {} and step {}", workflowId, step.name());
					String[] headers =  new String[stepParamsMap.keySet().size()+1];
					headers[0] = "state";
					HashMap<String,Integer> paramsIndexes = new HashMap<>();
					int i = 1;
					for ( String parameter : stepParamsMap.keySet() ) {
						headers[i] = parameter;
						paramsIndexes.put(parameter, i);
						i++;
					}
					cvsWriter.writeRecord(headers);
					// write step general metrics, if stored
					if ( stepStatesMap.containsKey("_") ) {
						//LOGGER.debug("Preparing CSV data for wf {} and step {} - general step metrics", workflowId, step.name());
						String[] values = getStoredMapValues(wfMVStore, "_", step, stepParamsMap.keySet().size(), paramsIndexes);
						cvsWriter.writeRecord(values);
					}
					// write step metrics for each state, if stored
					for ( String stateId : stepStatesMap.keySet() ) {
						if (!"_".equals(stateId) ) {
							//LOGGER.debug("Preparing CSV data for wf {} and step {} - state {} metrics", workflowId, step.name(), stateId);
							String[] values = getStoredMapValues(wfMVStore, stateId, step, stepParamsMap.keySet().size(), paramsIndexes);
							cvsWriter.writeRecord(values);
						}
					}
				}
			} catch (IOException e) {
				String errorMessage = "error getting cvs data for step " + step.name() + " and wf id " + workflowId;
				LOGGER.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			cvsWriter.flush();
			return content.toString();
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	private String[] getStoredMapValues(MVStore wfMVStore, String stateId, OnlineStep step, int paramsN, HashMap<String,Integer> paramsIndexes) {
		String[] values =  new String[paramsN+1];
		values[0] = stateId;
		Map<String, String> storedMap = wfMVStore.openMap(step.name() + "_" + stateId, mapBuilder);
		for ( String parameter : storedMap.keySet() ) {
			int index = paramsIndexes.get(parameter);
			values[index] = storedMap.get(parameter);
		}
		return values;
	}

	@Override
	public List<OnlineWorkflowDetails> listWorkflows() {
		LOGGER.info("Getting list of stored workflows");
		List<OnlineWorkflowDetails> workflowIds = new ArrayList<OnlineWorkflowDetails>();
		File[] files = config.getOnlineDbDir().toFile().listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().startsWith(STORED_WORKFLOW_PREFIX);
		    }
		});
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    } });
		for (File file : files) {
			if ( file.isFile() ) {
				OnlineWorkflowDetails workflowDetails = new OnlineWorkflowDetails(file.getName().substring(STORED_WORKFLOW_PREFIX.length()));
				workflowDetails.setWorkflowDate(new DateTime(file.lastModified()));
				workflowIds.add(workflowDetails);
			}
		}
		LOGGER.info("Found {} workflow(s)", workflowIds.size());
		return workflowIds;
	}
	
	@Override
	public List<OnlineWorkflowDetails> listWorkflows(DateTime basecaseDate) {
		LOGGER.info("Getting list of stored workflows run on basecase {}", basecaseDate);
		String wfNamePrefix = DateTimeFormat.forPattern("yyyyMMdd_HHmm_").print(basecaseDate);
		List<OnlineWorkflowDetails> workflowIds = new ArrayList<OnlineWorkflowDetails>();
		File[] files = config.getOnlineDbDir().toFile().listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().startsWith(STORED_WORKFLOW_PREFIX+wfNamePrefix);
		    }
		});
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    } });
		for (File file : files) {
			if ( file.isFile() ) {
				OnlineWorkflowDetails workflowDetails = new OnlineWorkflowDetails(file.getName().substring(STORED_WORKFLOW_PREFIX.length()));
				workflowDetails.setWorkflowDate(new DateTime(file.lastModified()));
				workflowIds.add(workflowDetails);
			}
		}
		LOGGER.info("Found {} workflow(s)", workflowIds.size());
		return workflowIds;
	}

	@Override
	public List<OnlineWorkflowDetails> listWorkflows(Interval basecaseInterval) {
		LOGGER.info("Getting list of stored workflows run on basecases within the interval {}", basecaseInterval);
		String dateFormatPattern = "yyyyMMdd_HHmm";
		DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormatPattern);
		List<OnlineWorkflowDetails> workflowIds = new ArrayList<OnlineWorkflowDetails>();
		File[] files = config.getOnlineDbDir().toFile().listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().startsWith(STORED_WORKFLOW_PREFIX);
		    }
		});
		Arrays.sort(files, new Comparator<File>(){
		    public int compare(File f1, File f2)
		    {
		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    } });
		for (File file : files) {
			if ( file.isFile() ) {
				String wfId = file.getName().substring(STORED_WORKFLOW_PREFIX.length());
				if ( wfId.length() > dateFormatPattern.length() && wfId.substring(dateFormatPattern.length(),dateFormatPattern.length()+1).equals("_")) {
					String basecaseName = wfId.substring(0, dateFormatPattern.length()-1);
					DateTime basecaseDate = DateTime.parse(basecaseName, formatter);
					if ( basecaseInterval.contains(basecaseDate.getMillis())) {
						OnlineWorkflowDetails workflowDetails = new OnlineWorkflowDetails(wfId);
						workflowDetails.setWorkflowDate(new DateTime(file.lastModified()));
						workflowIds.add(workflowDetails);
					}
				}
			}
		}
		LOGGER.info("Found {} workflow(s)", workflowIds.size());
		return workflowIds;
	}

	@Override
	public void storeResults(String workflowId, OnlineWorkflowResults results) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(results, "online workflow results is null");
		LOGGER.info("Storing results for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the results for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_RESULTS_MAP_NAME) )
			removeWfResults(workflowId, wfMVStore);
		MVMap<String, String> storedResultsMap = wfMVStore.openMap(STORED_RESULTS_MAP_NAME, mapBuilder);
		// store time horizon
		storedResultsMap.put(STORED_RESULTS_TIMEHORIZON_KEY, results.getTimeHorizon().getName());
		// store contingencies with actions
		storedResultsMap.put(STORED_RESULTS_CONTINGENCIES_WITH_ACTIONS_KEY, OnlineDbMVStoreUtils.contingenciesIdsToJson(results.getContingenciesWithActions()));
		// store contingencies with actions
		storedResultsMap.put(STORED_RESULTS_UNSAFE_CONTINGENCIES_KEY, OnlineDbMVStoreUtils.contingenciesIdsToJson(results.getUnsafeContingencies()));
		// store actions for contingencies
		for (String contingencyId : results.getContingenciesWithActions()) {
			MVMap<String, String> storedActionsInfosMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONINFO_MAP_SUFFIX, mapBuilder);
			MVMap<String, String> storedActionsMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_MAP_SUFFIX, mapBuilder);
			MVMap<String, String> storedActionsEquipmentsMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_EQUIPMENTS_MAP_SUFFIX, mapBuilder);
			MVMap<String, String> storedActionsParametersMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_PARAMETERS_MAP_SUFFIX, mapBuilder);
			for (Integer stateId : results.getUnsafeStatesWithActions(contingencyId).keySet()) {
				storedActionsInfosMap.put(stateId.toString() + STORED_RESULTS_ACTIONINFO_ACTIONSFOUND_KEY_SUFFIX, 
										 Boolean.toString(results.getUnsafeStatesWithActions(contingencyId).get(stateId)));
				storedActionsInfosMap.put(stateId.toString() + STORED_RESULTS_ACTIONINFO_STATUS_KEY_SUFFIX, results.getStateStatus(contingencyId, stateId).name());
				if ( results.getCause(contingencyId, stateId) != null )
					storedActionsInfosMap.put(stateId.toString() + STORED_RESULTS_ACTIONINFO_CAUSE_KEY_SUFFIX, results.getCause(contingencyId, stateId));
				if ( results.getActionPlan(contingencyId, stateId) != null )
					storedActionsInfosMap.put(stateId.toString() + STORED_RESULTS_ACTIONINFO_ACTIONPLAN_KEY_SUFFIX, results.getActionPlan(contingencyId, stateId));
				List<String> actionsIds = results.getActionsIds(contingencyId, stateId);
				if ( actionsIds != null ) {
					for(String actionId : actionsIds) {
						List<String> equipmentsIds = results.getEquipmentsIds(contingencyId, stateId, actionId);
						storedActionsEquipmentsMap.put(stateId + "_" + actionId, OnlineDbMVStoreUtils.actionsIdsToJson(equipmentsIds));
						for(String equipmentId : equipmentsIds) {
							ActionParameters actionParameters = results.getParameters(contingencyId, stateId, actionId, equipmentId);
							if ( actionParameters != null )
								storedActionsParametersMap.put(stateId + "_" + actionId + "_" + equipmentId, OnlineDbMVStoreUtils.actionParametersToJson(actionParameters));
						}
					}
				} else {
					actionsIds = new ArrayList<String>(); // I need anyway an empty list, for the getResults to work
				}
				storedActionsMap.put(stateId.toString(), OnlineDbMVStoreUtils.actionsIdsToJson(actionsIds));
			}
		}
		// store indexes for contingencies
		for (String contingencyId : results.getUnsafeContingencies()) {
			MVMap<String, String> storedIndexesMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_INDEXES_MAP_SUFFIX, mapBuilder);
			for (Integer stateId : results.getUnstableStates(contingencyId)) {
				Map<String, Boolean> indexesData = results.getIndexesData(contingencyId, stateId);
				storedIndexesMap.put(stateId.toString(), OnlineDbMVStoreUtils.indexesDataToJson(indexesData));
			}
		}
		wfMVStore.commit();
	}

	private void removeWfResults(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing wf results for workflow {}", workflowId);
		MVMap<String, String> storedResultsMap = wfMVStore.openMap(STORED_RESULTS_MAP_NAME, mapBuilder);
		// remove info about contingencies with action
		Collection<String> contingenciesWithAction = OnlineDbMVStoreUtils.jsonToContingenciesIds(
				storedResultsMap.get(STORED_RESULTS_CONTINGENCIES_WITH_ACTIONS_KEY));
		for (String contingencyId : contingenciesWithAction) {
			MVMap<String, String> storedActionsMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedActionsMap);
		}
		// remove info about unsafe contingencies 
		Collection<String> unsafeContingencies = OnlineDbMVStoreUtils.jsonToContingenciesIds(
				storedResultsMap.get(STORED_RESULTS_UNSAFE_CONTINGENCIES_KEY));
		for (String contingencyId : unsafeContingencies) {
			MVMap<String, String> storedIndexesMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_INDEXES_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedIndexesMap);
		}
		// remove info about stored wf results
		wfMVStore.removeMap(storedResultsMap);
		// commit removal
		wfMVStore.commit();
	}

	@Override
	public OnlineWorkflowResults getResults(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting results of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_RESULTS_MAP_NAME) ) {
				MVMap<String, String> storedResultsMap = wfMVStore.openMap(STORED_RESULTS_MAP_NAME, mapBuilder);
				// create workflow results
				OnlineWorkflowResultsImpl wfResults = new OnlineWorkflowResultsImpl(
																workflowId, 
																TimeHorizon.valueOf(storedResultsMap.get(STORED_RESULTS_TIMEHORIZON_KEY)));
				// add contingencies with actiions
				Collection<String> contingenciesWithAction = OnlineDbMVStoreUtils.jsonToContingenciesIds(
																storedResultsMap.get(STORED_RESULTS_CONTINGENCIES_WITH_ACTIONS_KEY));
				for (String contingencyId : contingenciesWithAction) {
					MVMap<String, String> storedActionsMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> storedActionsInfosMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONINFO_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> storedActionsParametersMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_PARAMETERS_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> storedActionsEquipmentsMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_ACTIONS_EQUIPMENTS_MAP_SUFFIX, mapBuilder);
					for (String stateId : storedActionsMap.keySet()) {
						boolean actionsFound = true;
						if ( storedActionsInfosMap.containsKey(stateId + STORED_RESULTS_ACTIONINFO_ACTIONSFOUND_KEY_SUFFIX) )
							actionsFound = Boolean.parseBoolean(storedActionsInfosMap.get(stateId + STORED_RESULTS_ACTIONINFO_ACTIONSFOUND_KEY_SUFFIX));
						CCOFinalStatus status = CCOFinalStatus.MANUAL_CORRECTIVE_ACTION_FOUND;
						if ( storedActionsInfosMap.containsKey(stateId + STORED_RESULTS_ACTIONINFO_STATUS_KEY_SUFFIX) )
							status = CCOFinalStatus.valueOf(storedActionsInfosMap.get(stateId + STORED_RESULTS_ACTIONINFO_STATUS_KEY_SUFFIX));
						String cause = storedActionsInfosMap.get(stateId + STORED_RESULTS_ACTIONINFO_CAUSE_KEY_SUFFIX);
						String actionPlan = storedActionsInfosMap.get(stateId + STORED_RESULTS_ACTIONINFO_ACTIONPLAN_KEY_SUFFIX);
						Map<String, Map<String, ActionParameters>> actions = null;
						if ( storedActionsMap.containsKey(stateId) ) {
							List<String> actionsIds = OnlineDbMVStoreUtils.jsonToActionsIds(storedActionsMap.get(stateId));
							actions = new HashMap<String, Map<String,ActionParameters>>();
							for(String actionId : actionsIds) {
								Map<String, ActionParameters> equipments = new HashMap<String, ActionParameters>();
								List<String> equipmentsIds = OnlineDbMVStoreUtils.jsonToActionsIds(storedActionsEquipmentsMap.get(stateId + "_" + actionId));
								if ( equipmentsIds != null ) {
									for(String equipmentId : equipmentsIds) {
										ActionParameters actionParameters = OnlineDbMVStoreUtils.jsonToActionParameters(storedActionsParametersMap.get(stateId + "_" + actionId + "_" + equipmentId));
										equipments.put(equipmentId, actionParameters);
									}
								}
								actions.put(actionId, equipments);
							}
						}
						wfResults.addContingenciesWithActions(contingencyId, Integer.parseInt(stateId), actionsFound, status, cause, actionPlan, actions);
					}
				}
				// add unsafe contingencies
				Collection<String> unsafeContingencies = OnlineDbMVStoreUtils.jsonToContingenciesIds(
																storedResultsMap.get(STORED_RESULTS_UNSAFE_CONTINGENCIES_KEY));
				for (String contingencyId : unsafeContingencies) {
					MVMap<String, String> storedIndexesMap = wfMVStore.openMap(contingencyId + STORED_RESULTS_INDEXES_MAP_SUFFIX, mapBuilder);
					for (String stateId : storedIndexesMap.keySet()) {
						Map<String, Boolean> indexesData = OnlineDbMVStoreUtils.jsonToIndexesData(storedIndexesMap.get(stateId));
						wfResults.addUnsafeContingencies(contingencyId, Integer.parseInt(stateId), indexesData);
					}
				}
				return wfResults;
			} else {
				LOGGER.warn("No results of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public void storeRulesResults(String workflowId, OnlineWorkflowRulesResults results) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(results, "online workflow rules results is null");
		LOGGER.info("Storing results of rules for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the results for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_RULES_RESULTS_MAP_NAME) )
			removeWfRulesResults(workflowId, wfMVStore);
		MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_RULES_RESULTS_MAP_NAME, mapBuilder);
		// store time horizon
		storedRulesResultsMap.put(STORED_RESULTS_TIMEHORIZON_KEY, results.getTimeHorizon().getName());
		// store contingencies with security rules results
		storedRulesResultsMap.put(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY, 
								  OnlineDbMVStoreUtils.contingenciesIdsToJson(results.getContingenciesWithSecurityRulesResults()));
		// store rules results for contingencies
		for (String contingencyId : results.getContingenciesWithSecurityRulesResults()) {
			MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
			MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
			for (Integer stateId : results.getStatesWithSecurityRulesResults(contingencyId)) {
				// store state status
				StateStatus status = results.getStateStatus(contingencyId, stateId);
				storedStateStatusMap.put(stateId.toString(), status.name());
				// store state rules results
				Map<String, Boolean> stateResults = results.getStateResults(contingencyId, stateId);
				storedStateResultsMap.put(stateId.toString(), OnlineDbMVStoreUtils.indexesDataToJson(stateResults));
			}
		}
		wfMVStore.commit();
	}
	
	private void removeWfRulesResults(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing rules results for workflow {}", workflowId);
		MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_RULES_RESULTS_MAP_NAME, mapBuilder);
		// remove rules results 
		Collection<String> rulesContingencies = OnlineDbMVStoreUtils.jsonToContingenciesIds(
				storedRulesResultsMap.get(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY));
		for (String contingencyId : rulesContingencies) {
			MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedStateStatusMap);
			MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedStateResultsMap);
		}
		// remove info about stored rules results
		wfMVStore.removeMap(storedRulesResultsMap);
		// commit removal
		wfMVStore.commit();
	}

	@Override
	public OnlineWorkflowRulesResults getRulesResults(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting rules results of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_RULES_RESULTS_MAP_NAME) ) {
				MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_RULES_RESULTS_MAP_NAME, mapBuilder);
				// create workflow rules results
				OnlineWorkflowRulesResultsImpl wfRulesResults = new OnlineWorkflowRulesResultsImpl(
																workflowId, 
																TimeHorizon.valueOf(storedRulesResultsMap.get(STORED_RESULTS_TIMEHORIZON_KEY)));
				// add contingencies with rules results
				Collection<String> contingenciesWithRules = OnlineDbMVStoreUtils.jsonToContingenciesIds(
																storedRulesResultsMap.get(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY));
				for (String contingencyId : contingenciesWithRules) {
					MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
					for (String stateId : storedStateStatusMap.keySet()) {
						Map<String, Boolean> stateResults = OnlineDbMVStoreUtils.jsonToIndexesData(storedStateResultsMap.get(stateId));
						StateStatus stateStatus = StateStatus.valueOf(storedStateStatusMap.get(stateId));
						wfRulesResults.addContingencyWithSecurityRulesResults(contingencyId, Integer.parseInt(stateId), stateStatus, stateResults);
					}
				}
				return wfRulesResults;
			} else {
				LOGGER.warn("No rules results of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public void storeWcaResults(String workflowId, OnlineWorkflowWcaResults results) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(results, "online workflow wca results is null");
		LOGGER.info("Storing results of WCA for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the results for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_WCA_RESULTS_MAP_NAME) )
			removeWfWcaResults(workflowId, wfMVStore);
		MVMap<String, String> storedWcaResultsMap = wfMVStore.openMap(STORED_WCA_RESULTS_MAP_NAME, mapBuilder);
		// store time horizon
		storedWcaResultsMap.put(STORED_RESULTS_TIMEHORIZON_KEY, results.getTimeHorizon().getName());
		// store wca results for contingencies
		MVMap<String, String> storedClustersMap = wfMVStore.openMap(STORED_WCA_RESULTS_CLUSTERS_MAP_NAME, mapBuilder);
		MVMap<String, String> storedCausesMap = wfMVStore.openMap(STORED_WCA_RESULTS_CAUSES_MAP_NAME, mapBuilder);
		for (String contingencyId : results.getContingencies()) {
			storedClustersMap.put(contingencyId, Integer.toString(results.getClusterIndex(contingencyId)));
			List<String> causes = results.getCauses(contingencyId);
			if (causes != null && causes.size() > 0)
				storedCausesMap.put(contingencyId, causes.get(0));
		}
		wfMVStore.commit();
	}
	
	private void removeWfWcaResults(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing WCA results for workflow {}", workflowId);
		MVMap<String, String> storedWcaResultsMap = wfMVStore.openMap(STORED_WCA_RESULTS_MAP_NAME, mapBuilder);
		// remove wca results 
		MVMap<String, String> storedClustersMap = wfMVStore.openMap(STORED_WCA_RESULTS_CLUSTERS_MAP_NAME, mapBuilder);
		wfMVStore.removeMap(storedClustersMap);
		MVMap<String, String> storedCausesMap = wfMVStore.openMap(STORED_WCA_RESULTS_CAUSES_MAP_NAME, mapBuilder);
		wfMVStore.removeMap(storedCausesMap);
		// remove info about stored wca results
		wfMVStore.removeMap(storedWcaResultsMap);
		// commit removal
		wfMVStore.commit();
	}

	@Override
	public OnlineWorkflowWcaResults getWcaResults(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting WCA results of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_WCA_RESULTS_MAP_NAME) ) {
				MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_WCA_RESULTS_MAP_NAME, mapBuilder);
				// create workflow rules results
				OnlineWorkflowWcaResultsImpl wfWcaResults = new OnlineWorkflowWcaResultsImpl(
																		workflowId, 
																		TimeHorizon.valueOf(storedRulesResultsMap.get(STORED_RESULTS_TIMEHORIZON_KEY)));
				// add classification of contingencies in clusters
				MVMap<String, String> storedClustersMap = wfMVStore.openMap(STORED_WCA_RESULTS_CLUSTERS_MAP_NAME, mapBuilder);
				MVMap<String, String> storedCausesMap = wfMVStore.openMap(STORED_WCA_RESULTS_CAUSES_MAP_NAME, mapBuilder);
				for(String contingencyId : storedClustersMap.keySet()) {
					String cause = storedCausesMap.get(contingencyId);
					wfWcaResults.addContingencyWithCluster(contingencyId,
							Integer.valueOf(storedClustersMap.get(contingencyId)),
							cause != null ? Arrays.asList(cause) : null);
				}
				return wfWcaResults;
			} else {
				LOGGER.warn("No WCA results of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	
	@Override
	public void storeWorkflowParameters(String workflowId, OnlineWorkflowParameters parameters) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(parameters, "online workflow parameters is null");
		LOGGER.info("Storing configuration parameters for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the parameters for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_PARAMETERS_MAP_NAME) )
			removeWfParameters(workflowId, wfMVStore);
		MVMap<String, String> storedParametersMap = wfMVStore.openMap(STORED_PARAMETERS_MAP_NAME, mapBuilder);
		// store basecase
		storedParametersMap.put(STORED_PARAMETERS_BASECASE_KEY, parameters.getBaseCaseDate().toString());
		// store number of states
		storedParametersMap.put(STORED_PARAMETERS_STATE_NUMBER_KEY, Integer.toString(parameters.getStates()));
		// store interval of historical data
		storedParametersMap.put(STORED_PARAMETERS_HISTO_INTERVAL_KEY, parameters.getHistoInterval().toString());
		// store offline workflow id
		storedParametersMap.put(STORED_PARAMETERS_OFFLINE_WF_ID_KEY, parameters.getOfflineWorkflowId());
		// store time horizon
		storedParametersMap.put(STORED_RESULTS_TIMEHORIZON_KEY, parameters.getTimeHorizon().getName());
		// store forecast error analysis id
		storedParametersMap.put(STORED_PARAMETERS_FEA_ID_KEY, parameters.getFeAnalysisId());
		// store rules purity threshold
		storedParametersMap.put(STORED_PARAMETERS_RULES_PURITY_KEY, Double.toString(parameters.getRulesPurityThreshold()));
		// store flag store states
		storedParametersMap.put(STORED_PARAMETERS_STORE_STATES_KEY, Boolean.toString(parameters.storeStates()));
		// store flag analyse basecase
		storedParametersMap.put(STORED_PARAMETERS_ANALYSE_BASECASE_KEY, Boolean.toString(parameters.analyseBasecase()));
		// store flag validation
		storedParametersMap.put(STORED_PARAMETERS_VALIDATION_KEY, Boolean.toString(parameters.validation()));
		// store security indexes
		if ( parameters.getSecurityIndexes() != null )
			storedParametersMap.put(STORED_PARAMETERS_SECURITY_INDEXES_KEY, OnlineDbMVStoreUtils.indexesTypesToJson(parameters.getSecurityIndexes()));
		// store case type
		storedParametersMap.put(STORED_PARAMETERS_CASE_TYPE_KEY, parameters.getCaseType().name());
		// store countries
		storedParametersMap.put(STORED_PARAMETERS_COUNTRIES_KEY, OnlineDbMVStoreUtils.countriesToJson(parameters.getCountries()));
		// store merge optimized flag
		storedParametersMap.put(STORED_PARAMETERS_MERGE_OPTIMIZED_KEY, Boolean.toString(parameters.isMergeOptimized()));
		// store merge optimized flag
		storedParametersMap.put(STORED_PARAMETERS_LIMIT_REDUCTION_KEY, Float.toString(parameters.getLimitReduction()));

		wfMVStore.commit();
	}

	private void removeWfParameters(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing parameters for workflow {}", workflowId);
		MVMap<String, String> storedParametersMap = wfMVStore.openMap(STORED_PARAMETERS_MAP_NAME, mapBuilder);
		// remove parameters 
		wfMVStore.removeMap(storedParametersMap);
		// commit removal
		wfMVStore.commit();
	}

	@Override
	public OnlineWorkflowParameters getWorkflowParameters(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting configuration parameters of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_PARAMETERS_MAP_NAME) ) {
				MVMap<String, String> storedParametersMap = wfMVStore.openMap(STORED_PARAMETERS_MAP_NAME, mapBuilder);
				DateTime baseCaseDate = DateTime.parse(storedParametersMap.get(STORED_PARAMETERS_BASECASE_KEY));
		        int states = Integer.parseInt(storedParametersMap.get(STORED_PARAMETERS_STATE_NUMBER_KEY));
		        String offlineWorkflowId = storedParametersMap.get(STORED_PARAMETERS_OFFLINE_WF_ID_KEY);
		        TimeHorizon timeHorizon = TimeHorizon.fromName(storedParametersMap.get(STORED_RESULTS_TIMEHORIZON_KEY));
		        Interval histoInterval = Interval.parse(storedParametersMap.get(STORED_PARAMETERS_HISTO_INTERVAL_KEY));
		        String feAnalysisId = storedParametersMap.get(STORED_PARAMETERS_FEA_ID_KEY);
		        double rulesPurityThreshold = Double.parseDouble((storedParametersMap.get(STORED_PARAMETERS_RULES_PURITY_KEY) == null) ? "1" : storedParametersMap.get(STORED_PARAMETERS_RULES_PURITY_KEY));
		        boolean storeStates = Boolean.parseBoolean(storedParametersMap.get(STORED_PARAMETERS_STORE_STATES_KEY));
		        boolean analyseBasecase = Boolean.parseBoolean(storedParametersMap.get(STORED_PARAMETERS_ANALYSE_BASECASE_KEY));
		        boolean validation = Boolean.parseBoolean(storedParametersMap.get(STORED_PARAMETERS_VALIDATION_KEY));
		        Set<SecurityIndexType> securityIndexes = null;
		        if ( storedParametersMap.containsKey(STORED_PARAMETERS_SECURITY_INDEXES_KEY) )
		        	securityIndexes = OnlineDbMVStoreUtils.jsonToIndexesTypes(storedParametersMap.get(STORED_PARAMETERS_SECURITY_INDEXES_KEY));
		       CaseType caseType = OnlineWorkflowParameters.DEFAULT_CASE_TYPE;
		       if ( storedParametersMap.containsKey(STORED_PARAMETERS_CASE_TYPE_KEY))
		    	   caseType = CaseType.valueOf(storedParametersMap.get(STORED_PARAMETERS_CASE_TYPE_KEY));
		       Set<Country> countries = new HashSet<Country>(OnlineWorkflowParameters.DEFAULT_COUNTRIES);
		       if ( storedParametersMap.containsKey(STORED_PARAMETERS_COUNTRIES_KEY))
		    	   countries = OnlineDbMVStoreUtils.jsonToCountries(storedParametersMap.get(STORED_PARAMETERS_COUNTRIES_KEY));
		       boolean mergeOptimized = OnlineWorkflowParameters.DAFAULT_MERGE_OPTIMIZED;
		       if ( storedParametersMap.containsKey(STORED_PARAMETERS_MERGE_OPTIMIZED_KEY))
		    	   mergeOptimized = Boolean.parseBoolean(storedParametersMap.get(STORED_PARAMETERS_MERGE_OPTIMIZED_KEY));
		       float limitReduction = OnlineWorkflowParameters.DEFAULT_LIMIT_REDUCTION;
		       if ( storedParametersMap.containsKey(STORED_PARAMETERS_LIMIT_REDUCTION_KEY))
		    	   limitReduction = Float.parseFloat(storedParametersMap.get(STORED_PARAMETERS_LIMIT_REDUCTION_KEY));
		        
		       return new OnlineWorkflowParameters(baseCaseDate, 
		    		   							   states,
		    		   							   histoInterval,
		    		   							   offlineWorkflowId,
		    		   							   timeHorizon,
		    		   							   feAnalysisId,
		    		   							   rulesPurityThreshold,
		    		   							   storeStates,
		    		   							   analyseBasecase,
		    		   							   validation,
		    		   							   securityIndexes,
		    		   							   caseType,
		    		   							   countries,
		    		   							   mergeOptimized,
		    		   							   limitReduction
		    		   							   );
			} else {
				LOGGER.warn("No configuration parameters of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public void storeStatesProcessingStatus(String workflowId, Map<Integer, ? extends StateProcessingStatus> statesProcessingStatus) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(statesProcessingStatus, "online workflow states processing status is null");
		LOGGER.info("Storing states processing status for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the states processing status for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_STATES_PROCESSING_STATUS_MAP_NAME) )
			removeStatesProcessingStatus(workflowId, wfMVStore);
		MVMap<String, String> statesProcessingStatusMap = wfMVStore.openMap(STORED_STATES_PROCESSING_STATUS_MAP_NAME, mapBuilder);
		// store states with processing status
		statesProcessingStatusMap.put(STORED_STATES_LIST_KEY, OnlineDbMVStoreUtils.stateIdsToJson(statesProcessingStatus.keySet()));
		// store processing status for states
		for (Integer stateId : statesProcessingStatus.keySet()) {
			MVMap<String, String> stateProcessingStatusMap = wfMVStore.openMap(stateId.toString() + STORED_STATE_PROCESSING_STATUS_MAP_SUFFIX, mapBuilder);
			for (String step : statesProcessingStatus.get(stateId).getStatus().keySet()) {
				// store processing status
				stateProcessingStatusMap.put(step, statesProcessingStatus.get(stateId).getStatus().get(step));
			}
			stateProcessingStatusMap.put(STORED_STATES_STATE_DETAILS_KEY, statesProcessingStatus.get(stateId).getDetail() == null ? "" : statesProcessingStatus.get(stateId).getDetail());
		}
		wfMVStore.commit();
	}

	private void removeStatesProcessingStatus(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing states processing status for workflow {}", workflowId);
		MVMap<String, String> statesProcessingStatusMap = wfMVStore.openMap(STORED_STATES_PROCESSING_STATUS_MAP_NAME, mapBuilder);
		// remove processing status for states
		Collection<Integer> stateWithProcessingStatus = OnlineDbMVStoreUtils.jsonToStatesIds(statesProcessingStatusMap.get(STORED_STATES_LIST_KEY));
		for (Integer stateId : stateWithProcessingStatus) {
			MVMap<String, String> stateProcessingStatusMap = wfMVStore.openMap(stateId.toString() + STORED_STATE_PROCESSING_STATUS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(stateProcessingStatusMap);
		}
		// remove info about states with processing status
		wfMVStore.removeMap(statesProcessingStatusMap);
		// commit removal
		wfMVStore.commit();
		
	}

	@Override
	public Map<Integer, ? extends StateProcessingStatus> getStatesProcessingStatus(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting states processing status of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_STATES_PROCESSING_STATUS_MAP_NAME) ) {
				MVMap<String, String> statesProcessingStatusMap = wfMVStore.openMap(STORED_STATES_PROCESSING_STATUS_MAP_NAME, mapBuilder);
				// create states processing status
				Map<Integer, StateProcessingStatus> statesProcessingStatus = new HashMap<Integer, StateProcessingStatus>();
				// add processing status for states
				Collection<Integer> stateWithProcessingStatus = OnlineDbMVStoreUtils.jsonToStatesIds(statesProcessingStatusMap.get(STORED_STATES_LIST_KEY));
				for (Integer stateId : stateWithProcessingStatus) {
					MVMap<String, String> stateProcessingStatusMap = wfMVStore.openMap(stateId + STORED_STATE_PROCESSING_STATUS_MAP_SUFFIX, mapBuilder);
					Map<String,String> processingStatus = new HashMap<String, String>();
					for (String step : stateProcessingStatusMap.keySet()) {
						if ( !step.equals(STORED_STATES_STATE_DETAILS_KEY) )
							processingStatus.put(step, stateProcessingStatusMap.get(step));
					}
					statesProcessingStatus.put(stateId, new StateProcessingStatusImpl(processingStatus, stateProcessingStatusMap.get(STORED_STATES_STATE_DETAILS_KEY)));
				}
				return statesProcessingStatus;
			} else {
				LOGGER.warn("No states processing status of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public void storeState(String workflowId, Integer stateId, Network network) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Storing state {} of workflow {}", stateIdStr, workflowId);
		if ( network.getStateManager().getStateIds().contains(stateIdStr) ) {
			network.getStateManager().setWorkingState(stateIdStr);
			Path workflowStatesFolder = getWorkflowStatesFolder(workflowId);
			Path stateFolder = Paths.get(workflowStatesFolder.toString(), STORED_STATE_PREFIX + stateId);
			if ( Files.exists(stateFolder) ) {
				Path stateFile = Paths.get(stateFolder.toString(), network.getId() + ".xml");
				stateFile.toFile().delete();
			} else {
				try {
					Files.createDirectories(stateFolder);
				} catch (IOException e) {
					String errorMessage = "online db: folder " + workflowStatesFolder + " for workflow " + workflowId 
										  +  " and state " + stateIdStr + " cannot be created: " + e.getMessage();
					LOGGER.error(errorMessage);
					throw new RuntimeException(errorMessage);
				}
			}
			DataSource dataSource = new FileDataSource(stateFolder, network.getId());
			Properties parameters = new Properties();
			parameters.setProperty("iidm.export.xml.indent", "true");
			parameters.setProperty("iidm.export.xml.with-branch-state-variables", "true");
			parameters.setProperty("iidm.export.xml.with-breakers", "true");
			parameters.setProperty("iidm.export.xml.with-properties", "true");
			Exporters.export("XML", network, parameters, dataSource);
			// store network state values, for later serialization
			Map<HistoDbAttributeId, Object> networkValues = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(network.getId(), true, true)).getSingleValueMap();
			ConcurrentHashMap<Integer, Map<HistoDbAttributeId, Object>> workflowStates = new ConcurrentHashMap<Integer, Map<HistoDbAttributeId,Object>>();
			if ( workflowsStates.containsKey(workflowId) )
				workflowStates = workflowsStates.get(workflowId);
			workflowStates.put(stateId, networkValues);
			workflowsStates.put(workflowId, workflowStates);
		} else {
			String errorMessage = "online db: no state " + stateIdStr + " in network of workflow " + workflowId;
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}
	
	private void serializeStoredWorkflowsStates() {
		LOGGER.info("Serializing stored workflows states");
		for(String workflowId : workflowsStates.keySet()) {
			if ( workflowStatesFolderExists(workflowId)) {
				LOGGER.info("Serializing network data of workflow {}", workflowId);
				ConcurrentHashMap<Integer, Map<HistoDbAttributeId, Object>> workflowStates = workflowsStates.get(workflowId);
				Path workflowStatesFolder = getWorkflowStatesFolder(workflowId);
				Path csvFile = Paths.get(workflowStatesFolder.toString(), SERIALIZED_STATES_FILENAME);
		        try(FileWriter fileWriter = new FileWriter(csvFile.toFile());
		        	CsvListWriter csvWriter = new CsvListWriter(fileWriter, new CsvPreference.Builder('"', ';', "\r\n").build())) {
		        	boolean printHeaders = true;
					for(Integer stateId : workflowStates.keySet()) {
						Map<HistoDbAttributeId, Object> networkValues = workflowStates.get(stateId); 
						if ( printHeaders ) {
							List<String> headers = new ArrayList<>(networkValues.size());
					        for (HistoDbAttributeId attrId : networkValues.keySet()) {
					            headers.add(attrId.toString());
					        }
					        ArrayList<String> headersList = new ArrayList<>();
					        headersList.add("workflow");
					        headersList.add("state");
					        headersList.addAll(Arrays.asList(headers.toArray(new String[] {})));
					        csvWriter.writeHeader(headersList.toArray(new String[] {}));
					        printHeaders = false;
						}
				        ArrayList<Object> valuesList = new ArrayList<>();
				        valuesList.add(workflowId);
				        valuesList.add(stateId);
				        valuesList.addAll(Arrays.asList(networkValues.values().toArray()));
				        csvWriter.write(valuesList.toArray());
					}
		        } catch (IOException e) {
					LOGGER.error("Error serializing network data for workflow {}", workflowId);
				}
			}
		}
	}

	@Override
	public List<Integer> listStoredStates(String workflowId) {
		LOGGER.info("Getting list of stored states for workflow {}", workflowId);
		List<Integer> storedStates = new ArrayList<Integer>();
		if ( workflowStatesFolderExists(workflowId)) {
			Path workflowStatesFolder = getWorkflowStatesFolder(workflowId);
			File[] files = workflowStatesFolder.toFile().listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().startsWith(STORED_STATE_PREFIX);
			    }
			});
			for (File file : files) {
				if ( file.isDirectory() ) {
					String stateId = file.getName().substring(STORED_STATE_PREFIX.length());
					storedStates.add(Integer.parseInt(stateId));
				}
			}
			Collections.sort(storedStates, new Comparator<Integer>() {
			    public int compare(Integer o1, Integer o2) {
			        return o1.compareTo(o2);
			    }
			});
			LOGGER.info("Found {} state(s) for workflow {}", storedStates.size(), workflowId);
		} else {
			LOGGER.info("Found no state(s) for workflow {}", workflowId);
		}
		return storedStates;
	}

	@Override
	public Network getState(String workflowId, Integer stateId) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Getting state {} of workflow {}", stateIdStr, workflowId);
		Path workflowStatesFolder = getWorkflowStatesFolder(workflowId);
		Path stateFolder = Paths.get(workflowStatesFolder.toString(), STORED_STATE_PREFIX + stateIdStr);
		if ( Files.exists(stateFolder) && Files.isDirectory(stateFolder) ) {
			if ( stateFolder.toFile().list().length == 1 ) {
				File stateFile = stateFolder.toFile().listFiles()[0];
				String stateFileName = stateFile.getName();
				if ( stateFileName.endsWith(".xml"));
				String basename = stateFileName.substring(0, stateFileName.length()-4);
				DataSource dataSource = new FileDataSource(stateFolder, basename);
				//Network network = Importers.import_("XML", dataSource, null);
				// with the new post processors configuration, the post processing is applied also to xml import
				Importer xmlImporter = Importers.getImporter("XML");
				Importer noppImporter = Importers.removePostProcessors(xmlImporter);
				Network network = noppImporter.import_(dataSource, null);
				return network;
			}
		}
		return null;
	}
	
	@Override
	public void exportState(String workflowId, Integer stateId, Path folder) {
		LOGGER.info("Exporting network data of workflow {} and state {} to folder {}", workflowId, stateId, folder);
		Network network = getState(workflowId, stateId);
		NetworkData networkData = NetworkDataExtractor.extract(network);
		NetworkDataExporter.export(networkData, folder);
	}
	
	@Override
	public boolean deleteWorkflow(String workflowId) {
		LOGGER.info("Deleting workflow {}", workflowId);
		boolean workflowDeleted = false;
		boolean workflowStatesDeleted = true;
		// if stored states for this workflow exist
		if ( workflowStatesFolderExists(workflowId) )
			// delete them
			workflowStatesDeleted = deleteStates(workflowId);
		// if stored states have been deleted
		if ( workflowStatesDeleted ) {
			// store workflow results
			Path workflowFile = Paths.get(config.getOnlineDbDir().toFile().toString(), STORED_WORKFLOW_PREFIX + workflowId);
			if ( workflowFile.toFile().exists() && workflowFile.toFile().isFile()  )
				try {
					workflowDeleted = Files.deleteIfExists(workflowFile);
				} catch (IOException e) {
					LOGGER.error("Cannot delete workflow {} from online DB: {}", workflowId, e.getMessage());
				}
			else
				LOGGER.warn("No workflow {} stored in the online DB", workflowId);
		}
		return workflowDeleted;
	}
	
	
	@Override
	public boolean deleteStates(String workflowId) {
		LOGGER.info("Deleting stored states of workflow {}", workflowId);
		boolean workflowStatesDeleted = false;
		Path workflowStatesFolder = Paths.get(config.getOnlineDbDir().toFile().toString(), STORED_WORKFLOW_STATES_FOLDER_PREFIX + workflowId);
		if ( workflowStatesFolder.toFile().exists() && workflowStatesFolder.toFile().isDirectory() )
			try {
				FileUtils.deleteDirectory(workflowStatesFolder.toFile());
				workflowStatesDeleted = true;
			} catch (IOException e) {
				LOGGER.error("Cannot delete stored states of workflow {} from online DB: ", workflowId, e.getMessage());
			}
		else
			LOGGER.warn("No states of workflow {} stored in the online DB", workflowId);
		return workflowStatesDeleted;
	}
	
	
	@Override
	public void exportStates(String workflowId, Path file) {
		if ( workflowStatesFolderExists(workflowId)) {
			LOGGER.info("Exporting states for workflow {}", workflowId);
			Path workflowStatesFolder = getWorkflowStatesFolder(workflowId);
			Path csvFile = Paths.get(workflowStatesFolder.toString(), SERIALIZED_STATES_FILENAME);
			if ( !csvFile.toFile().exists() ) {
				LOGGER.info("Serializing network data of workflow {}", workflowId);
				try(FileWriter fileWriter = new FileWriter(csvFile.toFile());
					CsvListWriter csvWriter = new CsvListWriter(fileWriter, new CsvPreference.Builder('"', ';', "\r\n").build())) {
					boolean printHeaders = true;
					for(Integer stateId : listStoredStates(workflowId)) {
						Network network = getState(workflowId, stateId);
						if ( network != null ) {
							Map<HistoDbAttributeId, Object> networkValues = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(network.getId(), true, true)).getSingleValueMap();
							if ( printHeaders ) {
								List<String> headers = new ArrayList<>(networkValues.size());
						        for (HistoDbAttributeId attrId : networkValues.keySet()) {
						            headers.add(attrId.toString());
						        }
						        ArrayList<String> headersList = new ArrayList<>();
						        headersList.add("workflow");
						        headersList.add("state");
						        headersList.addAll(Arrays.asList(headers.toArray(new String[] {})));
						        csvWriter.writeHeader(headersList.toArray(new String[] {}));
						        printHeaders = false;
							}
					        ArrayList<Object> valuesList = new ArrayList<>();
					        valuesList.add(workflowId);
					        valuesList.add(stateId);
					        valuesList.addAll(Arrays.asList(networkValues.values().toArray()));
					        csvWriter.write(valuesList.toArray());
						}
					}
				} catch (IOException e) {
					LOGGER.error("Error serializing network data for workflow {}", workflowId);
				}
			}
			try {
				Files.copy(csvFile, file, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else 
			LOGGER.error("No stored states for workflow {}", workflowId);
	}

	@Override
	public void storeViolations(String workflowId, Integer stateId, OnlineStep step, List<LimitViolation> violations) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Storing violations for wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		storeViolations(workflowId, STORED_VIOLATIONS_MAP_PREFIX + step.name() + "_" + stateIdStr, violations);
		LOGGER.info("Storing violations metadata for wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		storeViolationsMetadata(workflowId, stateIdStr, step, violations);
	}

	private void storeViolations(String workflowId, String mapName, List<LimitViolation> violations) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			Map<String, String> metricsMap = wfMVStore.openMap(mapName, mapBuilder);
			for ( LimitViolation limitViolation : violations ) {
//				Violation violation = new Violation(limitViolation.getSubject().getId(),
//													limitViolation.getLimitType(), 
//													limitViolation.getLimit(), 
//													limitViolation.getValue());
//				metricsMap.put(violation.getId(), OnlineDbMVStoreUtils.violationToJson(violation));
				String violationId = limitViolation.getSubject().getId();
				// there could be 2 violations on the same line, on both terminals: I need to store both
				if ( metricsMap.containsKey(violationId) )
					violationId += "__1"; 
				metricsMap.put(violationId, OnlineDbMVStoreUtils.limitViolationToJson(limitViolation));
			}
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing violations for wf " +  workflowId + " in map " + mapName + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

	private void storeViolationsMetadata(String workflowId, String stateId, OnlineStep step, List<LimitViolation> violations) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			
			// save info about stored wf steps
			MVMap<String, String> storedStepsMap = wfMVStore.openMap(STORED_VIOLATIONS_STEPS_MAP_NAME, mapBuilder);
			storedStepsMap.putIfAbsent(step.name(), "1");
			// save info about stored states per step
			MVMap<String, String> stepStateMap = wfMVStore.openMap(step.name() + STORED_VIOLATIONS_STATES_MAP_SUFFIX, mapBuilder);
			stepStateMap.putIfAbsent(stateId, "");
			
			// save info about stored wf states
			MVMap<String, String> storedStatesMap = wfMVStore.openMap(STORED_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			storedStatesMap.putIfAbsent(stateId, "1");
			// save info about stored steps per state
			MVMap<String, String> stepStepMap = wfMVStore.openMap(stateId + STORED_VIOLATIONS_STEPS_MAP_SUFFIX, mapBuilder);
			stepStepMap.putIfAbsent(step.name(), "");				
			
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing violations metadata for wf " +  workflowId + ", step "+ step.name() + ", state " + stateId + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

	@Override
	public List<LimitViolation> getViolations(String workflowId, Integer stateId, OnlineStep step) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Getting violations for wf {}, step {} and state {}", workflowId, step.name(), stateIdStr);
		return getStoredViolations(workflowId, STORED_VIOLATIONS_MAP_PREFIX + step.name() + "_" + stateIdStr, null);
	}

	private List<LimitViolation> getStoredViolations(String workflowId, String mapName, Network network) {
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.getMapNames().contains(mapName) ) {
				if ( network==null )
					// load network: used to get equipment from equipment id, when creating limit violations
					network = getState(workflowId, 0);
				if ( network!=null ) {
					List<LimitViolation> violations = new ArrayList<LimitViolation>();
					Map<String, String> storedMap = wfMVStore.openMap(mapName, mapBuilder);
					for ( String violationId : storedMap.keySet() ) {
//						Violation violation = OnlineDbMVStoreUtils.jsonToViolation(storedMap.get(violationId));
//						Identifiable subject = network.getIdentifiable(violation.getSubject());
//						if ( subject!= null )
//							violations.add(new LimitViolation(subject, 
//															  violation.getLimitType(), 
//															  violation.getLimit(), 
//															  violation.getValue()));
						LimitViolation violation = OnlineDbMVStoreUtils.jsonToLimitViolation(storedMap.get(violationId), network);
						if ( violation != null )
							violations.add(violation);
					}
					return violations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get violations", workflowId);
					return null;
				}	
			} else {
				LOGGER.warn("No map {} in wf {}", mapName, workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<OnlineStep, List<LimitViolation>> getViolations(String workflowId, Integer stateId) {
		String stateIdStr = Integer.toString(stateId);
		LOGGER.info("Getting violations for wf {} and state {}", workflowId, stateIdStr);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			if ( storedStatesMap.containsKey(stateIdStr) ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<OnlineStep, List<LimitViolation>> stateViolations = new HashMap<OnlineStep, List<LimitViolation>>();
					MVMap<String, String> storedStepsMap = wfMVStore.openMap(stateIdStr + STORED_VIOLATIONS_STEPS_MAP_SUFFIX, mapBuilder);
					for ( String stepName : storedStepsMap.keySet() ) {
						OnlineStep step = OnlineStep.valueOf(stepName);
						List<LimitViolation> violations = getStoredViolations(workflowId, STORED_VIOLATIONS_MAP_PREFIX + step.name() + "_" + stateId, network);
						if ( violations!=null )
							stateViolations.put(step, violations);
					}
					return stateViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No violations for wf {} and state {}", workflowId, stateIdStr);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	

	@Override
	public Map<Integer, List<LimitViolation>> getViolations(String workflowId, OnlineStep step) {
		LOGGER.info("Getting violations for wf {} and step {}", workflowId, step.name());
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedStepsMap = wfMVStore.openMap(STORED_VIOLATIONS_STEPS_MAP_NAME, mapBuilder);
			if ( storedStepsMap.containsKey(step.name()) ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<Integer, List<LimitViolation>> stepViolations = new HashMap<Integer, List<LimitViolation>>();
					MVMap<String, String> storedStatesMap = wfMVStore.openMap(step.name() + STORED_VIOLATIONS_STATES_MAP_SUFFIX, mapBuilder);
					for ( String stateId : storedStatesMap.keySet() ) {
						List<LimitViolation> violations = getStoredViolations(workflowId, STORED_VIOLATIONS_MAP_PREFIX + step.name() + "_" + stateId, network);
						if ( violations!=null )
							stepViolations.put(Integer.valueOf(stateId), violations);
					}
					return stepViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No violations for wf {} and step {}", workflowId, step.name());
				return null;
			}
		} else {
			LOGGER.warn("No data for wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public Map<Integer, Map<OnlineStep, List<LimitViolation>>> getViolations(String workflowId) {
		LOGGER.info("Getting violations for wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			if ( !storedStatesMap.isEmpty() ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<Integer, Map<OnlineStep, List<LimitViolation>>> wfViolations = new HashMap<Integer, Map<OnlineStep,List<LimitViolation>>>();
					for(String stateIdStr : storedStatesMap.keySet()) {
						Integer stateId = Integer.parseInt(stateIdStr);
						Map<OnlineStep, List<LimitViolation>> stateViolations = new HashMap<OnlineStep, List<LimitViolation>>();
						MVMap<String, String> storedStepsMap = wfMVStore.openMap(stateIdStr + STORED_VIOLATIONS_STEPS_MAP_SUFFIX, mapBuilder);
						if ( !storedStepsMap.isEmpty() ) {
							for ( String stepName : storedStepsMap.keySet() ) {
								OnlineStep step = OnlineStep.valueOf(stepName);
								List<LimitViolation> violations = getStoredViolations(workflowId, STORED_VIOLATIONS_MAP_PREFIX + step.name() + "_" + stateId, network);
								if ( violations!=null )
									stateViolations.put(step, violations);
							}
							wfViolations.put(stateId, stateViolations);
						}
					}
					return wfViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No violations for wf {}", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data for wf {}", workflowId);
			return null;
		}
	}
	
	@Override
	public void storePostContingencyViolations(String workflowId, Integer stateId, String contingencyId, 
											   boolean loadflowConverge, List<LimitViolation> violations) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Storing post contingency violations for wf {}, contingency {} and state {}", workflowId, contingencyId, stateIdStr);
		storeViolations(workflowId, STORED_PC_VIOLATIONS_MAP_PREFIX + contingencyId + "_" + stateIdStr, violations);
		LOGGER.info("Storing post contingency violations metadata for wf {}, contingency {} and state {}", workflowId, contingencyId, stateIdStr);
		storePCViolationsMetadata(workflowId, stateIdStr, contingencyId, violations);
		LOGGER.info("Storing post contingency loadflow convergence for wf {}, contingency {} and state {}", workflowId, contingencyId, stateIdStr);
		storePSLoadflowConvergence(workflowId, stateIdStr, contingencyId, loadflowConverge);
	}

	private synchronized void storePCViolationsMetadata(String workflowId, String stateId, String contingencyId, List<LimitViolation> violations) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			
			// save info about stored wf contingencies
			MVMap<String, String> storedContingenciesMap = wfMVStore.openMap(STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_NAME, mapBuilder);
			storedContingenciesMap.putIfAbsent(contingencyId, "1");
			// save info about stored states per contingency
			MVMap<String, String> contingencyStateMap = wfMVStore.openMap(contingencyId + STORED_PC_VIOLATIONS_STATES_MAP_SUFFIX, mapBuilder);
			contingencyStateMap.putIfAbsent(stateId, "");
			
			// save info about stored wf states
			MVMap<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			storedStatesMap.putIfAbsent(stateId, "1");
			// save info about stored contingencies per state
			MVMap<String, String> stateContingencyMap = wfMVStore.openMap(stateId + STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
			LOGGER.info("storePCViolationsMetadata: Adding contingency {} to map {} for workflow {}, state {}", 
					contingencyId,
					stateId + STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_SUFFIX, 
					workflowId, 
					stateId
					);
			//stateContingencyMap.putIfAbsent(contingencyId, "");				
			stateContingencyMap.put(contingencyId, "");
			
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing pc violations metadata for wf " +  workflowId + ", contingency "+ contingencyId + ", state " + stateId + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}
	
	private synchronized void storePSLoadflowConvergence(String workflowId, String stateId, String contingencyId, boolean loadflowConverge) {
		try {
			MVStore wfMVStore = getStore(workflowId);
			
			// save info about stored wf contingencies
			MVMap<String, String> storedContingenciesMap = wfMVStore.openMap(STORED_PC_LOADFLOW_CONTINGENCIES_MAP_NAME, mapBuilder);
			storedContingenciesMap.putIfAbsent(contingencyId, "1");
			// save info about stored states per contingency
			MVMap<String, String> contingencyStateMap = wfMVStore.openMap(contingencyId + STORED_PC_LOADFLOW_STATES_MAP_SUFFIX, mapBuilder);
			contingencyStateMap.putIfAbsent(stateId, Boolean.toString(loadflowConverge));
			
			// save info about stored wf states
			MVMap<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_LOADFLOW_STATES_MAP_NAME, mapBuilder);
			storedStatesMap.putIfAbsent(stateId, "1");
			// save info about stored contingencies per state
			MVMap<String, String> stateContingencyMap = wfMVStore.openMap(stateId + STORED_PC_LOADFLOW_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
			LOGGER.info("storePSLoadflowConvergence: Adding contingency {} to map {} for workflow {}, state {}", 
					contingencyId,
					stateId + STORED_PC_LOADFLOW_CONTINGENCIES_MAP_SUFFIX, 
					workflowId, 
					stateId
					);
			//stateContingencyMap.putIfAbsent(contingencyId, Boolean.toString(loadflowConverge));				
			stateContingencyMap.put(contingencyId, Boolean.toString(loadflowConverge));
			
			wfMVStore.commit();
		} catch(Throwable e) {
			String errorMessage = "Error storing pc loadflow convergence for wf " +  workflowId + ", contingency "+ contingencyId + ", state " + stateId + ": " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

	@Override
	public List<LimitViolation> getPostContingencyViolations(String workflowId, Integer stateId, String contingencyId) {
		String stateIdStr=String.valueOf(stateId);
		LOGGER.info("Getting post contingency violations for wf {}, contingency {} and state {}", workflowId, contingencyId, stateIdStr);
		return getStoredViolations(workflowId, STORED_PC_VIOLATIONS_MAP_PREFIX + contingencyId + "_" + stateIdStr, null);
	}

	@Override
	public Map<String, List<LimitViolation>> getPostContingencyViolations(String workflowId, Integer stateId) {
		String stateIdStr = Integer.toString(stateId);
		LOGGER.info("Getting post contingency violations for wf {} and state {}", workflowId, stateIdStr);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			if ( storedStatesMap.containsKey(stateIdStr) ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<String, List<LimitViolation>> stateViolations = new HashMap<String, List<LimitViolation>>();
					MVMap<String, String> storedContingenciesMap = wfMVStore.openMap(stateIdStr + STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
					for ( String contingencyId : storedContingenciesMap.keySet() ) {
						List<LimitViolation> violations = getStoredViolations(workflowId, STORED_PC_VIOLATIONS_MAP_PREFIX + contingencyId + "_" + stateId, network);
						if ( violations!=null )
							stateViolations.put(contingencyId, violations);
					}
					return stateViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get post contingency violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No post contingency violations for wf {} and state {}", workflowId, stateIdStr);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<Integer, List<LimitViolation>> getPostContingencyViolations(String workflowId, String contingencyId) {
		LOGGER.info("Getting post contingency violations for wf {} and contingency {}", workflowId, contingencyId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedContingenciesMap = wfMVStore.openMap(STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_NAME, mapBuilder);
			if ( storedContingenciesMap.containsKey(contingencyId) ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<Integer, List<LimitViolation>> contingencyViolations = new HashMap<Integer, List<LimitViolation>>();
					MVMap<String, String> storedStatesMap = wfMVStore.openMap(contingencyId + STORED_PC_VIOLATIONS_STATES_MAP_SUFFIX, mapBuilder);
					for ( String stateId : storedStatesMap.keySet() ) {
						List<LimitViolation> violations = getStoredViolations(workflowId, STORED_PC_VIOLATIONS_MAP_PREFIX + contingencyId + "_" + stateId, network);
						if ( violations!=null )
							contingencyViolations.put(Integer.valueOf(stateId), violations);
					}
					return contingencyViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get post contingency violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No post contingency violations for wf {} and contingency {}", workflowId, contingencyId);
				return null;
			}
		} else {
			LOGGER.warn("No data for wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<Integer, Map<String, List<LimitViolation>>> getPostContingencyViolations(String workflowId) {
		LOGGER.info("Getting post contingency violations for wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			// check if there are stored violations
			Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_VIOLATIONS_STATES_MAP_NAME, mapBuilder);
			if ( !storedStatesMap.isEmpty() ) {
				// load network: used to get equipment from equipment id, when creating limit violations
				Network network = getState(workflowId, 0);
				if ( network!=null ) {
					Map<Integer, Map<String, List<LimitViolation>>> wfViolations = new HashMap<Integer, Map<String,List<LimitViolation>>>();
					for(String stateIdStr : storedStatesMap.keySet()) {
						Integer stateId = Integer.parseInt(stateIdStr);
						Map<String, List<LimitViolation>> stateViolations = new HashMap<String, List<LimitViolation>>();
						MVMap<String, String> storedContingenciesMap = wfMVStore.openMap(stateIdStr + STORED_PC_VIOLATIONS_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
						if ( !storedContingenciesMap.isEmpty() ) {
							for ( String contingencyId : storedContingenciesMap.keySet() ) {
								List<LimitViolation> violations = getStoredViolations(workflowId, STORED_PC_VIOLATIONS_MAP_PREFIX + contingencyId + "_" + stateId, network);
								if ( violations!=null )
									stateViolations.put(contingencyId, violations);
							}
							wfViolations.put(stateId, stateViolations);
						}
					}
					return wfViolations;
				} else {
					LOGGER.warn("No network data (states) stored for wf {}, cannot get post contingency violations", workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No post contingency violations for wf {}", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data for wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<String, Boolean> getPostContingencyLoadflowConvergence(String workflowId, Integer stateId) {
		String stateIdStr = Integer.toString(stateId);
		if ( isWorkflowStored(workflowId) ) {
			Map<String, Boolean> loadflowConvergence = new HashMap<String, Boolean>();
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.getMapNames().contains(STORED_PC_LOADFLOW_STATES_MAP_NAME) ) {
				Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_LOADFLOW_STATES_MAP_NAME, mapBuilder);
				if ( storedStatesMap.containsKey(stateIdStr)) {
					MVMap<String, String> stateContingencyMap = wfMVStore.openMap(stateIdStr + STORED_PC_LOADFLOW_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
					for ( String contingencyId : stateContingencyMap.keySet() ) {
						loadflowConvergence.put(contingencyId, Boolean.valueOf(stateContingencyMap.get(contingencyId)));
					}
					return loadflowConvergence;
				} else {
					LOGGER.warn("No post contingency loadflow data for state {} in wf {}", stateIdStr, workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No post contingency loadflow data in wf {}", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<Integer, Boolean> getPostContingencyLoadflowConvergence(String workflowId, String contingencyId) {
		if ( isWorkflowStored(workflowId) ) {
			Map<Integer, Boolean> loadflowConvergence = new HashMap<Integer, Boolean>();
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.getMapNames().contains(STORED_PC_LOADFLOW_CONTINGENCIES_MAP_NAME) ) {
				Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_LOADFLOW_CONTINGENCIES_MAP_NAME, mapBuilder);
				if ( storedStatesMap.containsKey(contingencyId)) {
					MVMap<String, String> contingencyStateMap = wfMVStore.openMap(contingencyId + STORED_PC_LOADFLOW_STATES_MAP_SUFFIX, mapBuilder);
					for ( String stateId : contingencyStateMap.keySet() ) {
						loadflowConvergence.put(Integer.valueOf(stateId), Boolean.valueOf(contingencyStateMap.get(stateId)));
					}
					return loadflowConvergence;
				} else {
					LOGGER.warn("No post contingency loadflow data for contingency {} in wf {}", contingencyId, workflowId);
					return null;
				}
			} else {
				LOGGER.warn("No post contingency loadflow data in wf {}", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}

	@Override
	public Map<Integer, Map<String, Boolean>> getPostContingencyLoadflowConvergence(String workflowId) {
		if ( isWorkflowStored(workflowId) ) {
			Map<Integer, Map<String, Boolean>> loadflowConvergence = new HashMap<Integer, Map<String, Boolean>>();
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.getMapNames().contains(STORED_PC_LOADFLOW_STATES_MAP_NAME) ) {
				Map<String, String> storedStatesMap = wfMVStore.openMap(STORED_PC_LOADFLOW_STATES_MAP_NAME, mapBuilder);
				for ( String stateId : storedStatesMap.keySet() ) {
					MVMap<String, String> stateContingencyMap = wfMVStore.openMap(stateId + STORED_PC_LOADFLOW_CONTINGENCIES_MAP_SUFFIX, mapBuilder);
					HashMap<String, Boolean> stateLoadflowConvergence = new HashMap<String, Boolean>();
					for ( String contingencyId : stateContingencyMap.keySet() ) {
						stateLoadflowConvergence.put(contingencyId, Boolean.valueOf(stateContingencyMap.get(contingencyId)));
					}
					loadflowConvergence.put(Integer.valueOf(stateId), stateLoadflowConvergence);
				}
				return loadflowConvergence;
			} else {
				LOGGER.warn("No post contingency loadflow data in wf {}", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	// these wca rules methods are similar to the mcla rules methods: refactoring could be a good idea
	@Override
	public void storeWcaRulesResults(String workflowId, OnlineWorkflowRulesResults results) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(results, "online workflow wca rules results is null");
		LOGGER.info("Storing results of wca rules for workflow {}", workflowId);
		MVStore wfMVStore = getStore(workflowId);
		// check if the results for this wf have already been stored
		if ( wfMVStore.hasMap(STORED_WCA_RULES_RESULTS_MAP_NAME) )
			removeWfWcaRulesResults(workflowId, wfMVStore);
		MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_WCA_RULES_RESULTS_MAP_NAME, mapBuilder);
		// store time horizon
		storedRulesResultsMap.put(STORED_RESULTS_TIMEHORIZON_KEY, results.getTimeHorizon().getName());
		// store contingencies with security rules results
		storedRulesResultsMap.put(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY, 
								  OnlineDbMVStoreUtils.contingenciesIdsToJson(results.getContingenciesWithSecurityRulesResults()));
		// store wca rules results for contingencies
		for (String contingencyId : results.getContingenciesWithSecurityRulesResults()) {
			MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
			MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
			for (Integer stateId : results.getStatesWithSecurityRulesResults(contingencyId)) {
				// store state status
				StateStatus status = results.getStateStatus(contingencyId, stateId);
				storedStateStatusMap.put(stateId.toString(), status.name());
				// store state rules results
				Map<String, Boolean> stateResults = results.getStateResults(contingencyId, stateId);
				storedStateResultsMap.put(stateId.toString(), OnlineDbMVStoreUtils.indexesDataToJson(stateResults));
			}
		}
		wfMVStore.commit();
	}
		
	private void removeWfWcaRulesResults(String workflowId, MVStore wfMVStore) {
		LOGGER.debug("Removing existing wca rules results for workflow {}", workflowId);
		MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_WCA_RULES_RESULTS_MAP_NAME, mapBuilder);
		// remove rules results 
		Collection<String> rulesContingencies = OnlineDbMVStoreUtils.jsonToContingenciesIds(
				storedRulesResultsMap.get(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY));
		for (String contingencyId : rulesContingencies) {
			MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedStateStatusMap);
			MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
			wfMVStore.removeMap(storedStateResultsMap);
		}
		// remove info about stored rules results
		wfMVStore.removeMap(storedRulesResultsMap);
		// commit removal
		wfMVStore.commit();
	}

	@Override
	public OnlineWorkflowRulesResults getWcaRulesResults(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting wca rules results of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(STORED_WCA_RULES_RESULTS_MAP_NAME) ) {
				MVMap<String, String> storedRulesResultsMap = wfMVStore.openMap(STORED_WCA_RULES_RESULTS_MAP_NAME, mapBuilder);
				// create workflow rules results
				OnlineWorkflowRulesResultsImpl wfRulesResults = new OnlineWorkflowRulesResultsImpl(
																workflowId, 
																TimeHorizon.valueOf(storedRulesResultsMap.get(STORED_RESULTS_TIMEHORIZON_KEY)));
				// add contingencies with rules results
				Collection<String> contingenciesWithRules = OnlineDbMVStoreUtils.jsonToContingenciesIds(
																storedRulesResultsMap.get(STORED_RULES_RESULTS_CONTINGENCIES_WITH_RULES_KEY));
				for (String contingencyId : contingenciesWithRules) {
					MVMap<String, String> storedStateStatusMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_STATUS_MAP_SUFFIX, mapBuilder);
					MVMap<String, String> storedStateResultsMap = wfMVStore.openMap(contingencyId + STORED_WCA_RULES_RESULTS_STATE_RESULTS_MAP_SUFFIX, mapBuilder);
					for (String stateId : storedStateStatusMap.keySet()) {
						Map<String, Boolean> stateResults = OnlineDbMVStoreUtils.jsonToIndexesData(storedStateResultsMap.get(stateId));
						StateStatus stateStatus = StateStatus.valueOf(storedStateStatusMap.get(stateId));
						wfRulesResults.addContingencyWithSecurityRulesResults(contingencyId, Integer.parseInt(stateId), stateStatus, stateResults);
					}
				}
				return wfRulesResults;
			} else {
				LOGGER.warn("No wca rules results of wf {} stored in online db", workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	
	
	
	
	
	
	
	
	@Override
	public void close() throws Exception {
		closeStores();
		serializeStoredWorkflowsStates();
	}
	
	private boolean isWorkflowStored(String workflowId) {
		Path workflowFile = Paths.get(config.getOnlineDbDir().toString(), STORED_WORKFLOW_PREFIX + workflowId);
		return Files.exists(workflowFile);
	}
	
	private boolean workflowStatesFolderExists(String workflowId) {
		Path workflowStatesFolder = Paths.get(config.getOnlineDbDir().toString(), STORED_WORKFLOW_STATES_FOLDER_PREFIX + workflowId);
		return Files.exists(workflowStatesFolder) && Files.isDirectory(workflowStatesFolder);
	}
	
	private Path getWorkflowStatesFolder(String workflowId) {
		Path workflowStatesFolder = Paths.get(config.getOnlineDbDir().toString(), STORED_WORKFLOW_STATES_FOLDER_PREFIX + workflowId);
		if ( !workflowStatesFolderExists(workflowId) )
			try {
				Files.createDirectories(workflowStatesFolder);
			} catch (IOException e) {
				String errorMessage = "online db: folder " + workflowStatesFolder + " for workflow " + workflowId +  " cannot be created: " + e.getMessage();
				LOGGER.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			
		return workflowStatesFolder;
	}
	
	/*
	*  support methods                          
	*  to inspect the content of the online db 
	*/
	
	public List<String> getStoredMaps(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Getting stored maps of wf {}", workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			return new ArrayList<String>(wfMVStore.getMapNames());
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	public String exportStoredMapsList(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Exporting list of stored maps of wf {}", workflowId);
		StringBuffer storedMapList = new StringBuffer();
		List<String> storedMaps = getStoredMaps(workflowId);
		if ( storedMaps != null ) {
			for (String map : storedMaps) {
				storedMapList.append(map + "\n");
			}
		}
		return storedMapList.toString();
	}
	
	public String exportStoredMapContent(String workflowId, String mapName) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		Objects.requireNonNull(mapName, "map name id is null");
		LOGGER.info("Exporting content of stored map {}  of wf {}", mapName, workflowId);
		if ( isWorkflowStored(workflowId) ) {
			MVStore wfMVStore = getStore(workflowId);
			if ( wfMVStore.hasMap(mapName) ) {
				StringBuffer storedMapContent = new StringBuffer();
				storedMapContent.append("Map " + mapName + "\n");
				MVMap<String, String> storedMap = wfMVStore.openMap(mapName, mapBuilder);
				for (String key : storedMap.keySet()) {
					storedMapContent.append(key + " = " + storedMap.get(key) + "\n");
				}
				return storedMapContent.toString();
			} else {
				LOGGER.warn("No {} map in wf {}", mapName, workflowId);
				return null;
			}
		} else {
			LOGGER.warn("No data about wf {}", workflowId);
			return null;
		}
	}
	
	public String exportStoredMapsContent(String workflowId) {
		Objects.requireNonNull(workflowId, "workflow id is null");
		LOGGER.info("Exporting content of stored maps of wf {}", workflowId);
		StringBuffer storedMapList = new StringBuffer();
		List<String> storedMaps = getStoredMaps(workflowId);
		if ( storedMaps != null ) {
			for (String map : storedMaps) {
				storedMapList.append(exportStoredMapContent(workflowId, map) + "\n");
			}
		}
		return storedMapList.toString();
	}

}
