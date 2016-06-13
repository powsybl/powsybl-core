/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.validation;

import com.google.auto.service.AutoService;
import com.google.common.collect.Queues;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseRepositoryFactory;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.merge.MergeOptimizerFactory;
import eu.itesla_project.merge.MergeUtil;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import eu.itesla_project.modules.offline.CsvMetricsDb;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.modules.rules.expr.ExpressionAttributeList;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.simulation.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class OfflineValidationTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfflineValidationTool.class);

    static final double DEFAULT_PURITY_THRESHOLD = 0.95;

    private static final char CSV_SEPARATOR = ';';

    private static final String OK_S = "OK_S";
    private static final String NOK_S = "NOK_S";
    private static final String OK_R = "OK_R";
    private static final String NOK_R = "NOK_R";
    private static final String UNDEF_S = "UNDEF_S";
    private static final String UNDEF_R = "UNDEF_R";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "offline-validation";
            }

            @Override
            public String getTheme() {
                return "Validation";
            }

            @Override
            public String getDescription() {
                return "offline validation tool, security rules vs simulation";
            }

            @Override
            @SuppressWarnings("static-access")
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("workflow")
                        .desc("the workflow id (to find rules)")
                        .hasArg()
                        .required()
                        .argName("ID")
                        .build());
                options.addOption(Option.builder().longOpt("rules-db-name")
                        .desc("the rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                        .hasArg()
                        .argName("NAME")
                        .build());
                options.addOption(Option.builder().longOpt("purity-threshold")
                        .desc("the purity threshold (related to decision tree), default is " + DEFAULT_PURITY_THRESHOLD)
                        .hasArg()
                        .argName("THRESHOLD")
                        .build());
                options.addOption(Option.builder().longOpt("output-dir")
                        .desc("output dir")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("base-case-countries")
                        .desc("base case country list (ISO code)")
                        .hasArg()
                        .argName("COUNTRY1,COUNTRY2")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("history-interval")
                        .desc("history time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                        .hasArg()
                        .argName("DATE1/DATE2")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("merge-optimized")
                        .desc("run optimizer after merging")
                        .build());
                options.addOption(Option.builder().longOpt("case-type")
                        .desc("case type")
                        .hasArg()
                        .argName("CASE_TYPE")
                        .required()
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where CASE_TYPE is one of " + Arrays.toString(CaseType.values());
            }

        };
    }

    private static class ValidationStatus {

        private Boolean ruleOk;

        private Boolean simulationOk;

        public ValidationStatus(Boolean ruleOk, Boolean simulationOk) {
            this.ruleOk = ruleOk;
            this.simulationOk = simulationOk;
        }

        public Boolean isRuleOk() {
            return ruleOk;
        }

        public String isRuleOkToStr() {
            return ruleOk != null ? (ruleOk ? OK_R : NOK_R) : UNDEF_R;
        }

        public void setRuleOk(Boolean ruleOk) {
            this.ruleOk = ruleOk;
        }

        public Boolean isSimulationOk() {
            return simulationOk;
        }

        public String isSimulationOkToStr() {
            return simulationOk != null ? (simulationOk ? OK_S : NOK_S) : UNDEF_S;
        }

        public void setSimulationOk(Boolean simulationOk) {
            this.simulationOk = simulationOk;
        }
    }

    private static String toCategory(String s1, String s2) {
        return s1 + "_" + s2;
    }

    private static void writeComparisonFiles(Set<RuleId> rulesIds, Map<String, Map<RuleId, ValidationStatus>> statusPerRulePerCase, Path outputDir) throws IOException {
        for (RuleId ruleId : rulesIds) {
            Path comparisonFile = outputDir.resolve("comparison_" + ruleId.toString() + ".csv");

            System.out.println("writing " + comparisonFile + "...");

            try (BufferedWriter writer = Files.newBufferedWriter(comparisonFile, StandardCharsets.UTF_8)) {
                writer.write("base case");
                writer.write(CSV_SEPARATOR);
                writer.write("simulation");
                writer.write(CSV_SEPARATOR);
                writer.write("rule");
                writer.newLine();

                for (Map.Entry<String, Map<RuleId, ValidationStatus>> e : statusPerRulePerCase.entrySet()) {
                    String baseCaseName = e.getKey();
                    Map<RuleId, ValidationStatus> statusPerRule = e.getValue();
                    writer.write(baseCaseName);
                    ValidationStatus status = statusPerRule.get(ruleId);
                    if (status == null) {
                        status = new ValidationStatus(null, null);
                    }
                    writer.write(CSV_SEPARATOR);
                    writer.write(status.isSimulationOkToStr());
                    writer.write(CSV_SEPARATOR);
                    writer.write(status.isRuleOkToStr());
                    writer.newLine();
                }
            }
        }
    }

    private static void writeAttributesFiles(Set<RuleId> rulesIds, Map<String, Map<RuleId, Map<HistoDbAttributeId, Object>>> valuesPerRulePerCase, Path outputDir) throws IOException {
        for (RuleId ruleId : rulesIds) {
            Path attributesFile = outputDir.resolve("attributes_" + ruleId.toString() + ".csv");

            System.out.println("writing " + attributesFile + "...");

            try (BufferedWriter writer = Files.newBufferedWriter(attributesFile, StandardCharsets.UTF_8)) {
                writer.write("base case");

                Set<HistoDbAttributeId> allAttributeIds = new LinkedHashSet<>();
                for (Map<RuleId, Map<HistoDbAttributeId, Object>> valuesPerRule : valuesPerRulePerCase.values()) {
                    Map<HistoDbAttributeId, Object> values = valuesPerRule.get(ruleId);
                    if (values != null) {
                        allAttributeIds.addAll(values.keySet());
                    }
                }

                for (HistoDbAttributeId attributeId : allAttributeIds) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(attributeId.toString());
                }
                writer.newLine();

                for (Map.Entry<String, Map<RuleId, Map<HistoDbAttributeId, Object>>> e : valuesPerRulePerCase.entrySet()) {
                    String baseCaseName = e.getKey();
                    Map<RuleId, Map<HistoDbAttributeId, Object>> valuesPerRule = e.getValue();
                    writer.write(baseCaseName);

                    Map<HistoDbAttributeId, Object> values = valuesPerRule.get(ruleId);
                    for (HistoDbAttributeId attributeId : allAttributeIds) {
                        writer.write(CSV_SEPARATOR);
                        Object value = values.get(attributeId);
                        if (value != null && !(value instanceof Float && Float.isNaN((Float) value))) {
                            writer.write(Objects.toString(values.get(attributeId)));
                        }
                    }

                    writer.newLine();
                }
            }
        }
    }

    private static void writeSynthesisFile(Map<RuleId, Map<String, AtomicInteger>> synthesisPerRule,
                                           List<String> categories, Path synthesisFile) throws IOException {
        System.out.println("writing " + synthesisFile + "...");

        try (BufferedWriter writer = Files.newBufferedWriter(synthesisFile, StandardCharsets.UTF_8)) {
            writer.write("rule");
            for (String c : categories) {
                writer.write(CSV_SEPARATOR);
                writer.write(c);
            }
            writer.newLine();
            for (Map.Entry<RuleId, Map<String, AtomicInteger>> e : synthesisPerRule.entrySet()) {
                RuleId ruleId = e.getKey();
                Map<String, AtomicInteger> count = e.getValue();
                writer.write(ruleId.toString());
                for (String c : categories) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(Integer.toString(count.get(c).get()));
                }
                writer.newLine();
            }
        }
    }

    private static void writeCsv(Map<String, Map<RuleId, ValidationStatus>> statusPerRulePerCase,
                                 Map<String, Map<RuleId, Map<HistoDbAttributeId, Object>>> valuesPerRulePerCase,
                                 Path outputDir) throws IOException {
        Set<RuleId> rulesIds = new TreeSet<>();
        statusPerRulePerCase.values().stream().forEach(e -> rulesIds.addAll(e.keySet()));

        writeComparisonFiles(rulesIds, statusPerRulePerCase, outputDir);
        writeAttributesFiles(rulesIds, valuesPerRulePerCase, outputDir);

        List<String> categories = Arrays.asList(
                toCategory(OK_S, OK_R),
                toCategory(OK_S, NOK_R),
                toCategory(NOK_S, OK_R),
                toCategory(NOK_S, NOK_R),
                toCategory(OK_S, UNDEF_R),
                toCategory(NOK_S, UNDEF_R),
                toCategory(UNDEF_S, OK_R),
                toCategory(UNDEF_S, NOK_R),
                toCategory(UNDEF_S, UNDEF_R)
                );

        Map<RuleId, Map<String, AtomicInteger>> synthesisPerRule = new HashMap<>();
        for (RuleId ruleId : rulesIds) {
            synthesisPerRule.put(ruleId, categories.stream().collect(Collectors.toMap(Function.identity(), e -> new AtomicInteger())));
        }
        for (Map.Entry<String, Map<RuleId, ValidationStatus>> e : statusPerRulePerCase.entrySet()) {
            Map<RuleId, ValidationStatus> statusPerRule = e.getValue();
            for (RuleId ruleId : rulesIds) {
                ValidationStatus status = statusPerRule.get(ruleId);
                String category = toCategory(status.isSimulationOkToStr(), status.isRuleOkToStr());
                synthesisPerRule.get(ruleId).get(category).incrementAndGet();
            }
        }

        writeSynthesisFile(synthesisPerRule, categories, outputDir.resolve("synthesis.csv"));
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        String workflowId = line.getOptionValue("workflow");
        Path outputDir = Paths.get(line.getOptionValue("output-dir"));
        double purityThreshold = line.hasOption("purity-threshold") ? Double.parseDouble(line.getOptionValue("purity-threshold")) : DEFAULT_PURITY_THRESHOLD;
        Set<Country> countries = Arrays.stream(line.getOptionValue("base-case-countries").split(",")).map(Country::valueOf).collect(Collectors.toSet());
        Interval histoInterval = Interval.parse(line.getOptionValue("history-interval"));
        boolean mergeOptimized = line.hasOption("merge-optimized");
        CaseType caseType = CaseType.valueOf(line.getOptionValue("case-type"));

        CaseRepositoryFactory caseRepositoryFactory = config.getCaseRepositoryFactoryClass().newInstance();
        RulesDbClientFactory rulesDbClientFactory = config.getRulesDbClientFactoryClass().newInstance();
        ContingenciesAndActionsDatabaseClient contingencyDb = config.getContingencyDbClientFactoryClass().newInstance().create();
        SimulatorFactory simulatorFactory = config.getSimulatorFactoryClass().newInstance();
        DynamicDatabaseClientFactory dynamicDatabaseClientFactory = config.getDynamicDbClientFactoryClass().newInstance();
        LoadFlowFactory loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();
        MergeOptimizerFactory mergeOptimizerFactory = config.getMergeOptimizerFactoryClass().newInstance();

        SimulationParameters simulationParameters = SimulationParameters.load();

        try (ComputationManager computationManager = new LocalComputationManager();
             RulesDbClient rulesDb = rulesDbClientFactory.create(rulesDbName);
             CsvMetricsDb metricsDb = new CsvMetricsDb(outputDir, true, "metrics")) {

            CaseRepository caseRepository = caseRepositoryFactory.create(computationManager);

            Queue<DateTime> dates = Queues.synchronizedDeque(new ArrayDeque<>(caseRepository.dataAvailable(caseType, countries, histoInterval)));

            Map<String, Map<RuleId, ValidationStatus>> statusPerRulePerCase = Collections.synchronizedMap(new TreeMap<>());
            Map<String, Map<RuleId, Map<HistoDbAttributeId, Object>>> valuesPerRulePerCase = Collections.synchronizedMap(new TreeMap<>());

            int cores = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(cores);
            try {
                List<Future<?>> tasks = new ArrayList<>(cores);
                for (int i = 0 ; i < cores; i++) {
                    tasks.add(executorService.submit((Runnable) () -> {
                        while (dates.size() > 0) {
                            DateTime date = dates.poll();

                            try {
                                Network network = MergeUtil.merge(caseRepository, date, caseType, countries, loadFlowFactory, 0, mergeOptimizerFactory, computationManager, mergeOptimized);

                                System.out.println("case " + network.getId() + " loaded");

                                System.out.println("running simulation on " + network.getId() + "...");

                                network.getStateManager().allowStateMultiThreadAccess(true);
                                String baseStateId = network.getId();
                                network.getStateManager().cloneState(StateManager.INITIAL_STATE_ID, baseStateId);
                                network.getStateManager().setWorkingState(baseStateId);

                                Map<RuleId, ValidationStatus> statusPerRule = new HashMap<>();
                                Map<RuleId, Map<HistoDbAttributeId, Object>> valuesPerRule = new HashMap<>();

                                LoadFlow loadFlow = loadFlowFactory.create(network, computationManager, 0);
                                LoadFlowResult loadFlowResult = loadFlow.run();

                                System.err.println("load flow terminated (" + loadFlowResult.isOk() + ") on " + network.getId());

                                if (loadFlowResult.isOk()) {
                                    Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0, dynamicDatabaseClientFactory);
                                    ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingencyDb);
                                    Map<String, Object> context = new HashMap<>();
                                    stabilization.init(simulationParameters, context);
                                    impactAnalysis.init(simulationParameters, context);
                                    StabilizationResult stabilizationResult = stabilization.run();

                                    System.err.println("stabilization terminated ("  + stabilizationResult.getStatus() + ") on " + network.getId());

                                    metricsDb.store(workflowId, network.getId(), "STABILIZATION", stabilizationResult.getMetrics());

                                    if (stabilizationResult.getStatus() == StabilizationStatus.COMPLETED) {
                                        ImpactAnalysisResult impactAnalysisResult = impactAnalysis.run(stabilizationResult.getState());

                                        System.err.println("impact analysis terminated on " + network.getId());

                                        metricsDb.store(workflowId, network.getId(), "IMPACT_ANALYSIS", impactAnalysisResult.getMetrics());

                                        System.out.println("checking rules on " + network.getId() + "...");

                                        for (SecurityIndex securityIndex : impactAnalysisResult.getSecurityIndexes()) {
                                            for (RuleAttributeSet attributeSet : RuleAttributeSet.values()) {
                                                statusPerRule.put(new RuleId(attributeSet, securityIndex.getId()), new ValidationStatus(null, securityIndex.isOk()));
                                            }
                                        }
                                    }
                                }

                                Map<HistoDbAttributeId, Object> values = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false)).getSingleValueMap();
                                for (RuleAttributeSet attributeSet : RuleAttributeSet.values()) {
                                    for (Contingency contingency : contingencyDb.getContingencies(network)) {
                                        List<SecurityRule> securityRules = rulesDb.getRules(workflowId, attributeSet, contingency.getId(), null);
                                        for (SecurityRule securityRule : securityRules) {
                                            SecurityRuleExpression securityRuleExpression = securityRule.toExpression(purityThreshold);
                                            SecurityRuleCheckReport checkReport = securityRuleExpression.check(values);

                                            valuesPerRule.put(securityRule.getId(), ExpressionAttributeList.list(securityRuleExpression.getCondition()).stream()
                                                    .collect(Collectors.toMap(attributeId -> attributeId, new Function<HistoDbAttributeId, Object>() {
                                                        @Override
                                                        public Object apply(HistoDbAttributeId attributeId) {
                                                            Object value  = values.get(attributeId);
                                                            return value != null ? value : Float.NaN;
                                                        }
                                                    })));

                                            ValidationStatus status = statusPerRule.get(securityRule.getId());
                                            if (status == null) {
                                                status = new ValidationStatus(null, null);
                                                statusPerRule.put(securityRule.getId(), status);
                                            }
                                            if (checkReport.getMissingAttributes().isEmpty()) {
                                                status.setRuleOk(checkReport.isSafe());
                                            }
                                        }
                                    }
                                }

                                statusPerRulePerCase.put(network.getId(), statusPerRule);
                                valuesPerRulePerCase.put(network.getId(), valuesPerRule);
                            } catch (Exception e) {
                                LOGGER.error(e.toString(), e);
                            }
                        }
                    }));
                }
                for (Future<?> task : tasks) {
                    task.get();
                }
            } finally {
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            }

            writeCsv(statusPerRulePerCase, valuesPerRulePerCase, outputDir);
        }
    }
}
