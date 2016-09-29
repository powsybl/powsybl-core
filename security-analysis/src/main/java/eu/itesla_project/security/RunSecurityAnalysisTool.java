/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.io.ComponentDefaultConfig;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.contingency.ContingenciesProviderFactory;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.contingency.Contingency;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
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
public class RunSecurityAnalysisTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunSecurityAnalysisTool.class);

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "run-security-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "run security analysis using load flow";
            }

            @Override
            @SuppressWarnings("static-access")
            public Options getOptions() {
                Options options = new Options();
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
                options.addOption(Option.builder().longOpt("case-basename")
                        .desc("the case base name")
                        .hasArg()
                        .argName("NAME")
                        .build());
                options.addOption(Option.builder().longOpt("output-csv-file")
                        .desc("output CSV file path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("detailed")
                        .desc("detailed results of constraints")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where FORMAT is one of " + Importers.getFormats();
            }

        };
    }

    private static String toString(List<LimitViolation> violations, boolean detailed) {
        if (detailed) {
            StringBuilder builder = new StringBuilder();
            if (violations != null) {
                for (LimitViolation violation : violations) {
                    builder.append(violation.getSubject().getId())
                            .append(" (")
                            .append(violation.getValue() / violation.getLimit() * 100)
                            .append(") ");
                }
            }
            return builder.toString();
        } else {
            return violations != null ? (violations.isEmpty() ? "OK" : "NOK") : "NOK";
        }
    }

    @Override
    public void run(CommandLine line) throws Exception {
        ComponentDefaultConfig config = new ComponentDefaultConfig();
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        String caseBaseName = line.getOptionValue("case-basename");
        Path outputCsvFile = Paths.get(line.getOptionValue("output-csv-file"));
        boolean detailed = line.hasOption("detailed");

        ContingenciesProvider contingencyProvider = config.findFactoryImplClass(ContingenciesProviderFactory.class).newInstance().create();
        LoadFlowFactory loadFlowFactory = config.findFactoryImplClass(LoadFlowFactory.class).newInstance();

        try (ComputationManager computationManager = new LocalComputationManager()) {

            Importer importer = Importers.getImporter(caseFormat, computationManager);
            if (importer == null) {
                throw new RuntimeException("Format " + caseFormat + " not supported");
            }

            Map<String, Map<String, List<LimitViolation>>> statusPerContingencyPerCase = Collections.synchronizedMap(new TreeMap<>());

            Set<String> contingencyIds = Collections.synchronizedSet(new LinkedHashSet<>());

            if (caseBaseName != null) {
                System.out.println("loading case " + caseBaseName + " ...");

                // load the network
                Network network = importer.import_(new GenericReadOnlyDataSource(caseDir, caseBaseName), new Properties());

                List<Contingency> contingencies = contingencyProvider.getContingencies(network);
                contingencyIds.addAll(contingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

                StaticSecurityAnalysis securityAnalysis = new StaticSecurityAnalysis(network, loadFlowFactory, computationManager);

                statusPerContingencyPerCase.put(caseBaseName, securityAnalysis.run(contingencies));
            } else {
                Importers.importAll(caseDir, importer, true, network -> {
                    try {
                        List<Contingency> contingencies = contingencyProvider.getContingencies(network);
                        contingencyIds.addAll(contingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

                        StaticSecurityAnalysis securityAnalysis = new StaticSecurityAnalysis(network, loadFlowFactory, computationManager);

                        statusPerContingencyPerCase.put(network.getId(), securityAnalysis.run(contingencies));
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }, dataSource -> System.out.println("loading case " + dataSource.getBaseName() + " ..."));
            }

            try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
                writer.write("base case");
                for (String contingencyId : contingencyIds) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(contingencyId);
                }
                writer.newLine();

                for (Map.Entry<String, Map<String, List<LimitViolation>>> e : statusPerContingencyPerCase.entrySet()) {
                    String baseCaseName = e.getKey();
                    Map<String, List<LimitViolation>> statusPerContingency = e.getValue();
                    writer.write(baseCaseName);
                    for (String contingencyId : contingencyIds) {
                        List<LimitViolation> violations = statusPerContingency.get(contingencyId);
                        writer.write(CSV_SEPARATOR);
                        writer.write(toString(violations, detailed));
                    }
                    writer.newLine();
                }
            }
        }
    }

}
