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
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import static com.powsybl.iidm.tools.ConversionToolConstants.INPUT_FILE;
import static com.powsybl.iidm.tools.ConversionToolConstants.OUTPUT_FILE;
import static com.powsybl.iidm.tools.ConversionToolConstants.OUTPUT_FORMAT;
import static com.powsybl.iidm.tools.ConversionToolUtils.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class DefaultConversionOption implements ConversionOption {

    private final String inputFileOption;
    private final String outputFileOption;
    private final String outputFormatOption;

    public DefaultConversionOption() {
        this(INPUT_FILE);
    }

    public DefaultConversionOption(String inputFileOption) {
        this(inputFileOption, OUTPUT_FILE, OUTPUT_FORMAT);
    }

    public DefaultConversionOption(String inputFileOption, String outputFileOption, String outputFormatOption) {
        this.inputFileOption = Objects.requireNonNull(inputFileOption);
        this.outputFileOption = Objects.requireNonNull(outputFileOption);
        this.outputFormatOption = Objects.requireNonNull(outputFormatOption);
    }

    @Override
    public void addImportOptions(Options options) {
        options.addOption(Option.builder().longOpt(inputFileOption)
                .desc("the input network path")
                .hasArg()
                .argName(inputFileOption)
                .required()
                .build());
        options.addOption(createSkipPostProcOption());
        options.addOption(createImportParametersFileOption());
        options.addOption(createImportParameterOption());
    }

    @Override
    public void addExportOptions(Options options, boolean required) {
        options.addOption(Option.builder().longOpt(outputFormatOption)
                .desc("the output network format")
                .hasArg()
                .argName(outputFormatOption)
                .required(required)
                .build());
        options.addOption(Option.builder().longOpt(outputFileOption)
                .desc("the output network file")
                .hasArg()
                .argName(outputFileOption)
                .required(required)
                .build());
        options.addOption(createExportParametersFileOption());
        options.addOption(createExportParameterOption());
    }

    @Override
    public ImportConfig createImportConfig(CommandLine line) {
        return ConversionToolUtils.createImportConfig(line);
    }

    @Override
    public Network read(CommandLine line, ToolRunningContext context) throws IOException {
        return read(inputFileOption, line, context);
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
        String outputFile = line.getOptionValue(outputFileOption);
        DataSource ds = Exporters.createDataSource(context.getFileSystem().getPath(outputFile), new DefaultDataSourceObserver() {
            @Override
            public void opened(String streamName) {
                context.getOutputStream().println("Generating file " + streamName + "...");
            }
        });
        Exporters.export(line.getOptionValue(outputFormatOption), network,
                readProperties(line, ConversionToolUtils.OptionType.EXPORT, context), ds);
    }

    String getInputFileOption() {
        return inputFileOption;
    }

    String getOutputFileOption() {
        return outputFileOption;
    }

    String getOutputFormatOption() {
        return outputFormatOption;
    }
}
