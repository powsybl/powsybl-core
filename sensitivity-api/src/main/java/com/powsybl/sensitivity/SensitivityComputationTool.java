/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.commons.io.table.TableFormatterFactory;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionOption;
import com.powsybl.iidm.tools.DefaultConversionOption;
import com.powsybl.tools.AbstractCommand;
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

import static com.powsybl.iidm.tools.ConversionToolConstants.CASE_FILE;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(Tool.class)
public class SensitivityComputationTool implements Tool {

    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String OUTPUT_FORMAT_OPTION = "output-format";
    private static final String FACTORS_FILE_OPTION = "factors-file";
    private static final Supplier<ConversionOption> LOADER = Suppliers.memoize(() -> new DefaultConversionOption(CASE_FILE));

    private final ConversionOption conversionOption;

    public SensitivityComputationTool() {
        this(LOADER.get());
    }

    public SensitivityComputationTool(ConversionOption conversionOption) {
        this.conversionOption = conversionOption;
    }

    @Override
    public Command getCommand() {
        return new AbstractCommand("sensitivity-computation",
                "Computation",
                "Run sensitivity computation") {

            @Override
            public Options getOptions() {
                Options options = new Options();
                conversionOption.addImportOptions(options);
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
                return options;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path outputFile = null;
        String format = null;
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();

        // process a single network: output-file/output-format options available
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        Path sensitivityFactorsFile = context.getFileSystem().getPath(line.getOptionValue(FACTORS_FILE_OPTION));

        Network network = conversionOption.read(line, context);
        SensitivityComputation sensitivityComputation = defaultConfig.newFactoryImpl(SensitivityComputationFactory.class).create(network, context.getShortTimeExecutionComputationManager(), 0);

        SensitivityComputationParameters params = SensitivityComputationParameters.load();
        String workingStateId = network.getVariantManager().getWorkingVariantId();
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
