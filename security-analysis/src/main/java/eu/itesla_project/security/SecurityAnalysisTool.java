/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.io.table.*;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.contingency.ContingenciesProviderFactory;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "security-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run security analysis";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("case-file")
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("limit-types")
                        .desc("limit type filter (all if not set)")
                        .hasArg()
                        .argName("LIMIT-TYPES")
                        .build());
                options.addOption(Option.builder().longOpt("output-csv")
                        .desc("the CSV output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where LIMIT-TYPES is one of " + Arrays.toString(LimitViolationType.values());
            }
        };
    }

    private static void printPreContingencyViolations(SecurityAnalysisResult result, LimitViolationFilter limitViolationFilter,
                                                      Writer writer, TableFormatterFactory formatterFactory) throws IOException {
        List<LimitViolation> filteredLimitViolations = limitViolationFilter.apply(result.getPreContingencyResult().getLimitViolations());
        if (filteredLimitViolations.size() > 0) {
            try (TableFormatter formatter = formatterFactory.create(writer,
                                                                    "Pre-contingency violations",
                                                                    Locale.getDefault(),
                                                                    new Column("Equipment"),
                                                                    new Column("Violation type"),
                                                                    new Column("Violation name"),
                                                                    new Column("Value"),
                                                                    new Column("Limit"),
                                                                    new Column("Charge %"))) {
                filteredLimitViolations.stream()
                        .sorted((o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()))
                        .forEach(violation -> {
                            try {
                                formatter.writeCell(violation.getSubject().getId())
                                        .writeCell(violation.getLimitType().name())
                                        .writeCell(Objects.toString(violation.getLimitName(), ""))
                                        .writeCell(violation.getValue())
                                        .writeCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""))
                                        .writeCell(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }

    private static void printPostContingencyViolations(SecurityAnalysisResult result, LimitViolationFilter limitViolationFilter,
                                                       Writer writer, TableFormatterFactory formatterFactory) throws IOException {
        if (result.getPostContingencyResults().size() > 0) {
            try (TableFormatter formatter = formatterFactory.create(writer,
                                                                    "Post-contingency limit violations",
                                                                    Locale.getDefault(),
                                                                    new Column("Contingency"),
                                                                    new Column("Status"),
                                                                    new Column("Equipment"),
                                                                    new Column("Violation type"),
                                                                    new Column("Violation name"),
                                                                    new Column("Value"),
                                                                    new Column("Limit"),
                                                                    new Column("Charge %"))) {
                result.getPostContingencyResults()
                        .stream()
                        .sorted((o1, o2) -> o1.getContingency().getId().compareTo(o2.getContingency().getId()))
                        .forEach(postContingencyResult -> {
                            try {
                                List<LimitViolation> filteredLimitViolations = limitViolationFilter.apply(postContingencyResult.getLimitViolations());
                                if (filteredLimitViolations.size() > 0 || !postContingencyResult.isComputationOk()) {
                                    formatter.writeCell(postContingencyResult.getContingency().getId())
                                            .writeCell(postContingencyResult.isComputationOk() ? "converge" : "diverge")
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell()
                                            .writeEmptyCell();

                                    filteredLimitViolations.stream()
                                            .sorted((o1, o2) -> o1.getSubject().getId().compareTo(o2.getSubject().getId()))
                                            .forEach(violation -> {
                                                try {
                                                    formatter.writeEmptyCell()
                                                            .writeEmptyCell()
                                                            .writeCell(violation.getSubject().getId())
                                                            .writeCell(violation.getLimitType().name())
                                                            .writeCell(Objects.toString(violation.getLimitName(), ""))
                                                            .writeCell(violation.getValue())
                                                            .writeCell(Float.toString(violation.getLimit()) + (violation.getLimitReduction() != 1f ? " * " + violation.getLimitReduction() : ""))
                                                            .writeCell(Math.round(Math.abs(violation.getValue()) / violation.getLimit() * 100f));
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }

    private static void printResults(SecurityAnalysisResult result, LimitViolationFilter limitViolationFilter,
                                     Writer writer, TableFormatterFactory formatterFactory) throws IOException {
        printPreContingencyViolations(result, limitViolationFilter, writer, formatterFactory);
        printPostContingencyViolations(result, limitViolationFilter, writer, formatterFactory);
    }

    @Override
    public void run(CommandLine line) throws Exception {
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        Set<LimitViolationType> limitViolationTypes = line.hasOption("limit-types")
                ? Arrays.stream(line.getOptionValue("limit-types").split(",")).map(LimitViolationType::valueOf).collect(Collectors.toSet())
                : EnumSet.allOf(LimitViolationType.class);
        Path csvFile = null;
        if (line.hasOption("output-csv")) {
            csvFile = Paths.get(line.getOptionValue("output-csv"));
        }

        System.out.println("Loading network '" + caseFile + "'");

        // load network
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case '" + caseFile + "' not found");
        }
        network.getStateManager().allowStateMultiThreadAccess(true);

        ComponentDefaultConfig defaultConfig = new ComponentDefaultConfig();
        SecurityAnalysisFactory securityAnalysisFactory = defaultConfig.findFactoryImplClass(SecurityAnalysisFactory.class).newInstance();
        SecurityAnalysis securityAnalysis = securityAnalysisFactory.create(network, LocalComputationManager.getDefault(), 0);

        ContingenciesProviderFactory contingenciesProviderFactory = defaultConfig.findFactoryImplClass(ContingenciesProviderFactory.class).newInstance();
        ContingenciesProvider contingenciesProvider = contingenciesProviderFactory.create();

        // run security analysis on all N-1 lines
        SecurityAnalysisResult result = securityAnalysis.runAsync(contingenciesProvider)
                        .join();

        if (!result.getPreContingencyResult().isComputationOk()) {
            System.out.println("Pre-contingency state divergence");
        }
        LimitViolationFilter limitViolationFilter = new LimitViolationFilter(limitViolationTypes);
        if (csvFile != null) {
            System.out.println("Writing results to '" + csvFile + "'");
            try (Writer writer = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8)) {
                printResults(result, limitViolationFilter, writer, new CsvTableFormatterFactory());
            }
        } else {
            printResults(result, limitViolationFilter, new OutputStreamWriter(System.out), new AsciiTableFormatterFactory());
        }
    }
}
