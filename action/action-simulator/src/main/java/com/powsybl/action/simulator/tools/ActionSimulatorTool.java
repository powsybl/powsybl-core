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
import com.powsybl.action.simulator.loadflow.CaseExporter;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulator;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorConfig;
import com.powsybl.action.simulator.loadflow.LoadFlowActionSimulatorLogPrinter;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.tools.Command;
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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ActionSimulatorTool implements Tool {

    private static Logger LOGGER = LoggerFactory.getLogger(ActionSimulatorTool.class);

    private Path outputCaseFolder;
    private String outputCaseFormat;

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
                options.addOption(Option.builder().longOpt("case-file")
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("dsl-file")
                        .desc("the Groovy DSL path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("contingencies")
                        .desc("contingencies to test")
                        .hasArg()
                        .argName("CONTINGENCY1,CONTINGENCY2,...")
                        .build());
                options.addOption(Option.builder().longOpt("verbose")
                        .desc("verbose mode")
                        .required(false)
                        .build());
                options.addOption(Option.builder().longOpt("output-csv")
                        .desc("the CSV output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt("output-case-folder")
                        .desc("output case folder path")
                        .hasArg()
                        .argName("CASEFOLDER")
                        .build());
                options.addOption(Option.builder().longOpt("output-case-format")
                        .desc("case output format " + Exporters.getFormats())
                        .hasArg()
                        .argName("CASEFORMAT")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private ActionSimulator createActionSimulator(Network network, ToolRunningContext context, boolean verbose, Path csvFile) {
        // config
        LoadFlowActionSimulatorConfig config = LoadFlowActionSimulatorConfig.load();

        // log print
        LoadFlowActionSimulatorLogPrinter logPrinter = new LoadFlowActionSimulatorLogPrinter(context.getOutputStream(), context.getErrorStream(), verbose);

        // case exporter
        CaseExporter caseExporter = new CaseExporter(this.outputCaseFolder, this.outputCaseFormat);

        // security analysis print
        AbstractSecurityAnalysisResultBuilder securityAnalysisPrinter = new AbstractSecurityAnalysisResultBuilder() {
            @Override
            public void onFinalStateResult(SecurityAnalysisResult result) {
                context.getOutputStream().println("Final result");
                LimitViolationFilter filter = LimitViolationFilter.load();
                Writer soutWriter = new OutputStreamWriter(context.getOutputStream()) {
                    @Override
                    public void close() throws IOException {
                        flush();
                    }
                };
                AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();
                Security.printPreContingencyViolations(result, soutWriter, asciiTableFormatterFactory, filter);
                Security.printPostContingencyViolations(result, soutWriter, asciiTableFormatterFactory, filter, !config.isIgnorePreContingencyViolations());
                if (csvFile != null) {
                    try {
                        CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
                        Security.printPreContingencyViolations(result, Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8), csvTableFormatterFactory, filter);
                        Security.printPostContingencyViolations(result, Files.newBufferedWriter(csvFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND), csvTableFormatterFactory, filter, !config.isIgnorePreContingencyViolations());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        };

        return new LoadFlowActionSimulator(network, context.getComputationManager(), config, logPrinter, securityAnalysisPrinter, caseExporter);
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue("case-file"));
        Path dslFile = context.getFileSystem().getPath(line.getOptionValue("dsl-file"));
        List<String> contingencies = line.hasOption("contingencies") ? Arrays.stream(line.getOptionValue("contingencies").split(",")).collect(Collectors.toList())
                                                                     : Collections.emptyList();
        boolean verbose = line.hasOption("verbose");
        Path csvFile = line.hasOption("output-csv") ? context.getFileSystem().getPath(line.getOptionValue("output-csv")) : null;

        if (line.hasOption("output-case-folder")) {
            this.outputCaseFolder = Paths.get(line.getOptionValue("output-case-folder"));
            this.outputCaseFormat = line.getOptionValue("output-case-format");
        }

        context.getOutputStream().println("Loading network '" + caseFile + "'");

        // load network
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case " + caseFile + " not found");
        }

        if (line.hasOption("output-case-folder")) {
            if (!Files.exists(outputCaseFolder)) {
                Files.createDirectories(outputCaseFolder);
            }
            if (line.hasOption("output-case-folder")) {
                if (!line.hasOption("output-case-format")) {
                    throw new ParseException("Missing required option: output-case-format");
                }
            }
        }

        try {
            // load actions from Groovy DSL
            ActionDb actionDb = new ActionDslLoader(dslFile.toFile())
                    .load(network, new DefaultActionDslLoaderObserver() {
                        @Override
                        public void begin(String dslFile) {
                            context.getOutputStream().println("Loading DSL '" + dslFile + "'");
                        }

                        @Override
                        public void contingencyFound(String contingencyId) {
                            if (verbose) {
                                context.getOutputStream().println("    Found contingency '" + contingencyId + "'");
                            }
                        }

                        @Override
                        public void ruleFound(String ruleId) {
                            if (verbose) {
                                context.getOutputStream().println("    Found rule '" + ruleId + "'");
                            }
                        }

                        @Override
                        public void actionFound(String actionId) {
                            if (verbose) {
                                context.getOutputStream().println("    Found action '" + actionId + "'");
                            }
                        }
                    });

            if (contingencies.isEmpty()) {
                contingencies = actionDb.getContingencies().stream().map(Contingency::getId).collect(Collectors.toList());
            }

            // action simulator
            ActionSimulator actionSimulator = createActionSimulator(network, context, verbose, csvFile);
            context.getOutputStream().println("Using '" + actionSimulator.getName() + "' rules engine");

            // start simulator
            actionSimulator.start(actionDb, contingencies);

        } catch (Exception e) {
            LOGGER.trace(e.toString(), e); // to avoid user screen pollution...
            Throwable rootCause = StackTraceUtils.sanitizeRootCause(e);
            rootCause.printStackTrace(context.getErrorStream());
        }
    }

}
