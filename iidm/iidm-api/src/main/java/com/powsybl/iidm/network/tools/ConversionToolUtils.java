/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tools;

import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class ConversionToolUtils {

    private static final String IMPORT_PARAMETERS = "import-parameters";
    private static final String EXPORT_PARAMETERS = "export-parameters";

    public enum OptionType {
        IMPORT(IMPORT_PARAMETERS, 'I'),
        EXPORT(EXPORT_PARAMETERS, 'E');

        OptionType(String longOpt, char shortOpt) {
            this.longOpt = Objects.requireNonNull(longOpt);
            this.shortOpt = shortOpt;
        }

        char getShortOpt() {
            return shortOpt;
        }

        String getLongOpt() {
            return longOpt;
        }

        private final String longOpt;
        private final char shortOpt;
    }

    public static Option createExportParameterOption() {
        return Option.builder("E")
                .desc("use value for given exporter parameter")
                .argName("property=value")
                .numberOfArgs(2)
                .valueSeparator('=')
                .build();
    }

    public static Option createImportParameterOption() {
        return Option.builder("I")
                .desc("use value for given importer parameter")
                .argName("property=value")
                .numberOfArgs(2)
                .valueSeparator('=')
                .build();
    }

    public static Option createExportParametersFileOption() {
        return Option.builder().longOpt(EXPORT_PARAMETERS)
                .desc("the exporter configuration file")
                .hasArg()
                .argName("EXPORT_PARAMETERS")
                .build();
    }

    public static Option createImportParametersFileOption() {
        return Option.builder().longOpt(IMPORT_PARAMETERS)
                .desc("the importer configuration file")
                .hasArg()
                .argName("IMPORT_PARAMETERS")
                .build();
    }

    public static Properties readProperties(CommandLine line, OptionType optionType, ToolRunningContext context) throws IOException {
        Properties properties = new Properties();

        // Read the parameters file
        String filename = line.getOptionValue(optionType.getLongOpt(), (Supplier<String>) null);
        if (filename != null) {
            try (InputStream inputStream = Files.newInputStream(context.getFileSystem().getPath(filename))) {
                if (filename.endsWith(".xml")) {
                    properties.loadFromXML(inputStream);
                } else {
                    properties.load(inputStream);
                }
            }
        }

        // Append parameters from the command line
        properties.putAll(line.getOptionProperties(Character.toString(optionType.getShortOpt())));

        return properties;
    }

    private ConversionToolUtils() {
    }
}
