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
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.Interval;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class CheckSubstationUniqueToposTool implements Tool {
    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "check-substation-unique-topos";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "Check substation unique topologies";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("interval")
                        .desc("time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                        .hasArg()
                        .required()
                        .argName("DATE1/DATE2")
                        .build());
                options.addOption(Option.builder().longOpt("use-short-ids-dict")
                        .desc("replace real ids by short ones of the dictionary")
                        .hasArg()
                        .argName("DICT_PATH")
                        .build());
                options.addOption(Option.builder().longOpt("case-format")
                        .desc("the case format")
                        .hasArg()
                        .argName("FORMAT")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("case-dir")
                        .desc("the directory where the case is")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("case-basename")
                        .desc("the case base name (all cases of the directory if not set)")
                        .hasArg()
                        .argName("NAME")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("correlation-threshold")
                        .desc("the correlation threshold")
                        .hasArg()
                        .argName("THRESHOLD")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("probability-threshold")
                        .desc("the probability threshold")
                        .hasArg()
                        .argName("THRESHOLD")
                        .required()
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
    public void run(CommandLine line) throws Exception {
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        String caseBaseName = line.getOptionValue("case-basename");
        Interval interval = Interval.parse(line.getOptionValue("interval"));
        Path dictFile = null;
        if (line.hasOption("use-short-ids-dict")) {
            dictFile = Paths.get(line.getOptionValue("use-short-ids-dict"));
        }
        double correlationThreshold = Double.parseDouble(line.getOptionValue("correlation-threshold"));
        double probabilityThreshold = Double.parseDouble(line.getOptionValue("probability-threshold"));
        try (ComputationManager computationManager = new LocalComputationManager()) {
            Importer importer = Importers.getImporter(caseFormat, computationManager);
            if (importer == null) {
                throw new ITeslaException("Format " + caseFormat + " not supported");
            }
            Network network = importer.import_(new GenericReadOnlyDataSource(caseDir, caseBaseName), null);
            OfflineConfig config = OfflineConfig.load();
            try (TopologyMiner topologyMiner = config.getTopologyMinerFactoryClass().newInstance().create()) {
                Path topoCacheDir = TopologyContext.createTopoCacheDir(network, interval, correlationThreshold, probabilityThreshold);
                TopologyContext topologyContext = topologyMiner.loadContext(topoCacheDir, interval, correlationThreshold, probabilityThreshold);
                if (topologyContext == null) {
                    throw new RuntimeException("Topology context not found");
                }
                ShortIdDictionary dict = null;
                if (dictFile != null) {
                    dict = new ShortIdDictionary(dictFile);
                }
                new UniqueTopologyBuilder(topologyContext.getTopologyHistory(), dict)
                        .build(network);
            }
        }
    }
}
