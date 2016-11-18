/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.merge.MergeUtil;
import eu.itesla_project.modules.constraints.ConstraintsModifier;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.mcla.ForecastErrorsDataStorage;
import eu.itesla_project.modules.mcla.MontecarloSampler;
import eu.itesla_project.modules.mcla.MontecarloSamplerFactory;
import eu.itesla_project.modules.mcla.MontecarloSamplerParameters;
import eu.itesla_project.modules.online.*;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizer;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerParameters;
import eu.itesla_project.modules.rules.RulesDbClient;
import eu.itesla_project.modules.wca.*;
import eu.itesla_project.online.ContingencyStatesIndexesSynthesis.SecurityIndexInfo;
import eu.itesla_project.simulation.ImpactAnalysis;
import eu.itesla_project.simulation.SimulationParameters;
import eu.itesla_project.simulation.SimulatorFactory;
import eu.itesla_project.simulation.Stabilization;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowImpl implements OnlineWorkflow {

    Logger logger = LoggerFactory.getLogger(OnlineWorkflowImpl.class);

    private final ComputationManager computationManager;
    private final ContingenciesAndActionsDatabaseClient cadbClient;
    private final HistoDbClient histoDbClient;
    private final RulesDbClient rulesDbClient;
    private final ForecastErrorsDataStorage feDataStorage;
    private final OnlineWorkflowParameters parameters;
    private List<OnlineApplicationListener> listeners = new ArrayList<OnlineApplicationListener>();
    private final CaseRepository caseRepository;
    private final WCAFactory wcaFactory;
    private final LoadFlowFactory loadFlowFactory;
    private final OnlineDb onlineDb;
    private final UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory;
    private final CorrectiveControlOptimizerFactory optimizerFactory;
    private final SimulatorFactory simulatorFactory;
    private final MontecarloSamplerFactory montecarloSamplerFactory;
    private final MergeOptimizerFactory mergeOptimizerFactory;
    private final RulesFacadeFactory rulesFacadeFactory;
    private final OnlineWorkflowStartParameters startParameters;

    private String id;

    public OnlineWorkflowImpl(
            ComputationManager computationManager,
            ContingenciesAndActionsDatabaseClient cadbClient,
            HistoDbClient histoDbClient,
            RulesDbClient rulesDbClient,
            WCAFactory wcaFactory,
            LoadFlowFactory loadFlowFactory,
            ForecastErrorsDataStorage feDataStorage,
            OnlineDb onlineDB,
            UncertaintiesAnalyserFactory uncertaintiesAnalyserFactory,
            CorrectiveControlOptimizerFactory optimizerFactory,
            SimulatorFactory simulatorFactory,
            CaseRepository caseRepository,
            MontecarloSamplerFactory montecarloSamplerFactory,
            MergeOptimizerFactory mergeOptimizerFactory,
            RulesFacadeFactory rulesFacadeFactory,
            OnlineWorkflowParameters parameters,
            OnlineWorkflowStartParameters startParameters
    ) {
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(cadbClient, "contingencies and actions DB client is null");
        Objects.requireNonNull(histoDbClient, "histo DB client is null");
        Objects.requireNonNull(rulesDbClient, "rules DB client is null");
        Objects.requireNonNull(wcaFactory, "WCA factory is null");
        Objects.requireNonNull(loadFlowFactory, "loadFlow factory is null");
        Objects.requireNonNull(feDataStorage, "forecast errors data storage is null");
        Objects.requireNonNull(onlineDB, "online db is null");
        Objects.requireNonNull(optimizerFactory, "corrective control optimizer factory is null");
        Objects.requireNonNull(simulatorFactory, "simulator factory is null");
        Objects.requireNonNull(caseRepository, "case repository is null");
        Objects.requireNonNull(montecarloSamplerFactory, "montecarlo sampler factory is null");
        Objects.requireNonNull(parameters, "parameters is null");
        Objects.requireNonNull(startParameters, "start parameters is null");
        this.computationManager = computationManager;
        this.cadbClient = cadbClient;
        this.histoDbClient = histoDbClient;
        this.rulesDbClient = rulesDbClient;
        this.wcaFactory = wcaFactory;
        this.loadFlowFactory = loadFlowFactory;
        this.feDataStorage = feDataStorage;
        this.onlineDb = onlineDB;
        this.uncertaintiesAnalyserFactory = Objects.requireNonNull(uncertaintiesAnalyserFactory);
        this.optimizerFactory = optimizerFactory;
        this.simulatorFactory = simulatorFactory;
        this.caseRepository = caseRepository;
        this.montecarloSamplerFactory = montecarloSamplerFactory;
        this.mergeOptimizerFactory = Objects.requireNonNull(mergeOptimizerFactory);
        this.rulesFacadeFactory = rulesFacadeFactory;
        this.parameters = parameters;
        this.startParameters = startParameters;
        if (parameters.getCaseFile() != null) {
            //TODO avoid loading network twice, here and in the start method
            // load network, to get its ID and override existing parameters (base-case, countries, case-type)
            Network network = Importers.loadNetwork(parameters.getCaseFile());
            if (network == null) {
                throw new RuntimeException("Case '" + parameters.getCaseFile() + "' not found");
            }
            this.parameters.setBaseCaseDate(network.getCaseDate());
            this.parameters.setCountries(network.getCountries());
            this.parameters.setCaseType((network.getForecastDistance() == 0) ? CaseType.SN : CaseType.FO);
        }
        this.id = DateTimeFormat.forPattern("yyyyMMdd_HHmm_").print(this.parameters.getBaseCaseDate()) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        logger.info(this.parameters.toString());
    }


    /* (non-Javadoc)
     * @see eu.itesla_project.online.OnlineWorkflowInterface#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see eu.itesla_project.online.OnlineWorkflowInterface#start(eu.itesla_project.online.OnlineWorkflowContext)
     */
    @Override
    public void start(OnlineWorkflowContext oCtx) throws Exception {
        logger.info("{} Online workflow processing, started.", id);
        for (OnlineApplicationListener l : listeners)
            l.onWorkflowUpdate(new StatusSynthesis(id, StatusSynthesis.STATUS_RUNNING));

        Network network = null;
        if (parameters.getCaseFile() != null) {
            network = Importers.loadNetwork(parameters.getCaseFile());
            if (network == null) {
                throw new RuntimeException("Case '" + parameters.getCaseFile() + "' not found");
            }
        } else {
            network = MergeUtil.merge(caseRepository, parameters.getBaseCaseDate(), parameters.getCaseType(), parameters.getCountries(),
                    loadFlowFactory, 0, mergeOptimizerFactory, computationManager, parameters.isMergeOptimized());
        }

        logger.info("- Network id: " + network.getId());
        logger.info("- Network name: " + network.getName());

        // needed in order to correctly handle multithreading access to network
        network.getStateManager().allowStateMultiThreadAccess(true);


        oCtx.setWorkflowId(id);
        oCtx.setNetwork(network);
        oCtx.setOfflineWorkflowId(parameters.getOfflineWorkflowId());
        oCtx.setTimeHorizon(parameters.getTimeHorizon());

        // prepare the objects where the results of the forecast analysis will be saved
        oCtx.setResults(new ForecastAnalysisResults(this.getId(), oCtx.getTimeHorizon()));
        oCtx.setSecurityRulesResults(new SecurityRulesApplicationResults(this.getId(), oCtx.getTimeHorizon()));
        oCtx.setWcaResults(new WCAResults(this.getId(), oCtx.getTimeHorizon()));
        if (parameters.validation())
            oCtx.setWcaSecurityRulesResults(new SecurityRulesApplicationResults(this.getId(), oCtx.getTimeHorizon()));

        logger.info(" - WCA processing......");
        for (OnlineApplicationListener l : listeners)
            l.onWcaUpdate(new RunningSynthesis(id, true));

        // maybe we should put also the stopWcaOnViolations wca parameter as online workflow parameter
        WCAParameters wcaParameters = new WCAParameters(parameters.getHistoInterval(), parameters.getOfflineWorkflowId(), parameters.getSecurityIndexes(), parameters.getRulesPurityThreshold(), true);
        WCA wca = wcaFactory.create(oCtx.getNetwork(), computationManager, histoDbClient, rulesDbClient, uncertaintiesAnalyserFactory, cadbClient, loadFlowFactory);
        WCAResult result = wca.run(wcaParameters);

        for (OnlineApplicationListener l : listeners)
            l.onWcaUpdate(new RunningSynthesis(id, false));

        // ArrayList<String> stables = new ArrayList<String>();

        for (WCACluster cluster : result.getClusters()) {
            logger.info("WCA: contingency {} in cluster {}", cluster.getContingency().getId(), cluster.getNum().toString());
            oCtx.getWcaResults().addContingencyWithCluster(cluster.getContingency().getId(), cluster);
            if (parameters.validation()) { // if validation
                // do not filter out the contingencies
                oCtx.getContingenciesToAnalyze().add(cluster.getContingency());
            } else {
                if (cluster.getNum() != WCAClusterNum.ONE) { // cluster 1 -> contingency classified as "stable" -> no need for further analysis
                    // contingencies in clusters 2, 3 and 4 need further analysis
                    oCtx.getContingenciesToAnalyze().add(cluster.getContingency());

                }
            }
        }

        // notify all contingency stable and unstable
        for (OnlineApplicationListener l : listeners)
            l.onWcaContingencies(new WcaContingenciesSynthesis(id, oCtx.getWcaResults().getContingenciesWithClusters()));


        logger.info("{} Online workflow - Analysis of states, started.", id);

        // create modules used in the states analysis
        MontecarloSampler sampler = montecarloSamplerFactory.create(oCtx.getNetwork(), computationManager, feDataStorage);
        OnlineRulesFacade rulesFacade = rulesFacadeFactory.create(rulesDbClient);
        CorrectiveControlOptimizer optimizer = optimizerFactory.create(cadbClient, computationManager);
        Stabilization stabilization = simulatorFactory.createStabilization(oCtx.getNetwork(), computationManager, Integer.MAX_VALUE);
        ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(oCtx.getNetwork(), computationManager, Integer.MAX_VALUE, cadbClient);
        LoadFlow loadflow = loadFlowFactory.create(oCtx.getNetwork(), computationManager, 0);
        ConstraintsModifier constraintsModifier = new ConstraintsModifier(oCtx.getNetwork());
        StateAnalizerListener stateListener = new StateAnalizerListener();

        // initialize modules
        rulesFacade.init(new RulesFacadeParameters(oCtx.getOfflineWorkflowId(),
                oCtx.getContingenciesToAnalyze(),
                parameters.getRulesPurityThreshold(),
                parameters.getSecurityIndexes(),
                parameters.validation(),
                parameters.isHandleViolationsInN()));
        Map<String, Object> simulationInitContext = new HashMap<>();
        SimulationParameters simulationParameters = SimulationParameters.load();
        stabilization.init(simulationParameters, simulationInitContext);
        impactAnalysis.init(simulationParameters, simulationInitContext);
        optimizer.init(new CorrectiveControlOptimizerParameters());
        if (parameters.isHandleViolationsInN() && parameters.analyseBasecase()) { // I need to analyze basecase before initializing the sampler
            new StateAnalyzer(oCtx, sampler, loadflow, rulesFacade, optimizer, stabilization, impactAnalysis, onlineDb, stateListener,
                    constraintsModifier, parameters).call();
        }
        sampler.init(new MontecarloSamplerParameters(oCtx.getTimeHorizon(), parameters.getFeAnalysisId(), parameters.getStates()));

        // run states analysis
        int statesNumber = parameters.getStates();
        if (parameters.isHandleViolationsInN() && parameters.analyseBasecase()) // I already analyzed basecase
            statesNumber--;
        List<Callable<Void>> tasks = new ArrayList<>(statesNumber);
        for (int i = 0; i < statesNumber; i++) {
            tasks.add(new StateAnalyzer(oCtx, sampler, loadflow, rulesFacade, optimizer, stabilization, impactAnalysis, onlineDb, stateListener,
                    constraintsModifier, parameters));
        }
        ExecutorService taskExecutor = Executors.newFixedThreadPool(startParameters.getThreads());
        taskExecutor.invokeAll(tasks);
        taskExecutor.shutdown();
        logger.info("{} Online workflow - Analysis of states, terminated.", id);

        logger.info("{} Online workflow processing, terminated.", id);

        logger.info("WCA Results:\n" + oCtx.getWcaResults().toString());
        logger.info("Security Rules Application Results:\n" + oCtx.getSecurityRulesResults().toString());
        logger.info("Results:\n" + oCtx.getResults().toString());

        for (OnlineApplicationListener l : listeners)
            l.onWorkflowUpdate(new StatusSynthesis(id, StatusSynthesis.STATUS_TERMINATED));

        // store workflow parameters
        onlineDb.storeWorkflowParameters(id, parameters);
        // store status of the processing steps for the different states
        onlineDb.storeStatesProcessingStatus(id, stateListener.statusMap);
        // store workflow results
        onlineDb.storeResults(id, oCtx.getResults());
        // store workflow rules results
        onlineDb.storeRulesResults(id, oCtx.getSecurityRulesResults());
        // store workflow wca results
        onlineDb.storeWcaResults(id, oCtx.getWcaResults());
        if (parameters.validation())
            // store workflow wca rules results
            onlineDb.storeWcaRulesResults(id, oCtx.getWcaSecurityRulesResults());
        onlineDb.close();

        //send itesla notification to apogee only if config.xml contains configuration parameters
        for (OnlineApplicationListener l : listeners)
            l.onWorkflowEnd(oCtx, onlineDb, cadbClient, parameters);

    }

    /* (non-Javadoc)
     * @see eu.itesla_project.online.OnlineWorkflowInterface#addOnlineApplicationListener(eu.itesla_project.online.OnlineApplicationListener)
     */
    @Override
    public void addOnlineApplicationListener(OnlineApplicationListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see eu.itesla_project.online.OnlineWorkflowInterface#removeOnlineApplicationListener(eu.itesla_project.online.OnlineApplicationListener)
     */
    @Override
    public void removeOnlineApplicationListener(OnlineApplicationListener listener) {
        listeners.remove(listener);

    }


    class StateAnalizerListener {
        HashMap<Integer, WorkStatus> statusMap = new HashMap<Integer, WorkStatus>();
        WorkSynthesis work = new WorkSynthesis(id, statusMap);
        ContingencyStatesActionsSynthesis acts = new ContingencyStatesActionsSynthesis(id);
        ContingencyStatesIndexesSynthesis stindex = new ContingencyStatesIndexesSynthesis(id);
        IndexSecurityRulesResultsSynthesis stateWithSecRulesResults = new IndexSecurityRulesResultsSynthesis(id);

        public void onUpdate(Integer stateId, EnumMap<OnlineTaskType, OnlineTaskStatus> status, TimeHorizon t) {
            //statusMap.put(stateId, new WorkStatus(stateId, status, t.toString()));

            if (statusMap.containsKey(stateId)) {
                WorkStatus ws = statusMap.get(stateId);
                ws.setStatus(status);
                ws.setTimeHorizon(t.toString());
                statusMap.put(stateId, ws);
            } else
                statusMap.put(stateId, new WorkStatus(stateId, status, t.toString()));

            for (OnlineApplicationListener l : listeners)
                l.onWorkflowStateUpdate(work);

        }

        public void onSecurityRulesApplicationResults(String contingencyId, Integer stateId, OnlineWorkflowContext oCtx) {

            SecurityRulesApplicationResults rulesApplicationResults = oCtx.getSecurityRulesResults();
            stateWithSecRulesResults.addStateSecurityRuleIndexes(contingencyId, stateId, rulesApplicationResults);
            for (OnlineApplicationListener l : listeners)
                l.onStatesWithSecurityRulesResultsUpdate(stateWithSecRulesResults);
        }


        public void onUpdate(Integer stateId, EnumMap<OnlineTaskType, OnlineTaskStatus> status, TimeHorizon t, String detail) {
            //  statusMap.put(stateId, new WorkStatus(stateId, status, t.toString(),detail));
            if (statusMap.containsKey(stateId)) {
                WorkStatus ws = statusMap.get(stateId);
                StringBuffer sb = new StringBuffer();

                if (ws.getDetail() != null && !ws.getDetail().equals(""))
                    sb.append(ws.getDetail()).append("<br>").append(detail);
                else
                    sb.append(detail);

                ws.setDetail(sb.toString());
                ws.setStatus(status);
                ws.setTimeHorizon(t.toString());
                statusMap.put(stateId, ws);
            } else
                statusMap.put(stateId, new WorkStatus(stateId, status, t.toString(), detail));

            for (OnlineApplicationListener l : listeners)
                l.onWorkflowStateUpdate(work);

        }


        public void onImpactAnalysisResults(Integer stateId, OnlineWorkflowContext oCtx) {

            ForecastAnalysisResults res = oCtx.getResults();
            Collection<String> unsafes = res.getUnsafeContingencies();
            for (String c : unsafes) {

                List<Integer> sts = res.getUnstableStates(c);
                for (Integer s : sts) {
                    List<SecurityIndex> sec = res.getIndexes(c, s);
                    ArrayList<SecurityIndexInfo> indexes = new ArrayList<SecurityIndexInfo>();
                    for (SecurityIndex idx : sec) {
                        indexes.add(stindex.new SecurityIndexInfo(idx));
                    }
                    stindex.addStateIndexes(c, s, indexes);
                }

            }

            for (OnlineApplicationListener l : listeners)
                l.onStatesWithIndexesUpdate(stindex);


        }

        public void onOptimizerResults(Integer stateId, OnlineWorkflowContext oCtx) {

            ForecastAnalysisResults res = oCtx.getResults();
            Collection<String> conts = res.getContingenciesWithActions();


            for (String c : conts) {
                Map<Integer, Boolean> unsafeStatesWithActions = res.getUnsafeStatesWithActions(c);
                if (unsafeStatesWithActions != null) {
                    Set<Integer> sts = unsafeStatesWithActions.keySet();
                    for (Integer s : sts) {
                        List<String> actiondIds = res.getActionsIds(c, s);
                        if (actiondIds != null) {
                            ArrayList<ActionInfo> infos = new ArrayList<ActionInfo>();
                            for (String a : actiondIds)
                                infos.add(new ActionInfo(a));
                            acts.addStateActions(c, s, infos);
                        }
                    }
                }
            }
            for (OnlineApplicationListener l : listeners)
                l.onStatesWithActionsUpdate(acts);


        }
    }

}
