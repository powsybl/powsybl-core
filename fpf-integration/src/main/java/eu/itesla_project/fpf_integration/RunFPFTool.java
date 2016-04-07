/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.fpf_integration;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.fpf_integration.executor.FPFAnalysis;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.ForecastErrorsDataStorageImpl;
import eu.itesla_project.modules.cases.CaseRepository;
import eu.itesla_project.modules.cases.CaseType;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineWorkflowParameters;
import eu.itesla_project.modules.online.TimeHorizon;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.joda.time.DateTime;

import java.nio.file.Paths;
import java.util.List;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class RunFPFTool implements Tool {
    public static String FPF = "Fuzzy Power Flow";

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "run-fpf";
        }

        @Override
        public String getTheme() {
            return FPF;
        }

        @Override
        public String getDescription() {
            return "run fpf analysis";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(OptionBuilder.withLongOpt("analysis")
                    .withDescription("the analysis id")
                    .hasArg()
                    .withArgName("ID")
                    .create());
            options.addOption(OptionBuilder.withLongOpt("time-horizon")
                    .withDescription("time horizon (example DACF)")
                    .hasArg()
                    .withArgName("TH")
                    .create());
            options.addOption(OptionBuilder.withLongOpt("base-case-date")
                    .withDescription("base case date (example 2013-01-15T18:45:00+01:00)")
                    .hasArg()
                    .withArgName("DATE")
                    .create());
            options.addOption(OptionBuilder.withLongOpt("output-dir")
                    .withDescription("output dir where the FPF output files will be stored")
                    .hasArg()
                    .isRequired()
                    .withArgName("OUTPUTDIR")
                    .create());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }

    };


    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line) throws Exception {

        OnlineWorkflowParameters parameters=OnlineWorkflowParameters.loadDefault();
        OnlineConfig onlineConfig=OnlineConfig.load();

        String analysisId = line.hasOption("analysis")
                ? line.getOptionValue("analysis")
                : parameters.getFeAnalysisId();
        DateTime baseCaseDate = line.hasOption("base-case-date")
                ? DateTime.parse(line.getOptionValue("base-case-date"))
                : parameters.getBaseCaseDate();
        TimeHorizon timeHorizon = line.hasOption("time-horizon")
                ? TimeHorizon.fromName(line.getOptionValue("time-horizon"))
                : parameters.getTimeHorizon();

        String outputDir = line.getOptionValue("output-dir");

        ComputationManager computationManager = new LocalComputationManager();
        ForecastErrorsDataStorageImpl feDataStorage = new ForecastErrorsDataStorageImpl();
        CaseRepository caseRepository = onlineConfig.getCaseRepositoryFactoryClass().newInstance().create(computationManager);

        List<Network> networks = caseRepository.load(baseCaseDate, CaseType.FO, Country.FR);
        if (networks.isEmpty()) {
            throw new RuntimeException("Base case not found");
        }
        Network network = networks.get(0);
        System.out.println("- Network id: " + network.getId());
        System.out.println("- Network name: " + network.getName());

        ContingenciesAndActionsDatabaseClient contingenciesDb = onlineConfig.getContingencyDbClientFactoryClass().newInstance().create();
        List<Contingency> contingencyList=contingenciesDb.getContingencies(network);

        FPFAnalysis fpfce=new FPFAnalysis();
        fpfce.init(network,computationManager,feDataStorage);
        fpfce.run(analysisId, timeHorizon, contingencyList, Paths.get(outputDir));
    }
}