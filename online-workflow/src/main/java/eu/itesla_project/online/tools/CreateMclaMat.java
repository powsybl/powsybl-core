/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.mcla.NetworkUtils;
import eu.itesla_project.mcla.montecarlo.MCSMatFileWriter;
import eu.itesla_project.mcla.montecarlo.SamplingDataCreator;
import eu.itesla_project.mcla.montecarlo.data.SamplingNetworkData;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Quinary <itesla@quinary.com>
 */
@AutoService(Tool.class)
public class CreateMclaMat implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "create-mcla-mat";
        }

        @Override
        public String getTheme() {
            return Themes.MCLA;
        }

        @Override
        public String getDescription() {
            return "Create MCLA MAT file(s)";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("case-file")
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("output-folder")
                    .desc("the folder where to store the data")
                    .hasArg()
                    .argName("FOLDER")
                    .required()
                    .build());
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
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        Path outputFolder = Paths.get(line.getOptionValue("output-folder"));

        if (Files.isRegularFile(caseFile)) {
            System.out.println("loading case " + caseFile);
            // load the network
            Network network = Importers.loadNetwork(caseFile);
            if (network == null) {
                throw new RuntimeException("Case '" + caseFile + "' not found");
            }
            network.getStateManager().allowStateMultiThreadAccess(true);

            createMat(network, outputFolder);
        } else if (Files.isDirectory(caseFile)) {
            Importers.loadNetworks(caseFile, false, network -> {
                try {
                    createMat(network, outputFolder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, dataSource -> System.out.println("loading case " + dataSource.getBaseName()));
        }
    }

    private void createMat(Network network, Path outputFolder) throws IOException {
        System.out.println("creating mat file for network" + network.getId());
        ArrayList<String> generatorsIds = NetworkUtils.getGeneratorsIds(network);
        ArrayList<String> loadsIds = NetworkUtils.getLoadsIds(network);
        SamplingNetworkData samplingNetworkData = new SamplingDataCreator(network, generatorsIds, loadsIds).createSamplingNetworkData();
        Path networkDataMatFile = Files.createTempFile(outputFolder, "mcsamplerinput_" + network.getId() + "_", ".mat");
        System.out.println("saving data of network " + network.getId() + " in file " + networkDataMatFile.toString());
        new MCSMatFileWriter(networkDataMatFile).writeSamplingNetworkData(samplingNetworkData);
    }

}
