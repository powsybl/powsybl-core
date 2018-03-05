/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.google.auto.service.AutoService;
import com.powsybl.action.dsl.DefaultActionDslLoaderObserver;
import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.dsl.ActionDslLoader;
import com.powsybl.action.simulator.ActionSimulator;
import com.powsybl.action.simulator.loadflow.*;
import com.powsybl.action.simulator.parallel.*;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineUtil;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.Security;
import com.powsybl.security.SecurityAnalysisResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ActionSimulatorTool implements Tool, ActionSimulatorToolConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionSimulatorTool.class);

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "action-simulator";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Action simulator";
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
                options.addOption(Option.builder().longOpt(DSL_FILE)
                        .desc("the Groovy DSL path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(CONTINGENCIES)
                        .desc("contingencies to test")
                        .hasArg()
                        .argName("CONTINGENCY1,CONTINGENCY2,...")
                        .build());
                options.addOption(Option.builder().longOpt(VERBOSE)
                        .desc("verbose mode")
                        .required(false)
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CSV)
                        .desc("the CSV output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CASE_FOLDER)
                        .desc("output case folder path")
                        .hasArg()
                        .argName("CASEFOLDER")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CASE_FORMAT)
                        .desc("output case format " + Exporters.getFormats())
                        .hasArg()
                        .argName("CASEFORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_COMPRESSION_FORMAT)
                        .desc("output compression format " + CompressionFormat.getFormats())
                        .hasArg()
                        .argName("COMPRESSION_FORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(PARALLEL)
                        .desc("parallizer into Y parts")
                        .hasArg()
                        .argName("NUMBER")
                        .build());
                options.addOption(Option.builder().longOpt(SUB_CONTINGENCIES)
                        .desc("fitration")
                        .hasArg()
                        .argName("X/Y")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private static LoadFlowActionSimulatorObserver createLogPrinter(ToolRunningContext context, boolean verbose) {
        return new LoadFlowActionSimulatorLogPrinter(context.getOutputStream(), context.getErrorStream(), verbose);
    }

    private static LoadFlowActionSimulatorObserver createSecurityAnalysisPrinter(Network network, ToolRunningContext context, LoadFlowActionSimulatorConfig config, Path csvFile,
                                                                                 MergeSuggester suggester) {
        return new AbstractSecurityAnalysisResultBuilder() {
            @Override
            public void onFinalStateResult(SecurityAnalysisResult result) {
                context.getOutputStream().println("Final result");
                LimitViolationFilter filter = LimitViolationFilter.load();
                Writer soutWriter = new OutputStreamWriter(context.getOutputStream());
                AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();
                if (!suggester.ignore("PreContingency")) {
                    Security.printPreContingencyViolations(result, network, soutWriter, asciiTableFormatterFactory, filter);
                }
                Security.printPostContingencyViolations(result, network, soutWriter, asciiTableFormatterFactory, filter, !config.isIgnorePreContingencyViolations());
                if (csvFile != null) {
                    try (Writer writer = Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8)) {
                        CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
                        if (!suggester.ignore("PreContingency")) {
                            Security.printPreContingencyViolations(result, network, writer, csvTableFormatterFactory, filter);
                        }
                        Security.printPostContingencyViolations(result, network, writer, csvTableFormatterFactory, filter, !config.isIgnorePreContingencyViolations());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public List<ChunkPath> getChunkFiles() {
                ChunkPath chunkFile = new ChunkPath(csvFile, MergeStrategy.APPEND, OUTPUT_CSV);
                return Collections.singletonList(chunkFile);
            }
        };
    }

    private static class SecurityAnalysisPrinterMergeSuggerter implements MergeSuggester {

        final Map<String, Boolean> map = Collections.singletonMap("PreContingenc", true);

        @Override
        public boolean ignore(String seg) {
            return map.get(seg) == null ? false : map.get(seg);
        }
    }

    private static LoadFlowActionSimulatorObserver createCaseExporter(Path outputCaseFolder, String basename, String outputCaseFormat, CompressionFormat compressionFormat) {
        return new CaseExporter(outputCaseFolder, basename, outputCaseFormat, compressionFormat);
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        Path dslFile = context.getFileSystem().getPath(line.getOptionValue(DSL_FILE));
        List<String> contingencies = line.hasOption(CONTINGENCIES) ? Arrays.stream(line.getOptionValue(CONTINGENCIES).split(",")).collect(Collectors.toList())
                                                                     : Collections.emptyList();
        boolean verbose = line.hasOption(VERBOSE);
        Path csvFile = line.hasOption(OUTPUT_CSV) ? context.getFileSystem().getPath(line.getOptionValue(OUTPUT_CSV)) : null;

        Path outputCaseFolder = null;
        String outputCaseFormat = null;
        if (line.hasOption(OUTPUT_CASE_FOLDER)) {
            outputCaseFolder = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_CASE_FOLDER));
            outputCaseFormat = line.getOptionValue(OUTPUT_CASE_FORMAT);
            if (!line.hasOption(OUTPUT_CASE_FORMAT)) {
                throw new ParseException("Missing required option: output-case-format");
            } else if (!outputCaseFolder.toFile().exists()) {
                Files.createDirectories(outputCaseFolder);
            }
        }

        // load network
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case " + caseFile + " not found");
        }

        try {
            // load actions from Groovy DSL
            ActionDb actionDb = new ActionDslLoader(dslFile.toFile())
                    .load(network, new ActionDslLoaderObserver(context.getOutputStream(), verbose));

            if (contingencies.isEmpty()) {
                contingencies = actionDb.getContingencies().stream().map(Contingency::getId).collect(Collectors.toList());
            }

            LoadFlowActionSimulatorConfig config = LoadFlowActionSimulatorConfig.load();

            List<LoadFlowActionSimulatorObserver> observers = new ArrayList<>();
            observers.add(createLogPrinter(context, verbose));
            if (outputCaseFolder != null) {
                CompressionFormat compressionFormat = CommandLineUtil.getOptionValue(line, OUTPUT_COMPRESSION_FORMAT, CompressionFormat.class, null);
                observers.add(createCaseExporter(outputCaseFolder, DataSourceUtil.getBaseName(caseFile), outputCaseFormat, compressionFormat));
            }

            // action simulator
            ActionSimulator actionSimulator = null;
            if (line.hasOption(PARALLEL)) {
                String para = line.getOptionValue(PARALLEL);
                actionSimulator = new ParallelLoadFlowActionSimulator(network, context, Integer.parseInt(para), line, config, observers);
            } else if (line.hasOption(SUB_CONTINGENCIES)) {
                String filtreOpt = line.getOptionValue(SUB_CONTINGENCIES);
                Filtration filtration = new Filtration(filtreOpt);
                actionSimulator = new LocalLoadFlowActionSimulator(network, filtration, config, observers);
                observers.add(createSecurityAnalysisPrinter(network, context, config, csvFile, new SecurityAnalysisPrinterMergeSuggerter()));
            } else {
                actionSimulator = new LoadFlowActionSimulator(network, context.getComputationManager(), config, observers);
                observers.add(createSecurityAnalysisPrinter(network, context, config, csvFile, new DefaultMergeSuggester()));
            }
            context.getOutputStream().println("Using '" + actionSimulator.getName() + "' rules engine");

            // start simulator
            actionSimulator.start(actionDb, contingencies);

        } catch (Exception e) {
            LOGGER.trace(e.toString(), e); // to avoid user screen pollution...
            Throwable rootCause = StackTraceUtils.sanitizeRootCause(e);
            rootCause.printStackTrace(context.getErrorStream());
        }
    }

    private static class ActionDslLoaderObserver extends DefaultActionDslLoaderObserver {

        private final PrintStream outputStream;

        private final boolean verbose;

        public ActionDslLoaderObserver(PrintStream outputStream, boolean verbose) {
            this.outputStream = Objects.requireNonNull(outputStream);
            this.verbose = verbose;
        }

        @Override
        public void begin(String dslFile) {
            outputStream.println("Loading DSL '" + dslFile + "'");
        }

        @Override
        public void contingencyFound(String contingencyId) {
            if (verbose) {
                outputStream.println("    Found contingency '" + contingencyId + "'");
            }
        }

        @Override
        public void ruleFound(String ruleId) {
            if (verbose) {
                outputStream.println("    Found rule '" + ruleId + "'");
            }
        }

        @Override
        public void actionFound(String actionId) {
            if (verbose) {
                outputStream.println("    Found action '" + actionId + "'");
            }
        }
    }

}
