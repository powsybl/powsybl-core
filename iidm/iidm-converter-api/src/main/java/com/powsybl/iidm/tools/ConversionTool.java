/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DefaultDataSourceObserver;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.GroovyScriptPostProcessor;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(Tool.class)
public class ConversionTool implements Tool {

    private static final String INPUT_FILE = "input-file";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String OUTPUT_FILE = "output-file";
    private static final String GROOVY_SCRIPT = "groovy-script";
    private static final String IMPORT_PARAMETERS = "import-parameters";
    private static final String EXPORT_PARAMETERS = "export-parameters";

    private enum OptionType {
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

    protected ImportConfig createImportConfig() {
        return ImportConfig.load();
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "convert-network";
            }

            @Override
            public String getTheme() {
                return "Data conversion";
            }

            @Override
            public String getDescription() {
                return "convert a network from one format to another";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE)
                        .desc("the input file")
                        .hasArg()
                        .argName("INPUT_FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT)
                        .desc("the output file format")
                        .hasArg()
                        .argName("OUTPUT_FORMAT")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("the output file")
                        .hasArg()
                        .argName("OUTPUT_FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(IMPORT_PARAMETERS)
                        .desc("the importer configuation file")
                        .hasArg()
                        .argName("IMPORT_PARAMETERS")
                        .build());
                options.addOption(Option.builder("I")
                        .desc("use value for given importer parameter")
                        .argName("property=value")
                        .numberOfArgs(2)
                        .valueSeparator('=')
                        .build());
                options.addOption(Option.builder().longOpt(EXPORT_PARAMETERS)
                        .desc("the exporter configuration file")
                        .hasArg()
                        .argName("EXPORT_PARAMETERS")
                        .build());
                options.addOption(Option.builder("E")
                        .desc("use value for given exporter parameter")
                        .argName("property=value")
                        .numberOfArgs(2)
                        .valueSeparator('=')
                        .build());
                options.addOption(Option.builder().longOpt(GROOVY_SCRIPT)
                        .desc("Groovy script to change the network")
                        .hasArg()
                        .argName("FILE")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where OUTPUT_FORMAT is one of " + Exporters.getFormats();
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String inputFile = line.getOptionValue(INPUT_FILE);
        String outputFormat = line.getOptionValue(OUTPUT_FORMAT);
        String outputFile = line.getOptionValue(OUTPUT_FILE);

        Exporter exporter = Exporters.getExporter(outputFormat);
        if (exporter == null) {
            throw new PowsyblException("Target format " + outputFormat + " not supported");
        }

        Properties inputParams = readProperties(line, OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(context.getFileSystem().getPath(inputFile), context.getShortTimeExecutionComputationManager(), createImportConfig(), inputParams);

        if (line.hasOption(GROOVY_SCRIPT)) {
            Path groovyScript = context.getFileSystem().getPath(line.getOptionValue(GROOVY_SCRIPT));
            context.getOutputStream().println("Applying Groovy script " + groovyScript + "...");
            new GroovyScriptPostProcessor(groovyScript).process(network, context.getShortTimeExecutionComputationManager());
        }

        Properties outputParams = readProperties(line, OptionType.EXPORT, context);
        DataSource ds2 = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new DefaultDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }

    private static Properties readProperties(CommandLine line, OptionType optionType, ToolRunningContext context) throws IOException {
        Properties properties = new Properties();

        // Read the parameters file
        String filename = line.getOptionValue(optionType.getLongOpt(), null);
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
}
