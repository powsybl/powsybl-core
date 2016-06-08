/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.io.CacheManager;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.commons.io.XmlPlatformConfig;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.ComputationResourcesStatus;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.modules.*;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.contingencies.LineContingency;
import eu.itesla_project.modules.contingencies.impl.ContingencyImpl;
import eu.itesla_project.modules.ddb.DynamicDatabaseCacheClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.offline.MetricsDb;
import eu.itesla_project.modules.offline.OfflineDb;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.modules.rules.RulesBuilder;
import eu.itesla_project.modules.sampling.*;
import eu.itesla_project.modules.simulation.*;
import eu.itesla_project.modules.topo.TopologyMiner;
import eu.itesla_project.modules.topo.TopologyMinerFactory;
import eu.itesla_project.modules.validation.ValidationDb;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class OfflineWorkflowTest {

    @BeforeClass
    public static void init() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
    }

    @Test
    public void test() throws Exception {

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            Path cfgDir = fileSystem.getPath("/config");
            Path cacheDir = fileSystem.getPath("/cache");

            try (OutputStream os = Files.newOutputStream(cfgDir.resolve("config.xml"))) {
                ByteStreams.copy(OfflineWorkflowTest.class.getResourceAsStream("/config.xml"), os);
            }

            PlatformConfig.setDefaultConfig(new XmlPlatformConfig(cfgDir, "config", fileSystem));
            PlatformConfig.setDefaultCacheManager(new CacheManager(cacheDir));

            // workflow parameters: topo init is not activated
            DateTime baseCaseDate = DateTime.parse("2013-01-15T18:45:00+01:00");
            Interval histoInterval = Interval.parse("2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00");

            OfflineWorkflowCreationParameters creationParameters
                    = new OfflineWorkflowCreationParameters(EnumSet.of(Country.FR),
                    baseCaseDate,
                    histoInterval,
                    true,
                    true,
                    false,
                    0,
                    0,
                    false,
                    false,
                    false,
                    null,
                    0);
            OfflineWorkflowStartParameters startParameters = new OfflineWorkflowStartParameters(10, 1, 5, 5, -1, 100);

            // create init network
            Network network = EurostagTutorialExample1Factory.create();

            // computation manager mock
            ComputationManager computationManager = Mockito.mock(ComputationManager.class);
            Mockito.when(computationManager.getVersion())
                    .thenReturn("mock");
            ComputationResourcesStatus status = new ComputationResourcesStatus() {
                @Override
                public DateTime getDate() {
                    return DateTime.now();
                }

                @Override
                public int getAvailableCores() {
                    return 1;
                }

                @Override
                public int getBusyCores() {
                    return 0;
                }

                @Override
                public Map<String, Integer> getBusyCoresPerApp() {
                    return Collections.emptyMap();
                }
            };
            Mockito.when(computationManager.getResourcesStatus())
                    .thenReturn(status);

            // dynamic database mock (should not be called)
            DynamicDatabaseCacheClient ddbClient = Mockito.mock(DynamicDatabaseCacheClient.class, new ThrowsException(new UnsupportedOperationException()));
            DynamicDatabaseClientFactory ddbClientFactory = Mockito.mock(DynamicDatabaseClientFactory.class);
            Mockito.when(ddbClientFactory.create(true)).thenReturn(ddbClient);

            // contingencies db mock
            ContingenciesAndActionsDatabaseClient cadbClient = Mockito.mock(ContingenciesAndActionsDatabaseClient.class);
            ContingencyImpl contingency = new ContingencyImpl("line", new LineContingency("line"));
            List<Contingency> contingencies = Arrays.asList(contingency);
            Mockito.when(cadbClient.getContingencies(network))
                    .thenReturn(contingencies);
            ContingenciesAndActionsDatabaseClientFactory cadbClientFactory = Mockito.mock(ContingenciesAndActionsDatabaseClientFactory.class);
            Mockito.when(cadbClientFactory.create()).thenReturn(cadbClient);

            // histodb client mock (only called to prepare base case)
            HistoDbClient histoDbClient = Mockito.mock(HistoDbClient.class);
            HistoDbStats histoDbStats = new HistoDbStats();
            for (VoltageLevel vl : network.getVoltageLevels()) {
                histoDbStats.setValue(HistoDbStatsType.MIN, HistoDbUtil.createVoltageAttributeId(vl), Float.NaN);
                histoDbStats.setValue(HistoDbStatsType.MAX, HistoDbUtil.createVoltageAttributeId(vl), Float.NaN);
            }
            Mockito.when(histoDbClient.queryStats(Matchers.anySet(), Matchers.eq(histoInterval), Matchers.eq(HistoDbHorizon.SN), Matchers.eq(true)))
                    .thenReturn(histoDbStats);
            HistoDbClientFactory histoDbClientFactory = Mockito.mock(HistoDbClientFactory.class);
            Mockito.when(histoDbClientFactory.create()).thenReturn(histoDbClient);

            // topo miner mock (should not be called)
            TopologyMiner topologyMiner = Mockito.mock(TopologyMiner.class);
            TopologyMinerFactory topologyMinerFactory = Mockito.mock(TopologyMinerFactory.class);
            Mockito.when(topologyMinerFactory.create()).thenReturn(topologyMiner);

            // rules builder mock (should not be called)
            RulesBuilder rulesBuilder = Mockito.mock(RulesBuilder.class, new ThrowsException(new UnsupportedOperationException()));

            // simulation db mock
            OfflineDb offlineDb = Mockito.mock(OfflineDb.class);
            Mockito.when(offlineDb.createSample(Matchers.anyString())).thenAnswer(new Answer<Integer>() {
                private int sampleId = 0;

                @Override
                public Integer answer(InvocationOnMock invocation) throws Throwable {
                    return sampleId++;
                }
            });

            // validation db mock
            ValidationDb validationDb = Mockito.mock(ValidationDb.class);

            // case repository mock (should not be called)
            CaseRepository caseRepository = Mockito.mock(CaseRepository.class);
            Mockito.when(caseRepository.load(baseCaseDate, CaseType.SN, Country.FR)).
                    thenReturn(Collections.singletonList(network));

            // sampler mock
            SamplerFactory samplerFactory = Mockito.mock(SamplerFactory.class);
            Sampler sampler = Mockito.mock(Sampler.class);
            Mockito.when(sampler.getName()).thenReturn("sampler mock");
            Mockito.when(sampler.sample(Matchers.anyInt(), Matchers.anyObject()))
                    .thenAnswer(invocation -> {
                        int n = invocation.getArgumentAt(0, Integer.class);
                        SampleIdGenerator sampleIdGenerator = invocation.getArgumentAt(1, SampleIdGenerator.class);
                        return new SamplerResult() {
                            @Override
                            public boolean isOk() {
                                return true;
                            }

                            @Override
                            public List<Sample> getSamples() {
                                List<Sample> samples = new ArrayList<>(n);
                                for (int i = 0; i < n; i++) {
                                    int id = sampleIdGenerator.newId();
                                    Sample sample = new Sample() {
                                        @Override
                                        public int getId() {
                                            return id;
                                        }

                                        @Override
                                        public void apply(Network network) {
                                        }

                                        @Override
                                        public SampleCharacteritics getCharacteritics() {
                                            return new SampleCharacteritics(0, 0, 0, 0, 0, 0, 0, 0);
                                        }
                                    };
                                    samples.add(sample);
                                }
                                return samples;
                            }
                        };
                    });
            Mockito.when(samplerFactory.create(network, computationManager, OfflineWorkflowImpl.SAMPLING_PRIORITY, histoDbClient))
                    .thenReturn(sampler);

            // WP4.2 mock
            Optimizer optimizer = Mockito.mock(Optimizer.class);
            Mockito.when(optimizer.getName()).thenReturn("optimizer mock");
            OptimizerFactory optimizerFactory = Mockito.mock(OptimizerFactory.class);
            Mockito.when(optimizerFactory.create(network, computationManager, OfflineWorkflowImpl.STARTING_POINT_INIT_PRIORITY, histoDbClient, topologyMiner))
                    .thenReturn(optimizer);
            OptimizerResult optimizerResult = new OptimizerResult() {
                @Override
                public boolean isFeasible() {
                    return true;
                }

                @Override
                public Map<String, String> getMetrics() {
                    return ImmutableMap.of("final_status", "FEASIBLE");
                }
            };
            Mockito.when(optimizer.run()).thenReturn(optimizerResult);

            // load flow mock
            LoadFlow loadFlow = Mockito.mock(LoadFlow.class);
            Mockito.when(loadFlow.getName()).thenReturn("load flow mock");
            LoadFlowFactory loadFlowFactory = Mockito.mock(LoadFlowFactory.class);
            Mockito.when(loadFlowFactory.create(network, computationManager, OfflineWorkflowImpl.LOAD_FLOW_PRIORITY))
                    .thenReturn(loadFlow);
            LoadFlowResult loadFlowResult = new LoadFlowResult() {
                @Override
                public boolean isOk() {
                    return true;
                }

                @Override
                public Map<String, String> getMetrics() {
                    return Collections.emptyMap();
                }

                @Override
                public String getLogs() {
                    return null;
                }
            };
            Mockito.when(loadFlow.run(Matchers.any(LoadFlowParameters.class)))
                    .thenReturn(loadFlowResult);

            // dynamic simulation stabilization mock
            Stabilization stabilization = Mockito.mock(Stabilization.class);
            Mockito.when(stabilization.getName()).thenReturn("stabilization mock");
            ImpactAnalysis impactAnalysis = Mockito.mock(ImpactAnalysis.class);
            Mockito.when(impactAnalysis.getName()).thenReturn("impact analysis mock");
            SimulatorFactory simulatorFactory = Mockito.mock(SimulatorFactory.class);
            Mockito.when(simulatorFactory.createStabilization(network, computationManager, OfflineWorkflowImpl.STABILIZATION_PRIORITY, ddbClientFactory))
                    .thenReturn(stabilization);
            Mockito.when(simulatorFactory.createImpactAnalysis(network, computationManager, OfflineWorkflowImpl.IMPACT_ANALYSIS_PRIORITY, cadbClient))
                    .thenReturn(impactAnalysis);
            Mockito.when(stabilization.run())
                    .thenReturn(new StabilizationResult() {
                        @Override
                        public StabilizationStatus getStatus() {
                            return StabilizationStatus.COMPLETED;
                        }

                        @Override
                        public Map<String, String> getMetrics() {
                            return Collections.emptyMap();
                        }

                        @Override
                        public SimulationState getState() {
                            return new SimulationState() {
                                @Override
                                public String getName() {
                                    return "";
                                }
                            };
                        }
                    });

            // dynamic simulation impact analysis mock
            ImpactAnalysisResult impactAnalysisResult = new ImpactAnalysisResult(ImmutableMap.of("successPercent", "100"));
            Mockito.doAnswer(invocation -> {
                ImpactAnalysisProgressListener listener = invocation.getArgumentAt(2, ImpactAnalysisProgressListener.class);
                listener.onProgress(contingencies.size());
                return CompletableFuture.completedFuture(impactAnalysisResult);
            }).when(impactAnalysis)
                    .runAsync(Matchers.any(SimulationState.class), Matchers.isNull(Set.class), Matchers.any(ImpactAnalysisProgressListener.class));

            // metrics db mock
            MetricsDb metricsDb = Mockito.mock(MetricsDb.class);

            MergeOptimizerFactory mergeOptimizerFactory = Mockito.mock(MergeOptimizerFactory.class);

            ExecutorService executorService = Executors.newCachedThreadPool();
            try {
                // start the workflow !
                new OfflineWorkflowImpl("test", creationParameters, computationManager, ddbClientFactory, cadbClientFactory, histoDbClientFactory,
                        topologyMinerFactory, rulesBuilder, offlineDb, validationDb, caseRepository, samplerFactory, loadFlowFactory,
                        optimizerFactory, simulatorFactory, mergeOptimizerFactory, metricsDb, executorService)
                        .start(startParameters);
            } finally {
                executorService.shutdownNow();
            }
        }
    }
}

