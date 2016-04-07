/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.modules.histo.IIDM2DB;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class TopoExtractionTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopoExtractionTool.class);

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "topo-extraction";
            }

            @Override
            public String getTheme() {
                return "Topology";
            }

            @Override
            public String getDescription() {
                return "extract substation topology";
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
                options.addOption(Option.builder().longOpt("voltage-level-ids")
                        .desc("list of voltage level id separated by ,")
                        .hasArg()
                        .argName("LIST")
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
        String[] voltageLevelIds = line.getOptionValue("voltage-level-ids").split(",");
        Importer importer = Importers.getImporter(caseFormat);
        if (importer == null) {
            throw new ITeslaException("Format " + caseFormat + " not supported");
        }
        System.out.print("Case name");
        for (String voltageLevelId : voltageLevelIds) {
            System.out.print(CSV_SEPARATOR + voltageLevelId);
        }
        System.out.println();
        Importers.importAll(caseDir, importer, false, network -> {
            try {
                for (String voltageLevelId : voltageLevelIds) {
                    System.out.print(CSV_SEPARATOR);
                    VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
                    if (vl != null) {
                        JSONArray toposArray = IIDM2DB.toTopoSet(vl);
                        System.out.print(toposArray.toString());
                    }

                }
                System.out.println();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }, dataSource -> System.out.print(dataSource.getBaseName()));
    }

}
