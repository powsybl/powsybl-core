/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.HistoDbAttributeIdParser;
import eu.itesla_project.modules.histo.IIDM2DB;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class PrintCaseAttributesTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintCaseAttributesTool.class);

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "print-case-attributes";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "print case attributes";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("case-format")
                        .desc("the case format")
                        .hasArg()
                        .argName("FORMAT")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("case-dir")
                        .desc("the directory where cases are")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("attributes")
                        .desc("attribute list separated by a coma")
                        .hasArg()
                        .required()
                        .argName("ATTR1,ATTR2,...")
                        .build());
                options.addOption(Option.builder().longOpt("output-csv-file")
                        .desc("output CSV file path (pretty print on standard output if not specified)")
                        .hasArg()
                        .required()
                        .argName("FILE")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where FORMAT is one of " + Importers.getFormats();
            }
        };
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        Set<HistoDbAttributeId> attributeIds = new LinkedHashSet<>();
        for (String str : line.getOptionValue("attributes").split(",")) {
            attributeIds.add(HistoDbAttributeIdParser.parse(str));
        }
        Path outputCsvFile = Paths.get(line.getOptionValue("output-csv-file"));

        try (ComputationManager computationManager = new LocalComputationManager()) {
            Importer importer = Importers.getImporter(caseFormat, computationManager);
            if (importer == null) {
                throw new ITeslaException("Format " + caseFormat + " not supported");
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
                writer.write("Case name");
                for (HistoDbAttributeId attributeId : attributeIds) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(attributeId.toString());
                }
                writer.newLine();
                Importers.importAll(caseDir, importer, false, network -> {
                    try {
                        writer.write(network.getId());
                        Map<HistoDbAttributeId, Object> values = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false)).getSingleValueMap();
                        for (HistoDbAttributeId attributeId : attributeIds) {
                            writer.write(CSV_SEPARATOR);
                            Object value = values.get(attributeId);
                            if (value != null) {
                                writer.write(value.toString());
                            }

                        }
                        writer.newLine();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                });
            }
        }
    }

}
