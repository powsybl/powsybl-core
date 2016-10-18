/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import com.google.common.collect.ForwardingBlockingDeque;
import com.google.common.collect.Queues;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.modules.*;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.histo.HistoDbClientFactory;
import eu.itesla_project.modules.histo.HistoDbUtil;
import eu.itesla_project.modules.offline.*;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.RulesBuilder;
import eu.itesla_project.modules.sampling.*;
import eu.itesla_project.modules.topo.TopologyContext;
import eu.itesla_project.modules.topo.TopologyMiner;
import eu.itesla_project.modules.topo.TopologyMinerFactory;
import eu.itesla_project.modules.topo.UniqueTopologyBuilder;
import eu.itesla_project.modules.validation.ValidationDb;
import eu.itesla_project.security.Security;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static eu.itesla_project.modules.offline.OfflineTaskStatus.FAILED;
import static eu.itesla_project.modules.offline.OfflineTaskStatus.SUCCEED;
import static eu.itesla_project.modules.offline.OfflineTaskType.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineWorkflowImpl extends AbstractOfflineWorkflow {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineWorkflowImpl.class);

    protected static final int TIMEOUT = 100;

    protected  interface ImpactAnalysisController {

        boolean isStopRequested();

        SimulationState nextState();

    }

    protected  interface ImpactAnalysisResultCallback {

        void onStart(SimulationState state);

        void onResult(SimulationState state, ImpactAnalysisResult result);

    }

    static final int SAMPLING_PRIORITY = 0;
    static final int STARTING_POINT_INIT_PRIORITY = 1;
    static final int LOAD_FLOW_PRIORITY = 2;
    static final int STABILIZATION_PRIORITY = 3;
    static final int IMPACT_ANALYSIS_PRIORITY = 4;

    private final HistoDbClientFactory histoDbClientFactory;

    private final TopologyMinerFactory topologyMinerFactory;

    private final SamplerFactory samplerFactory;

    private final OptimizerFactory optimizerFactory;

    private final List<OfflineWorkflowListener> listeners = new CopyOnWriteArrayList<>();

    // synthesis

    private final List<OfflineWorkflowSynthesisListener> synthesisListeners = new CopyOnWriteArrayList<>();

    private final Lock sampleSynthesisLock = new ReentrantLock();

    private final Map<Integer, SampleSynthesis> samplesSynthesis = new LinkedHashMap<>();

    private final Lock securityIndexesSynthesisLock = new ReentrantLock();

    private final SecurityIndexSynthesis securityIndexSynthesis = new SecurityIndexSynthesis();

    private final SampleIdGenerator idGenerator;

    public OfflineWorkflowImpl(String id, OfflineWorkflowCreationParameters creationParameters, ComputationManager computationManager,
                               ContingenciesAndActionsDatabaseClientFactory cadbClientFactory,
                               HistoDbClientFactory histoDbClientFactory, TopologyMinerFactory topologyMinerFactory,
                               RulesBuilder rulesBuilder, OfflineDb offlineDb, ValidationDb validationDb, CaseRepository caseRepository,
                               SamplerFactory samplerFactory, LoadFlowFactory loadFlowFactory, OptimizerFactory optimizerFactory,
                               SimulatorFactory simulatorFactory, MergeOptimizerFactory mergeOptimizerFactory,
                               MetricsDb metricsDb, ExecutorService executorService) throws IOException {
        super(id, creationParameters, computationManager, cadbClientFactory, rulesBuilder, offlineDb, caseRepository,
                loadFlowFactory, simulatorFactory, mergeOptimizerFactory, metricsDb, validationDb, executorService);
        this.histoDbClientFactory = Objects.requireNonNull(histoDbClientFactory);
        this.topologyMinerFactory = Objects.requireNonNull(topologyMinerFactory);
        this.samplerFactory = Objects.requireNonNull(samplerFactory);
        this.optimizerFactory = Objects.requireNonNull(optimizerFactory);
        idGenerator = () -> OfflineWorkflowImpl.this.offlineDb.createSample(OfflineWorkflowImpl.this.id);
    }

    private SamplerResult runSampling(int n, final WorkflowContext context, WorkflowStartContext startContext) {
        LOGGER.debug("Workflow {}: sampling started", id);

        SamplerResult result = null;
        try {
            result = context.getSampler().sample(n, idGenerator);

            LOGGER.debug("Workflow {}: sampling terminated (ok={})", id, result.isOk());

            if (result.isOk()) {
                for (Sample sample : result.getSamples()) {
                    changeTaskStatus(startContext, sample.getId(), SAMPLING, SUCCEED, null);

                    LOGGER.debug("Workflow {}, characteristics of sample {}: {}", id, sample.getId(), sample.getCharacteritics());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

        return result;
    }

    private boolean runStartingPointInit(WorkflowContext context, WorkflowStartContext startContext, int sampleId) {

        LOGGER.debug("Workflow {}, sample {}: starting point initialization started", id, sampleId);

        // call starting point initialization module to fill control
        // variables and to setup the topology
        try {
            OptimizerResult result = context.getOptimizer().run();

            String finalStatus = result.getMetrics().get("final_status");
            LOGGER.debug("Workflow {}, sample {}: starting point initialization terminated (status={}, feasible={})",
                    id, sampleId, finalStatus, result.isFeasible());

            changeTaskStatus(startContext, sampleId, STARTING_POINT_INITIALIZATION,
                    result.isFeasible() ? SUCCEED : FAILED,
                    result.isFeasible() ? null : finalStatus);

            metricsDb.store(id, "sample-" + sampleId, STARTING_POINT_INITIALIZATION.name(), result.getMetrics());

            Networks.printBalanceSummary("sample " + sampleId, context.getNetwork(), LOGGER);

            // check that we have a good starting point
            return result.isFeasible();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    public void changeTaskStatus(WorkflowStartContext startContext, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String failureReason) {
        // store the task status in the db
        offlineDb.storeTaskStatus(id, sampleId, taskType, taskStatus, failureReason);

        updateSampleSynthesis(startContext, sampleId, taskType, taskStatus, failureReason);
    }

    public void storeSecurityIndexes(int sampleId, Collection<SecurityIndex> securityIndexes) {
        // store the security indexes in the offline db
        long startTime = System.currentTimeMillis();
        offlineDb.storeSecurityIndexes(id, sampleId, securityIndexes);

        LOGGER.debug("Workflow {}, sample {}: security indexes stored in {} ms",
                id, sampleId, (System.currentTimeMillis() - startTime));

        updateSecurityIndexesSynthesis(securityIndexes);
    }

    protected boolean runLoadFlow(WorkflowContext context, WorkflowStartContext startContext, Sample sample) {
        LOGGER.debug("Workflow {}, sample {}: load flow started", id, sample.getId());

        try {
            LoadFlowResult result = context.getLoadflow().run(context.getLoadFlowParameters());

            LOGGER.debug("Workflow {}, sample {}: load flow terminated (ok={})", id, sample.getId(), result.isOk());

            changeTaskStatus(startContext, sample.getId(), LOAD_FLOW, result.isOk() ? SUCCEED : FAILED, null);

            metricsDb.store(id, "sample-" + sample.getId(), LOAD_FLOW.name(), result.getMetrics());

            // consistency check on sampled variables
            SampleCharacteritics characteritics = SampleCharacteritics.fromNetwork(context.getNetwork(),
                    creationParameters.isGenerationSampled(),
                    creationParameters.isBoundariesSampled());
            if (!characteritics.equals(sample.getCharacteritics())) {
                LOGGER.warn("Sampled variables inconsistency for {}: {} != {}", sample.getId(),
                        characteritics, sample.getCharacteritics());
            }

            return result.isOk();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    protected SimulationState runStabilization(WorkflowContext context, WorkflowStartContext startContext, int sampleId) {
        // before dynamic simualtion, check there is no static constraints
        String report = Security.printLimitsViolations(context.getNetwork());
        if (report != null) {
            LOGGER.warn("Constraints for sample {}:\n{}", sampleId, report);
        }

        LOGGER.debug("Workflow {}, sample {}: stabilization started", id, sampleId);

        try {
            StabilizationResult result = context.getStabilization().run();

            LOGGER.debug("Workflow {}, sample {}: stabilization terminated (status={})", id, sampleId, result.getStatus());

            changeTaskStatus(startContext, sampleId, STABILIZATION, result.getStatus() == StabilizationStatus.COMPLETED ? SUCCEED : FAILED, null);

            metricsDb.store(id, "sample-" + sampleId, STABILIZATION.name(), result.getMetrics());

            return result.getStatus() == StabilizationStatus.COMPLETED ? result.getState() : null;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return null;
        }
    }

    protected void storeImpactAnalysisResults(WorkflowContext context, WorkflowStartContext startContext, int sampleId, ImpactAnalysisResult result) throws Exception {
        LOGGER.debug("Workflow {}, sample {}: impact analysis terminated ({}%)",
                id, sampleId, parseSuccessPercent(result.getMetrics()));

        changeTaskStatus(startContext, sampleId, IMPACT_ANALYSIS, SUCCEED, null);

        storeSecurityIndexes(sampleId, result.getSecurityIndexes());

        metricsDb.store(id, "sample-" + sampleId, IMPACT_ANALYSIS.name(), result.getMetrics());
    }

    public void runImpactAnalysis(WorkflowContext context, int availableCores, ImpactAnalysisController controller, final ImpactAnalysisResultCallback callback,
                                  ContingenciesAndActionsDatabaseClient cadbClient) throws Exception {
        final AtomicInteger busyCores = new AtomicInteger();

        final int[] startCounter = new int[1];
        final Lock startLock = new ReentrantLock();
        final Condition zero = startLock.newCondition();

        int contingencyCount = cadbClient.getContingencies(context.getNetwork()).size();

        while (!controller.isStopRequested()) {
            SimulationState state;
            if (busyCores.get() >= availableCores || (state = controller.nextState()) == null) {
                TimeUnit.MILLISECONDS.sleep(TIMEOUT);
                continue;
            }

            busyCores.addAndGet(contingencyCount);

            callback.onStart(state);

            context.getImpactAnalysis().runAsync(state, null, index -> busyCores.decrementAndGet())
                    .thenAcceptAsync(impactAnalysisResult -> {
                        try {
                            try {
                                callback.onResult(state, impactAnalysisResult);
                            } finally {
                                startLock.lock();
                                try {
                                    startCounter[0]--;
                                    if (startCounter[0] == 0) {
                                        zero.signal();
                                    }
                                } finally {
                                    startLock.unlock();
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }, executorService);

            startLock.lock();
            try {
                startCounter[0]++;
            } finally {
                startLock.unlock();
            }
        }

        // wait for startCount to reach zero
        startLock.lock();
        try {
            while (startCounter[0] != 0) {
                zero.await();
            }
        } finally {
            startLock.unlock();
        }
    }

    public static void clearSv(Network network) {
        // clear state variables
        for (Generator g : network.getGenerators()) {
            g.getTerminal().setP(Float.NaN).setQ(Float.NaN);
        }
        for (Load l : network.getLoads()) {
            l.getTerminal().setP(Float.NaN).setQ(Float.NaN);
        }
        for (ShuntCompensator sc : network.getShunts()) {
            sc.getTerminal().setP(Float.NaN).setQ(Float.NaN);
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            dl.getTerminal().setP(Float.NaN).setQ(Float.NaN);
        }
        for (Line l : network.getLines()) {
            l.getTerminal1().setP(Float.NaN).setQ(Float.NaN);
            l.getTerminal2().setP(Float.NaN).setQ(Float.NaN);
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            twt.getTerminal1().setP(Float.NaN).setQ(Float.NaN);
            twt.getTerminal2().setP(Float.NaN).setQ(Float.NaN);
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            twt.getLeg1().getTerminal().setP(Float.NaN).setQ(Float.NaN);
            twt.getLeg2().getTerminal().setP(Float.NaN).setQ(Float.NaN);
            twt.getLeg3().getTerminal().setP(Float.NaN).setQ(Float.NaN);
        }
        for (Bus b : network.getBusBreakerView().getBuses()) {
            b.setV(Float.NaN);
            b.setAngle(Float.NaN);
        }
    }

    public static void prepareBaseCase(Network network, OfflineWorkflowCreationParameters creationParameters,
                                       HistoDbClient histoDbClient) throws IOException, InterruptedException {
        clearSv(network);

        // connect loads and intermittent generation and boundary lines (so everything that can be sampled)
        for (Load l : network.getLoads()) {
            l.getTerminal().connect();
        }
        if (creationParameters.isGenerationSampled()) {
            for (Generator g : network.getGenerators()) {
                if (g.getEnergySource().isIntermittent()) {
                    g.getTerminal().connect();
                }
            }
        }
        if (creationParameters.isBoundariesSampled()) {
            for (DanglingLine dl : network.getDanglingLines()) {
                dl.getTerminal().connect();
            }
        }

        // TODO also override generator regulating status, phase shitfer regulating status and transformer regulating status?

        // resize voltage limits with historical data
        HistoDbUtil.fixVoltageLimits(network, histoDbClient, creationParameters.getHistoInterval());

        // temporary workaround for Elia data, missing pmin, pmax
        HistoDbUtil.fixGeneratorActiveLimits(network, histoDbClient, creationParameters.getHistoInterval());
    }

    public static TopologyContext prepareBaseCase(Network network, OfflineWorkflowCreationParameters creationParameters,
                                                   HistoDbClient histoDbClient, TopologyMiner topologyMiner, ComputationManager computationManager) throws IOException, InterruptedException {
        prepareBaseCase(network, creationParameters, histoDbClient);

        TopologyContext topologyContext = null;
        if (creationParameters.isInitTopo()) {
            topologyContext = TopologyContext.create(network,
                                                     topologyMiner, histoDbClient, computationManager,
                                                     creationParameters.getHistoInterval(),
                                                     creationParameters.getCorrelationThreshold(),
                                                     creationParameters.getProbabilityThreshold());

            new UniqueTopologyBuilder(topologyContext.getTopologyHistory()).build(network);
        }

        return topologyContext;
    }

    @Override
    protected void startImpl(final WorkflowStartContext startContext) throws Exception {
        Network network = loadAndMergeNetwork(creationParameters.getBaseCaseDate(), LOAD_FLOW_PRIORITY);

        // We want to work on multiple samples at the same time, so we are going
        // to use the multi-states feature of IIDM network model. Each of the
        // sample is mapped to a state created by cloning the initial state of
        // the network
        network.getStateManager().allowStateMultiThreadAccess(true);
        network.getStateManager().setWorkingState(StateManager.INITIAL_STATE_ID);

        Networks.printBalanceSummary("snapshot", network, LOGGER);

        try (HistoDbClient histoDbClient = histoDbClientFactory.create();
             TopologyMiner topologyMiner = topologyMinerFactory.create()) {

            ContingenciesAndActionsDatabaseClient cadbClient = cadbClientFactory.create();

            // prepare base case
            TopologyContext topologyContext = prepareBaseCase(network, creationParameters, histoDbClient, topologyMiner, computationManager);

            Networks.printBalanceSummary("base case", network, LOGGER);

            LOGGER.info("{} contingencies", cadbClient.getContingencies(network).size());

            //note: if ~/sampler2wp41.properties file does not exist, uses a mocksampler; otherwise uses a sampler provided by matlab-integration module
            //      samplers require MATLAB Compiler Runtime (ref readme.txt in matlab-integration)
            Sampler sampler = samplerFactory.create(network, computationManager, SAMPLING_PRIORITY, histoDbClient);

            LOGGER.info("Sampling module: {} {}", sampler.getName(), Objects.toString(sampler.getVersion(), ""));

            Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, STABILIZATION_PRIORITY);
            ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, IMPACT_ANALYSIS_PRIORITY, cadbClient);
            Optimizer optimizer = optimizerFactory.create(network, computationManager, STARTING_POINT_INIT_PRIORITY, histoDbClient, topologyMiner);
            LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, LOAD_FLOW_PRIORITY);

            LOGGER.info("Starting point init module: {} {}", optimizer.getName(), Objects.toString(optimizer.getVersion(), ""));
            LOGGER.info("Load flow module: {} {}", loadFlow.getName(), Objects.toString(loadFlow.getVersion(), ""));
            LOGGER.info("Stabilization module: {} {}", stabilization.getName(), Objects.toString(stabilization.getVersion(), ""));
            LOGGER.info("Impact analysis module: {} {}", impactAnalysis.getName(), Objects.toString(impactAnalysis.getVersion(), ""));

            int cores = computationManager.getResourcesStatus().getAvailableCores();
            final int stateQueueSize = startContext.getStartParameters().getStateQueueSize() != -1 ? startContext.getStartParameters().getStateQueueSize() : 2;

            LOGGER.trace("State queue initial size: {}", stateQueueSize);

            // module initializations
            sampler.init(new SamplerParameters(creationParameters.getHistoInterval(), creationParameters.isGenerationSampled(), creationParameters.isBoundariesSampled()));
            optimizer.init(new OptimizerParameters(creationParameters.getHistoInterval()), topologyContext);
            Map<String, Object> simulationInitContext = new HashMap<>();
            SimulationParameters simulationParameters = SimulationParameters.load();
            LOGGER.info(simulationParameters.toString());
            stabilization.init(simulationParameters, simulationInitContext);
            impactAnalysis.init(simulationParameters, simulationInitContext);

            changeWorkflowStatus(new OfflineWorkflowStatus(id, OfflineWorkflowStep.SAMPLING, creationParameters, startContext.getStartParameters()));

            LoadFlowParameters loadFlowParameters = createLoadFlowParameters();
            final WorkflowContext context = new WorkflowContext(network, sampler, optimizer, loadFlow, stabilization,
                    impactAnalysis, loadFlowParameters);

            final BlockingDeque<Sample> samples = new ForwardingBlockingDeque<Sample>() {

                private final BlockingDeque<Sample> delegate = new LinkedBlockingDeque<>(startContext.getStartParameters().getSampleQueueSize());

                @Override
                protected BlockingDeque<Sample> delegate() {
                    return delegate;
                }

                @Override
                public boolean offer(Sample o) {
                    boolean inserted = super.offer(o);
                    LOGGER.trace("Sample queue size ++: {}", delegate.size());
                    return inserted;
                }

                @Override
                public Sample poll(long timeout, TimeUnit unit) throws InterruptedException {
                    Sample sample = super.poll(timeout, unit);
                    if (sample != null) {
                        int size = delegate.size();
                        LOGGER.trace("Sample queue size --: {}", size);
                        if (size == 0) {
                            LOGGER.warn("Sample queue is empty");
                        }
                    }
                    return sample;
                }

            };

            List<Future<?>> sampleFutures = new ArrayList<>();
            final Semaphore samplePermits = new Semaphore(startContext.getStartParameters().getSampleQueueSize(), true);

            for (int i = 0; i < startContext.getStartParameters().getSamplingThreads(); i++) {
                sampleFutures.add(executorService.submit(() -> {
                    try {
                        // and then we continousloy re-sample
                        while (!isStopRequested(startContext)) {
                            // wait for a sample permit
                            while (!isStopRequested(startContext) && !samplePermits.tryAcquire(startContext.getStartParameters().getSamplesPerThread(), TIMEOUT, TimeUnit.MILLISECONDS)) {
                            }

                            if (!isStopRequested(startContext)) {
                                SamplerResult result = runSampling(startContext.getStartParameters().getSamplesPerThread(), context, startContext);
                                if (result != null && result.isOk()) {
                                    for (Sample sample : result.getSamples()) {
                                        samples.offer(sample);
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                }));
            }

            List<Future<?>> stateFutures = new ArrayList<>();
            final Semaphore statePermits = new Semaphore(stateQueueSize, true);
            final Queue<SimulationState> states = Queues.synchronizedQueue(new ArrayDeque<SimulationState>(stateQueueSize));
            final Map<SimulationState, Integer> sampleIds = Collections.synchronizedMap(new HashMap<>());

            for (int i = 0; i < stateQueueSize; i++) {
                stateFutures.add(executorService.submit(() -> {
                    try {
                        while (!isStopRequested(startContext)) {
                            // wait for a state permit
                            while (!isStopRequested(startContext) && !statePermits.tryAcquire(TIMEOUT, TimeUnit.MILLISECONDS)) {
                            }

                            // wait for a sample to be available
                            Sample sample = null;
                            while (!isStopRequested(startContext) && (sample = samples.poll(TIMEOUT, TimeUnit.MILLISECONDS)) == null) {
                            }

                            if (sample != null) {
                                samplePermits.release();

                                String stateId = "Sample-" + sample.getId();

                                // create a new network state
                                context.getNetwork().getStateManager().cloneState(StateManager.INITIAL_STATE_ID, stateId);
                                try {
                                    // set current thread working state
                                    context.getNetwork().getStateManager().setWorkingState(stateId);

                                    // apply the sample to the network
                                    sample.apply(context.getNetwork());

                                    SimulationState state = null;
                                    if (!isStopRequested(startContext) && runStartingPointInit(context, startContext, sample.getId())
                                            && !isStopRequested(startContext) && runLoadFlow(context, startContext, sample)
                                            && !isStopRequested(startContext) && (state = runStabilization(context, startContext, sample.getId())) != null) {
                                        sampleIds.put(state, sample.getId());
                                        states.add(state);
                                        LOGGER.trace("State queue size ++: {}", states.size());
                                    } else {
                                        statePermits.release();
                                    }

                                    // in any case store the sample in the and simulation db and validation
                                    // db for later deep analysis
                                    storeState(sample.getId(), context.getNetwork());

                                    startContext.incrementProcessedSamples();

                                    try {
                                        validationDb.save(context.getNetwork(), OfflineWorkflow.getValidationDir(id), "sample-" + sample.getId());
                                    } catch (Exception e) {
                                        LOGGER.error(e.toString(), e);
                                    }
                                } finally {
                                    context.getNetwork().getStateManager().removeState(stateId);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                }));
            }

            ImpactAnalysisController controller = new ImpactAnalysisController() {

                @Override
                public boolean isStopRequested() {
                    return OfflineWorkflowImpl.this.isStopRequested(startContext);
                }

                @Override
                public SimulationState nextState() {
                    SimulationState state = states.poll();
                    if (state != null) {
                        statePermits.release();
                        int size = states.size();
                        LOGGER.trace("State queue size --: {}", size);
                        if (size == 0) {
                            LOGGER.warn("State queue is empty");
                        }
                    }
                    return state;
                }

            };

            ImpactAnalysisResultCallback callback = new ImpactAnalysisResultCallback() {

                @Override
                public void onStart(SimulationState state) {
                    int sampleId = sampleIds.get(state);
                    LOGGER.debug("Workflow {}, sample {}: impact analysis started", id, sampleId);
                }

                @Override
                public void onResult(SimulationState state, ImpactAnalysisResult result) {
                    try {
                        int sampleId = sampleIds.remove(state);
                        storeImpactAnalysisResults(context, startContext, sampleId, result);
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            };

            try {
                runImpactAnalysis(context, cores, controller, callback, cadbClient);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
                stopRequested.set(true);
            }

            for (Future<?> f : stateFutures) {
                f.get();
            }

            samplePermits.release(samples.size());

            for (Future<?> f : sampleFutures) {
                f.get();
            }

            // some offline db implementation may require to explicitly flush data on disk
            offlineDb.flush(id);

            // clean samples synthesis
            sampleSynthesisLock.lock();
            try {
                samplesSynthesis.clear();
            } finally {
                sampleSynthesisLock.unlock();
            }
            notifySampleSynthesisChange();
        }
    }

    @Override
    protected void notifyStatusChange(OfflineWorkflowStatus status) {
        for (OfflineWorkflowListener l : listeners) {
            try {
                l.onWorkflowStatusChange(status);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    @Override
    public void addListener(OfflineWorkflowListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(OfflineWorkflowListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void addSynthesisListener(OfflineWorkflowSynthesisListener listener) {
        synthesisListeners.add(listener);
    }

    @Override
    public void removeSynthesisListener(OfflineWorkflowSynthesisListener listener) {
        synthesisListeners.remove(listener);
    }

    public void storeState(int sampleId, Network network) {
         // store the state in the offline db
        long startTime = System.currentTimeMillis();
        offlineDb.storeState(id, sampleId, network, creationParameters.getAttributesCountryFilter());

        LOGGER.debug("Workflow {}, sample {}: state stored in {} ms",
                id, sampleId, (System.currentTimeMillis() - startTime));
/*
        for (OfflineWorkflowListener listener : listeners) {
            try {
                listener.onStateStorage(sampleId, network);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        */
    }

    protected void updateSecurityIndexesSynthesis(Collection<SecurityIndex> securityIndexes) {
        // notify listeners
        /*
        for (OfflineWorkflowListener listener : listeners) {
            try {
                listener.onSecurityIndexesStorage(sampleId, securityIndexes);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
                */

        // update synthesis
        securityIndexesSynthesisLock.lock();
        try {
            for (SecurityIndex si : securityIndexes) {
                securityIndexSynthesis.addSecurityIndex(si);
            }

            //notifySecurityIndexesSynthesisChange();
        } finally {
            securityIndexesSynthesisLock.unlock();
        }
    }

    private void notifySampleSynthesisChange() {
        sampleSynthesisLock.lock();
        try {
            // notify synthesis listeners
            for (OfflineWorkflowSynthesisListener l : synthesisListeners) {
                try {
                    l.onSamplesChange(samplesSynthesis.values());
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        } finally {
            sampleSynthesisLock.unlock();
        }
    }

    protected void updateSampleSynthesis(WorkflowStartContext startContext, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String failureReason) {
        // notify listeners
        OfflineTaskEvent event = new OfflineTaskEvent(taskType, taskStatus, failureReason);
        /*
        for (OfflineWorkflowListener listener : listeners) {
            try {
                listener.onSampleUpdate(sampleId, event);
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
                */

        // update synthesis
        sampleSynthesisLock.lock();
        try {
            SampleSynthesis sample = samplesSynthesis.get(sampleId);
            if (sample == null) {
                samplesSynthesis.put(sampleId, new SampleSynthesis(sampleId, event));
            } else {
                sample.setLastTaskEvent(event);
            }

            // remove useless samples, just keep 3 complete samples for visualization purpose
            long historySize = Math.round((double) startContext.getStartParameters().getStateQueueSize() / 2 + 0.5d);
            int i = 0;
            for (Iterator<Map.Entry<Integer, SampleSynthesis>> it = samplesSynthesis.entrySet().iterator(); it.hasNext();) {
                Map.Entry<Integer, SampleSynthesis> entry = it.next();
                SampleSynthesis sample2 = entry.getValue();
                if ((sample2.getLastTaskEvent().getTaskType() == OfflineTaskType.IMPACT_ANALYSIS
                        && sample2.getLastTaskEvent().getTaskStatus() == OfflineTaskStatus.SUCCEED)
                        || sample2.getLastTaskEvent().getTaskStatus() == OfflineTaskStatus.FAILED) {
                    if (i > historySize) {
                        it.remove();
                    } else {
                        i++;
                    }
                }
            }

            notifySampleSynthesisChange();
        } finally {
            sampleSynthesisLock.unlock();
        }
    }

    @Override
    protected void notifyRuleStorage(String workflowId, RuleId ruleId, boolean ok, float percentComplete) {
        LOGGER.info("Rule {} for workflow {}: {}", ruleId, workflowId, ok ? "success" : "failure");
        for(OfflineWorkflowListener listener : listeners) {
            listener.onSecurityRuleStorage(workflowId, ruleId, ok, percentComplete);
        }
    }

    @Override
    public void notifySynthesisListeners() {
        notifyStatusChange(status.get());
        notifySampleSynthesisChange();
        //notifySecurityIndexesSynthesisChange();
    }

}
