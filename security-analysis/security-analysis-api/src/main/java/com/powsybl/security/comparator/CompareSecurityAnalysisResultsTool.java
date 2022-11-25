/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.comparator;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
@AutoService(Tool.class)
public class CompareSecurityAnalysisResultsTool implements Tool {

    private static final String RESULT1_FILE_OPTION = "result1-file";
    private static final String RESULT2_FILE_OPTION = "result2-file";
    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String THRESHOLD_OPTION = "threshold";

    private static final double THRESHOLD_DEFAULT = 0d;

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "compare-security-analysis-results";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Compare security analysis results";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(RESULT1_FILE_OPTION)
                        .desc("security analysis result 1 file path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(RESULT2_FILE_OPTION)
                        .desc("security analysis result 2 file path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                        .desc("output file path, where the comparison results will be stored")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(THRESHOLD_OPTION)
                        .desc("threshold used for results comparison, default is " + THRESHOLD_DEFAULT)
                        .hasArg()
                        .argName("THRESHOLD")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }

        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path results1File = context.getFileSystem().getPath(line.getOptionValue(RESULT1_FILE_OPTION));
        Path results2File = context.getFileSystem().getPath(line.getOptionValue(RESULT2_FILE_OPTION));
        Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
        double threshold = line.hasOption(THRESHOLD_OPTION) ? Double.parseDouble(line.getOptionValue(THRESHOLD_OPTION)) : THRESHOLD_DEFAULT;
        try (Writer outputWriter = Files.newBufferedWriter(outputFile)) {
            SecurityAnalysisResult result1 = SecurityAnalysisResultDeserializer.read(results1File);
            SecurityAnalysisResult result2 = SecurityAnalysisResultDeserializer.read(results2File);
            SecurityAnalysisResultEquivalence resultEquivalence = new SecurityAnalysisResultEquivalence(threshold, outputWriter);
            context.getOutputStream().println("Comparison result: " + (resultEquivalence.equivalent(result1, result2) ? "success" : "fail"));
        }
    }

}
