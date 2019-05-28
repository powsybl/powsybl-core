/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.tools;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DefaultDataSourceObserver;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.Properties;

import static com.powsybl.iidm.tools.ConversionToolConstants.OUTPUT_FILE;
import static com.powsybl.iidm.tools.ConversionToolConstants.OUTPUT_FORMAT;
import static com.powsybl.iidm.tools.ConversionToolUtils.readProperties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class DefaultConversionOption implements ConversionOption {

    private final String readOption;

    public DefaultConversionOption(String readOption) {
        this.readOption = readOption;
    }

    @Override
    public ImportConfig createImportConfig(CommandLine line) {
        return ConversionToolUtils.createImportConfig(line);
    }

    @Override
    public Network read(CommandLine line, ToolRunningContext context) throws IOException {
        return read(readOption, line, context);
    }

    @Override
    public Network read(String commandOption, CommandLine line, ToolRunningContext context) throws IOException {
        String inputFile = line.getOptionValue(commandOption);
        context.getOutputStream().println("Loading network " + inputFile + "...");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(context.getFileSystem().getPath(inputFile), context.getShortTimeExecutionComputationManager(), createImportConfig(line), inputParams);
        if (network == null) {
            throw new PowsyblException("Network " + inputFile + " not found");
        }
        return network;
    }

    @Override
    public void write(Network network, CommandLine line, ToolRunningContext context) throws IOException {
        Exporter exporter = Exporters.getExporter(line.getOptionValue(OUTPUT_FORMAT));
        String outputFile = line.getOptionValue(OUTPUT_FILE);
        Properties outputParams = readProperties(line, ConversionToolUtils.OptionType.EXPORT, context);
        DataSource ds2 = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new DefaultDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        exporter.export(network, outputParams, ds2);
    }
}
