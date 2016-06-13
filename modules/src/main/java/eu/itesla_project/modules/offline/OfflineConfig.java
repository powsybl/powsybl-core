/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.cases.CaseRepositoryFactory;
import eu.itesla_project.modules.OptimizerFactory;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClientFactory;
import eu.itesla_project.modules.rules.RulesBuilderFactory;
import eu.itesla_project.modules.rules.RulesDbClientFactory;
import eu.itesla_project.modules.sampling.SamplerFactory;
import eu.itesla_project.modules.simulation.SimulatorFactory;
import eu.itesla_project.modules.topo.TopologyMinerFactory;
import eu.itesla_project.modules.validation.DefaultValidationDbFactory;
import eu.itesla_project.modules.validation.ValidationDbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineConfig.class);

    public static final String DEFAULT_SIMULATION_DB_NAME = "offlinedb";
    public static final String DEFAULT_RULES_DB_NAME = "rulesdb";
    public static final String DEFAULT_METRICS_DB_NAME = "metricsdb";

    private final Class<? extends OfflineDbFactory> offlineDbFactoryClass;

    private final Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass;

    private final Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass;

    private final Class<? extends HistoDbClientFactory> histoDbClientFactoryClass;

    private final Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass;

    private final Class<? extends RulesBuilderFactory> rulesBuilderFactoryClass;

    private final Class<? extends TopologyMinerFactory> topologyMinerFactoryClass;

    private final Class<? extends ValidationDbFactory> validationDbFactoryClass;

    private final Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass;

    private final Class<? extends SamplerFactory> samplerFactoryClass;

    private final Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    private final Class<? extends OptimizerFactory> optimizerFactoryClass;

    private final Class<? extends SimulatorFactory> simulatorFactoryClass;

    private final Class<? extends MetricsDbFactory> metricsDbFactoryClass;

    private final Class<? extends MergeOptimizerFactory> mergeOptimizerFactoryClass;

    public static OfflineConfig create(Class<? extends OfflineDbFactory> offlineDbFactoryClass,
                                       Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass,
                                       Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass,
                                       Class<? extends HistoDbClientFactory> histoDbClientFactoryClass,
                                       Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass,
                                       Class<? extends RulesBuilderFactory> rulesBuilderFactoryClass,
                                       Class<? extends TopologyMinerFactory> topologyMinerFactoryClass,
                                       Class<? extends ValidationDbFactory> validationDbFactoryClass,
                                       Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass,
                                       Class<? extends SamplerFactory> samplerFactoryClass,
                                       Class<? extends LoadFlowFactory> loadFlowFactoryClass,
                                       Class<? extends OptimizerFactory> optimizerFactoryClass,
                                       Class<? extends SimulatorFactory> simulatorFactoryClass,
                                       Class<? extends MetricsDbFactory> metricsDbFactoryClass,
                                       Class<? extends MergeOptimizerFactory> mergeOptimizerFactoryClass) {
        LOGGER.info("Offline workflow config:\nofflineDbFactoryClass={}\ndynamicDbClientFactoryClass={}\n" +
                        "contingencyDbClientFactoryClass={}\nhistoDbClientFactoryClass={}\n" +
                        "rulesDbClientFactoryClass={}\nrulesBuilderFactoryClass={}\n" +
                        "topologyMinerFactoryClass={}\nvalidationDbFactoryClass={}\n" +
                        "caseRepositoryFactoryClass={}\nsamplerFactoryClass={}\n" +
                        "loadFlowFactoryClass={}\noptimizerFactoryClass={}\n" +
                        "simulatorFactoryClass={}\nmetricsDbFactoryClass={}\n" +
                        "mergeOptimizerFactoryClass={}",
                offlineDbFactoryClass.getName(), dynamicDbClientFactoryClass.getName(),
                contingencyDbClientFactoryClass.getName(), histoDbClientFactoryClass.getName(),
                rulesDbClientFactoryClass.getName(), rulesBuilderFactoryClass.getName(),
                topologyMinerFactoryClass.getName(), validationDbFactoryClass.getName(),
                caseRepositoryFactoryClass.getName(), samplerFactoryClass.getName(),
                loadFlowFactoryClass.getName(), optimizerFactoryClass.getName(),
                simulatorFactoryClass.getName(), metricsDbFactoryClass.getName(),
                mergeOptimizerFactoryClass.getName());
        return new OfflineConfig(offlineDbFactoryClass,
                                 dynamicDbClientFactoryClass,
                                 contingencyDbClientFactoryClass,
                                 histoDbClientFactoryClass,
                                 rulesDbClientFactoryClass,
                                 rulesBuilderFactoryClass,
                                 topologyMinerFactoryClass,
                                 validationDbFactoryClass,
                                 caseRepositoryFactoryClass,
                                 samplerFactoryClass,
                                 loadFlowFactoryClass,
                                 optimizerFactoryClass,
                                 simulatorFactoryClass,
                                 metricsDbFactoryClass,
                                 mergeOptimizerFactoryClass);
    }

    public static OfflineConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("offline");
        Class<? extends OfflineDbFactory> offlineDbFactoryClass = config.getClassProperty("offlineDbFactoryClass", OfflineDbFactory.class);
        Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass = config.getClassProperty("dynamicDbClientFactoryClass", DynamicDatabaseClientFactory.class);
        Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass = config.getClassProperty("contingencyDbClientFactoryClass", ContingenciesAndActionsDatabaseClientFactory.class);
        Class<? extends HistoDbClientFactory> histoDbClientFactoryClass = config.getClassProperty("histoDbClientFactoryClass", HistoDbClientFactory.class);
        Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass = config.getClassProperty("rulesDbClientFactoryClass", RulesDbClientFactory.class);
        Class<? extends RulesBuilderFactory> rulesBuilderFactoryClass = config.getClassProperty("rulesBuilderFactoryClass", RulesBuilderFactory.class);
        Class<? extends TopologyMinerFactory> topologyMinerFactoryClass = config.getClassProperty("topologyMinerFactoryClass", TopologyMinerFactory.class);
        Class<? extends ValidationDbFactory> validationDbFactoryClass = config.getClassProperty("validationDbFactoryClass", ValidationDbFactory.class, DefaultValidationDbFactory.class);
        Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass = config.getClassProperty("caseRepositoryFactoryClass", CaseRepositoryFactory.class);
        Class<? extends SamplerFactory> samplerFactoryClass = config.getClassProperty("samplerFactoryClass", SamplerFactory.class);
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);
        Class<? extends OptimizerFactory> optimizerFactoryClass = config.getClassProperty("optimizerFactoryClass", OptimizerFactory.class);
        Class<? extends SimulatorFactory> simulatorFactoryClass = config.getClassProperty("simulatorFactoryClass", SimulatorFactory.class);
        Class<? extends MetricsDbFactory> metricsDbFactoryClass = config.getClassProperty("metricsDbFactoryClass", MetricsDbFactory.class);
        Class<? extends MergeOptimizerFactory> mergeOptimizerFactoryClass = config.getClassProperty("mergeOptimizerFactoryClass", MergeOptimizerFactory.class);
        return create(offlineDbFactoryClass,
                      dynamicDbClientFactoryClass,
                      contingencyDbClientFactoryClass,
                      histoDbClientFactoryClass,
                      rulesDbClientFactoryClass,
                      rulesBuilderFactoryClass,
                      topologyMinerFactoryClass,
                      validationDbFactoryClass,
                      caseRepositoryFactoryClass,
                      samplerFactoryClass,
                      loadFlowFactoryClass,
                      optimizerFactoryClass,
                      simulatorFactoryClass,
                      metricsDbFactoryClass,
                      mergeOptimizerFactoryClass);
    }

    private OfflineConfig(Class<? extends OfflineDbFactory> offlineDbFactoryClass,
                          Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass,
                          Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass,
                          Class<? extends HistoDbClientFactory> histoDbClientFactoryClass,
                          Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass,
                          Class<? extends RulesBuilderFactory> rulesBuilderFactoryClass,
                          Class<? extends TopologyMinerFactory> topologyMinerFactoryClass,
                          Class<? extends ValidationDbFactory> validationDbFactoryClass,
                          Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass,
                          Class<? extends SamplerFactory> samplerFactoryClass,
                          Class<? extends LoadFlowFactory> loadFlowFactoryClass,
                          Class<? extends OptimizerFactory> optimizerFactoryClass,
                          Class<? extends SimulatorFactory> simulatorFactoryClass,
                          Class<? extends MetricsDbFactory> metricsDbFactoryClass,
                          Class<? extends MergeOptimizerFactory> mergeOptimizerFactoryClass) {
        this.offlineDbFactoryClass = Objects.requireNonNull(offlineDbFactoryClass);
        this.dynamicDbClientFactoryClass = Objects.requireNonNull(dynamicDbClientFactoryClass);
        this.contingencyDbClientFactoryClass = Objects.requireNonNull(contingencyDbClientFactoryClass);
        this.histoDbClientFactoryClass = Objects.requireNonNull(histoDbClientFactoryClass);
        this.rulesDbClientFactoryClass = Objects.requireNonNull(rulesDbClientFactoryClass);
        this.rulesBuilderFactoryClass = Objects.requireNonNull(rulesBuilderFactoryClass);
        this.topologyMinerFactoryClass = Objects.requireNonNull(topologyMinerFactoryClass);
        this.validationDbFactoryClass = Objects.requireNonNull(validationDbFactoryClass);
        this.caseRepositoryFactoryClass = Objects.requireNonNull(caseRepositoryFactoryClass);
        this.samplerFactoryClass = Objects.requireNonNull(samplerFactoryClass);
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
        this.optimizerFactoryClass = Objects.requireNonNull(optimizerFactoryClass);
        this.simulatorFactoryClass = Objects.requireNonNull(simulatorFactoryClass);
        this.metricsDbFactoryClass = Objects.requireNonNull(metricsDbFactoryClass);
        this.mergeOptimizerFactoryClass = Objects.requireNonNull(mergeOptimizerFactoryClass);
    }

    public Class<? extends OfflineDbFactory> getOfflineDbFactoryClass() {
        return offlineDbFactoryClass;
    }

    public Class<? extends DynamicDatabaseClientFactory> getDynamicDbClientFactoryClass() {
        return dynamicDbClientFactoryClass;
    }

    public Class<? extends ContingenciesAndActionsDatabaseClientFactory> getContingencyDbClientFactoryClass() {
        return contingencyDbClientFactoryClass;
    }

    public Class<? extends HistoDbClientFactory> getHistoDbClientFactoryClass() {
        return histoDbClientFactoryClass;
    }

    public Class<? extends RulesDbClientFactory> getRulesDbClientFactoryClass() {
        return rulesDbClientFactoryClass;
    }

    public Class<? extends RulesBuilderFactory> getRulesBuilderFactoryClass() {
        return rulesBuilderFactoryClass;
    }

    public Class<? extends TopologyMinerFactory> getTopologyMinerFactoryClass() {
        return topologyMinerFactoryClass;
    }

    public Class<? extends ValidationDbFactory> getValidationDbFactoryClass() {
        return validationDbFactoryClass;
    }

    public Class<? extends CaseRepositoryFactory> getCaseRepositoryFactoryClass() {
        return caseRepositoryFactoryClass;
    }

    public Class<? extends SamplerFactory> getSamplerFactoryClass() {
        return samplerFactoryClass;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public Class<? extends OptimizerFactory> getOptimizerFactoryClass() {
        return optimizerFactoryClass;
    }

    public Class<? extends SimulatorFactory> getSimulatorFactoryClass() {
        return simulatorFactoryClass;
    }

    public Class<? extends MetricsDbFactory> getMetricsDbFactoryClass() {
        return metricsDbFactoryClass;
    }

    public Class<? extends MergeOptimizerFactory> getMergeOptimizerFactoryClass() {
        return mergeOptimizerFactoryClass;
    }
}
