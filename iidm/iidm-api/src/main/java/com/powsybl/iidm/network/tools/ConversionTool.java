/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DefaultDataSourceObserver;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(Tool.class)
public class ConversionTool implements Tool {

    private static final String INPUT_FILE = "input-file";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String OUTPUT_FILE = "output-file";

    protected ImportConfig createImportConfig() {
        return ImportConfig.load();
    }

    protected NetworkFactory createNetworkFactory() {
        return NetworkFactory.findDefault();
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
                options.addOption(ConversionToolUtils.createImportParametersFileOption());
                options.addOption(ConversionToolUtils.createImportParameterOption());
                options.addOption(ConversionToolUtils.createExportParametersFileOption());
                options.addOption(ConversionToolUtils.createExportParameterOption());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where OUTPUT_FORMAT is one of " + Exporter.getFormats();
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String inputFile = line.getOptionValue(INPUT_FILE);
        String outputFormat = line.getOptionValue(OUTPUT_FORMAT);
        String outputFile = line.getOptionValue(OUTPUT_FILE);

        Exporter exporter = Exporter.find(outputFormat);
        if (exporter == null) {
            throw new PowsyblException("Target format " + outputFormat + " not supported");
        }

        Properties inputParams = ConversionToolUtils.readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Network.read(context.getFileSystem().getPath(inputFile), context.getShortTimeExecutionComputationManager(), createImportConfig(), inputParams, createNetworkFactory(), new ImportersServiceLoader(), ReportNode.NO_OP);

        Properties outputParams = ConversionToolUtils.readProperties(line, ConversionToolUtils.OptionType.EXPORT, context);
        DataSource ds2 = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new DefaultDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }
}
