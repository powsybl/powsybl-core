/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

import eu.itesla_project.modules.MergeOptimizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.modules.cases.CaseRepositoryFactory;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbClientFactory;
import eu.itesla_project.modules.optimizer.CorrectiveControlOptimizerFactory;
import eu.itesla_project.modules.rules.RulesDbClientFactory;
import eu.itesla_project.modules.wca.UncertaintiesAnalyserFactory;
import eu.itesla_project.modules.wca.WCAFactory;
import eu.itesla_project.modules.mcla.MontecarloSamplerFactory;
import eu.itesla_project.modules.simulation.SimulatorFactory;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineConfig {
    /*
    #example of online.properties file.

    #services factories declarations
    dynamicDbClientFactoryClass=eu.itesla_project.modules.test.ZipFileDynamicDatabaseFactory
    contingencyDbClientFactoryClass=eu.itesla_project.modules.test.CsvFileContingenciesAndActionsDatabaseClientFactory
    histoDbClientFactoryClass=eu.itesla_project.histodb.client.impl.HistoDbClientFactoryImpl
    rulesDbClientFactoryClass=eu.itesla_project.histodb.client.impl.RulesDbClientFactoryImpl
    #wcaFactoryClass=eu.itesla_project.online.modules.mock.WCAMockFactoryImpl
    wcaFactoryClass=eu.itesla_project.wca.WCAFactoryImpl
    #loadFlowFactoryClass=eu.itesla_project.online.modules.mock.LoadFlowMockFactoryImpl
    loadFlowFactoryClass=eu.itesla_project.helmflow.HelmFlowFactoryImpl
    onlineDbFactoryClass=eu.itesla_project.online.db.OnlineDbMVStoreFactory
    uncertaintiesAnalyserFactoryClass=eu.itesla_project.wca.uncertainties.UncertaintiesAnalyserFactoryTestImpl
    correctiveControlOptimizerFactoryClass=eu.itesla_project.online.modules.mock.CorrectiveControlOptimizerMockFactoryImpl
    simulatorFactoryClass=eu.itesla_project.eurostag.EurostagFactory
    caseRepositoryFactoryClass=eu.itesla_project.modules.cases.EntsoeCaseRepositoryFactory
    #montecarloSamplerFactoryClass=eu.itesla_project.online.modules.mock.MontecarloSamplerFactoryMock
    montecarloSamplerFactoryClass=eu.itesla_project.matlab.mcla.montecarlo.MontecarloSamplerFactoryImpl
    mergeOptimizerFactoryClass=com.rte_france.itesla.merge.MergeOptimizerFactoryImpl
    rulesFacadeFactoryClass=eu.itesla_project.online.security_rules.SecurityRulesFacadeFactory
	*/

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineConfig.class);

    private final Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass;
    private final Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass;
    private final Class<? extends HistoDbClientFactory> histoDbClientFactoryClass;
    private final Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass;
    private final Class<? extends WCAFactory> wcaFactoryClass;
    private final Class<? extends LoadFlowFactory> loadFlowFactoryClass;
    private final Class<? extends OnlineDbFactory> onlineDbFactoryClass;
    private final Class<? extends UncertaintiesAnalyserFactory> uncertaintiesAnalyserFactoryClass;
    private final Class<? extends CorrectiveControlOptimizerFactory> correctiveControlOptimizerFactoryClass;
    private final Class<? extends SimulatorFactory> simulatorFactoryClass;
    private final Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass;
    private final Class<? extends MontecarloSamplerFactory>  montecarloSamplerFactory;
    private final Class<? extends MergeOptimizerFactory>  mergeOptimizerFactory;
    private final Class<? extends RulesFacadeFactory>  rulesFacadeFactory;

	public static OnlineConfig create(Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass,
                                       Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass,
                                       Class<? extends HistoDbClientFactory> histoDbClientFactoryClass,
                                       Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass,
                                       Class<? extends WCAFactory> wcaFactoryClass,
                                       Class<? extends LoadFlowFactory> loadFlowFactoryClass,
                                       Class<? extends OnlineDbFactory> onlineDbFactoryClass,
                                       Class<? extends UncertaintiesAnalyserFactory> uncertaintiesAnalyserFactoryClass,
                                       Class<? extends CorrectiveControlOptimizerFactory> correctiveControlOptimizerFactoryClass,
                                       Class<? extends SimulatorFactory> simulatorFactoryClass, 
                                       Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass,
                                       Class<? extends MontecarloSamplerFactory>  montecarloSamplerFactory,
                                       Class<? extends MergeOptimizerFactory>  mergeOptimizerFactory,
                                       Class<? extends RulesFacadeFactory>  rulesFacadeFactory) {
        LOGGER.info("Online workflow config: " +
                        "dynamicDbClientFactoryClass={}, contingencyDbClientFactoryClass={}, " +
                        "histoDbClientFactoryClass={}, rulesDbClientFactoryClass={}, " +
                        "wcaFactoryClass={}, loadFlowFactoryClass={}, onlineDbFactoryClass={}, " +
                        "uncertaintiesAnalyserFactoryClass={}, correctiveControlOptimizerFactoryClass={}, " +
                        "simulatorFactoryClass={}, caseRepositoryFactoryClass={}, montecarloSamplerFactory={}, " + 
                        "mergeOptimizerFactory={}, rulesFacadeFactory={}",
                dynamicDbClientFactoryClass.getName(), contingencyDbClientFactoryClass.getName(),
                histoDbClientFactoryClass.getName(), rulesDbClientFactoryClass.getName(), wcaFactoryClass.getName(), 
                loadFlowFactoryClass.getName(), onlineDbFactoryClass.getName(), uncertaintiesAnalyserFactoryClass.getName(),
                correctiveControlOptimizerFactoryClass.getName(), simulatorFactoryClass.getName(), caseRepositoryFactoryClass.getName(),
                montecarloSamplerFactory.getName(), mergeOptimizerFactory.getName(), rulesFacadeFactory.getName());
        return new OnlineConfig(
                dynamicDbClientFactoryClass,
                contingencyDbClientFactoryClass,
                histoDbClientFactoryClass,
                rulesDbClientFactoryClass,
                wcaFactoryClass,
                loadFlowFactoryClass,
                onlineDbFactoryClass,
                uncertaintiesAnalyserFactoryClass,
                correctiveControlOptimizerFactoryClass,
                simulatorFactoryClass,
                caseRepositoryFactoryClass,
                montecarloSamplerFactory,
                mergeOptimizerFactory,
                rulesFacadeFactory);
    }

    public static OnlineConfig load() throws IOException, ParseException, ClassNotFoundException {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("online");
        Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass = config.getClassProperty("dynamicDbClientFactoryClass", DynamicDatabaseClientFactory.class);
        Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass = config.getClassProperty("contingencyDbClientFactoryClass", ContingenciesAndActionsDatabaseClientFactory.class);
        Class<? extends HistoDbClientFactory> histoDbClientFactoryClass = config.getClassProperty("histoDbClientFactoryClass", HistoDbClientFactory.class);
        Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass = config.getClassProperty("rulesDbClientFactoryClass", RulesDbClientFactory.class);
        Class<? extends WCAFactory> wcaFactoryClass = config.getClassProperty("wcaFactoryClass", WCAFactory.class);
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);
        Class<? extends OnlineDbFactory> onlineDbFactoryClass = config.getClassProperty("onlineDbFactoryClass", OnlineDbFactory.class);
        Class<? extends UncertaintiesAnalyserFactory> uncertaintiesAnalyserFactoryClass = config.getClassProperty("uncertaintiesAnalyserFactoryClass", UncertaintiesAnalyserFactory.class);
        Class<? extends CorrectiveControlOptimizerFactory> correctiveControlOptimizerFactoryClass = config.getClassProperty("correctiveControlOptimizerFactoryClass", CorrectiveControlOptimizerFactory.class);
        Class<? extends SimulatorFactory> simulatorFactoryClass = config.getClassProperty("simulatorFactoryClass", SimulatorFactory.class);
        Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass = config.getClassProperty("caseRepositoryFactoryClass", CaseRepositoryFactory.class);
        Class<? extends MontecarloSamplerFactory> montecarloSamplerFactory = config.getClassProperty("montecarloSamplerFactoryClass", MontecarloSamplerFactory.class);
        Class<? extends MergeOptimizerFactory> mergeOptimizerFactoryClass = config.getClassProperty("mergeOptimizerFactoryClass", MergeOptimizerFactory.class);
        Class<? extends RulesFacadeFactory> rulesFacadeFactoryClass = config.getClassProperty("rulesFacadeFactoryClass", RulesFacadeFactory.class);
        return create(
                dynamicDbClientFactoryClass, contingencyDbClientFactoryClass, histoDbClientFactoryClass, 
                rulesDbClientFactoryClass, wcaFactoryClass, loadFlowFactoryClass, onlineDbFactoryClass,
                uncertaintiesAnalyserFactoryClass, correctiveControlOptimizerFactoryClass, simulatorFactoryClass,
                caseRepositoryFactoryClass, montecarloSamplerFactory, mergeOptimizerFactoryClass, rulesFacadeFactoryClass);
    }

    private OnlineConfig(Class<? extends DynamicDatabaseClientFactory> dynamicDbClientFactoryClass,
                          Class<? extends ContingenciesAndActionsDatabaseClientFactory> contingencyDbClientFactoryClass,
                          Class<? extends HistoDbClientFactory> histoDbClientFactoryClass,
                          Class<? extends RulesDbClientFactory> rulesDbClientFactoryClass,
                          Class<? extends WCAFactory> wcaFactoryClass,
                          Class<? extends LoadFlowFactory> loadFlowFactoryClass,
                          Class<? extends OnlineDbFactory> onlineDbFactoryClass,
                          Class<? extends UncertaintiesAnalyserFactory> uncertaintiesAnalyserFactoryClass,
                          Class<? extends CorrectiveControlOptimizerFactory> correctiveControlOptimizerFactoryClass,
                          Class<? extends SimulatorFactory> simulatorFactoryClass,
                          Class<? extends CaseRepositoryFactory> caseRepositoryFactoryClass,
                          Class<? extends MontecarloSamplerFactory> montecarloSamplerFactory,
                         Class<? extends MergeOptimizerFactory>  mergeOptimizerFactory,
                         Class<? extends RulesFacadeFactory>  rulesFacadeFactory) {
        this.dynamicDbClientFactoryClass = Objects.requireNonNull(dynamicDbClientFactoryClass);
        this.contingencyDbClientFactoryClass = Objects.requireNonNull(contingencyDbClientFactoryClass);
        this.histoDbClientFactoryClass = Objects.requireNonNull(histoDbClientFactoryClass);
        this.rulesDbClientFactoryClass = Objects.requireNonNull(rulesDbClientFactoryClass);
        this.wcaFactoryClass = Objects.requireNonNull(wcaFactoryClass);
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
        this.onlineDbFactoryClass = Objects.requireNonNull(onlineDbFactoryClass);
        this.uncertaintiesAnalyserFactoryClass = Objects.requireNonNull(uncertaintiesAnalyserFactoryClass);
        this.correctiveControlOptimizerFactoryClass = Objects.requireNonNull(correctiveControlOptimizerFactoryClass);
        this.simulatorFactoryClass = Objects.requireNonNull(simulatorFactoryClass);
        this.caseRepositoryFactoryClass = Objects.requireNonNull(caseRepositoryFactoryClass);
        this.montecarloSamplerFactory = montecarloSamplerFactory;
        this.mergeOptimizerFactory = mergeOptimizerFactory;
        this.rulesFacadeFactory = rulesFacadeFactory;
    }

    public Class<? extends ContingenciesAndActionsDatabaseClientFactory> getContingencyDbClientFactoryClass() {
        return contingencyDbClientFactoryClass;
    }

    public Class<? extends DynamicDatabaseClientFactory> getDynamicDbClientFactoryClass() { 
    	return dynamicDbClientFactoryClass; 
    }

    public Class<? extends HistoDbClientFactory> getHistoDbClientFactoryClass() {
        return histoDbClientFactoryClass;
    }

    public Class<? extends RulesDbClientFactory> getRulesDbClientFactoryClass() {
        return rulesDbClientFactoryClass;
    }

    public Class<? extends WCAFactory> getWcaFactoryClass() { 
    	return wcaFactoryClass; 
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() { 
    	return loadFlowFactoryClass; 
    }
    
    public Class<? extends OnlineDbFactory> getOnlineDbFactoryClass() { 
    	return onlineDbFactoryClass; 
    }

    public Class<? extends UncertaintiesAnalyserFactory> getUncertaintiesAnalyserFactoryClass() {
        return uncertaintiesAnalyserFactoryClass;
    }
    
    public Class<? extends CorrectiveControlOptimizerFactory> getCorrectiveControlOptimizerFactoryClass() {
        return correctiveControlOptimizerFactoryClass;
    }

    public Class<? extends SimulatorFactory> getSimulatorFactoryClass() {
        return simulatorFactoryClass;
    }

    public Class<? extends MontecarloSamplerFactory> getMontecarloSamplerFactory() {return montecarloSamplerFactory;}

    public Class<? extends CaseRepositoryFactory> getCaseRepositoryFactoryClass() {
        return caseRepositoryFactoryClass;
    }

    public Class<? extends MergeOptimizerFactory> getMergeOptimizerFactory() {
        return mergeOptimizerFactory;
    }
    
    public Class<? extends RulesFacadeFactory> getRulesFacadeFactory() {
		return rulesFacadeFactory;
	}
}
