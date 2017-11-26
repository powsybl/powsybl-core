/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption(Option.builder()
                .longOpt("cim-zip-path")
                .desc("CIM zip file or directory")
                .hasArg()
                .argName("PATH")
                .required()
                .build());
        OPTIONS.addOption(Option.builder()
                .longOpt("output-dir")
                .desc("directory to write anonymized zip file")
                .hasArg()
                .argName("DIR")
                .required()
                .build());
        OPTIONS.addOption(Option.builder()
                .longOpt("dic-file")
                .desc("ID dictionary file")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        OPTIONS.addOption(Option.builder()
                .longOpt("skip-external-ref")
                .desc("do not anonymize external references")
                .build());
    }

    private Main() {
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cim-anonymizer", OPTIONS);
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(OPTIONS, args);
            Path cimZipPath = Paths.get(line.getOptionValue("cim-zip-path"));
            Path outputDir = Paths.get(line.getOptionValue("output-dir"));
            Path dicFile = Paths.get(line.getOptionValue("dic-file"));
            boolean skipExternalRef = line.hasOption("skip-external-ref");

            CimAnonymizer anomymizer = new CimAnonymizer();
            CimAnonymizer.Logger logger = new CimAnonymizer.Logger() {
                @Override
                public void logAnonymizingFile(Path file) {
                    System.out.println("Anonymizing " + file);
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
        } catch (ParseException e) {
            printHelp();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }
    }
}
