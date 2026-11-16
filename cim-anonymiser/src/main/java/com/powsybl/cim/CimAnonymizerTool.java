/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(Tool.class)
public class CimAnonymizerTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CimAnonymizerTool.class);

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "cim-anonymizer";
            }

            @Override
            public String getTheme() {
                return "Data conversion";
            }

            @Override
            public String getDescription() {
                return "CIM files anonymization";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder()
                        .longOpt("cim-path")
                        .desc("CIM zip file or directory")
                        .hasArg()
                        .argName("PATH")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("output-dir")
                        .desc("Directory to write anonymized zip files")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("mapping-file")
                        .desc("File to store the ID mapping")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder()
                        .longOpt("skip-external-refs")
                        .desc("Do not anonymize external references")
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
        Path cimZipPath = options.getPath("cim-path").orElseThrow(IllegalStateException::new);
        Path outputDir = options.getPath("output-dir").orElseThrow(IllegalStateException::new);
        Path mappingFile = options.getPath("mapping-file").orElseThrow(IllegalStateException::new);
        boolean skipExternalRef = options.hasOption("skip-external-refs");

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
                stream.forEach(cimZipFile -> anomymizer.anonymizeZip(cimZipFile, outputDir, mappingFile, logger, skipExternalRef));
            }
        } else {
            anomymizer.anonymizeZip(cimZipPath, outputDir, mappingFile, logger, skipExternalRef);
        }
    }
}
