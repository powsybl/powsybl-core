/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

import eu.itesla_project.commons.Version;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.ComputationResourcesStatus;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.mcla.ForecastErrorsDataStorageImpl;
import eu.itesla_project.modules.MergeOptimizerFactory;
import eu.itesla_project.modules.cases.CaseRepository;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.MontecarloSamplerFactory;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.RulesFacadeFactory;
import eu.itesla_project.modules.online.TimeHorizon;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.Security;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.simulation.*;
import eu.itesla_project.modules.wca.UncertaintiesAnalyserFactory;
import eu.itesla_project.modules.wca.WCAFactory;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysis;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisConfig;
import eu.itesla_project.offline.forecast_errors.ForecastErrorsAnalysisParameters;
import gnu.trove.list.array.TIntArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class LocalOnlineApplication extends NotificationBroadcasterSupport implements OnlineApplication, OnlineApplicationListener,  LocalOnlineApplicationMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalOnlineApplication.class);

    private static final int BUSY_CORES_HISTORY_SIZE = 20;

    private  OnlineConfig config;

    private final ComputationManager computationManager;

    private final ScheduledExecutorService ses;

    private final ExecutorService es;

    private final boolean enableJmx;
    
    private OnlineWorkflow workflow ;

    private  HistoDbClient histoDbClient;

    private  DynamicDatabaseClientFactory   ddbClientFactory;

    private  ContingenciesAndActionsDatabaseClient cadbClient;

    private  RulesDbClient rulesDb;

    private  WCAFactory wcaFactory;

    private  LoadFlowFactory loadFlowFactory;

    private  ForecastErrorsDataStorage feDataStorage;
    
    private  OnlineDb onlineDb;

    private  UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory;
    
    private  CorrectiveControlOptimizerFactory correctiveControlOptimizerFactory;
    
    private  CaseRepository caseRepository;

    private  SimulatorFactory simulatorFactory;

    private  MontecarloSamplerFactory montecarloSamplerFactory;
    
    private MergeOptimizerFactory mergeOptimizerFactory;
    
    private RulesFacadeFactory rulesFacadeFactory;

    private final ScheduledFuture<?> future;

    private final Map<String, OnlineWorkflowImpl> workflows = new ConcurrentHashMap<>();

    private final TIntArrayList busyCores;

    private final Lock listenersLock = new ReentrantLock();
    private final Lock workflowLock = new ReentrantLock();
    private final List<OnlineApplicationListener> listeners = new CopyOnWriteArrayList<>();

    private final AtomicInteger notificationIndex = new AtomicInteger();

    public LocalOnlineApplication(OnlineConfig config,
                                  ComputationManager computationManager,
                                  ScheduledExecutorService ses,
                                  ExecutorService es,
                                  boolean enableJmx)
            throws IllegalAccessException, InstantiationException, InstanceAlreadyExistsException,
            MBeanRegistrationException, MalformedObjectNameException, NotCompliantMBeanException, IOException {
        this.config = config;
        this.computationManager = computationManager;
        this.ses = ses;
        this.es = es;

        LOGGER.info("Version: {}", Version.VERSION);
       
        init();        	
       
        this.enableJmx = enableJmx;
      

        busyCores = new TIntArrayList();
        ComputationResourcesStatus status = computationManager.getResourcesStatus();
        int v = status.getBusyCores();
        for (int i = 0; i < BUSY_CORES_HISTORY_SIZE; i++) {
            busyCores.add(v);
        }

        if (enableJmx) {
            // create and register online application mbean
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, new ObjectName(BEAN_NAME));

        }

        future = ses.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    notifyBusyCoresUpdate(false);
                } catch (Throwable t) {
                    LOGGER.error(t.toString(), t);
                }
            }
        }, 0, 20, TimeUnit.SECONDS);
    }
    
    
    
    private  void init() throws InstantiationException, IllegalAccessException{
    
         histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create();
         ddbClientFactory = config.getDynamicDbClientFactoryClass().newInstance();
         cadbClient = config.getContingencyDbClientFactoryClass().newInstance().create();
         rulesDb = config.getRulesDbClientFactoryClass().newInstance().create("rulesdb");
         wcaFactory = config.getWcaFactoryClass().newInstance();
         loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
         onlineDb = config.getOnlineDbFactoryClass().newInstance().create();
         uncertaintiesAnalyserFactory = config.getUncertaintiesAnalyserFactoryClass().newInstance();
         correctiveControlOptimizerFactory = config.getCorrectiveControlOptimizerFactoryClass().newInstance();
         simulatorFactory = config.getSimulatorFactoryClass().newInstance();
         montecarloSamplerFactory = config.getMontecarloSamplerFactory().newInstance();
         caseRepository = config.getCaseRepositoryFactoryClass().newInstance().create(computationManager);
         mergeOptimizerFactory = config.getMergeOptimizerFactory().newInstance();
         rulesFacadeFactory = config.getRulesFacadeFactory().newInstance();

    	this.feDataStorage = new ForecastErrorsDataStorageImpl(); //TODO ...
    	
    }

    @Override
    public int[] getBusyCores() {
        return busyCores.toArray();
    }

    @Override
    public void notifyListeners() {
    	notifyBusyCoresUpdate(true);
    }

    private void notifyBusyCoresUpdate(boolean force) {
        listenersLock.lock();
        try {
            // busy cores

            if (!force) {
            	long tim=System.currentTimeMillis();
            	if (tim % 2==0)            		
            		busyCores.insert(0, computationManager.getResourcesStatus().getBusyCores());
            	else
            		busyCores.insert(0, 1);

                busyCores.removeAt(busyCores.size()-1); // remove the older one
            }
            if (enableJmx) {
                sendNotification(new AttributeChangeNotification(this,
                                                                 notificationIndex.getAndIncrement(),
                                                                 System.currentTimeMillis(),
                                                                 "Busy cores has changed",
                                                                 BUSY_CORES_ATTRIBUTE,
                                                                 "int[]",
                                                                 null,
                                                                 busyCores.toArray()));
            }
            
        } finally {
            listenersLock.unlock();
        }
    }

    @Override
    public int getAvailableCores() {
        return computationManager.getResourcesStatus().getAvailableCores();
    }


    @Override
    public void startWorkflow( OnlineWorkflowStartParameters start, OnlineWorkflowParameters params)  {
    	
    	try {
			config=OnlineConfig.load();
			init();
		} catch (ClassNotFoundException | IOException | ParseException | InstantiationException | IllegalAccessException e1) {
			
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
    	
    	
        OnlineWorkflowContext oCtx = new OnlineWorkflowContext();
        OnlineWorkflowStartParameters startParams= start!=null ? start : OnlineWorkflowStartParameters.loadDefault();
        OnlineWorkflowParameters onlineParams = params !=null ? params : OnlineWorkflowParameters.loadDefault();
        
        
        LOGGER.info("Starting workflow: "+startParams.toString()+"\n"+onlineParams.toString());
        
        if(!workflowLock.tryLock())
        {
        	throw new RuntimeException("Already running");
        }
       
        try {
			workflow =startParams.getOnlineWorkflowFactoryClass().newInstance().create(computationManager, cadbClient, ddbClientFactory, histoDbClient, rulesDb, wcaFactory, loadFlowFactory, feDataStorage,
			        onlineDb, uncertaintiesAnalyserFactory, correctiveControlOptimizerFactory, simulatorFactory, caseRepository,
			        montecarloSamplerFactory, mergeOptimizerFactory, rulesFacadeFactory, onlineParams, startParams);
		} catch (InstantiationException  | IllegalAccessException e1  ) {
			e1.printStackTrace();
			throw new RuntimeException(e1.getMessage());
		}
        
        workflow.addOnlineApplicationListener(this);

       for (OnlineApplicationListener l : listeners)
           workflow.addOnlineApplicationListener(l);


        try {
            if (startParams.getOnlineApplicationListenerFactoryClass() != null) {
                OnlineApplicationListener listener = startParams.getOnlineApplicationListenerFactoryClass().newInstance().create();
                workflow.addOnlineApplicationListener(listener);
            }
        } catch (InstantiationException e ) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            workflow.start(oCtx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally
        {
            workflowLock.unlock();
            workflow=null;
        }
    }

    @Override
    public void stopWorkflow() {
    	
       // workflow.stop();
    }
    
    @Override
    public void addListener(OnlineApplicationListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(OnlineApplicationListener l) {
        listeners.remove(l);
    }
    

    @Override
    public void close() throws Exception {
        future.cancel(true);
       // histoDbClient.close();

        if (enableJmx) {
            // unregister application mbean
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(BEAN_NAME));
        }
        synchronized(this)
        {
        	this.notifyAll();
        }
    }

	@Override
	public void onBusyCoresUpdate(int[] busyCores) {
		
		
	}

	@Override
    public void onWorkflowStateUpdate(WorkSynthesis status) {
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                    "WorkflowStateUpdate",
                    WORK_STATES_ATTRIBUTE,
                    status.getClass().getName(),
                    null,
                    status));
        }

    }

	@Override
    public void onWorkflowUpdate(StatusSynthesis status) {
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                    "Running",
                    RUNNING_ATTRIBUTE,
                    status.getClass().getName(),
                    null,
                    status));
        }

    }

	@Override
    public void onWcaUpdate(RunningSynthesis wcaRunning) {
        LOGGER.info("SEND onWcaUpdate" + wcaRunning);
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                    "WcaRunning",
                    WCA_RUNNING_ATTRIBUTE,
                    wcaRunning.getClass().getName(),
                    null,
                    wcaRunning));
        }

    }

	@Override
    public void onStatesWithActionsUpdate(ContingencyStatesActionsSynthesis statesActions) {

        LOGGER.info("onStatesWithActionsUpdate received " + statesActions.getClass().getName());
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                    "StateActions",
                    STATES_ACTIONS_ATTRIBUTE,
                    statesActions.getClass().getName(),
                    null,
                    statesActions));
        }

    }

	@Override
    public void onStatesWithIndexesUpdate(ContingencyStatesIndexesSynthesis indexeActions) {
        LOGGER.info("onStatesWithIndexesUpdate received " + indexeActions.getClass().getName());
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                   // "StateActions",
                    "StatesIndexes",
                    STATES_INDEXES_ATTRIBUTE,
                    indexeActions.getClass().getName(),
                    null,
                    indexeActions));
        }

    }

	@Override
	public void onStatesWithSecurityRulesResultsUpdate(IndexSecurityRulesResultsSynthesis indexesResults) {
        LOGGER.info("onStatesWithSecurityRulesResultsUpdate received " + indexesResults.getClass().getName());
        if (enableJmx) {
            sendNotification(new AttributeChangeNotification(this,
                    notificationIndex.getAndIncrement(),
                    System.currentTimeMillis(),
                    "IndexSecurityRulesResults",
                    INDEXES_SECURITY_RULES_ATTRIBUTE,
                    indexesResults.getClass().getName(),
                    null,
                    indexesResults));
        }

    }

	/*@Override
    public void onStableContingencies(StableContingenciesSynthesis stableContingencies) {
        sendNotification(new AttributeChangeNotification(this,
                notificationIndex.getAndIncrement(),
                System.currentTimeMillis(),
                "StableContingencies",
                STABLE_CONTINGENCIES_ATTRIBUTE,
                stableContingencies.getClass().getName(),
                null,
                stableContingencies));

    }
	
	@Override
    public void onUnstableContingencies(UnstableContingenciesSynthesis unstableContingencies) {
        sendNotification(new AttributeChangeNotification(this,
                notificationIndex.getAndIncrement(),
                System.currentTimeMillis(),
                "UnstableContingencies",
                UNSTABLE_CONTINGENCIES_ATTRIBUTE,
                unstableContingencies.getClass().getName(),
                null,
                unstableContingencies));

    }*/
	
	@Override
	public void onWcaContingencies(WcaContingenciesSynthesis wcaContingencies) {
	        sendNotification(new AttributeChangeNotification(this,
	                notificationIndex.getAndIncrement(),
	                System.currentTimeMillis(),
	                "WcaContingencies",
	                WCA_CONTINGENCIES_ATTRIBUTE,
	                wcaContingencies.getClass().getName(),
	                null,
	                wcaContingencies));
	
	}
	
	@Override
	public void ping() {
		
	}

	@Override
	public void onDisconnection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnection() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown()  {
		System.out.println("Shutdown !!!");
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    @Override
    public void runFeaAnalysis(OnlineWorkflowStartParameters startconfig, ForecastErrorsAnalysisParameters parameters, String timeHorizonS) {
        LOGGER.info("Starting fea analysis: "+parameters.toString()+"\n"+timeHorizonS);

        if(!workflowLock.tryLock())
        {
            throw new RuntimeException("a computation is already running.");
        }

        try {
            ForecastErrorsAnalysis feAnalysis = new ForecastErrorsAnalysis(computationManager, ForecastErrorsAnalysisConfig.load(), parameters);
            if ("".equals(timeHorizonS)) {
                feAnalysis.start();
            } else {
                TimeHorizon timeHorizon = TimeHorizon.fromName(timeHorizonS);
                feAnalysis.start(timeHorizon);
            }
            System.out.println("Forecast Errors Analysis Terminated");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }  finally {
            workflowLock.unlock();
            workflow=null;
        }
    }

    @Override
	public void onWorkflowEnd(OnlineWorkflowContext context, OnlineDb onlineDb, ContingenciesAndActionsDatabaseClient cadbClient, OnlineWorkflowParameters parameters) {
    	System.out.println("LocalOnlineApplicationListener onWorkFlowEnd");
		
		
	}


    // start td simulations

    private static final String EMPTY_CONTINGENCY_ID = "Empty-Contingency";
    private Map<String, Boolean> runTDSimulation(Network network, Set<String> contingencyIds, boolean emptyContingency,
                                                 ComputationManager computationManager, SimulatorFactory simulatorFactory,
                                                 DynamicDatabaseClientFactory ddbFactory, ContingenciesAndActionsDatabaseClient contingencyDb,
                                                 Writer metricsContent) throws Exception {
        Map<String, Boolean> tdSimulationResults = new HashMap<String, Boolean>();
        Map<String, Object> initContext = new HashMap<>();
        SimulationParameters simulationParameters = SimulationParameters.load();
        // run stabilization
        Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0, ddbFactory);
        stabilization.init(simulationParameters, initContext);
        ImpactAnalysis impactAnalysis = null;
        System.out.println("running stabilization on network " + network.getId());
        StabilizationResult stabilizationResults = stabilization.run();
        metricsContent.write("****** BASECASE " + network.getId()+"\n");
        metricsContent.write("*** Stabilization Metrics ***\n");
        Map<String, String> stabilizationMetrics = stabilizationResults.getMetrics();
        if ( stabilizationMetrics!=null && !stabilizationMetrics.isEmpty()) {
            for(String parameter : stabilizationMetrics.keySet())
                metricsContent.write(parameter + " = " + stabilizationMetrics.get(parameter)+"\n");
        }
        metricsContent.flush();
        if (stabilizationResults.getStatus() == StabilizationStatus.COMPLETED) {
            if ( emptyContingency ) // store data for t-d simulation on empty contingency, i.e. stabilization
                tdSimulationResults.put(EMPTY_CONTINGENCY_ID, true);
            // check if there are contingencies to run impact analysis
            if ( contingencyIds==null && contingencyDb.getContingencies(network).size()==0 )
                contingencyIds = new HashSet<String>();
            if ( contingencyIds==null || !contingencyIds.isEmpty() ) {
                // run impact analysis
                impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingencyDb);
                impactAnalysis.init(simulationParameters, initContext);
                System.out.println("running impact analysis on network " + network.getId());
                ImpactAnalysisResult impactAnalisResults = impactAnalysis.run(stabilizationResults.getState(), contingencyIds);
                for(SecurityIndex index : impactAnalisResults.getSecurityIndexes() ) {
                    tdSimulationResults.put(index.getId().toString(), index.isOk());
                }
                metricsContent.write("*** Impact Analysis Metrics ***\n");
                Map<String, String> impactAnalysisMetrics = impactAnalisResults.getMetrics();
                if ( impactAnalysisMetrics!=null && !impactAnalysisMetrics.isEmpty()) {
                    for(String parameter : impactAnalysisMetrics.keySet())
                        metricsContent.write(parameter + " = " + impactAnalysisMetrics.get(parameter)+"\n");
                }
                metricsContent.flush();
            }
        } else {
            if ( emptyContingency ) // store data for t-d simulation on empty contingency, i.e. stabilization
                tdSimulationResults.put(EMPTY_CONTINGENCY_ID, false);
        }
        return tdSimulationResults;
    }

    private Path getFile(Path folder, String filename) {
        if ( folder != null )
            return Paths.get(folder.toString(), filename);
        return Paths.get(filename);
    }
    
    private void writeCsvViolations(String basecase, List<LimitViolation> networkViolations, CsvWriter cvsWriter) throws IOException {
    	for(LimitViolation violation : networkViolations) {
            String[] values = new String[]{basecase,
                    violation.getSubject().getId(),
                    violation.getLimitType().name(),
                    Float.toString(violation.getValue()),
                    Float.toString(violation.getLimit())};
            cvsWriter.writeRecord(values);
        }
    	cvsWriter.flush();
    }
    
    private void writeCsvTDResults(String basecase, Set<String> securityIndexIds, Map<String, Boolean> tdSimulationResults, 
    							boolean writeHeaders, CsvWriter cvsWriter) throws IOException {
    	String[] indexIds = securityIndexIds.toArray(new String[securityIndexIds.size()]);
        Arrays.sort(indexIds);
        if ( writeHeaders ) {
            String[] resultsHeaders = new String[indexIds.length+1];
            resultsHeaders[0] = "Basecase";
            int i = 1;
            for(String securityIndexId : indexIds)
            	resultsHeaders[i++] = securityIndexId;
            cvsWriter.writeRecord(resultsHeaders);
            cvsWriter.flush();
        }
        String[] values = new String[indexIds.length+1];
        values[0] = basecase;
        int i = 1;
        for(String securityIndexId : indexIds) {
            String result = "NA";
            if ( tdSimulationResults.containsKey(securityIndexId) )
                result = tdSimulationResults.get(securityIndexId) ? "OK" : "KO";
            values[i++] = result;
        }
        cvsWriter.writeRecord(values);
        cvsWriter.flush();
    }

    private void runTDSimulations(Path caseDir, String caseBaseName, String contingenciesIds, Boolean emptyContingency, Path outputFolder) throws Exception {
        //TODO check nulls
        //in contingenciesIds, we assume a comma separated list of ids ...
        Set<String> contingencyIds = (contingenciesIds != null) ?  Sets.newHashSet(contingenciesIds.split(",")) : null;
        Path metricsFile = getFile(outputFolder, "metrics.log");
        Path violationsFile = getFile(outputFolder, "networks-violations.csv");
        Path resultsFile = getFile(outputFolder, "simulation-results.csv");
        try (FileWriter metricsContent = new FileWriter(metricsFile.toFile());
        	 FileWriter violationsContent = new FileWriter(violationsFile.toFile());
        	 FileWriter resultsContent = new FileWriter(resultsFile.toFile())) {
        	CsvWriter violationsCvsWriter = new CsvWriter(violationsContent, ',');
        	CsvWriter resultsCvsWriter = new CsvWriter(resultsContent, ',');
        	Set<String> securityIndexIds = new LinkedHashSet<>();
        	try {
	            String[] violationsHeaders = new String[]{"Basecase", "Equipment", "Type", "Value", "Limit"};
	            violationsCvsWriter.writeRecord(violationsHeaders);
	            violationsCvsWriter.flush();
	        	Importer importer = Importers.addPostProcessors(Importers.getImporter("CIM1"), computationManager);
	            if (caseBaseName != null) {
	                Network network = importer.import_(new GenericReadOnlyDataSource(caseDir, caseBaseName), new Properties());
	                List<LimitViolation> networkViolations = Security.checkLimits(network);
	                writeCsvViolations(network.getId(), networkViolations, violationsCvsWriter);
	                Map<String, Boolean> tdSimulationResults = runTDSimulation(network,
	                        contingencyIds,
	                        emptyContingency,
	                        computationManager,
	                        simulatorFactory,
	                        ddbClientFactory,
	                        cadbClient,
	                        metricsContent);
	                securityIndexIds.addAll(tdSimulationResults.keySet());
	                writeCsvTDResults(network.getId(), securityIndexIds, tdSimulationResults, true, resultsCvsWriter);
	            } else {
	                Importers.importAll(caseDir, importer, false, network -> {
	                    try {
	                    	
	                        List<LimitViolation> networkViolations = Security.checkLimits(network);
	                        writeCsvViolations(network.getId(), networkViolations, violationsCvsWriter);
	                        Map<String, Boolean> tdSimulationResults = runTDSimulation(network,
	                                contingencyIds,
	                                emptyContingency,
	                                computationManager,
	                                simulatorFactory,
	                                ddbClientFactory,
	                                cadbClient,
	                                metricsContent);
	                        boolean writeHeaders = false;
	    	                if ( securityIndexIds.isEmpty() ) {
	    		                securityIndexIds.addAll(tdSimulationResults.keySet());
	    		                writeHeaders = true;
	    	                }
	                        writeCsvTDResults(network.getId(), securityIndexIds, tdSimulationResults, writeHeaders, resultsCvsWriter);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }, dataSource -> System.out.println("loading case " + dataSource.getBaseName()));
	            }
        	} catch (IOException ioxcp) {
        		ioxcp.printStackTrace();	
        	} finally {
        		violationsCvsWriter.close();
        		resultsCvsWriter.close();
        	}
        }
    }

    @Override
    public void runTDSimulations(OnlineWorkflowStartParameters startconfig, String caseDirS, String caseBaseName, String contingenciesIds, String emptyContingencyS, String outputFolderS) {
        LOGGER.info("Starting td simulations: "+startconfig.toString()+"\n");

        if(!workflowLock.tryLock())
        {
            throw new RuntimeException("a computation is already running.");
        }

        try {
            Path caseDir = Paths.get(caseDirS);
            boolean emptyContingency = Boolean.parseBoolean(emptyContingencyS);
            Path outputFolder = Paths.get(outputFolderS);
            runTDSimulations(caseDir, caseBaseName, contingenciesIds, emptyContingency, outputFolder);
            System.out.println("TD simulations terminated");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }  finally {
            workflowLock.unlock();
            workflow=null;
        }
    }

    // end td simulations


}
