/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim;

import com.google.auto.service.AutoService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class CimAnonymizerTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CimAnonymizerTool.class);

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "anonymize-cim";
            }

            @Override
            public String getTheme() {
                return "Data conversion";
            }

            @Override
            public String getDescription() {
                return "CIM file anonymization";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder()
                        .longOpt("cim-zip-path")
                        .desc("CIM zip file or directory")
                        .hasArg()
                        .argName("PATH")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("output-dir")
                        .desc("directory to write anonymized zip file")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("dic-file")
                        .desc("ID dictionary file")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("skip-external-ref")
                        .desc("do not anonymize external references")
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
        ToolOptions options = new ToolOptions(line, context.getFileSystem());
        Path cimZipPath = options.getPath("cim-zip-path").orElseThrow(AssertionError::new);
        Path outputDir = options.getPath("output-dir").orElseThrow(AssertionError::new);
        Path dicFile = options.getPath("dic-file").orElseThrow(AssertionError::new);
        boolean skipExternalRef = options.hasOption("skip-external-ref");

        CimAnonymizer anomymizer = new CimAnonymizer();
        CimAnonymizer.Logger logger = new CimAnonymizer.Logger() {
            @Override
            public void logAnonymizingFile(Path file) {
                context.getOutputStream().println("Anonymizing " + file);
            }

            @Override
            public void logSkipped(Set<String> skipped) {
                LOGGER.debug("Skipped {}", skipped);
            }
        };

        if (Files.isDirectory(cimZipPath)) {
            try (Stream<Path> stream = Files.list(cimZipPath).filter(cimZipFile -> cimZipFile.getFileName().toString().endsWith(".zip"))) {
                stream.forEach(cimZipFile -> anomymizer.anonymizeZip(cimZipFile, outputDir, dicFile, logger, skipExternalRef));
            }
        } else {
            anomymizer.anonymizeZip(cimZipPath, outputDir, dicFile, logger, skipExternalRef);
        }
    }
}
