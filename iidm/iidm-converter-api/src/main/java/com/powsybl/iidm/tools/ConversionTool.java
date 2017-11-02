/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.commons.datasource.DefaultDataSourceObserver;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger LOGGER = LoggerFactory.getLogger(ConversionTool.class);

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
            throw new PowsyblException("Target format " + outputFormat + " not supported");
        }

        Properties inputParams = readProperties(line, OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(context.getFileSystem().getPath(inputFile), context.getComputationManager(), createImportConfig(), inputParams);

        Properties outputParams = readProperties(line, OptionType.EXPORT, context);

        if (outputParams.containsKey("forceBusBranchTopo")) {
            LOGGER.warn("forceBusBranchTopo functionality is deprecated!");
        }

        DataSource ds2 = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new DefaultDataSourceObserver() {
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
