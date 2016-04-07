/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.simulation;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.securityindexes.SecurityIndex;
import eu.itesla_project.modules.securityindexes.SecurityIndexId;
import eu.itesla_project.modules.securityindexes.SecurityIndexType;
import org.apache.commons.cli.CommandLine;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ImpactAnalysisTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImpactAnalysisTool.class);

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return ImpactAnalysisCommand.INSTANCE;
    }

    private static String okToStr(Boolean b) {
        String str;
        if (b == null) {
            str = "NA";
        } else {
            str = b ? "OK" : "NOK";
        }
        return str;
    }

    private static List<String> toRow(Collection<SecurityIndex> securityIndexes) {
        List<String> l = new ArrayList<>();
        Map<SecurityIndexType, Boolean> ok = new EnumMap<>(SecurityIndexType.class);
        for (SecurityIndex securityIndex : securityIndexes) {
            ok.put(securityIndex.getId().getSecurityIndexType(), securityIndex.isOk());
        }

        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
            Boolean b = ok.get(securityIndexType);
            l.add(okToStr(b));
        }

        return l;
    }

    private static void prettyPrint(Multimap<String, SecurityIndex> securityIndexesPerContingency) {
        Table table = new Table(1 + SecurityIndexType.values().length, BorderStyle.CLASSIC_WIDE);
        table.addCell("Contingency");
        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
            table.addCell(securityIndexType.toString());
        }

        for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexesPerContingency.asMap().entrySet()) {
            String contingencyId = entry.getKey();
            table.addCell(contingencyId);
            for (String str : toRow(entry.getValue())) {
                table.addCell(str);
            }
        }

        System.out.println(table.render());
    }

    private static void writeCsv(Multimap<String, SecurityIndex> securityIndexesPerContingency, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);
        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Contingency");
            for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexType.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Collection<SecurityIndex>> entry : securityIndexesPerContingency.asMap().entrySet()) {
                String contingencyId = entry.getKey();
                writer.write(contingencyId);
                for (String str : toRow(entry.getValue())) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(str);
                }
                writer.newLine();
            }
        }
    }

    private static void writeCsv(Map<String, Map<SecurityIndexId, SecurityIndex>> securityIndexesPerCase, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);

        Set<SecurityIndexId> securityIndexIds = new LinkedHashSet<>();
        for (Map<SecurityIndexId, SecurityIndex> securityIndexesPerId : securityIndexesPerCase.values()) {
            if (securityIndexesPerId != null) {
                securityIndexIds.addAll(securityIndexesPerId.keySet());
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Base case");
            for (SecurityIndexId securityIndexId : securityIndexIds) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexId.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Map<SecurityIndexId, SecurityIndex>> entry : securityIndexesPerCase.entrySet()) {
                String baseCaseName = entry.getKey();
                writer.write(baseCaseName);

                Map<SecurityIndexId, SecurityIndex> securityIndexes = entry.getValue();
                for (SecurityIndexId securityIndexId : securityIndexIds) {
                    Boolean b = null;
                    if (securityIndexes != null) {
                        SecurityIndex securityIndex = securityIndexes.get(securityIndexId);
                        if (securityIndex != null) {
                            b = securityIndex.isOk();
                        }
                    }
                    writer.write(CSV_SEPARATOR);
                    writer.write(okToStr(b));
                }

                writer.newLine();
            }
        }
    }

    private static Multimap<String, SecurityIndex> runImpactAnalysis(Path caseDirName, String caseBaseName, Set<String> contingencyIds, Importer importer,
                                                                     ComputationManager computationManager, SimulatorFactory simulatorFactory,
                                                                     DynamicDatabaseClientFactory ddbFactory, ContingenciesAndActionsDatabaseClient contingencyDb) throws Exception {
        System.out.println("loading case " + caseBaseName + "...");

        // load the network
        Network network = importer.import_(new GenericReadOnlyDataSource(caseDirName, caseBaseName), new Properties());

        return runImpactAnalysis(network, contingencyIds, computationManager, simulatorFactory, ddbFactory, contingencyDb);
    }

    private static Multimap<String, SecurityIndex> runImpactAnalysis(Network network, Set<String> contingencyIds,
                                                                     ComputationManager computationManager, SimulatorFactory simulatorFactory,
                                                                     DynamicDatabaseClientFactory ddbFactory, ContingenciesAndActionsDatabaseClient contingencyDb) throws Exception {
        Stabilization stabilization = simulatorFactory.createStabilization(network, computationManager, 0, ddbFactory);
        ImpactAnalysis impactAnalysis = simulatorFactory.createImpactAnalysis(network, computationManager, 0, contingencyDb);
        Map<String, Object> initContext = new HashMap<>();
        SimulationParameters simulationParameters = SimulationParameters.load();
        stabilization.init(simulationParameters, initContext);
        impactAnalysis.init(simulationParameters, initContext);
        System.out.println("running stabilization simulation...");
        StabilizationResult sr = stabilization.run();
        System.out.println("stabilization status: " + sr.getStatus());
        System.out.println("stabilization metrics: " + sr.getMetrics());
        if (sr.getStatus() == StabilizationStatus.COMPLETED) {
            System.out.println("running impact analysis...");
            ImpactAnalysisResult iar = impactAnalysis.run(sr.getState(), contingencyIds);
            System.out.println("impact analysis metrics: " + iar.getMetrics());

            return Multimaps.index(iar.getSecurityIndexes(), new Function<SecurityIndex, String>() {
                @Override
                public String apply(SecurityIndex securityIndex) {
                    return securityIndex.getId().getContingencyId();
                }
            });

        }
        return null;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        String caseBaseName = null;
        if (line.hasOption("case-basename")) {
            caseBaseName = line.getOptionValue("case-basename");
        }
        final Set<String> contingencyIds = line.hasOption("contingencies")
                ?  Sets.newHashSet(line.getOptionValue("contingencies").split(",")) : null;
        Path outputCsvFile = null;
        if (line.hasOption("output-csv-file")) {
            outputCsvFile = Paths.get(line.getOptionValue("output-csv-file"));
        }

        try (ComputationManager computationManager = new LocalComputationManager()) {

            DynamicDatabaseClientFactory ddbFactory = config.getDynamicDbClientFactoryClass().newInstance();
            ContingenciesAndActionsDatabaseClient contingencyDb = config.getContingencyDbClientFactoryClass().newInstance().create();
            SimulatorFactory simulatorFactory = config.getSimulatorFactoryClass().newInstance();

            Importer importer = Importers.getImporter(caseFormat, computationManager);
            if (importer == null) {
                throw new ITeslaException("Format " + caseFormat + " not supported");
            }

            if (caseBaseName != null) {

                Multimap<String, SecurityIndex> securityIndexesPerContingency
                        = runImpactAnalysis(caseDir, caseBaseName, contingencyIds, importer, computationManager,
                                            simulatorFactory, ddbFactory, contingencyDb);

                if (securityIndexesPerContingency != null) {
                    if (outputCsvFile == null) {
                        prettyPrint(securityIndexesPerContingency);
                    } else {
                        writeCsv(securityIndexesPerContingency, outputCsvFile);
                    }
                }
            } else {
                if (outputCsvFile == null) {
                    throw new RuntimeException("In case of multiple impact analyses, only ouput to csv file is supported");
                }
                Map<String, Map<SecurityIndexId, SecurityIndex>> securityIndexesPerCase = new LinkedHashMap<>();
                Importers.importAll(caseDir, importer, false, network -> {
                    try {
                        Multimap<String, SecurityIndex> securityIndexesPerContingency
                                = runImpactAnalysis(network, contingencyIds, computationManager,
                                simulatorFactory, ddbFactory, contingencyDb);
                        if (securityIndexesPerContingency == null) {
                            securityIndexesPerCase.put(network.getId(), null);
                        } else {
                            Map<SecurityIndexId, SecurityIndex> securityIndexesPerId = securityIndexesPerContingency.values().stream().collect(Collectors.toMap(SecurityIndex::getId, e -> e));
                            securityIndexesPerCase.put(network.getId(), securityIndexesPerId);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }, dataSource -> System.out.println("loading case " + dataSource.getBaseName() + "..."));

                writeCsv(securityIndexesPerCase, outputCsvFile);
            }
        }
    }

}
