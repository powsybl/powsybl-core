/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.contingency.ContingenciesProvider;
import eu.itesla_project.contingency.ContingenciesProviderFactory;
import eu.itesla_project.eurostag.network.EsgGeneralParameters;
import eu.itesla_project.eurostag.network.EsgNetwork;
import eu.itesla_project.eurostag.network.io.EsgWriter;
import eu.itesla_project.eurostag.tools.EurostagNetworkModifier;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.DynamicDatabaseClient;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.IIDMDynamicDatabaseFactory;
import eu.itesla_project.iidm.eurostag.export.BranchParallelIndexes;
import eu.itesla_project.iidm.eurostag.export.EurostagDictionary;
import eu.itesla_project.iidm.eurostag.export.EurostagEchExport;
import eu.itesla_project.iidm.eurostag.export.EurostagEchExportConfig;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.simulation.SimulationParameters;
import org.apache.commons.cli.CommandLine;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class EurostagExportTool implements Tool, EurostagConstants {

    @Override
    public Command getCommand() {
        return EurostagExportCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        ComponentDefaultConfig defaultConfig = ComponentDefaultConfig.load();
        EurostagConfig eurostagConfig = EurostagConfig.load();
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        Path outputDir = Paths.get(line.getOptionValue("output-dir"));
        if (!Files.isDirectory(outputDir)) {
            throw new RuntimeException(outputDir + " is not a directory");
        }
        DynamicDatabaseClient ddbClient = new IIDMDynamicDatabaseFactory().create(eurostagConfig.isDdbCaching());

        System.out.println("loading case...");
        // load network
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case '" + caseFile + "' not found");
        }
        network.getStateManager().allowStateMultiThreadAccess(true);

        System.out.println("exporting ech...");
        // export .ech and dictionary
        EurostagEchExportConfig exportConfig = new EurostagEchExportConfig();
        BranchParallelIndexes parallelIndexes = BranchParallelIndexes.build(network, exportConfig);
        EurostagDictionary dictionary = EurostagDictionary.create(network, parallelIndexes, exportConfig);
        new EurostagEchExport(network, exportConfig, parallelIndexes, dictionary).write(outputDir.resolve("sim.ech"));

        try (Writer writer = Files.newBufferedWriter(outputDir.resolve("sim.ech"), StandardCharsets.UTF_8)) {
            EsgGeneralParameters parameters = new EsgGeneralParameters();
            parameters.setTransformerVoltageControl(false);
            parameters.setSvcVoltageControl(false);
            EsgNetwork networkEch = new EurostagEchExport(network, exportConfig, parallelIndexes, dictionary).createNetwork(parameters);
            new EurostagNetworkModifier().hvLoadModelling(networkEch);
            new EsgWriter(networkEch, parameters).write(writer, network.getId() + "/" + network.getStateManager().getWorkingStateId());
        }
        dictionary.dump(outputDir.resolve("dict.csv"));
        System.out.println("exporting dta...");

        // export .dta
        ddbClient.dumpDtaFile(outputDir, "sim.dta", network, parallelIndexes.toMap(), EurostagUtil.VERSION, dictionary.toMap());

        System.out.println("exporting seq...");

            // export .seq
            EurostagScenario scenario = new EurostagScenario(SimulationParameters.load(), eurostagConfig);
            try (BufferedWriter writer = Files.newBufferedWriter(outputDir.resolve(PRE_FAULT_SEQ_FILE_NAME), StandardCharsets.UTF_8)) {
                scenario.writePreFaultSeq(writer, PRE_FAULT_SAC_FILE_NAME);
            }
            ContingenciesProvider contingenciesProvider = defaultConfig.newFactoryImpl(ContingenciesProviderFactory.class).create();
            scenario.writeFaultSeqArchive(contingenciesProvider.getContingencies(network), network, dictionary, faultNum -> FAULT_SEQ_FILE_NAME.replace(eu.itesla_project.computation.Command.EXECUTION_NUMBER_PATTERN, Integer.toString(faultNum)))
                    .as(ZipExporter.class).exportTo(outputDir.resolve(ALL_SCENARIOS_ZIP_FILE_NAME).toFile());

        // export limits
        try (OutputStream os = Files.newOutputStream(outputDir.resolve(LIMITS_ZIP_FILE_NAME))) {
            EurostagImpactAnalysis.writeLimits(network, dictionary, os);
        }
    }

}
