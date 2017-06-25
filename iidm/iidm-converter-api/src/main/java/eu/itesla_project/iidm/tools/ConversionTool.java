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
import java.nio.file.Paths;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(Tool.class)
public class ConversionTool implements Tool {

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

        Properties inputParams = readProperties(line.getOptionValue("import-parameters"));
        Network network = Importers.loadNetwork(Paths.get(inputFile), context.getComputationManager(), ImportConfig.load(), inputParams);

        Properties outputParams = readProperties(line.getOptionValue("export-parameters"));
        DataSource ds2 = Exporters.createDataSource(Paths.get(outputFile), new AbstractDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }

    Properties readProperties(String propertyFilename) throws IOException {
        Properties properties = new Properties();
        if (propertyFilename != null) {
            try (InputStream inputStream = Files.newInputStream(Paths.get(propertyFilename))) {
                if (propertyFilename.endsWith(".xml"))
                    properties.loadFromXML(inputStream);
                else
                    properties.load(inputStream);
            }
        }

        return properties;
    }
}
