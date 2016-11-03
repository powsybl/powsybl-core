/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.commons.Version;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationFilter;
import eu.itesla_project.security.LimitViolationType;
import eu.itesla_project.security.Security;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.TsoOverloadSecurityIndex;
import eu.itesla_project.simulation.securityindexes.TsoOvervoltageSecurityIndex;
import eu.itesla_project.simulation.securityindexes.TsoUndervoltageSecurityIndex;
import eu.itesla_project.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PostContLoadFlowSimImpactAnalysis implements ImpactAnalysis, PostContLoadFlowSimConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostContLoadFlowSimImpactAnalysis.class);

    private final Network network;

    private final ComputationManager computationManager;

    private final ContingenciesProvider contingenciesProvider;

    private final PostContLoadFlowSimConfig config;

    private final LoadFlowParameters loadFlowParameters;

    private final List<Contingency> allContingencies = new ArrayList<>();

    private final LoadFlow loadFlow;

    private final LimitViolationFilter baseVoltageFilter;

    PostContLoadFlowSimImpactAnalysis(Network network, ComputationManager computationManager, int priority,
                                      ContingenciesProvider contingenciesProvider, PostContLoadFlowSimConfig config,
                                      LoadFlowFactory loadFlowFactory) {
        Objects.requireNonNull(network, "network is null");
        Objects.requireNonNull(computationManager, "computation manager is null");
        Objects.requireNonNull(contingenciesProvider, "contingencies provider is null");
        Objects.requireNonNull(config, "config is null");
        this.network = network;
        this.computationManager = computationManager;
        this.contingenciesProvider = contingenciesProvider;
        this.config = config;
        loadFlow = loadFlowFactory.create(network, computationManager, priority);
        loadFlowParameters = new LoadFlowParameters().setVoltageInitMode(config.isWarnStartActivated()
                ? LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES
                : LoadFlowParameters.VoltageInitMode.DC_VALUES);
        baseVoltageFilter = new LimitViolationFilter(null, config.getMinBaseVoltageFilter());
    }

    @Override
    public String getName() {
        return PRODUCT_NAME;
    }

    @Override
    public String getVersion() {
        return ImmutableMap.builder().put("postContLoadFlowSimVersion", VERSION)
                                     .putAll(Version.VERSION.toMap())
                                     .build()
                                     .toString();
    }

    @Override
    public void init(SimulationParameters parameters, Map<String, Object> context) throws Exception {
        // read all contingencies
        allContingencies.addAll(contingenciesProvider.getContingencies(network));
    }

    private List<Contingency> getContingenciesToSimulate(Set<String> contingencyIds) {
        if (contingencyIds == null) {
            // take all contingencies
            return allContingencies;
        } else {
            // filter contingencies
            return allContingencies.stream()
                    .filter(c -> contingencyIds.contains(c.getId()))
                    .collect(Collectors.toList());
        }
    }

    private static void checkState(SimulationState state) {
        Objects.requireNonNull(state, "state is null");
        if (!(state instanceof PostContLoadFlowSimState)) {
            throw new RuntimeException("Incompatiblity between stabilization and impact analysis implementations");
        }
    }

    private static String getContingencyStateId(Contingency contingency, String baseStateId) {
        return baseStateId + "_" + contingency.getId();
    }

    private void createPostContingencyState(Contingency contingency, String baseStateId, String contingencyStateId) {
        network.getStateManager().cloneState(baseStateId, contingencyStateId);
        network.getStateManager().setWorkingState(contingencyStateId);
        contingency.toTask().modify(network);
    }

    private void removePostContingencyState(String contingencyStateId, Map<String, String> metrics) {
        network.getStateManager().setWorkingState(contingencyStateId);

        network.getStateManager().removeState(contingencyStateId);
    }

    private static Map<LimitViolationType, List<LimitViolation>>  groupViolationsByType(List<LimitViolation> violations) {
        Map<LimitViolationType, List<LimitViolation>> violationsByType =  violations.stream()
                .collect(Collectors.groupingBy(new Function<LimitViolation, LimitViolationType>() {
                    @Override
                    public LimitViolationType apply(LimitViolation violation) {
                        return violation.getLimitType();
                    }
                }));
        for (LimitViolationType violationType : LimitViolationType.values()) {
            if (!violationsByType.containsKey(violationType)) {
                violationsByType.put(violationType, Collections.emptyList());
            }
        }
        return violationsByType;
    }

    private void analyseLoadFlowResult(String baseStateId, int index, Contingency contingency, String contingencyStateId,
                                       LoadFlowResult loadFlowResult, Map<LimitViolationType, List<LimitViolation>> baseViolationsByType,
                                       Map<String, String> metrics, List<SecurityIndex> securityIndexes, AtomicInteger okCount) {
        // re-suffixe all metrics with contingency index
        metrics.putAll(loadFlowResult.getMetrics().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey() + "_" + index, Map.Entry::getValue)));

        if (loadFlowResult.isOk()) {
            okCount.incrementAndGet();

            network.getStateManager().setWorkingState(contingencyStateId);

            List<LimitViolation> violations = baseVoltageFilter.apply(Security.checkLimits(network, config.getCurrentLimitType(), 1f));
            String report = Security.printLimitsViolations(violations, CURRENT_FILTER);
            if (report != null) {
                LOGGER.info("Constraints after contingency {} for {}:\n{}", contingency.getId(), baseStateId, report);
            }

            Map<LimitViolationType, List<LimitViolation>> violationsByType = groupViolationsByType(violations);

            for (Map.Entry<LimitViolationType, List<LimitViolation>> entry : violationsByType.entrySet()) {
                LimitViolationType violationType = entry.getKey();
                List<LimitViolation> violations2 = entry.getValue();

                // filter equipment already violated in base state
                Set<String> equipmentsViolatedInBaseState;
                if (config.isBaseCaseConstraintsFiltered()) {
                    equipmentsViolatedInBaseState = baseViolationsByType.get(violationType)
                            .stream()
                            .map(violation -> violation.getSubject().getId())
                            .collect(Collectors.toSet());
                } else {
                    equipmentsViolatedInBaseState = Collections.emptySet();
                }

                List<String> equipments = violations2.stream()
                        .map(violation -> violation.getSubject().getId())
                        .filter(equipment -> !equipmentsViolatedInBaseState.contains(equipment))
                        .distinct()
                        .collect(Collectors.toList());

                switch (violationType) {
                    case CURRENT:
                        securityIndexes.add(new TsoOverloadSecurityIndex(contingency.getId(), equipments.size(), equipments, true));
                        break;
                    case LOW_VOLTAGE:
                        securityIndexes.add(new TsoUndervoltageSecurityIndex(contingency.getId(), equipments.size(), true));
                        break;
                    case HIGH_VOLTAGE:
                        securityIndexes.add(new TsoOvervoltageSecurityIndex(contingency.getId(), equipments.size(), true));
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        } else {
            securityIndexes.add(new TsoOverloadSecurityIndex(contingency.getId(), 0, Collections.emptyList(), false));
            securityIndexes.add(new TsoUndervoltageSecurityIndex(contingency.getId(), 0, false));
            securityIndexes.add(new TsoOvervoltageSecurityIndex(contingency.getId(), 0, false));
        }
    }

    @Override
    public ImpactAnalysisResult run(SimulationState state) throws Exception {
        return run(state, null);
    }

    private void checkMultiThreadAccess() {
        if (!network.getStateManager().isStateMultiThreadAccessAllowed()) {
            throw new IllegalStateException("Multi thread access has to be allowed in the network");
        }
    }

    private static void putSuccessPercentMetric(Map<String, String> metrics, AtomicInteger okCount, List<Contingency> contingencies) {
        float successPercent = 100f * okCount.get() / contingencies.size();
        metrics.put("successPercent", Float.toString(successPercent));
    }

    @Override
    public ImpactAnalysisResult run(SimulationState state, Set<String> contingencyIds) throws Exception {
        // FIXME dirty implementation, just to deal with loadflow implementation that do not support asynchronous computation
        checkState(state);
        checkMultiThreadAccess();

        String baseStateId = ((PostContLoadFlowSimState) state).getBaseStateId();
        Map<LimitViolationType, List<LimitViolation>> baseViolationsByType
                = groupViolationsByType(((PostContLoadFlowSimState) state).getBaseViolations());

        List<Contingency> contingencies = getContingenciesToSimulate(contingencyIds);

        List<SecurityIndex> securityIndexes = Collections.synchronizedList(new ArrayList<>());
        Map<String, String> metrics = Collections.synchronizedMap(new HashMap<>());

        AtomicInteger okCount = new AtomicInteger();

        ExecutorService executorService = Executors.newFixedThreadPool(contingencies.size());
        try {
            List<Future<?>> futures = new ArrayList<>(contingencies.size());

            for (int i = 0; i < contingencies.size(); i++) {
                Contingency contingency = contingencies.get(i);
                final int index = i;

                String contingencyStateId = getContingencyStateId(contingency, baseStateId);

                futures.add(executorService.submit((Runnable) () -> {
                    try {
                        createPostContingencyState(contingency, baseStateId, contingencyStateId);

                        LoadFlowResult loadFlowResult = loadFlow.run(loadFlowParameters);

                        analyseLoadFlowResult(baseStateId, index, contingency, contingencyStateId, loadFlowResult, baseViolationsByType,
                                              metrics, securityIndexes, okCount);

                        removePostContingencyState(contingencyStateId, metrics);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable.toString(), throwable);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);
        }

        putSuccessPercentMetric(metrics, okCount, contingencies);

        return new ImpactAnalysisResult(metrics, securityIndexes);
    }

    public CompletableFuture<ImpactAnalysisResult> runAsync(SimulationState state, Set<String> contingencyIds, ImpactAnalysisProgressListener listener) {
        checkState(state);
        checkMultiThreadAccess();

        String baseStateId = ((PostContLoadFlowSimState) state).getBaseStateId();
        Map<LimitViolationType, List<LimitViolation>> baseViolationsByType
                = groupViolationsByType(((PostContLoadFlowSimState) state).getBaseViolations());

        List<Contingency> contingencies = getContingenciesToSimulate(contingencyIds);

        List<CompletableFuture<Void>> results = new ArrayList<>(contingencies.size());

        List<SecurityIndex> securityIndexes = Collections.synchronizedList(new ArrayList<>());
        Map<String, String> metrics = Collections.synchronizedMap(new HashMap<>());

        AtomicInteger okCount = new AtomicInteger();

        for (int i = 0; i < contingencies.size(); i++) {
            Contingency contingency = contingencies.get(i);
            final int index = i;

            String contingencyStateId = getContingencyStateId(contingency, baseStateId);

            results.add(
                CompletableFuture.runAsync(() -> createPostContingencyState(contingency, baseStateId, contingencyStateId), computationManager.getExecutor())
                .thenComposeAsync(aVoid -> loadFlow.runAsync(contingencyStateId, loadFlowParameters),
                        computationManager.getExecutor())
                .thenAcceptAsync(loadFlowResult -> analyseLoadFlowResult(baseStateId, index, contingency, contingencyStateId, loadFlowResult, baseViolationsByType,
                                      metrics, securityIndexes, okCount), computationManager.getExecutor())
                .whenCompleteAsync((aVoid, throwable) -> {
                    if (throwable != null) {
                        LOGGER.error(throwable.toString(), throwable);
                    }
                    removePostContingencyState(contingencyStateId, metrics);
                }, computationManager.getExecutor())
            );
        }

        return CompletableFuture.allOf(results.toArray(new CompletableFuture[results.size()]))
                .thenApplyAsync(aVoid -> {
                    putSuccessPercentMetric(metrics, okCount, contingencies);

                    return new ImpactAnalysisResult(metrics, securityIndexes);
                });
    }

}
