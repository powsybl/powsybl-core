/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool implements Tool {

    private static final String CASE_FILE_OPTION = "case-file";
    private static final String PARAMETERS_FILE = "parameters-file";
    private static final String LIMIT_TYPES_OPTION = "limit-types";
    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String OUTPUT_FORMAT_OPTION = "output-format";
    private static final String CONTINGENCIES_FILE_OPTION = "contingencies-file";
    private static final String WITH_EXTENSIONS_OPTION = "with-extensions";

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
                options.addOption(Option.builder().longOpt(CASE_FILE_OPTION)
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE)
                    .desc("loadflow parameters as JSON file")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(LIMIT_TYPES_OPTION)
                    .desc("limit type filter (all if not set)")
                    .hasArg()
                    .argName("LIMIT-TYPES")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                    .desc("the output path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                    .desc("the output format " + SecurityAnalysisResultExporters.getFormats())
                    .hasArg()
                    .argName("FORMAT")
                    .build());
                options.addOption(Option.builder().longOpt(CONTINGENCIES_FILE_OPTION)
                    .desc("the contingencies path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(WITH_EXTENSIONS_OPTION)
                    .desc("the extension list to enable")
                    .hasArg()
                    .argName("EXTENSIONS")
                    .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return String.join(System.lineSeparator(),
                    "Allowed LIMIT-TYPES values are " + Arrays.toString(LimitViolationType.values()),
                    "Allowed EXTENSIONS values are " + SecurityAnalysisInterceptors.getExtensionNames()
                    );
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));

        Set<LimitViolationType> limitViolationTypes = line.hasOption(LIMIT_TYPES_OPTION)
            ? Arrays.stream(line.getOptionValue(LIMIT_TYPES_OPTION).split(",")).map(LimitViolationType::valueOf).collect(Collectors.toSet())
            : EnumSet.allOf(LimitViolationType.class);

        Set<SecurityAnalysisInterceptor> interceptors = line.hasOption(WITH_EXTENSIONS_OPTION)
            ? Arrays.stream(line.getOptionValue(WITH_EXTENSIONS_OPTION).split(",")).map(SecurityAnalysisInterceptors::createInterceptor).collect(Collectors.toSet())
            : Collections.emptySet();

        // Output file and output format
        Path outputFile = null;
        String format = null;
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        // Contingencies file
        Path contingenciesFile = line.hasOption(CONTINGENCIES_FILE_OPTION) ? context.getFileSystem().getPath(line.getOptionValue(CONTINGENCIES_FILE_OPTION)) : null;

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        LimitViolationFilter limitViolationFilter = LimitViolationFilter.load();
        limitViolationFilter.setViolationTypes(limitViolationTypes);

        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();
        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonSecurityAnalysisParameters.update(parameters, parametersFile);
        }

        SecurityAnalyzer analyzer = new SecurityAnalyzer(limitViolationFilter, context.getLongTimeExecutionComputationManager(), 0, interceptors);
        SecurityAnalysisResult result = analyzer.analyze(network, contingenciesFile, parameters);

        if (!result.getPreContingencyResult().isComputationOk()) {
            context.getErrorStream().println("Pre-contingency state divergence");
        } else {
            if (outputFile != null) {
                context.getOutputStream().println("Writing results to '" + outputFile + "'");
                SecurityAnalysisResultExporters.export(result, outputFile, format);
            } else {
                // To avoid the closing of System.out
                Writer writer = new OutputStreamWriter(context.getOutputStream());
                Security.print(result, network, writer, new AsciiTableFormatterFactory(), TableFormatterConfig.load());
            }
        }
    }
}
