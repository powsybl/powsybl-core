/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.merge;

import com.google.auto.service.AutoService;
import eu.itesla_project.cases.CaseRepository;
import eu.itesla_project.cases.CaseRepositoryFactory;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.commons.datasource.FileDataSource;
import eu.itesla_project.iidm.export.Exporter;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class MergeByDateTool implements Tool {
    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "merge-by-date";
            }

            @Override
            public String getTheme() {
                return "Data conversion";
            }

            @Override
            public String getDescription() {
                return "Merge files by date using case repository";
            }

            @Override
            @SuppressWarnings("static-access")
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("date")
                        .desc("merge date date (example 2013-01-15T18:45:00+01:00)")
                        .hasArg()
                        .argName("DATE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("countries")
                        .desc("country list to merge (ISO code)")
                        .hasArg()
                        .argName("COUNTRY1,COUNTRY2")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("output-dir")
                        .desc("output directory")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("output-format")
                        .desc("the output format")
                        .hasArg()
                        .argName("FORMAT")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt("optimize")
                        .desc("run merge optimizer")
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
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        CaseRepository caseRepository = defaultConfig.newFactoryImpl(CaseRepositoryFactory.class).create(LocalComputationManager.getDefault());
        LoadFlowFactory loadFlowFactory = defaultConfig.newFactoryImpl(LoadFlowFactory.class);
        MergeOptimizerFactory mergeOptimizerFactory = defaultConfig.newFactoryImpl(MergeOptimizerFactory.class);
        Set<Country> countries = Arrays.stream(line.getOptionValue("countries").split(",")).map(Country::valueOf).collect(Collectors.toSet());
        DateTime date = DateTime.parse(line.getOptionValue("date"));
        Path outputDir = Paths.get(line.getOptionValue("output-dir"));
        String outputFormat = line.getOptionValue("output-format");
        Exporter exporter = Exporters.getExporter(outputFormat);
        if (exporter == null) {
            throw new RuntimeException("Format " + outputFormat + " not supported");
        }
        boolean optimize = line.hasOption("optimize");

        context.getOutputStream().println("merging...");

        Network merge = MergeUtil.merge(caseRepository, date, CaseType.SN, countries, loadFlowFactory, 0,
                mergeOptimizerFactory, LocalComputationManager.getDefault(), optimize);

        context.getOutputStream().println("exporting...");

        String baseName = merge.getId().replace(" ", "_");
        exporter.export(merge, null, new FileDataSource(outputDir, baseName));
    }
}
