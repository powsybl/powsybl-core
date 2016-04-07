/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

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
import eu.itesla_project.modules.offline.OfflineConfig;
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
public class CheckSecurityTool implements Tool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckSecurityTool.class);

    private static final char CSV_SEPARATOR = ';';

    @Override
    public Command getCommand() {
        return CheckSecurityCommand.INSTANCE;
    }

    private static void prettyPrint(Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkStatusPerContingency, Set<SecurityIndexType> securityIndexTypes) {
        Table table = new Table(1 + securityIndexTypes.size(), BorderStyle.CLASSIC_WIDE);
        table.addCell("Contingency");
        for (SecurityIndexType securityIndexType : securityIndexTypes) {
            table.addCell(securityIndexType.toString());
        }

        for (Map.Entry<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> entry : checkStatusPerContingency.entrySet()) {
            String contingencyId = entry.getKey();
            Map<SecurityIndexType, SecurityRuleCheckStatus> checkStatus = entry.getValue();

            table.addCell(contingencyId);

            for (SecurityIndexType securityIndexType : securityIndexTypes) {
                table.addCell(checkStatus.get(securityIndexType).name());
            }
        }

        System.out.println(table.render());
    }

    private static void writeCsv(Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkStatusPerContingency, Set<SecurityIndexType> securityIndexTypes, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);

        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Contingency");
            for (SecurityIndexType securityIndexType : securityIndexTypes) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexType.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> entry : checkStatusPerContingency.entrySet()) {
                String contingencyId = entry.getKey();
                Map<SecurityIndexType, SecurityRuleCheckStatus> checkStatus = entry.getValue();

                writer.write(contingencyId);

                for (SecurityIndexType securityIndexType : securityIndexTypes) {
                    writer.write(CSV_SEPARATOR);
                    writer.write(checkStatus.get(securityIndexType).name());
                }

                writer.newLine();
            }
        }
    }

    private static void writeCsv2(Map<String, Map<SecurityIndexId, SecurityRuleCheckStatus>> checkStatusPerBaseCase, Path outputCsvFile) throws IOException {
        Objects.requireNonNull(outputCsvFile);

        Set<SecurityIndexId> securityIndexIds = new LinkedHashSet<>();
        for (Map<SecurityIndexId, SecurityRuleCheckStatus> checkStatus : checkStatusPerBaseCase.values()) {
            securityIndexIds.addAll(checkStatus.keySet());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputCsvFile, StandardCharsets.UTF_8)) {
            writer.write("Base case");
            for (SecurityIndexId securityIndexId : securityIndexIds) {
                writer.write(CSV_SEPARATOR);
                writer.write(securityIndexId.toString());
            }
            writer.newLine();

            for (Map.Entry<String, Map<SecurityIndexId, SecurityRuleCheckStatus>> entry : checkStatusPerBaseCase.entrySet()) {
                String baseCaseName = entry.getKey();
                writer.write(baseCaseName);

                Map<SecurityIndexId, SecurityRuleCheckStatus> checkStatus = entry.getValue();
                for (SecurityIndexId securityIndexId : securityIndexIds) {
                    writer.write(CSV_SEPARATOR);
                    SecurityRuleCheckStatus status = checkStatus.get(securityIndexId);
                    writer.write(status.name());
                }

                writer.newLine();
            }
        }
    }

    private static Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkRules(Importer importer, Path caseDir, String caseBaseName,
                                                                                           RulesDbClient rulesDb, String workflowId, RuleAttributeSet attributeSet,
                                                                                           Set<SecurityIndexType> securityIndexTypes, Set<String> contingencies,
                                                                                           double purityThreshold) {
        Objects.requireNonNull(caseBaseName);

        System.out.println("loading case " + caseBaseName + "...");

        // load the network
        Network network = importer.import_(new GenericReadOnlyDataSource(caseDir, caseBaseName), new Properties());

        System.out.println("checking rules...");

        return SecurityRuleUtil.checkRules(network, rulesDb, workflowId, attributeSet, securityIndexTypes, contingencies, purityThreshold);
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
        RulesDbClientFactory rulesDbClientFactory = config.getRulesDbClientFactoryClass().newInstance();
        String workflowId = line.getOptionValue("workflow");
        RuleAttributeSet attributeSet = RuleAttributeSet.valueOf(line.getOptionValue("attribute-set"));
        String caseFormat = line.getOptionValue("case-format");
        Path caseDir = Paths.get(line.getOptionValue("case-dir"));
        String caseBaseName = null;
        if (line.hasOption("case-basename")) {
            caseBaseName = line.getOptionValue("case-basename");
        }
        double purityThreshold = line.hasOption("purity-threshold") ? Double.parseDouble(line.getOptionValue("purity-threshold")) : CheckSecurityCommand.DEFAULT_PURITY_THRESHOLD;
        Path outputCsvFile = null;
        if (line.hasOption("output-csv-file")) {
            outputCsvFile = Paths.get(line.getOptionValue("output-csv-file"));
        }
        Set<SecurityIndexType> securityIndexTypes = line.hasOption("security-index-types")
                ? Arrays.stream(line.getOptionValue("security-index-types").split(",")).map(SecurityIndexType::valueOf).collect(Collectors.toSet())
                : EnumSet.allOf(SecurityIndexType.class);
        final Set<String> contingencies = line.hasOption("contingencies") ? Arrays.stream(line.getOptionValue("contingencies").split(",")).collect(Collectors.toSet()) : null;

        try (RulesDbClient rulesDb = rulesDbClientFactory.create(rulesDbName)) {

            try (ComputationManager computationManager = new LocalComputationManager()) {

                Importer importer = Importers.getImporter(caseFormat, computationManager);
                if (importer == null) {
                    throw new ITeslaException("Format " + caseFormat + " not supported");
                }

                if (caseBaseName != null) {

                    System.out.println("checking rules...");

                    Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkStatusPerContingency
                            = checkRules(importer, caseDir, caseBaseName, rulesDb, workflowId, attributeSet, securityIndexTypes, contingencies, purityThreshold);

                    if (outputCsvFile == null) {
                        prettyPrint(checkStatusPerContingency, securityIndexTypes);
                    } else {
                        writeCsv(checkStatusPerContingency, securityIndexTypes, outputCsvFile);
                    }
                } else {
                    if (outputCsvFile == null) {
                        throw new RuntimeException("In case of multiple impact security checks, only ouput to csv file is supported");
                    }
                    Map<String, Map<SecurityIndexId, SecurityRuleCheckStatus>> checkStatusPerBaseCase = Collections.synchronizedMap(new LinkedHashMap<>());
                    Importers.importAll(caseDir, importer, true, network -> {
                        try {
                            Map<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> checkStatusPerContingency
                                    = SecurityRuleUtil.checkRules(network, rulesDb, workflowId, attributeSet, securityIndexTypes, contingencies, purityThreshold);

                            Map<SecurityIndexId, SecurityRuleCheckStatus> checkStatusMap = new HashMap<>();
                            for (Map.Entry<String, Map<SecurityIndexType, SecurityRuleCheckStatus>> entry : checkStatusPerContingency.entrySet()) {
                                String contingencyId = entry.getKey();
                                for (Map.Entry<SecurityIndexType, SecurityRuleCheckStatus> entry1 : entry.getValue().entrySet()) {
                                    SecurityIndexType type = entry1.getKey();
                                    SecurityRuleCheckStatus status = entry1.getValue();
                                    checkStatusMap.put(new SecurityIndexId(contingencyId, type), status);
                                }
                            }

                            checkStatusPerBaseCase.put(network.getId(), checkStatusMap);
                        } catch (Exception e) {
                            LOGGER.error(e.toString(), e);
                        }
                    }, dataSource -> System.out.println("loading case " + dataSource.getBaseName() + "..."));

                    writeCsv2(checkStatusPerBaseCase, outputCsvFile);
                }
            }
        }
    }

}
