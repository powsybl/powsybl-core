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
import eu.itesla_project.iidm.datasource.*;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import org.apache.commons.cli.CommandLine;

import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ConversionTool implements Tool {

    @Override
    public Command getCommand() {
        return ConversionCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String sourceFormat = line.getOptionValue("source");
        String targetFormat = line.getOptionValue("target");
        String inputDirName = line.getOptionValue("input-dir");
        String inputBaseName = line.getOptionValue("input-basename");
        String outputDirName = line.getOptionValue("output-dir");
        String outputBaseName = line.getOptionValue("output-basename");

        Importer importer = Importers.getImporter(sourceFormat, context.getComputationManager());
        if (importer == null) {
            throw new ITeslaException("Source format " + sourceFormat + " not supported");
        }
        Exporter exporter = Exporters.getExporter(targetFormat);
        if (exporter == null) {
            throw new ITeslaException("Target format " + targetFormat + " not supported");
        }

        Properties inputParams = new Properties();
        // TODO get parameters through the command line
        ReadOnlyDataSource ds1 = new GenericReadOnlyDataSource(context.getFileSystem().getPath(inputDirName), inputBaseName);
        Network network = importer.import_(ds1, inputParams);

        Properties outputParams = new Properties();
        // TODO get parameters through the command line
        DataSource ds2 = new FileDataSource(context.getFileSystem().getPath(outputDirName), outputBaseName, new AbstractDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }

}
