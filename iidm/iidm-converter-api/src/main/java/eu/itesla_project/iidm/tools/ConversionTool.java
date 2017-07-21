/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.iidm.datasource.AbstractDataSourceObserver;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.import_.ImportConfig;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(Tool.class)
public class ConversionTool implements Tool {

    private enum OptionType {
        IMPORT("import-parameters", 'I'),
        EXPORT("export-parameters", 'E');

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
        return ConversionCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String inputFile = line.getOptionValue("input-file");
        String outputFormat = line.getOptionValue("output-format");
        String outputFile = line.getOptionValue("output-file");

        Exporter exporter = Exporters.getExporter(outputFormat);
        if (exporter == null) {
            throw new ITeslaException("Target format " + outputFormat + " not supported");
        }

        Properties inputParams = readProperties(line, OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(context.getFileSystem().getPath(inputFile), context.getComputationManager(), createImportConfig(), inputParams);

        Properties outputParams = readProperties(line, OptionType.EXPORT, context);
        DataSource ds2 = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new AbstractDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }

    Properties readProperties(CommandLine line, OptionType optionType, ToolRunningContext context) throws IOException {
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
