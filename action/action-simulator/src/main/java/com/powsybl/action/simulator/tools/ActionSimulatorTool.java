/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.tools;

import com.google.auto.service.AutoService;
import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.dsl.ActionDslLoader;
import com.powsybl.action.dsl.DefaultActionDslLoaderObserver;
import com.powsybl.action.simulator.ActionSimulator;
import com.powsybl.action.simulator.loadflow.*;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.Partition;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.Security;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineUtil;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.powsybl.action.simulator.tools.ActionSimulatorToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ActionSimulatorTool implements Tool {

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
                options.addOption(Option.builder().longOpt(APPLY_IF_SOLVED_VIOLATIONS)
                        .desc("apply the first tested action which solves all violations")
                        .required(false)
                        .build());
                options.addOption(Option.builder().longOpt(VERBOSE)
                        .desc("verbose mode")
                        .required(false)
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("the output file path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT)
                        .desc("the output file format " + SecurityAnalysisResultExporters.getFormats())
                        .hasArg()
                        .argName("FORMAT")
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
                options.addOption(Option.builder().longOpt(TASK_COUNT)
                        .desc("number of tasks used for parallelization")
                        .hasArg()
                        .argName("NTASKS")
                        .build());
                options.addOption(Option.builder().longOpt(TASK)
                        .desc("task identifier (task-index/task-count)")
                        .hasArg()
                        .argName("TASKID")
                        .build());
                options.addOption(Option.builder().longOpt(EXPORT_AFTER_EACH_ROUND)
                        .desc("export case after each round")
                        .required(false)
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

    /**
     * Creates the consumer which will print the result to standard output.
     */
    private static Consumer<SecurityAnalysisResult> createResultPrinter(Network network, ToolRunningContext context) {
        return r -> {
            context.getOutputStream().println("Final result");
            Writer soutWriter = new OutputStreamWriter(context.getOutputStream());
            Security.print(r, network, soutWriter, new AsciiTableFormatterFactory(), TableFormatterConfig.load());
        };
    }

    /**
     * Creates the consumer which will print the result to standard output.
     */
    private static Consumer<SecurityAnalysisResult> createResultExporter(Path outputFile, String format) {
        return r -> SecurityAnalysisResultExporters.export(r, outputFile, format);
    }

    private static LoadFlowActionSimulatorObserver createCaseExporter(Path outputCaseFolder, String basename, String outputCaseFormat, CompressionFormat compressionFormat, boolean exportEachRound) {
        return new CaseExporter(outputCaseFolder, basename, outputCaseFormat, compressionFormat, exportEachRound);
    }

    /**
     * If option output-case-folder is present, creates the corresponding case exporter.
     */
    private static Optional<LoadFlowActionSimulatorObserver> optionalCaseExporter(CommandLine line, FileSystem fileSystem, String baseName) throws IOException, ParseException {

        if (!line.hasOption(OUTPUT_CASE_FOLDER)) {
            return Optional.empty();
        }

        Path outputCaseFolder = fileSystem.getPath(line.getOptionValue(OUTPUT_CASE_FOLDER));
        String outputCaseFormat = Optional.ofNullable(line.getOptionValue(OUTPUT_CASE_FORMAT))
                                        .orElseThrow(() -> new ParseException("Missing required option: output-case-format"));
        if (!outputCaseFolder.toFile().exists()) {
            Files.createDirectories(outputCaseFolder);
        }

        boolean exportEachRound = line.hasOption(EXPORT_AFTER_EACH_ROUND);

        CompressionFormat compressionFormat = CommandLineUtil.getOptionValue(line, OUTPUT_COMPRESSION_FORMAT, CompressionFormat.class, null);
        return Optional.of(createCaseExporter(outputCaseFolder, baseName, outputCaseFormat, compressionFormat, exportEachRound));
    }

    /**
     * If options output-file and output-format are present, creates the corresponding printer.
     */
    private static Optional<Consumer<SecurityAnalysisResult>> optionalResultPrinter(CommandLine line, FileSystem fileSystem) throws ParseException {
        if (!line.hasOption(OUTPUT_FILE)) {
            return Optional.empty();
        }
        if (!line.hasOption(OUTPUT_FORMAT)) {
            throw new ParseException("Missing required option: " + OUTPUT_FORMAT);
        }

        Path outputFile = fileSystem.getPath(line.getOptionValue(OUTPUT_FILE));
        String format = line.getOptionValue(OUTPUT_FORMAT);

        return Optional.of(createResultExporter(outputFile, format));
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        Path dslFile = context.getFileSystem().getPath(line.getOptionValue(DSL_FILE));
        List<String> contingencies = line.hasOption(CONTINGENCIES) ? Arrays.stream(line.getOptionValue(CONTINGENCIES).split(",")).collect(Collectors.toList())
                                                                     : Collections.emptyList();
        boolean verbose = line.hasOption(VERBOSE);
        boolean applyIfSolved = line.hasOption(APPLY_IF_SOLVED_VIOLATIONS);
        boolean isSubTask = line.hasOption(TASK_COUNT);

        if (isSubTask) {
            checkOptionsInParallel(line);
        }

        //Create observers
        List<LoadFlowActionSimulatorObserver> observers = new ArrayList<>();
        if (!isSubTask) {
            optionalCaseExporter(line, context.getFileSystem(), DataSourceUtil.getBaseName(caseFile))
                    .ifPresent(observers::add);
        }
        observers.add(createLogPrinter(context, verbose));

        // load network
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile);

        try {
            // load actions from Groovy DSL
            ActionDb actionDb = new ActionDslLoader(dslFile.toFile())
                    .load(network, new ActionDslLoaderObserver(context.getOutputStream(), verbose));

            if (contingencies.isEmpty()) {
                contingencies = actionDb.getContingencies().stream().map(Contingency::getId).collect(Collectors.toList());
            }

            LoadFlowActionSimulatorConfig config = LoadFlowActionSimulatorConfig.load();

            List<Consumer<SecurityAnalysisResult>> resultHandlers = new ArrayList<>();
            resultHandlers.add(createResultPrinter(network, context));
            optionalResultPrinter(line, context.getFileSystem()).ifPresent(resultHandlers::add);

            // action simulator
            LOGGER.debug("Creating action simulator.");
            if (isSubTask) {

                context.getOutputStream().println("Using parallel load flow action simulator rules engine");

                int taskCount = Integer.parseInt(line.getOptionValue(TASK_COUNT));
                ParallelLoadFlowActionSimulator actionSimulator = new ParallelLoadFlowActionSimulator(network,
                        context.getLongTimeExecutionComputationManager(), taskCount, config, applyIfSolved, resultHandlers);

                String dsl = new String(Files.readAllBytes(dslFile), StandardCharsets.UTF_8);
                actionSimulator.run(dsl, contingencies);
            } else {

                ActionSimulator actionSimulator = createActionSimulator(network, context, line, config, applyIfSolved, observers, resultHandlers);

                context.getOutputStream().println("Using '" + actionSimulator.getName() + "' rules engine");

                // start simulator
                actionSimulator.start(actionDb, contingencies);
            }


        } catch (Exception e) {
            LOGGER.trace(e.toString(), e); // to avoid user screen pollution...
            Throwable rootCause = StackTraceUtils.sanitizeRootCause(e);
            rootCause.printStackTrace(context.getErrorStream());
        }
    }



    private ActionSimulator createActionSimulator(Network network, ToolRunningContext context, CommandLine line,
                                                  LoadFlowActionSimulatorConfig config, boolean applyIfSolved,
                                                  List<LoadFlowActionSimulatorObserver> observers,
                                                  List<Consumer<SecurityAnalysisResult>> resultHandlers) throws IOException {
        ActionSimulator actionSimulator;
        //Add an observer which will create the result and handle it
        observers.add(new SecurityAnalysisResultHandler(resultHandlers));
        if (line.hasOption(TASK)) {
            Partition partition = Partition.parse(line.getOptionValue(TASK));
            actionSimulator = new LocalLoadFlowActionSimulator(network, partition, config, applyIfSolved, observers);
        } else {
            actionSimulator = new LoadFlowActionSimulator(network, context.getShortTimeExecutionComputationManager(), config, applyIfSolved, observers);
        }
        return actionSimulator;
    }

    private void checkOptionsInParallel(CommandLine line) {
        if (line.hasOption(OUTPUT_CASE_FOLDER)
                || line.hasOption(OUTPUT_CASE_FORMAT)
                || line.hasOption(OUTPUT_COMPRESSION_FORMAT)) {
            throw new IllegalArgumentException("Not supported in parallel mode yet.");
        }
        if (!line.hasOption(OUTPUT_FILE)) {
            throw new IllegalArgumentException("Missing required option: output-file in parallel mode");
        }
    }

    private static class ActionDslLoaderObserver extends DefaultActionDslLoaderObserver {

        private final PrintStream outputStream;

        private final boolean verbose;

        ActionDslLoaderObserver(PrintStream outputStream, boolean verbose) {
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
