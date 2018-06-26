/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.google.auto.service.AutoService;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.simulation.securityindexes.SecurityIndex;
import com.powsybl.simulation.securityindexes.SecurityIndexId;
import com.powsybl.simulation.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ImpactAnalysisTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImpactAnalysisTool.class);

    private static final char CSV_SEPARATOR = ';';
    private static final String CASE_FILE = "case-file";
    private static final String CONTINGENCIES = "contingencies";
    private static final String OUTPUT_CSV_FILE = "output-csv-file";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "run-impact-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "run an impact analysis";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE)
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(CONTINGENCIES)
                        .desc("contingencies to test separated by , (all the db in not set)")
                        .hasArg()
                        .argName("LIST")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CSV_FILE)
                        .desc("output CSV file path (pretty print on standard output if not specified)")
                        .hasArg()
                        .argName("FILE")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private static String okToStr(Boolean b) {
        String str;
        if (b == null) {
            str = "NA";
        } else {
            str = b ? "OK" : "NOK";
        }
        return str;
    }

    private static List<String> toRow(Collection<SecurityIndex> securityIndexes) {
        List<String> l = new ArrayList<>();
        Map<SecurityIndexType, Boolean> ok = new EnumMap<>(SecurityIndexType.class);
        for (SecurityIndex securityIndex : securityIndexes) {
            ok.put(securityIndex.getId().getSecurityIndexType(), securityIndex.isOk());
        }

        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
            Boolean b = ok.get(securityIndexType);
            l.add(okToStr(b));
        }

        return l;
    }

    private static void prettyPrint(Multimap<String, SecurityIndex> securityIndexesPerContingency, PrintStream out) {
        Table table = new Table(1 + SecurityIndexType.values().length, BorderStyle.CLASSIC_WIDE);
        table.addCell("Contingency");
        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
            table.addCell(securityIndexType.toString());
        }

        for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexesPerContingency.asMap().entrySet()) {
            String contingencyId = entry.getKey();
            table.addCell(contingencyId);
            for (String str : toRow(entry.getValue())) {
                table.addCell(str);
            }
        }

        out.println(table.render());
    }

    private static void writeCsv(Multimap<String, SecurityIndex> securityIndexesPerContingency, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);
        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Contingency");
            for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexType.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexesPerContingency.asMap().entrySet()) {
                String contingencyId = entry.getKey();
                writer.write(contingencyId);
                for (String str : toRow(entry.getValue())) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(str);
                }
                writer.newLine();
            }
        }
    }

    private static void writeCsv(Map<String, Map<SecurityIndexId, SecurityIndex>> securityIndexesPerCase, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);

        Set<SecurityIndexId> securityIndexIds = new LinkedHashSet<>();
        for (Map<SecurityIndexId, SecurityIndex> securityIndexesPerId : securityIndexesPerCase.values()) {
            if (securityIndexesPerId != null) {
                securityIndexIds.addAll(securityIndexesPerId.keySet());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Base case");
            for (SecurityIndexId securityIndexId : securityIndexIds) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexId.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Map<SecurityIndexId, SecurityIndex>> entry : securityIndexesPerCase.entrySet()) {
                String baseCaseName = entry.getKey();
                writer.write(baseCaseName);

                Map<SecurityIndexId, SecurityIndex> securityIndexes = entry.getValue();
                for (SecurityIndexId securityIndexId : securityIndexIds) {
                    Boolean b = null;
                    if (securityIndexes != null) {
                        SecurityIndex securityIndex = securityIndexes.get(securityIndexId);
                        if (securityIndex != null) {
                            b = securityIndex.isOk();
                        }
                    }
                    writer.write(CSV_SEPARATOR);
                    writer.write(okToStr(b));
                }

                writer.newLine();
            }
        }
    }

    private static Multimap<String, SecurityIndex> runImpactAnalysis(Network network, Set<String> contingencyIds,
                                                                     ComputationManager computationManager, SimulatorFactory simulatorFactory,
                                                                     ContingenciesProvider contingenciesProvider,
                                                                     PrintStream out) throws Exception {
        Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0);
        ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingenciesProvider);
        Map<String, Object> initContext = new HashMap<>();
        SimulationParameters simulationParameters = SimulationParameters.load();
        stabilization.init(simulationParameters, initContext);
        impactAnalysis.init(simulationParameters, initContext);
        out.println("running stabilization simulation...");
        StabilizationResult sr = stabilization.run();
        out.println("stabilization status: " + sr.getStatus());
        out.println("stabilization metrics: " + sr.getMetrics());
        if (sr.getStatus() == StabilizationStatus.COMPLETED) {
            out.println("running impact analysis...");
            ImpactAnalysisResult iar = impactAnalysis.run(sr.getState(), contingencyIds);
            out.println("impact analysis metrics: " + iar.getMetrics());

            return Multimaps.index(iar.getSecurityIndexes(), securityIndex -> securityIndex.getId().getContingencyId());

        }
        return null;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        final Set<String> contingencyIds = line.hasOption(CONTINGENCIES)
                ?  Sets.newHashSet(line.getOptionValue(CONTINGENCIES).split(",")) : null;
        Path outputCsvFile = null;
        if (line.hasOption(OUTPUT_CSV_FILE)) {
            outputCsvFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_CSV_FILE));
        }

        ContingenciesProvider contingenciesProvider = defaultConfig.newFactoryImpl(ContingenciesProviderFactory.class).create();
        SimulatorFactory simulatorFactory = defaultConfig.newFactoryImpl(SimulatorFactory.class);

        if (Files.isRegularFile(caseFile)) {
            runSingleAnalysis(context, caseFile, outputCsvFile, contingencyIds, contingenciesProvider, simulatorFactory);
        } else if (Files.isDirectory(caseFile)) {
            runMultipleAnalyses(context, caseFile, outputCsvFile, contingencyIds, contingenciesProvider, simulatorFactory);
        }
    }

    private void runSingleAnalysis(ToolRunningContext context, Path caseFile, Path outputCsvFile, Set<String> contingencyIds, ContingenciesProvider contingenciesProvider,
                                   SimulatorFactory simulatorFactory) throws Exception {
        context.getOutputStream().println("loading case " + caseFile + "...");
        // load the network
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }
        network.getStateManager().allowStateMultiThreadAccess(true);

        Multimap<String, SecurityIndex> securityIndexesPerContingency
                = runImpactAnalysis(network, contingencyIds, context.getShortTimeExecutionComputationManager(),
                simulatorFactory, contingenciesProvider, context.getOutputStream());

        if (securityIndexesPerContingency != null) {
            if (outputCsvFile == null) {
                prettyPrint(securityIndexesPerContingency, context.getOutputStream());
            } else {
                writeCsv(securityIndexesPerContingency, outputCsvFile);
            }
        }
    }

    private void runMultipleAnalyses(ToolRunningContext context, Path caseFile, Path outputCsvFile, Set<String> contingencyIds, ContingenciesProvider contingenciesProvider,
                                     SimulatorFactory simulatorFactory) throws Exception {
        if (outputCsvFile == null) {
            throw new PowsyblException("In case of multiple impact analyses, only output to csv file is supported");
        }
        Map<String, Map<SecurityIndexId, SecurityIndex>> securityIndexesPerCase = new LinkedHashMap<>();
        Importers.loadNetworks(caseFile, false, network -> {
            try {
                Multimap<String, SecurityIndex> securityIndexesPerContingency
                        = runImpactAnalysis(network, contingencyIds, context.getShortTimeExecutionComputationManager(),
                        simulatorFactory, contingenciesProvider, context.getOutputStream());
                if (securityIndexesPerContingency == null) {
                    securityIndexesPerCase.put(network.getId(), null);
                } else {
                    Map<SecurityIndexId, SecurityIndex> securityIndexesPerId = securityIndexesPerContingency.values().stream().collect(Collectors.toMap(SecurityIndex::getId, e -> e));
                    securityIndexesPerCase.put(network.getId(), securityIndexesPerId);
                }
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }, dataSource -> context.getOutputStream().println("loading case " + dataSource.getBaseName() + "..."));

        writeCsv(securityIndexesPerCase, outputCsvFile);
    }

}
