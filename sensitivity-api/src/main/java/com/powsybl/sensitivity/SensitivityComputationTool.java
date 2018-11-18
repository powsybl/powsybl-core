/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import com.powsybl.sensitivity.converter.SensitivityComputationResultExporters;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(Tool.class)
public class SensitivityComputationTool implements Tool {

    private static final String CASE_FILE_OPTION = "case-file";
    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String OUTPUT_FORMAT_OPTION = "output-format";
    private static final String SKIP_POSTPROC_OPTION = "skip-postproc";
    private static final String FACTORS_FILE_OPTION = "factors-file";

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "sensitivity-computation";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run sensitivity computation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE_OPTION)
                        .desc("the case path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(FACTORS_FILE_OPTION)
                        .desc("sensitivity factors input file path")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                        .desc("sensitivity computation results output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                        .desc("the output format " + SensitivityComputationResultExporters.getFormats())
                        .hasArg()
                        .argName("FORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(SKIP_POSTPROC_OPTION)
                        .desc("skip network importer post processors (when configured)")
                        .build());
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
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));
        boolean skipPostProc = line.hasOption(SKIP_POSTPROC_OPTION);
        Path outputFile = null;
        String format = null;
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();

        ImportConfig importConfig = (!skipPostProc) ? ImportConfig.load() : new ImportConfig();
        // process a single network: output-file/output-format options available
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        Path sensitivityFactorsFile = context.getFileSystem().getPath(line.getOptionValue(FACTORS_FILE_OPTION));

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), importConfig, null);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }
        SensitivityComputation sensitivityComputation = defaultConfig.newFactoryImpl(SensitivityComputationFactory.class).create(network, context.getShortTimeExecutionComputationManager(), 0);

        SensitivityComputationParameters params = SensitivityComputationParameters.load();
        String workingStateId = network.getStateManager().getWorkingStateId();
        SensitivityFactorsProviderFactory factorsProviderFactory = defaultConfig.newFactoryImpl(SensitivityFactorsProviderFactory.class);
        SensitivityFactorsProvider factorsProvider = factorsProviderFactory.create(sensitivityFactorsFile);
        SensitivityComputationResults result = sensitivityComputation.run(factorsProvider, workingStateId, params).join();


        if (!result.isOk()) {
            context.getErrorStream().println("Initial state divergence");
        } else {
            if (outputFile != null) {
                context.getOutputStream().println("Writing results to '" + outputFile + "'");
                SensitivityComputationResultExporters.export(result, outputFile, format);
            } else {
                // To avoid the closing of System.out
                Writer writer = new OutputStreamWriter(context.getOutputStream());
                printSensitivityComputationResult(result, writer, new AsciiTableFormatterFactory(), TableFormatterConfig.load());
            }
        }
    }

    private void printSensitivityComputationResult(SensitivityComputationResults result, Writer writer, TableFormatterFactory formatterFactory,
                                     TableFormatterConfig formatterConfig) {
        try (TableFormatter formatter = formatterFactory.create(writer,
                "sensitivity computation results",
                formatterConfig,
                new Column("VariableId"),
                new Column("VariableName"),
                new Column("FunctionId"),
                new Column("FunctionName"),
                new Column("VariableRefValue"),
                new Column("FunctionRefValue"),
                new Column("SensitivityValue"))) {
            result.getSensitivityValues().forEach(sensitivityValue -> {
                try {
                    formatter.writeCell(sensitivityValue.getFactor().getVariable().getId());
                    formatter.writeCell(sensitivityValue.getFactor().getVariable().getName());
                    formatter.writeCell(sensitivityValue.getFactor().getFunction().getId());
                    formatter.writeCell(sensitivityValue.getFactor().getFunction().getName());
                    formatter.writeCell(sensitivityValue.getVariableReference());
                    formatter.writeCell(sensitivityValue.getFunctionReference());
                    formatter.writeCell(sensitivityValue.getValue());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
