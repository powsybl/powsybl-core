/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.validation;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.security.StaticSecurityAnalysis;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.rules.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class OverloadValidationTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(OverloadValidationTool.class);

    static final double DEFAULT_PURITY_THRESHOLD = 0.95;

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "overload-validation";
            }

            @Override
            public String getTheme() {
                return "Validation";
            }

            @Override
            public String getDescription() {
                return "overload validation tool, offline rules vs loadflow";
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
                options.addOption(Option.builder().longOpt("case-format")
                        .desc("the case format")
                        .hasArg()
                        .argName("FORMAT")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("case-dir")
                        .desc("the directory where the case is")
                        .hasArg()
                        .argName("DIR")
                        .required()
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
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where FORMAT is one of " + Importers.getFormats();
            }

        };
    }

    private static class OverloadStatus {

        private final boolean offlineRuleOk;

        private final boolean lfOk;

        public OverloadStatus(boolean offlineRuleOk, Boolean lfOk) {
            this.offlineRuleOk = offlineRuleOk;
            this.lfOk = lfOk;
        }

        public boolean isOfflineRuleOk() {
            return offlineRuleOk;
        }

        public boolean isLfOk() {
            return lfOk;
        }
    }

    private static String okToString(boolean ok) {
        return ok ? "OK" : "NOK";
    }

    private static void writeCsv(Set<String> contingencyIds, Map<String, Map<String, OverloadStatus>> statusPerContingencyPerCase, Path outputDir) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve("comparison.csv"), StandardCharsets.UTF_8)) {
            writer.write("base case");
            for (String contingencyId : contingencyIds) {
                writer.write(CSV_SEPARATOR);
                writer.write(contingencyId + " load flow");
                writer.write(CSV_SEPARATOR);
                writer.write(contingencyId + " offline rule");
            }
            writer.newLine();

            for (Map.Entry<String, Map<String, OverloadStatus>> e : statusPerContingencyPerCase.entrySet()) {
                String baseCaseName = e.getKey();
                Map<String, OverloadStatus> statusPerContingency = e.getValue();
                writer.write(baseCaseName);
                for (String contingencyId : contingencyIds) {
                    OverloadStatus overloadStatus = statusPerContingency.get(contingencyId);
                    writer.write(CSV_SEPARATOR);
                    writer.write(Boolean.toString(overloadStatus.isLfOk()));
                    writer.write(CSV_SEPARATOR);
                    writer.write(Boolean.toString(overloadStatus.isOfflineRuleOk()));
                }
                writer.newLine();
            }
        }

        List<String> categories = Arrays.asList("OK_OK", "NOK_NOK", "OK_NOK", "NOK_OK");

        Map<String, Map<String, AtomicInteger>> synthesisPerContingency = new HashMap<>();
        for (String contingencyId : contingencyIds) {
            synthesisPerContingency.put(contingencyId, categories.stream().collect(Collectors.toMap(Function.identity(), e -> new AtomicInteger())));
        }
        for (Map.Entry<String, Map<String, OverloadStatus>> e : statusPerContingencyPerCase.entrySet()) {
            Map<String, OverloadStatus> statusPerContingency = e.getValue();
            for (String contingencyId : contingencyIds) {
                OverloadStatus overloadStatus = statusPerContingency.get(contingencyId);
                synthesisPerContingency.get(contingencyId).get(okToString(overloadStatus.isLfOk()) + "_" + okToString(overloadStatus.isOfflineRuleOk())).incrementAndGet();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve("synthesis.csv"), StandardCharsets.UTF_8)) {
            writer.write("contingency");
            for (String c : categories) {
                writer.write(CSV_SEPARATOR);
                writer.write(c);
            }
            writer.newLine();
            for (Map.Entry<String, Map<String, AtomicInteger>> e : synthesisPerContingency.entrySet()) {
                String contingencyId = e.getKey();
                Map<String, AtomicInteger> count = e.getValue();
                writer.write(contingencyId);
                for (String c : categories) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(Integer.toString(count.get(c).get()));
                }
                writer.newLine();
            }
        }
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        RulesDbClientFactory rulesDbClientFactory = config.getRulesDbClientFactoryClass().newInstance();
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        String workflowId = line.getOptionValue("workflow");
        Path outputDir = Paths.get(line.getOptionValue("output-dir"));
        double purityThreshold = line.hasOption("purity-threshold") ? Double.parseDouble(line.getOptionValue("purity-threshold")) : DEFAULT_PURITY_THRESHOLD;

        ContingenciesAndActionsDatabaseClient contingencyDb = config.getContingencyDbClientFactoryClass().newInstance().create();
        LoadFlowFactory loadFlowFactory = config.getLoadFlowFactoryClass().newInstance();

        try (ComputationManager computationManager = new LocalComputationManager();
             RulesDbClient rulesDb = rulesDbClientFactory.create(rulesDbName)) {

            Importer importer = Importers.getImporter(caseFormat, computationManager);
            if (importer == null) {
                throw new RuntimeException("Format " + caseFormat + " not supported");
            }

            Map<String, Map<String, OverloadStatus>> statusPerContingencyPerCase = Collections.synchronizedMap(new TreeMap<>());

            Set<String> contingencyIds = Collections.synchronizedSet(new LinkedHashSet<>());

            Importers.importAll(caseDir, importer, true, network -> {
                try {
                    List<Contingency> contingencies = contingencyDb.getContingencies(network);
                    contingencyIds.addAll(contingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

                    System.out.println("running security analysis...");

                    StaticSecurityAnalysis securityAnalysis = new StaticSecurityAnalysis(network, loadFlowFactory, computationManager);
                    Map<String, List<LimitViolation>> violationsPerContingency = securityAnalysis.run(contingencies);

                    System.out.println("checking rules...");

                    Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> offlineRuleCheckPerContingency
                            = SecurityRuleUtil.checkRules(network, rulesDb, workflowId, RuleAttributeSet.MONTE_CARLO,
                                                          EnumSet.of(SecurityIndexType.TSO_OVERLOAD), null, purityThreshold);

                    Map<String, OverloadStatus> statusPerContingency = new HashMap<>();

                    for (Contingency contingency : contingencies) {
                        List<LimitViolation> violations = violationsPerContingency.get(contingency.getId());
                        boolean lfOk = violations != null && violations.isEmpty();
                        Map<SecurityIndexType, SecurityRuleCheckStatus> offlineRuleCheck = offlineRuleCheckPerContingency.get(contingency.getId());
                        boolean offlineRuleOk = offlineRuleCheck != null && offlineRuleCheck.get(SecurityIndexType.TSO_OVERLOAD) == SecurityRuleCheckStatus.OK;
                        statusPerContingency.put(contingency.getId(), new OverloadStatus(offlineRuleOk, lfOk));
                    }

                    statusPerContingencyPerCase.put(network.getId(), statusPerContingency);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            }, dataSource -> System.out.println("loading case " + dataSource.getBaseName() + " ..."));

            writeCsv(contingencyIds, statusPerContingencyPerCase, outputDir);
        }
    }
}
