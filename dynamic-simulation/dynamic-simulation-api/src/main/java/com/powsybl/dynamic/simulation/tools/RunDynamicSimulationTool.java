/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation.tools;

import static com.powsybl.iidm.tools.ConversionToolUtils.createExportParameterOption;
import static com.powsybl.iidm.tools.ConversionToolUtils.createExportParametersFileOption;
import static com.powsybl.iidm.tools.ConversionToolUtils.createImportParameterOption;
import static com.powsybl.iidm.tools.ConversionToolUtils.createImportParametersFileOption;
import static com.powsybl.iidm.tools.ConversionToolUtils.readProperties;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.dynamic.simulation.DynamicSimulation;
import com.powsybl.dynamic.simulation.DynamicSimulationParameters;
import com.powsybl.dynamic.simulation.DynamicSimulationResult;
import com.powsybl.dynamic.simulation.json.DynamicSimulationResultSerializer;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionToolUtils;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(Tool.class)
public class RunDynamicSimulationTool implements Tool {

    private static final String CASE_FILE = "case-file";
    private static final String SKIP_POSTPROC = "skip-postproc";
    private static final String OUTPUT_FILE = "output-file";
    private static final String OUTPUT_FORMAT = "output-format";

    private enum Format {
        CSV,
        JSON
    }

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "dynamic-simulation";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run dynamic simulation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE)
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                    .desc("dynamic simulation results output path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT)
                    .desc("dynamic simulation results output format " + Arrays.toString(Format.values()))
                    .hasArg()
                    .argName("FORMAT")
                    .build());
                options.addOption(Option.builder().longOpt(SKIP_POSTPROC)
                    .desc("skip network importer post processors (when configured)")
                    .build());
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
                options.addOption(createExportParametersFileOption());
                options.addOption(createExportParameterOption());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }

        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        boolean skipPostProc = line.hasOption(SKIP_POSTPROC);
        Path outputFile = null;
        Format format = null;

        ImportConfig importConfig = (!skipPostProc) ? ImportConfig.load() : new ImportConfig();
        // process a single network: output-file/output-format options available
        if (line.hasOption(OUTPUT_FILE)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));
            if (!line.hasOption(OUTPUT_FORMAT)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT);
            }
            format = Format.valueOf(line.getOptionValue(OUTPUT_FORMAT));
        }

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(),
            importConfig, inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        DynamicSimulationParameters params = DynamicSimulationParameters.load();
        DynamicSimulationResult result = DynamicSimulation.run(network,
            context.getShortTimeExecutionComputationManager(), params);

        if (outputFile != null) {
            exportResult(result, context, outputFile, format);
        } else {
            printResult(result, context);
        }
    }

    private void printDynamicSimulationResult(DynamicSimulationResult result, Path outputFile,
        TableFormatterFactory formatterFactory,
        TableFormatterConfig formatterConfig) {
        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            printDynamicSimulationResult(result, writer, formatterFactory, formatterConfig);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printDynamicSimulationResult(DynamicSimulationResult result, Writer writer,
        TableFormatterFactory formatterFactory,
        TableFormatterConfig formatterConfig) {
        try (TableFormatter formatter = formatterFactory.create(writer,
            "dynamic simulation results",
            formatterConfig,
            new Column("Result"),
            new Column("Metrics"))) {
            formatter.writeCell(result.isOk());
            formatter.writeCell(result.getMetrics().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printResult(DynamicSimulationResult result, ToolRunningContext context) {
        Writer writer = new OutputStreamWriter(context.getOutputStream());

        AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();
        printDynamicSimulationResult(result, writer, asciiTableFormatterFactory, TableFormatterConfig.load());
    }

    private void exportResult(DynamicSimulationResult result, ToolRunningContext context, Path outputFile,
        Format format) {
        context.getOutputStream().println("Writing results to '" + outputFile + "'");
        switch (format) {
            case CSV:
                CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
                printDynamicSimulationResult(result, outputFile, csvTableFormatterFactory, TableFormatterConfig.load());
                break;

            case JSON:
                DynamicSimulationResultSerializer.write(result, outputFile);
                break;
        }
    }
}
