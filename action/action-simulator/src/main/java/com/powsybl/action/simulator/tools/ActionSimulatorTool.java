/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.Security;
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

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ActionSimulatorTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionSimulatorTool.class);

    private static final String CASE_FILE = "case-file";
    private static final String DSL_FILE = "dsl-file";
    private static final String CONTINGENCIES = "contingencies";
    private static final String APPLY_IF_SOLVED_VIOLATIONS = "apply-if-solved-violations";
    private static final String VERBOSE = "verbose";
    private static final String OUTPUT_FILE = "output-file";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String OUTPUT_CASE_FOLDER = "output-case-folder";
    private static final String OUTPUT_CASE_FORMAT = "output-case-format";
    private static final String OUTPUT_COMPRESSION_FORMAT = "output-compression-format";

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
     * Creates the observer which will build the result and print it to standard output.
     */
    private static SecurityAnalysisResultHandler createResultPrinter(Network network, ToolRunningContext context) {
        return new SecurityAnalysisResultHandler().add(
                r -> {
                    context.getOutputStream().println("Final result");
                    Writer soutWriter = new OutputStreamWriter(context.getOutputStream());
                    Security.print(r, network, soutWriter, new AsciiTableFormatterFactory(), TableFormatterConfig.load());
                }
        );
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

            SecurityAnalysisResultHandler resultPrinter = createResultPrinter(network, context);
            if (line.hasOption(OUTPUT_FILE)) {
                if (!line.hasOption(OUTPUT_FORMAT)) {
                    throw new ParseException("Missing required option: " + OUTPUT_FORMAT);
                }

                Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));
                String format = line.getOptionValue(OUTPUT_FORMAT);

                //Add a handler which will print the result to the output file
                resultPrinter.add(r -> SecurityAnalysisResultExporters.export(r, outputFile, format));
            }
            observers.add(resultPrinter);

            if (outputCaseFolder != null) {
                CompressionFormat compressionFormat = CommandLineUtil.getOptionValue(line, OUTPUT_COMPRESSION_FORMAT, CompressionFormat.class, null);
                observers.add(createCaseExporter(outputCaseFolder, DataSourceUtil.getBaseName(caseFile), outputCaseFormat, compressionFormat));
            }

            // action simulator
            ActionSimulator actionSimulator = new LoadFlowActionSimulator(network, context.getShortTimeExecutionComputationManager(),
                    config, line.hasOption(APPLY_IF_SOLVED_VIOLATIONS), observers);
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
